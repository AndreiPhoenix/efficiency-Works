package com.example.currency;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ThreadPoolDemo {

    // ❌ Пример плохого пула - приводит к context switching
    private final ExecutorService badPool = Executors.newCachedThreadPool();

    // ✅ Пример хорошего пула
    private final ExecutorService goodPool = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors()
    );

    // ✅ Work stealing pool (лучший для многих задач)
    private final ExecutorService workStealingPool = Executors.newWorkStealingPool();

    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger activeThreads = new AtomicInteger(0);
    private final List<String> threadNames = new CopyOnWriteArrayList<>();

    public void demonstrateContextSwitchingProblem(int taskCount) throws InterruptedException {
        System.out.println("\n=== Демонстрация проблемы Context Switching ===");
        System.out.println("Запускаем " + taskCount + " задач на CachedThreadPool");

        completedTasks.set(0);
        threadNames.clear();

        long startTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            badPool.submit(() -> {
                activeThreads.incrementAndGet();
                threadNames.add(Thread.currentThread().getName());

                simulateTask(taskId);

                completedTasks.incrementAndGet();
                latch.countDown();
                activeThreads.decrementAndGet();
            });
        }

        latch.await();
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("CachedThreadPool результаты:");
        System.out.println("  Время выполнения: " + duration + " ms");
        System.out.println("  Уникальных потоков: " + threadNames.stream().distinct().count());
        System.out.println("  Макс активных потоков: " + activeThreads.get());
        System.out.println("  Context switching overhead: высокий");
    }

    public void demonstrateGoodPool(int taskCount) throws InterruptedException {
        System.out.println("\n=== Демонстрация FixedThreadPool ===");

        completedTasks.set(0);
        threadNames.clear();

        long startTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            goodPool.submit(() -> {
                activeThreads.incrementAndGet();
                threadNames.add(Thread.currentThread().getName());

                simulateTask(taskId);

                completedTasks.incrementAndGet();
                latch.countDown();
                activeThreads.decrementAndGet();
            });
        }

        latch.await();
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("FixedThreadPool результаты:");
        System.out.println("  Время выполнения: " + duration + " ms");
        System.out.println("  Уникальных потоков: " + threadNames.stream().distinct().count());
        System.out.println("  Макс активных потоков: " + activeThreads.get());
        System.out.println("  Context switching overhead: низкий");
    }

    public void demonstrateMemoryLeak() throws InterruptedException {
        System.out.println("\n=== Демонстрация утечки памяти ===");

        List<byte[]> leakyList = new ArrayList<>();
        ExecutorService leakyPool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < 100; i++) {
            final int chunkId = i;
            leakyPool.submit(() -> {
                // Создаем большой массив и сохраняем ссылку - УТЕЧКА!
                byte[] data = new byte[10 * 1024 * 1024]; // 10 MB
                leakyList.add(data); // Никогда не удаляем!

                System.out.println("Создан chunk " + chunkId + ", всего утечка: " +
                        (leakyList.size() * 10) + " MB");

                // Имитация работы
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        leakyPool.shutdown();
        leakyPool.awaitTermination(1, TimeUnit.MINUTES);

        System.out.println("Утечка памяти создана: " + (leakyList.size() * 10) + " MB");
        System.out.println("Для очистки выполните System.gc()");

        // Не очищаем leakyList намеренно - демонстрация утечки
    }

    public void demonstrateBackpressure() {
        System.out.println("\n=== Демонстрация Backpressure ===");

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2, // core pool
                4, // max pool
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(2), // маленькая очередь
                new ThreadPoolExecutor.CallerRunsPolicy() // политика при переполнении
        );

        AtomicInteger rejected = new AtomicInteger(0);

        for (int i = 0; i < 20; i++) {
            final int taskId = i;
            try {
                executor.submit(() -> {
                    System.out.println("Задача " + taskId + " выполняется в " +
                            Thread.currentThread().getName());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            } catch (RejectedExecutionException e) {
                rejected.incrementAndGet();
                System.out.println("Задача " + taskId + " отвергнута - backpressure!");
            }
        }

        executor.shutdown();
        System.out.println("Отвергнуто задач: " + rejected.get());
    }

    private void simulateTask(int taskId) {
        // Имитация I/O bound задачи
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));

            // Имитация CPU работы
            double result = 0;
            for (int i = 0; i < 1000; i++) {
                result += Math.sin(i) * Math.cos(i);
            }

            if (taskId % 100 == 0) {
                log.debug("Задача {} выполнена, результат: {}", taskId, result);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ThreadPoolDemo demo = new ThreadPoolDemo();

        // Демонстрация проблем
        demo.demonstrateContextSwitchingProblem(1000);
        demo.demonstrateGoodPool(1000);
        demo.demonstrateBackpressure();

        // Внимание! Следующий метод создаст утечку памяти
        // demo.demonstrateMemoryLeak();

        demo.badPool.shutdown();
        demo.goodPool.shutdown();
        demo.workStealingPool.shutdown();
    }
}