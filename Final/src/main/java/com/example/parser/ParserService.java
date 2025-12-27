package com.example.parser;

import com.example.model.DataModel;
import com.example.repository.DataRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class ParserService {

    private final DataRepository dataRepository;
    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    private ExecutorService executorService;

    private Counter successfulParses;
    private Counter failedParses;
    private Counter dbInserts;
    private Timer parseTimer;

    @PostConstruct
    public void init() {
        // Инициализация ExecutorService
        executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );

        // Инициализация метрик
        successfulParses = Counter.builder("parser.success")
                .description("Number of successful parses")
                .register(meterRegistry);

        failedParses = Counter.builder("parser.failures")
                .description("Number of failed parses")
                .register(meterRegistry);

        dbInserts = Counter.builder("db.inserts")
                .description("Number of database inserts")
                .register(meterRegistry);

        parseTimer = Timer.builder("parser.duration")
                .description("Time taken for parsing")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);
    }

    @Scheduled(fixedDelay = 5000)
    public void scheduledParsing() {
        parseData(100); // Уменьшил с 1000 для тестирования
    }

    public void parseData(int batchSize) {
        log.debug("Starting parsing of {} records", batchSize);

        Span span = tracer.spanBuilder("parseData")
                .setAttribute("batch.size", batchSize)
                .startSpan();

        try {
            parseTimer.record(() -> {
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                AtomicInteger successCount = new AtomicInteger();
                AtomicInteger failureCount = new AtomicInteger();

                for (int i = 0; i < batchSize; i++) {
                    final int recordNumber = i;
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        Span recordSpan = tracer.spanBuilder("parseRecord")
                                .setAttribute("record.number", recordNumber)
                                .startSpan();

                        try {
                            DataModel data = parseSingleRecord();
                            saveToDatabase(data);
                            successCount.incrementAndGet();
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            log.error("Failed to parse record {}", recordNumber, e);
                            recordSpan.recordException(e);
                        } finally {
                            recordSpan.end();
                        }
                    }, executorService);
                    futures.add(future);
                }

                // Ждем завершения всех задач
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .join();

                successfulParses.increment(successCount.get());
                failedParses.increment(failureCount.get());

                log.info("Parsing completed: {} successful, {} failed",
                        successCount.get(), failureCount.get());
            });
        } catch (Exception e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    @Transactional
    public void saveToDatabase(DataModel data) {
        dataRepository.save(data);
        dbInserts.increment();
    }

    private DataModel parseSingleRecord() {
        Span span = tracer.spanBuilder("parseSingleRecord").startSpan();

        try {
            // Имитация парсинга данных
            DataModel data = new DataModel();
            data.setData("Sample data " + System.currentTimeMillis());
            data.setValue(Math.random() * 1000);

            Random random = new Random();
            data.setCategory("CATEGORY_" + random.nextInt(10));
            data.setTimestamp(LocalDateTime.now());
            data.setProcessingTime(System.nanoTime());

            // Имитация обработки (максимум 5 мс)
            Thread.sleep(random.nextInt(5));

            return data;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            span.recordException(e);
            throw new RuntimeException("Parsing interrupted", e);
        } finally {
            span.end();
        }
    }

    // Методы для различных реализаций парсинга (для бенчмаркинга)
    public List<DataModel> parseWithForLoop(int count) {
        List<DataModel> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            result.add(parseSingleRecord());
        }
        return result;
    }

    public List<DataModel> parseWithStream(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> parseSingleRecord())
                .collect(Collectors.toList());
    }

    public List<DataModel> parseWithParallelStream(int count) {
        return IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> parseSingleRecord())
                .collect(Collectors.toList());
    }

    @PreDestroy
    public void cleanup() {
        log.info("Shutting down executor service");
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                    if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                        log.error("Executor service did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}