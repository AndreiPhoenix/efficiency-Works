package com.example.benchmark;

import com.example.MainApp;
import com.example.parser.ParserService;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SingleShotTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 0)
@Measurement(iterations = 1)
@Fork(value = 1)
public class MemoryBenchmark {

    private ConfigurableApplicationContext context;
    private ParserService parserService;

    @Setup(Level.Trial)
    public void setup() {
        System.setProperty("spring.profiles.active", "benchmark");
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
    public void benchmarkObjectAllocation(Blackhole bh) {
        List<String> objects = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            objects.add(new String("Object_" + i + "_" + System.currentTimeMillis()));
        }
        bh.consume(objects);
        System.gc();
    }

    @Benchmark
    public void benchmarkParserMemory(Blackhole bh) {
        for (int i = 0; i < 5; i++) {
            bh.consume(parserService.parseWithForLoop(500));
            System.gc();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MemoryBenchmark.class.getSimpleName())
                .result("benchmark-results/memory-benchmark.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}