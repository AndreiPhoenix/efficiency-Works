package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
        System.out.println("Demo Application with Hexagonal Architecture started!");
        System.out.println("Available endpoints:");
        System.out.println("  POST /api/reviews/analyze/{productId}");
        System.out.println("  GET  /api/reviews/{productId}");
        System.out.println("  GET  /api/reviews/sentiment?text=your_text");
    }
}