package com.example.currency;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class GCPerformanceDemo {

    public void demonstrateGCImpact() {
        System.out.println("\n=== Демонстрация влияния GC на производительность ===");

        List<byte[]> garbage = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        long gcTimeBefore = getTotalGCTime();

        // Создаем много мусора
        for (int i = 0; i < 100000; i++) {
            // Чередуем размеры объектов
            int size = (i % 10 == 0) ? 1024 * 1024 : 1024; // 1 MB или 1 KB
            byte[] data = new byte[size];

            // Каждый 1000-й объект сохраняем, чтобы создать давление на память
            if (i % 1000 == 0) {
                garbage.add(data);
            }

            // Создаем временные объекты
            String temp = "temp-" + i + "-" + System.currentTimeMillis();

            if (i % 10000 == 0) {
                System.out.println("Создано объектов: " + i);
                printGCMetrics();
            }
        }

        long endTime = System.currentTimeMillis();
        long gcTimeAfter = getTotalGCTime();
        long totalGCTime = gcTimeAfter - gcTimeBefore;

        System.out.println("\n=== Результаты ===");
        System.out.println("Общее время выполнения: " + (endTime - startTime) + " ms");
        System.out.println("Время, потраченное на GC: " + totalGCTime + " ms");
        System.out.println("Процент времени на GC: " +
                (double) totalGCTime / (endTime - startTime) * 100 + "%");

        if (totalGCTime > 1000) {
            System.out.println("⚠️  ВНИМАНИЕ: Слишком много времени тратится на GC!");
        }
    }

    private long getTotalGCTime() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                .sum();
    }

    private void printGCMetrics() {
        System.out.println("\n--- Метрики GC ---");

        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            System.out.printf("%s: count=%d, time=%d ms%n",
                    gc.getName(),
                    gc.getCollectionCount(),
                    gc.getCollectionTime());
        }

        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();

        System.out.printf("Memory: used=%d MB, max=%d MB (%.1f%%)%n",
                used / 1024 / 1024,
                max / 1024 / 1024,
                (double) used / max * 100);
    }

    public void demonstrateDifferentGCTypes() {
        System.out.println("\n=== Сравнение стратегий работы с памятью ===");

        // Стратегия 1: Создаем много мелких объектов
        System.out.println("Стратегия 1: Много мелких объектов");
        long start1 = System.currentTimeMillis();
        List<String> smallObjects = new ArrayList<>();

        for (int i = 0; i < 1000000; i++) {
            smallObjects.add("object-" + i + "-" + System.nanoTime());
        }

        long time1 = System.currentTimeMillis() - start1;
        System.out.println("Время: " + time1 + " ms");
        printGCMetrics();

        // Очищаем
        smallObjects.clear();
        System.gc();

        // Стратегия 2: Создаем меньше, но более крупных объектов
        System.out.println("\nСтратегия 2: Крупные объекты, пулы");
        long start2 = System.currentTimeMillis();
        List<StringBuilder> builders = new ArrayList<>();

        // Используем пул объектов
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = new StringBuilder(1024); // Предварительный размер
            for (int j = 0; j < 1000; j++) {
                sb.append("data-").append(j).append("-");
            }
            builders.add(sb);
        }

        long time2 = System.currentTimeMillis() - start2;
        System.out.println("Время: " + time2 + " ms");
        printGCMetrics();
    }

    public static void main(String[] args) {
        GCPerformanceDemo demo = new GCPerformanceDemo();

        System.out.println("=== Настройки JVM ===");
        System.out.println("Max Heap: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " MB");
        System.out.println("Processors: " + Runtime.getRuntime().availableProcessors());

        demo.demonstrateGCImpact();
        demo.demonstrateDifferentGCTypes();
    }
}