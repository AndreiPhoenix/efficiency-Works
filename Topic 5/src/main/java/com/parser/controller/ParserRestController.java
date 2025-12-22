package com.parser.controller;

import com.parser.service.ParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/parser")
public class ParserRestController {
    private final ParserService parserService;

    @PostMapping("/start")
    public Map<String, String> startParsing(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int delay) {

        String jobId = parserService.startParsing(query, delay);

        Map<String, String> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("message", "Parsing started");
        response.put("pollingUrl", "/api/parser/status/" + jobId);
        response.put("pollingInterval", "500ms");
        return response;
    }

    @GetMapping("/status/{jobId}")
    public Map<String, Object> getStatus(@PathVariable String jobId) {
        var job = parserService.getJobStatus(jobId);

        if (job == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Job not found");
            return error;
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", job.getJobId());
        response.put("status", job.getStatus());
        response.put("result", job.getResult());
        response.put("createdTime", job.getCreatedTime());
        response.put("currentTime", System.currentTimeMillis());

        // Для завершенных задач парсим результат
        if ("COMPLETED".equals(job.getStatus()) && job.getResult() != null) {
            try {
                response.put("parsedResult", parserService.getObjectMapper()
                        .readValue(job.getResult(), Object.class));
            } catch (Exception e) {
                response.put("parsedResult", job.getResult());
            }
        }

        return response;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        var jobs = parserService.getJobStorage().getJobs();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", jobs.size());

        long pending = jobs.values().stream().filter(j -> "PENDING".equals(j.getStatus())).count();
        long processing = jobs.values().stream().filter(j -> "PROCESSING".equals(j.getStatus())).count();
        long completed = jobs.values().stream().filter(j -> "COMPLETED".equals(j.getStatus())).count();
        long error = jobs.values().stream().filter(j -> "ERROR".equals(j.getStatus())).count();

        stats.put("pending", pending);
        stats.put("processing", processing);
        stats.put("completed", completed);
        stats.put("error", error);

        return stats;
    }
}