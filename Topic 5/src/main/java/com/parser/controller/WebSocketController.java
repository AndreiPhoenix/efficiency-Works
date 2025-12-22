package com.parser.controller;

import com.parser.service.ParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketController {
    private final ParserService parserService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/startParsing")
    public void startParsing(Map<String, Object> request) {
        String query = (String) request.get("query");
        int delay = (int) request.getOrDefault("delay", 5);
        String clientId = (String) request.get("clientId");

        // Используем унифицированный метод из ParserService
        parserService.startParsingWebSocket(query, delay, clientId, messagingTemplate);
    }
}