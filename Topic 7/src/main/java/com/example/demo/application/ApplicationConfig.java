package com.example.demo.application;

import com.example.demo.core.service.ReviewAnalyzerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ReviewAnalyzerService reviewAnalyzerService(
            com.example.demo.core.ports.output.ReviewFetcherPort reviewFetcher,
            com.example.demo.core.ports.output.ResultPublisherPort resultPublisher) {
        return new ReviewAnalyzerService(reviewFetcher, resultPublisher);
    }
}