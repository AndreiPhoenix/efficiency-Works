package com.example.benchmark;

import com.example.MainApp;
import com.example.model.DataModel;
import com.example.parser.ParserService;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.AverageTime, Mode.Throughput})
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(value = 2)
public class CompleteParserBenchmark {

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
    @OperationsPerInvocation(100)
    public void benchmarkForLoopSmall(Blackhole bh) {
        List<DataModel> result = parserService.parseWithForLoop(100);
        bh.consume(result);
    }

    @Benchmark
    @OperationsPerInvocation(100)
    public void benchmarkStreamSmall(Blackhole bh) {
        List<DataModel> result = parserService.parseWithStream(100);
        bh.consume(result);
    }

    @Benchmark
    @OperationsPerInvocation(100)
    public void benchmarkParallelStreamSmall(Blackhole bh) {
        List<DataModel> result = parserService.parseWithParallelStream(100);
        bh.consume(result);
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void benchmarkForLoopLarge(Blackhole bh) {
        List<DataModel> result = parserService.parseWithForLoop(1000);
        bh.consume(result);
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void benchmarkStreamLarge(Blackhole bh) {
        List<DataModel> result = parserService.parseWithStream(1000);
        bh.consume(result);
    }

    @Benchmark
    @OperationsPerInvocation(10)
    public void benchmarkParallelStreamLarge(Blackhole bh) {
        List<DataModel> result = parserService.parseWithParallelStream(1000);
        bh.consume(result);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(CompleteParserBenchmark.class.getSimpleName())
                .addProfiler(GCProfiler.class)
                .result("benchmark-results/complete-benchmark.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}