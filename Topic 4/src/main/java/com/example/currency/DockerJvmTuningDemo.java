package com.example.currency;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

public class DockerJvmTuningDemo {

    public void printJvmInfo() {
        System.out.println("\n=== Информация о JVM ===");

        Runtime runtime = Runtime.getRuntime();
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JVM Name: " + System.getProperty("java.vm.name"));
        System.out.println("JVM Vendor: " + System.getProperty("java.vm.vendor"));

        System.out.println("\n--- Память ---");
        System.out.println("Max Memory:   " + runtime.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("Total Memory: " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("Free Memory:  " + runtime.freeMemory() / 1024 / 1024 + " MB");
        System.out.println("Processors:   " + runtime.availableProcessors());

        System.out.println("\n--- GC ---");
        ManagementFactory.getGarbageCollectorMXBeans().forEach(gc -> {
            System.out.println(gc.getName() + ": " + gc.getCollectionCount() + " collections");
        });

        System.out.println("\n--- JVM Аргументы ---");
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        arguments.forEach(arg -> {
            if (arg.contains("Xm") || arg.contains("GC") || arg.contains("Heap")) {
                System.out.println("  " + arg);
            }
        });

        System.out.println("\n--- Система ---");
        System.out.println("OS: " + System.getProperty("os.name") + " " +
                System.getProperty("os.version"));
        System.out.println("Arch: " + System.getProperty("os.arch"));
    }

    public void demonstrateContainerAwareness() {
        System.out.println("\n=== Осведомленность о контейнере ===");

        // Проверяем, запущены ли мы в контейнере
        boolean isContainerized = false;
        try {
            // Проверяем cgroup
            ProcessBuilder pb = new ProcessBuilder("sh", "-c", "cat /proc/self/cgroup");
            Process process = pb.start();
            String output = new String(process.getInputStream().readAllBytes());
            isContainerized = output.contains("docker") || output.contains("kubepods");

            System.out.println("В контейнере: " + (isContainerized ? "ДА" : "НЕТ"));

            if (isContainerized) {
                System.out.println("\n=== Важные настройки для Docker ===");
                System.out.println("✅ Обязательные флаги:");
                System.out.println("  -XX:+UseContainerSupport");
                System.out.println("  -XX:MaxRAMPercentage=75.0");
                System.out.println("  -XX:InitialRAMPercentage=50.0");
                System.out.println("\n✅ Рекомендуемые флаги:");
                System.out.println("  -XX:+UseG1GC (или -XX:+UseZGC для Java 17+)");
                System.out.println("  -XX:MaxGCPauseMillis=200");
                System.out.println("  -XX:ParallelGCThreads=2");
                System.out.println("  -XX:ConcGCThreads=2");
                System.out.println("  -XX:+AlwaysPreTouch");
                System.out.println("\n❌ Избегайте:");
                System.out.println("  -Xmx, -Xms (используйте MaxRAMPercentage)");
                System.out.println("  Больших значений для ParallelGCThreads");
            }

        } catch (Exception e) {
            System.out.println("Не удалось определить окружение: " + e.getMessage());
        }
    }

    public void simulateMemoryPressure() {
        System.out.println("\n=== Симуляция давления памяти в контейнере ===");

        // Попытка выделить больше памяти, чем доступно
        List<byte[]> memoryHog = new ArrayList<>();
        int allocatedMB = 0;

        try {
            while (allocatedMB < 2000) { // Пытаемся выделить 2GB
                byte[] chunk = new byte[10 * 1024 * 1024]; // 10 MB
                memoryHog.add(chunk);
                allocatedMB += 10;

                System.out.println("Выделено: " + allocatedMB + " MB");

                if (allocatedMB % 100 == 0) {
                    Runtime runtime = Runtime.getRuntime();
                    long used = runtime.totalMemory() - runtime.freeMemory();
                    System.out.println("  Использовано JVM: " + used / 1024 / 1024 + " MB");
                }

                // Замедляем выделение
                Thread.sleep(100);
            }
        } catch (OutOfMemoryError e) {
            System.out.println("\n⚠️  OutOfMemoryError! Выделено: " + allocatedMB + " MB");
            System.out.println("В контейнере это может привести к OOM Killer!");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Очищаем память
            memoryHog.clear();
            System.gc();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        DockerJvmTuningDemo demo = new DockerJvmTuningDemo();

        demo.printJvmInfo();
        demo.demonstrateContainerAwareness();

        // Внимание! Этот метод может вызвать OOM
        // demo.simulateMemoryPressure();
    }
}