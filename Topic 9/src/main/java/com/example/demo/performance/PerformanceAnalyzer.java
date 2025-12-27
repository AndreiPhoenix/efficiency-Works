package com.example.demo.performance;

import com.example.demo.domain.CurrencyRate;
import com.example.demo.domain.DataGenerator;
import com.example.demo.fileaccess.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class PerformanceAnalyzer {

    public static class BenchmarkResult {
        private String methodName;
        private long writeTimeNanos;
        private long sequentialReadTimeNanos;
        private long randomReadTimeNanos;
        private long memoryUsedBytes;
        private int recordCount;

        public BenchmarkResult(String methodName, int recordCount) {
            this.methodName = methodName;
            this.recordCount = recordCount;
        }

        public void printResults() {
            log.info("=== Benchmark Results for {} ({} records) ===", methodName, recordCount);
            log.info("Write time: {} ms", TimeUnit.NANOSECONDS.toMillis(writeTimeNanos));
            log.info("Sequential read time: {} ms", TimeUnit.NANOSECONDS.toMillis(sequentialReadTimeNanos));
            log.info("Random read time: {} ms", TimeUnit.NANOSECONDS.toMillis(randomReadTimeNanos));
            log.info("Memory used: {} MB", memoryUsedBytes / (1024 * 1024));
            log.info("==============================================");
        }
    }

    public static BenchmarkResult runBenchmark(FileAccessService service, String methodName,
                                               int recordCount, String filePath) throws IOException {

        BenchmarkResult result = new BenchmarkResult(methodName, recordCount);

        // Generate test data
        List<CurrencyRate> records = DataGenerator.generateCurrencyRates(recordCount);

        // Measure write performance
        long startTime = System.nanoTime();
        service.writeRecords(records, filePath);
        result.writeTimeNanos = System.nanoTime() - startTime;

        // Force garbage collection before memory measurement
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

        // Measure memory usage
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Measure sequential read performance
        startTime = System.nanoTime();
        List<CurrencyRate> readRecords = service.readSequential(filePath);
        result.sequentialReadTimeNanos = System.nanoTime() - startTime;

        // Measure random read performance
        startTime = System.nanoTime();
        for (int i = 0; i < Math.min(1000, recordCount); i++) {
            service.readRandom(filePath, (long) (Math.random() * recordCount));
        }
        result.randomReadTimeNanos = System.nanoTime() - startTime;

        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        result.memoryUsedBytes = memoryAfter - memoryBefore;

        // Verify data integrity
        if (readRecords.size() != records.size()) {
            log.warn("Data integrity check failed for {}: expected {}, got {}",
                    methodName, records.size(), readRecords.size());
        }

        return result;
    }

    public static void main(String[] args) throws IOException {
        int[] recordCounts = {10000, 50000, 100000};

        for (int recordCount : recordCounts) {
            log.info("\n\n=== Running benchmarks for {} records ===\n", recordCount);

            // RandomAccessFile
            BenchmarkResult rafResult = runBenchmark(
                    new RandomAccessFileService(),
                    "RandomAccessFile",
                    recordCount,
                    "random_access_" + recordCount + ".dat"
            );
            rafResult.printResults();

            // FileChannel
            BenchmarkResult fcResult = runBenchmark(
                    new FileChannelService(),
                    "FileChannel",
                    recordCount,
                    "file_channel_" + recordCount + ".dat"
            );
            fcResult.printResults();

            // Memory Mapped File
            BenchmarkResult mmapResult = runBenchmark(
                    new MemoryMappedFileService(),
                    "MemoryMappedFile",
                    recordCount,
                    "memory_mapped_" + recordCount + ".dat"
            );
            mmapResult.printResults();
        }
    }
}