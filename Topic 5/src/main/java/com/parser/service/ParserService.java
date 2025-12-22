package com.parser.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Getter
public class ParserService {
    private final JobStorage jobStorage;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // Метод для REST API
    public String startParsing(String query, int delaySeconds) {
        String jobId = UUID.randomUUID().toString();
        JobStorage.ParsingJob job = new JobStorage.ParsingJob(jobId);
        jobStorage.addJob(jobId, job);

        executorService.submit(() -> {
            try {
                job.setStatus("PROCESSING");

                // Имитация задержки
                Thread.sleep(delaySeconds * 1000L);

                // Генерация результатов парсинга
                List<Map<String, String>> vacancies = generateMockVacancies(query);
                job.setResult(objectMapper.writeValueAsString(vacancies));
                job.setStatus("COMPLETED");

            } catch (InterruptedException | JsonProcessingException e) {
                job.setStatus("ERROR");
                job.setResult("Error during parsing: " + e.getMessage());
            }
        });

        return jobId;
    }

    // Метод для WebSocket
    public void startParsingWebSocket(String query, int delaySeconds, String clientId,
                                      SimpMessagingTemplate messagingTemplate) {
        String jobId = UUID.randomUUID().toString();
        JobStorage.ParsingJob job = new JobStorage.ParsingJob(jobId);
        jobStorage.addJob(jobId, job);

        // Отправляем начальный статус
        sendWebSocketMessage(messagingTemplate, clientId, jobId, "PENDING",
                "Parsing started", null);

        executorService.submit(() -> {
            try {
                job.setStatus("PROCESSING");

                // Отправляем статус PROCESSING
                sendWebSocketMessage(messagingTemplate, clientId, jobId, "PROCESSING",
                        "Parsing in progress...", null);

                // Имитация задержки
                Thread.sleep(delaySeconds * 1000L);

                // Генерация результатов
                List<Map<String, String>> vacancies = generateMockVacancies(query);
                job.setResult(objectMapper.writeValueAsString(vacancies));
                job.setStatus("COMPLETED");

                // Отправляем результаты
                Map<String, Object> additionalData = new HashMap<>();
                additionalData.put("vacancies", vacancies);
                additionalData.put("count", vacancies.size());

                sendWebSocketMessage(messagingTemplate, clientId, jobId, "COMPLETED",
                        "Found " + vacancies.size() + " vacancies for: " + query,
                        additionalData);

            } catch (InterruptedException | JsonProcessingException e) {
                job.setStatus("ERROR");
                job.setResult("Error during parsing: " + e.getMessage());

                Map<String, Object> errorData = new HashMap<>();
                errorData.put("details", e.getMessage());

                sendWebSocketMessage(messagingTemplate, clientId, jobId, "ERROR",
                        "Error during parsing", errorData);
            }
        });
    }

    // Вспомогательный метод для отправки WebSocket сообщений
    private void sendWebSocketMessage(SimpMessagingTemplate messagingTemplate,
                                      String clientId, String jobId,
                                      String status, String message,
                                      Map<String, Object> additionalData) {
        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("status", status);
        response.put("message", message);
        response.put("timestamp", System.currentTimeMillis());

        if (additionalData != null) {
            response.putAll(additionalData);
        }

        messagingTemplate.convertAndSend("/topic/parser/" + clientId, response);
    }

    // Генерация mock вакансий
    private List<Map<String, String>> generateMockVacancies(String query) {
        List<Map<String, String>> vacancies = new ArrayList<>();
        Random random = new Random();

        for (int i = 1; i <= 5; i++) {
            Map<String, String> vacancy = new HashMap<>();
            vacancy.put("id", String.valueOf(i));
            vacancy.put("title", query + " Developer " + i);
            vacancy.put("company", "Company " + (i % 3 + 1));
            vacancy.put("salary", (i * 1000 + random.nextInt(500)) + "$");
            vacancy.put("source", i % 2 == 0 ? "HH.ru" : "LinkedIn");
            vacancy.put("experience", (i % 3 + 1) + " years");
            vacancy.put("location", i % 2 == 0 ? "Moscow" : "Remote");
            vacancies.add(vacancy);
        }

        return vacancies;
    }

    // Получение статуса работы
    public JobStorage.ParsingJob getJobStatus(String jobId) {
        return jobStorage.getJob(jobId);
    }
}