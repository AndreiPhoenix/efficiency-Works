package com.example.demo;

import com.example.demo.domain.DataGenerator;
import com.example.demo.performance.PerformanceAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
@Slf4j
public class DemoApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting File Access Benchmark Application");

        try {
            // Generate test data if needed
            if (args.length > 0 && "generate".equals(args[0])) {
                DataGenerator.main(new String[]{});
            }

            // Run benchmarks
            PerformanceAnalyzer.main(new String[]{});

        } catch (IOException e) {
            log.error("Error running benchmarks", e);
        }

        log.info("Benchmark completed. Check logs for results.");
    }
}