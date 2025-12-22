package com.parser.service;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Data
public class JobStorage {
    private Map<String, ParsingJob> jobs = new ConcurrentHashMap<>();

    @Data
    public static class ParsingJob {
        private String jobId;
        private String status; // PENDING, PROCESSING, COMPLETED, ERROR
        private String result;
        private long createdTime;

        public ParsingJob(String jobId) {
            this.jobId = jobId;
            this.status = "PENDING";
            this.createdTime = System.currentTimeMillis();
        }
    }

    // Метод для получения работы по ID
    public ParsingJob getJob(String jobId) {
        return jobs.get(jobId);
    }

    // Метод для добавления работы
    public void addJob(String jobId, ParsingJob job) {
        jobs.put(jobId, job);
    }
}