package com.parser.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketStatusController {

    @MessageMapping("/ping")
    public void handlePing(Map<String, Object> request,
                           SimpMessagingTemplate messagingTemplate) {
        String clientId = (String) request.get("clientId");

        Map<String, Object> response = Map.of(
                "type", "PONG",
                "timestamp", System.currentTimeMillis(),
                "message", "WebSocket connection is alive"
        );

        messagingTemplate.convertAndSend("/topic/status/" + clientId, response);
    }
}