package com.example.demo.infrastructure.web.controller;

import com.example.demo.application.dto.PerformanceResult;
import com.example.demo.application.service.PerformanceService;
import com.example.demo.infrastructure.web.dto.PerformanceTestRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@Tag(name = "Performance Tests", description = "API для тестирования производительности баз данных")
public class PerformanceController {

    private final PerformanceService performanceService;

    @PostMapping("/test")
    @Operation(summary = "Запустить тесты производительности")
    public ResponseEntity<List<PerformanceResult>> runPerformanceTests(
            @RequestBody PerformanceTestRequest request) {
        log.info("Running performance tests with batch size: {}", request.getBatchSize());
        List<PerformanceResult> results = performanceService.runPerformanceTests(request.getBatchSize());
        return ResponseEntity.ok(results);
    }

    @GetMapping("/summary")
    @Operation(summary = "Получить сводку по производительности")
    public ResponseEntity<String> getPerformanceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("📊 Database Performance Comparison\n");
        summary.append("===============================\n");
        summary.append("Operations tested:\n");
        summary.append("1. Single Record Insert\n");
        summary.append("2. Batch Insert\n");
        summary.append("3. Read by ID\n");
        summary.append("4. Read by Currency Pair\n");
        summary.append("5. Read by Date Range\n");
        summary.append("6. Read by Source\n");
        summary.append("\nUse POST /api/v1/performance/test to run tests");

        return ResponseEntity.ok(summary.toString());
    }
}