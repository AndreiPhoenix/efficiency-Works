package com.example.demo.application.service;

import com.example.demo.application.dto.PerformanceResult;
import com.example.demo.domain.model.ExchangeRate;
import com.example.demo.domain.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    @Qualifier("postgresRepository")
    private final ExchangeRateRepository postgresRepository;

    @Qualifier("mongoRepository")
    private final ExchangeRateRepository mongoRepository;

    private static final Currency USD = Currency.getInstance("USD");
    private static final Currency EUR = Currency.getInstance("EUR");

    public List<PerformanceResult> runPerformanceTests(int batchSize) {
        List<PerformanceResult> results = new ArrayList<>();

        log.info("🚀 Starting performance tests with batch size: {}", batchSize);

        // Генерация тестовых данных
        List<ExchangeRate> testData = generateTestData(batchSize);

        // Очистка данных
        postgresRepository.deleteAll();
        mongoRepository.deleteAll();

        // Тест 1: Вставка одной записи
        results.add(testSingleInsert());

        // Тест 2: Пакетная вставка
        results.add(testBatchInsert(testData));

        // Тест 3: Чтение по ID
        results.add(testReadById());

        // Тест 4: Чтение по валютной паре
        results.add(testReadByCurrencyPair());

        // Тест 5: Чтение по диапазону дат
        results.add(testReadByDateRange());

        // Тест 6: Поиск по источнику
        results.add(testReadBySource());

        return results;
    }

    private List<ExchangeRate> generateTestData(int count) {
        Random random = new Random();
        List<ExchangeRate> data = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ExchangeRate rate = ExchangeRate.builder()
                    .baseCurrency(USD)
                    .targetCurrency(EUR)
                    .rate(BigDecimal.valueOf(0.85 + random.nextDouble() * 0.1))
                    .timestamp(LocalDateTime.now().minusHours(random.nextInt(720))) // До 30 дней назад
                    .source(random.nextBoolean() ? "ECB" : "FED")
                    .bid(BigDecimal.valueOf(0.849))
                    .ask(BigDecimal.valueOf(0.851))
                    .high(BigDecimal.valueOf(0.86))
                    .low(BigDecimal.valueOf(0.84))
                    .volume(random.nextLong(1000000))
                    .build();
            data.add(rate);
        }

        return data;
    }

    private PerformanceResult testSingleInsert() {
        ExchangeRate testRate = generateTestData(1).get(0);

        // PostgreSQL
        long postgresStart = System.nanoTime();
        postgresRepository.save(testRate);
        long postgresTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - postgresStart);

        // MongoDB
        long mongoStart = System.nanoTime();
        mongoRepository.save(testRate);
        long mongoTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - mongoStart);

        return PerformanceResult.of("Single Insert", postgresTime, mongoTime, 1);
    }

    private PerformanceResult testBatchInsert(List<ExchangeRate> data) {
        // PostgreSQL
        long postgresStart = System.nanoTime();
        postgresRepository.saveAll(data);
        long postgresTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - postgresStart);

        // MongoDB
        long mongoStart = System.nanoTime();
        mongoRepository.saveAll(data);
        long mongoTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - mongoStart);

        return PerformanceResult.of("Batch Insert", postgresTime, mongoTime, data.size());
    }

    private PerformanceResult testReadById() {
        // Сохраняем тестовые данные
        ExchangeRate postgresRate = postgresRepository.save(generateTestData(1).get(0));
        ExchangeRate mongoRate = mongoRepository.save(generateTestData(1).get(0));

        // PostgreSQL
        long postgresStart = System.nanoTime();
        postgresRepository.findById(postgresRate.getId());
        long postgresTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - postgresStart);

        // MongoDB
        long mongoStart = System.nanoTime();
        mongoRepository.findById(mongoRate.getId());
        long mongoTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - mongoStart);

        return PerformanceResult.builder()
                .operation("Read by ID")
                .postgresTimeMs(postgresTime)
                .mongoTimeMs(mongoTime)
                .differenceMs(mongoTime - postgresTime)
                .postgresThroughput(1 * 1000.0 / Math.max(postgresTime, 1))
                .mongoThroughput(1 * 1000.0 / Math.max(mongoTime, 1))
                .winner(postgresTime < mongoTime ? "PostgreSQL" : "MongoDB")
                .build();
    }

    private PerformanceResult testReadByCurrencyPair() {
        // PostgreSQL
        long postgresStart = System.nanoTime();
        postgresRepository.findByCurrencyPair(USD, EUR);
        long postgresTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - postgresStart);

        // MongoDB
        long mongoStart = System.nanoTime();
        mongoRepository.findByCurrencyPair(USD, EUR);
        long mongoTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - mongoStart);

        long count = postgresRepository.count();
        return PerformanceResult.of("Read by Currency Pair", postgresTime, mongoTime, (int) count);
    }

    private PerformanceResult testReadByDateRange() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7);

        // PostgreSQL
        long postgresStart = System.nanoTime();
        postgresRepository.findByDateRange(start, end);
        long postgresTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - postgresStart);

        // MongoDB
        long mongoStart = System.nanoTime();
        mongoRepository.findByDateRange(start, end);
        long mongoTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - mongoStart);

        int records = postgresRepository.findByDateRange(start, end).size();
        return PerformanceResult.of("Read by Date Range", postgresTime, mongoTime, records);
    }

    private PerformanceResult testReadBySource() {
        // PostgreSQL
        long postgresStart = System.nanoTime();
        postgresRepository.findBySource("ECB");
        long postgresTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - postgresStart);

        // MongoDB
        long mongoStart = System.nanoTime();
        mongoRepository.findBySource("ECB");
        long mongoTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - mongoStart);

        int records = postgresRepository.findBySource("ECB").size();
        return PerformanceResult.of("Read by Source", postgresTime, mongoTime, records);
    }
}