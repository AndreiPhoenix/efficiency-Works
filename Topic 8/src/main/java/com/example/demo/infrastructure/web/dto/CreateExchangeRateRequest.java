package com.example.demo.infrastructure.web.dto;

import com.example.demo.domain.model.ExchangeRate;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Data
@Schema(description = "Запрос на создание курса валют")
public class CreateExchangeRateRequest {

    @NotBlank
    @Schema(description = "Базовая валюта (например, USD)", example = "USD")
    private String baseCurrency;

    @NotBlank
    @Schema(description = "Целевая валюта (например, EUR)", example = "EUR")
    private String targetCurrency;

    @NotNull
    @Positive
    @Schema(description = "Курс обмена", example = "0.85")
    private BigDecimal rate;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "Время курса", example = "2024-01-15 14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Источник данных", example = "ECB")
    private String source;

    @Schema(description = "Цена покупки", example = "0.849")
    private BigDecimal bid;

    @Schema(description = "Цена продажи", example = "0.851")
    private BigDecimal ask;

    @Schema(description = "Максимальная цена", example = "0.86")
    private BigDecimal high;

    @Schema(description = "Минимальная цена", example = "0.84")
    private BigDecimal low;

    @Schema(description = "Объем торгов", example = "1000000")
    private Long volume;

    public ExchangeRate toDomain() {
        return ExchangeRate.builder()
                .baseCurrency(Currency.getInstance(baseCurrency))
                .targetCurrency(Currency.getInstance(targetCurrency))
                .rate(rate)
                .timestamp(timestamp)
                .source(source)
                .bid(bid)
                .ask(ask)
                .high(high)
                .low(low)
                .volume(volume)
                .build();
    }
}