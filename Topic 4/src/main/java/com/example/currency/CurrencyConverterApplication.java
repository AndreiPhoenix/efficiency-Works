package com.example.currency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class CurrencyConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyConverterApplication.class, args);
        printStartupInfo();
    }

    private static void printStartupInfo() {
        Runtime runtime = Runtime.getRuntime();
        System.out.println("\n========================================");
        System.out.println("Currency Converter Application Started");
        System.out.println("========================================");
        System.out.println("JVM Memory:");
        System.out.println("  Max Memory:   " + runtime.maxMemory() / 1024 / 1024 + " MB");
        System.out.println("  Total Memory: " + runtime.totalMemory() / 1024 / 1024 + " MB");
        System.out.println("  Free Memory:  " + runtime.freeMemory() / 1024 / 1024 + " MB");
        System.out.println("  Processors:   " + runtime.availableProcessors());
        System.out.println("========================================\n");
    }
}