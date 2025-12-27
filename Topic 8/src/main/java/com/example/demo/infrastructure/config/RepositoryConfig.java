package com.example.demo.infrastructure.config;

import com.example.demo.domain.repository.ExchangeRateRepository;
import com.example.demo.infrastructure.jpa.JpaExchangeRateRepositoryAdapter;
import com.example.demo.infrastructure.mongodb.MongoExchangeRateRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class RepositoryConfig {

    private final com.example.demo.infrastructure.jpa.JpaExchangeRateRepository jpaRepository;
    private final com.example.demo.infrastructure.jpa.JpaExchangeRateMapper jpaMapper;
    private final com.example.demo.infrastructure.mongodb.MongoExchangeRateRepository mongoRepository;
    private final com.example.demo.infrastructure.mongodb.MongoExchangeRateMapper mongoMapper;

    @Bean
    @Primary
    public ExchangeRateRepository postgresRepository() {
        return new JpaExchangeRateRepositoryAdapter(jpaRepository, jpaMapper);
    }

    @Bean
    public ExchangeRateRepository mongoRepository() {
        return new MongoExchangeRateRepositoryAdapter(mongoRepository, mongoMapper);
    }
}