package com.example.demo.domain.repository;

import com.example.demo.domain.model.ExchangeRate;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

public interface ExchangeRateRepository {
    ExchangeRate save(ExchangeRate rate);
    List<ExchangeRate> saveAll(List<ExchangeRate> rates);
    Optional<ExchangeRate> findById(String id);
    List<ExchangeRate> findByCurrencyPair(Currency base, Currency target);
    List<ExchangeRate> findByDateRange(LocalDateTime start, LocalDateTime end);
    List<ExchangeRate> findBySource(String source);
    List<ExchangeRate> findBySourceAndDateRange(String source, LocalDateTime start, LocalDateTime end);
    long count();
    void deleteAll();
    boolean existsById(String id);
}