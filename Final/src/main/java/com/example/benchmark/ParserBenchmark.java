package com.example.benchmark;

import com.example.MainApp;
import com.example.model.DataModel;
import com.example.parser.ParserService;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 2)
public class ParserBenchmark {

    private ConfigurableApplicationContext context;
    private ParserService parserService;

    @Setup(Level.Trial)
    public void setup() {
        // Запускаем Spring Boot приложение один раз для всех бенчмарков
        System.setProperty("spring.profiles.active", "test");
        context = org.springframework.boot.SpringApplication.run(MainApp.class);
        parserService = context.getBean(ParserService.class);
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Benchmark
    public void benchmarkForLoop(Blackhole bh) {
        List<DataModel> result = parserService.parseWithForLoop(100);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkStream(Blackhole bh) {
        List<DataModel> result = parserService.parseWithStream(100);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkParallelStream(Blackhole bh) {
        List<DataModel> result = parserService.parseWithParallelStream(100);
        bh.consume(result);
    }
}