package com.example.demo.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceResult {
    private String operation;
    private long postgresTimeMs;
    private long mongoTimeMs;
    private long differenceMs;
    private double postgresThroughput;
    private double mongoThroughput;
    private String winner;

    public static PerformanceResult of(String operation, long postgresTime, long mongoTime, int records) {
        double postgresThroughput = records * 1000.0 / Math.max(postgresTime, 1);
        double mongoThroughput = records * 1000.0 / Math.max(mongoTime, 1);

        return PerformanceResult.builder()
                .operation(operation)
                .postgresTimeMs(postgresTime)
                .mongoTimeMs(mongoTime)
                .differenceMs(mongoTime - postgresTime)
                .postgresThroughput(postgresThroughput)
                .mongoThroughput(mongoThroughput)
                .winner(postgresTime < mongoTime ? "PostgreSQL" : "MongoDB")
                .build();
    }
}