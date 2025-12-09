package com.memory.leak;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class MemoryAnalyzer {

    public static void printMemoryStats() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        System.out.println("=== Memory Statistics ===");
        System.out.printf("Heap Used: %d MB / %d MB%n",
                heapUsage.getUsed() / (1024 * 1024),
                heapUsage.getMax() / (1024 * 1024));
        System.out.printf("Non-Heap Used: %d MB%n",
                nonHeapUsage.getUsed() / (1024 * 1024));

        Runtime runtime = Runtime.getRuntime();
        System.out.printf("Free Memory: %d MB%n",
                runtime.freeMemory() / (1024 * 1024));
        System.out.printf("Total Memory: %d MB%n",
                runtime.totalMemory() / (1024 * 1024));
    }
}