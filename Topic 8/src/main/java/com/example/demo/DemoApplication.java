package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.demo.infrastructure.jpa")
@EnableMongoRepositories(basePackages = "com.example.demo.infrastructure.mongodb")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        log.info("✅ Database Comparison Application started!");
        log.info("📊 PostgreSQL + MongoDB performance comparison");
        log.info("🌐 Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("📈 Actuator: http://localhost:8080/actuator");
    }
}
