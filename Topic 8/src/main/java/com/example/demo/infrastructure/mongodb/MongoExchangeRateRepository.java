package com.example.demo.infrastructure.mongodb;

import com.example.demo.infrastructure.mongodb.document.ExchangeRateDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MongoExchangeRateRepository extends MongoRepository<ExchangeRateDocument, String> {

    List<ExchangeRateDocument> findByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);

    List<ExchangeRateDocument> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<ExchangeRateDocument> findBySource(String source);

    List<ExchangeRateDocument> findBySourceAndTimestampBetween(String source, LocalDateTime start, LocalDateTime end);

    @Query("{'timestamp': {$gte: ?0}}")
    long countSince(LocalDateTime since);

    boolean existsByBaseCurrencyAndTargetCurrencyAndTimestamp(
            String baseCurrency, String targetCurrency, LocalDateTime timestamp);
}