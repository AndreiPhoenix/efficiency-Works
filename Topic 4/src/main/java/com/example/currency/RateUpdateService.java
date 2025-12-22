package com.example.currency;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
class MemoryLeakDemo {

    // ❌ Статическая коллекция, которая никогда не очищается
    private static final List<byte[]> STATIC_LEAK = new ArrayList<>();

    // ❌ ThreadLocal без очистки
    private static final ThreadLocal<List<String>> THREAD_LOCAL_LEAK =
            ThreadLocal.withInitial(ArrayList::new);

    // ❌ Неправильный кэш без ограничений
    private final Map<String, byte[]> unlimitedCache = new HashMap<>();

    public void demonstrateStaticLeak() {
        System.out.println("\n=== Демонстрация статической утечки ===");

        for (int i = 0; i < 100; i++) {
            byte[] data = new byte[1024 * 1024]; // 1 MB
            STATIC_LEAK.add(data);

            if (i % 10 == 0) {
                System.out.println("Добавлено " + (i + 1) + " MB в статическую утечку");
                printMemoryUsage();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void demonstrateThreadLocalLeak(ExecutorService executor) throws InterruptedException {
        System.out.println("\n=== Демонстрация ThreadLocal утечки ===");

        int taskCount = 100;
        CountDownLatch latch = new CountDownLatch(taskCount);

        for (int i = 0; i < taskCount; i++) {
            final int taskId = i;
            executor.submit(() -> {
                // Добавляем данные в ThreadLocal
                List<String> list = THREAD_LOCAL_LEAK.get();
                for (int j = 0; j < 1000; j++) {
                    list.add("task-" + taskId + "-data-" + j + "-" + UUID.randomUUID());
                }

                // НИКОГДА не вызываем THREAD_LOCAL_LEAK.remove() - УТЕЧКА!

                latch.countDown();
            });
        }

        latch.await();
        System.out.println("ThreadLocal утечка создана");
        printMemoryUsage();
    }

    public void demonstrateCacheLeak() {
        System.out.println("\n=== Демонстрация утечки в кэше ===");

        for (int i = 0; i < 10000; i++) {
            String key = "key-" + i + "-" + UUID.randomUUID();
            byte[] value = new byte[1024]; // 1 KB

            unlimitedCache.put(key, value);

            // Никогда не удаляем старые записи - УТЕЧКА!

            if (i % 1000 == 0) {
                System.out.println("Кэш размер: " + unlimitedCache.size() + " записей");
                printMemoryUsage();
            }
        }
    }

    public void demonstrateListenerLeak() {
        System.out.println("\n=== Демонстрация утечки слушателей ===");

        List<Runnable> listeners = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            final int listenerId = i;
            Runnable listener = () -> {
                // Ничего не делаем, но держим ссылку
            };

            listeners.add(listener);

            // Никогда не удаляем слушателей - УТЕЧКА!
        }

        System.out.println("Создано " + listeners.size() + " слушателей");
    }

    public void demonstrateStringInternLeak() {
        System.out.println("\n=== Демонстрация утечки String.intern() ===");

        List<String> internedStrings = new ArrayList<>();

        for (int i = 0; i < 100000; i++) {
            // Создаем уникальную строку и интернируем её
            String str = "leaked-string-" + i + "-" + UUID.randomUUID();
            String interned = str.intern(); // Попадает в PermGen/Metaspace

            internedStrings.add(interned);

            if (i % 10000 == 0) {
                System.out.println("Интернировано строк: " + (i + 1));
            }
        }
    }

    private void printMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();

        System.out.printf("Память: использовано=%d MB, макс=%d MB, использование=%.1f%%%n",
                used / 1024 / 1024,
                max / 1024 / 1024,
                (double) used / max * 100);

        // Детальная информация по пулам памяти
        for (MemoryPoolMXBean pool : ManagementFactory.getMemoryPoolMXBeans()) {
            if (pool.getUsage().getMax() > 0) {
                System.out.printf("  %s: %d/%d MB%n",
                        pool.getName(),
                        pool.getUsage().getUsed() / 1024 / 1024,
                        pool.getUsage().getMax() / 1024 / 1024);
            }
        }
    }

    // ✅ Методы для решения проблем

    public void cleanThreadLocals() {
        THREAD_LOCAL_LEAK.remove(); // Важно вызывать после завершения работы
    }

    public void useLimitedCache() {
        // ✅ Используем LRU кэш с ограничением
        Map<String, byte[]> limitedCache = new LinkedHashMap<String, byte[]>(100, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
                return size() > 1000; // Ограничиваем размер
            }
        };
    }

    public void useWeakReferences() {
        // ✅ Используем WeakReference для кэша
        Map<String, WeakReference<byte[]>> weakCache = new HashMap<>();

        for (int i = 0; i < 10000; i++) {
            String key = "key-" + i;
            byte[] value = new byte[1024];

            weakCache.put(key, new WeakReference<>(value));

            // GC может очистить значения при нехватке памяти
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MemoryLeakDemo demo = new MemoryLeakDemo();
        ExecutorService executor = Executors.newFixedThreadPool(4);

        try {
            // Запускаем демонстрации (по одной за раз)
            // demo.demonstrateStaticLeak();
            // demo.demonstrateThreadLocalLeak(executor);
            // demo.demonstrateCacheLeak();
            // demo.demonstrateListenerLeak();
            // demo.demonstrateStringInternLeak();

            System.out.println("\n=== Решения проблем ===");
            demo.cleanThreadLocals();
            demo.useLimitedCache();
            demo.useWeakReferences();

        } finally {
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);

            // Вызываем GC для демонстрации
            System.gc();
            demo.printMemoryUsage();
        }
    }
}