package com.example.demo.performance;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryProfiler {

    public static void printMemoryUsage(String phase) {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        log.info("{} - Memory Usage:", phase);
        log.info("  Total: {} MB", totalMemory / (1024 * 1024));
        log.info("  Free: {} MB", freeMemory / (1024 * 1024));
        log.info("  Used: {} MB", usedMemory / (1024 * 1024));
        log.info("  Max: {} MB", maxMemory / (1024 * 1024));
        log.info("  Usage: {}%", (usedMemory * 100) / totalMemory);
    }

    public static void forceGcAndSleep() {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}