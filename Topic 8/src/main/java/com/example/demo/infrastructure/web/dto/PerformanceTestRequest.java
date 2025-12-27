package com.example.demo.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Запрос на запуск тестов производительности")
public class PerformanceTestRequest {

    @NotNull
    @Min(1)
    @Max(10000)
    @Schema(description = "Размер пакета для тестирования", example = "1000", defaultValue = "1000")
    private Integer batchSize;
}