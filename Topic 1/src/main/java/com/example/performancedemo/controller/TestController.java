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
@Timed(value = "test.controller")
public class TestController {

    // Для демонстрации разных структур данных
    private final Map<String, String> synchronizedMap =
            java.util.Collections.synchronizedMap(new HashMap<>());
    private final Map<String, String> concurrentMap = new ConcurrentHashMap<>();

    /**
     * Эндпоинт для тестирования с обычными потоками
     * Имитирует долгую операцию (100ms)
     */
    @GetMapping("/api/blocking")
    public Map<String, Object> blockingEndpoint(
            @RequestParam(defaultValue = "100") long delayMs) {

        // Имитация полезной работы
        processData();

        // Имитация блокирующей операции (БД, внешний API и т.д.)
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Работа с разными структурами данных для демонстрации
        String key = "key-" + ThreadLocalRandom.current().nextInt(1000);

        // Тест synchronizedMap
        synchronizedMap.put(key, "value-" + System.currentTimeMillis());
        String valueFromSync = synchronizedMap.get(key);

        // Тест ConcurrentHashMap
        concurrentMap.put(key, "value-" + System.currentTimeMillis());
        String valueFromConcurrent = concurrentMap.get(key);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("thread", Thread.currentThread().getName());
        response.put("delayMs", delayMs);
        response.put("synchronizedMapSize", synchronizedMap.size());
        response.put("concurrentMapSize", concurrentMap.size());
        response.put("timestamp", System.currentTimeMillis());

        log.debug("Processed request with thread: {}",
                Thread.currentThread().getName());

        return response;
    }

    /**
     * Эндпоинт для вычисления чисел Фибоначчи (CPU-intensive операция)
     */
    @GetMapping("/api/fibonacci")
    public Map<String, Object> fibonacci(
            @RequestParam(defaultValue = "30") int n) {

        long startTime = System.currentTimeMillis();
        long result = calculateFibonacci(n);
        long elapsedTime = System.currentTimeMillis() - startTime;

        Map<String, Object> response = new HashMap<>();
        response.put("n", n);
        response.put("result", result);
        response.put("calculationTimeMs", elapsedTime);
        response.put("thread", Thread.currentThread().getName());

        return response;
    }

    /**
     * Эндпоинт для получения метрик пропускной способности
     */
    @GetMapping("/api/metrics/throughput")
    public Map<String, Object> getThroughputMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Здесь обычно получаем метрики из MeterRegistry
        // Для демонстрации возвращаем заглушку
        metrics.put("totalRequests",
                io.micrometer.core.instrument.Metrics.globalRegistry
                        .find("http.server.requests")
                        .counter()
                        .count());
        metrics.put("currentTime", System.currentTimeMillis());

        return metrics;
    }

    private long calculateFibonacci(int n) {
        if (n <= 1) return n;
        return calculateFibonacci(n - 1) + calculateFibonacci(n - 2);
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