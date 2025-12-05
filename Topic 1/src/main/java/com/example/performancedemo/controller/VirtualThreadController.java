package com.example.performancedemo.controller;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@Timed(value = "virtual.controller")
public class VirtualThreadController {

    private final Map<String, String> concurrentMap = new ConcurrentHashMap<>();

    /**
     * Эндпоинт для тестирования с виртуальными потоками
     * Имитирует долгую операцию (100ms)
     */
    @GetMapping("/api/virtual/blocking")
    public Map<String, Object> virtualBlockingEndpoint(
            @RequestParam(defaultValue = "100") long delayMs) {

        // Имитация полезной работы
        processData();

        // Имитация блокирующей операции
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Работа с ConcurrentHashMap
        String key = "key-" + ThreadLocalRandom.current().nextInt(1000);
        concurrentMap.put(key, "value-" + System.currentTimeMillis());
        String value = concurrentMap.get(key);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("thread", Thread.currentThread().getName());
        response.put("isVirtual", Thread.currentThread().isVirtual());
        response.put("delayMs", delayMs);
        response.put("mapSize", concurrentMap.size());
        response.put("timestamp", System.currentTimeMillis());

        log.debug("Processed request with virtual thread: {}",
                Thread.currentThread().getName());

        return response;
    }

    /**
     * Эндпоинт для тестирования массовых параллельных операций
     */
    @GetMapping("/api/virtual/parallel")
    public Map<String, Object> parallelOperations(
            @RequestParam(defaultValue = "10") int tasks) {

        long startTime = System.currentTimeMillis();

        try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = new java.util.ArrayList<java.util.concurrent.Future<String>>();

            for (int i = 0; i < tasks; i++) {
                final int taskId = i;
                futures.add(executor.submit(() -> {
                    try {
                        Thread.sleep(100); // Имитация I/O
                        return "Task-" + taskId + " completed by " +
                                Thread.currentThread().getName();
                    } catch (InterruptedException e) {
                        return "Task-" + taskId + " interrupted";
                    }
                }));
            }

            // Собираем результаты
            var results = new java.util.ArrayList<String>();
            for (var future : futures) {
                results.add(future.get());
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("totalTasks", tasks);
            response.put("completedTasks", results.size());
            response.put("executionTimeMs", elapsedTime);
            response.put("averageTimePerTaskMs", (double) elapsedTime / tasks);
            response.put("resultsSample", results.subList(0, Math.min(5, results.size())));

            return response;
        } catch (Exception e) {
            throw new RuntimeException("Parallel execution failed", e);
        }
    }

    private void processData() {
        // Имитация обработки данных
        int[] array = new int[1000];
        for (int i = 0; i < array.length; i++) {
            array[i] = ThreadLocalRandom.current().nextInt();
        }
        java.util.Arrays.sort(array);
    }
}