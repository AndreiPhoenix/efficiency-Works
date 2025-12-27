package com.example.demo.infrastructure.web.controller;

import com.example.demo.application.service.ExchangeRateService;
import com.example.demo.domain.model.ExchangeRate;
import com.example.demo.infrastructure.web.dto.CreateExchangeRateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/exchange-rates")
@RequiredArgsConstructor
@Tag(name = "Exchange Rates", description = "API для управления курсами валют")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping
    @Operation(summary = "Создать новый курс валют")
    public ResponseEntity<ExchangeRate> create(@Valid @RequestBody CreateExchangeRateRequest request) {
        ExchangeRate rate = request.toDomain();
        ExchangeRate saved = exchangeRateService.save(rate);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/batch")
    @Operation(summary = "Пакетное создание курсов валют")
    public ResponseEntity<List<ExchangeRate>> createBatch(@Valid @RequestBody List<CreateExchangeRateRequest> requests) {
        List<ExchangeRate> rates = requests.stream()
                .map(CreateExchangeRateRequest::toDomain)
                .toList();
        List<ExchangeRate> saved = exchangeRateService.saveAll(rates);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить курс валюты по ID")
    public ResponseEntity<ExchangeRate> getById(@PathVariable String id) {
        return exchangeRateService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pair")
    @Operation(summary = "Получить курсы по валютной паре")
    public ResponseEntity<List<ExchangeRate>> getByCurrencyPair(
            @RequestParam String base,
            @RequestParam String target) {
        Currency baseCurrency = Currency.getInstance(base);
        Currency targetCurrency = Currency.getInstance(target);
        List<ExchangeRate> rates = exchangeRateService.findByCurrencyPair(baseCurrency, targetCurrency);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/range")
    @Operation(summary = "Получить курсы за период")
    public ResponseEntity<List<ExchangeRate>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<ExchangeRate> rates = exchangeRateService.findByDateRange(start, end);
        return ResponseEntity.ok(rates);
    }

    @GetMapping("/convert")
    @Operation(summary = "Конвертировать сумму")
    public ResponseEntity<BigDecimal> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {
        Currency fromCurrency = Currency.getInstance(from);
        Currency toCurrency = Currency.getInstance(to);
        BigDecimal result = exchangeRateService.convert(fromCurrency, toCurrency, amount);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    @Operation(summary = "Получить статистику")
    public ResponseEntity<String> getStats() {
        long count = exchangeRateService.count();
        return ResponseEntity.ok("Total exchange rates: " + count);
    }
}