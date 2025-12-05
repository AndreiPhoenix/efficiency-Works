package com.example.performancedemo.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@Threads(8) // Тестируем с 8 потоками
public class MapBenchmark {

    private Map<String, Integer> synchronizedMap;
    private Map<String, Integer> concurrentHashMap;
    private String[] keys;
    private Random random;

    @Setup
    public void setup() {
        synchronizedMap = Collections.synchronizedMap(new HashMap<>());
        concurrentHashMap = new ConcurrentHashMap<>();

        // Инициализируем ключи
        keys = new String[10000];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = "key-" + i;
        }

        random = new Random();

        // Предварительно заполняем мапы
        for (String key : keys) {
            synchronizedMap.put(key, random.nextInt());
            concurrentHashMap.put(key, random.nextInt());
        }
    }

    @Benchmark
    @Group("synchronized")
    @GroupThreads(4)
    public void synchronizedPut(Blackhole bh) {
        String key = keys[random.nextInt(keys.length)];
        synchronizedMap.put(key, random.nextInt());
        bh.consume(key);
    }

    @Benchmark
    @Group("synchronized")
    @GroupThreads(4)
    public Integer synchronizedGet() {
        String key = keys[random.nextInt(keys.length)];
        return synchronizedMap.get(key);
    }

    @Benchmark
    @Group("concurrent")
    @GroupThreads(4)
    public void concurrentPut(Blackhole bh) {
        String key = keys[random.nextInt(keys.length)];
        concurrentHashMap.put(key, random.nextInt());
        bh.consume(key);
    }

    @Benchmark
    @Group("concurrent")
    @GroupThreads(4)
    public Integer concurrentGet() {
        String key = keys[random.nextInt(keys.length)];
        return concurrentHashMap.get(key);
    }

    @Benchmark
    @Group("synchronizedCompute")
    public Integer synchronizedCompute() {
        String key = keys[random.nextInt(keys.length)];
        return synchronizedMap.compute(key, (k, v) ->
                (v == null) ? random.nextInt() : v + 1);
    }

    @Benchmark
    @Group("concurrentCompute")
    public Integer concurrentCompute() {
        String key = keys[random.nextInt(keys.length)];
        return concurrentHashMap.compute(key, (k, v) ->
                (v == null) ? random.nextInt() : v + 1);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MapBenchmark.class.getSimpleName())
                .build();

        new Runner(opt).run();
    }
}