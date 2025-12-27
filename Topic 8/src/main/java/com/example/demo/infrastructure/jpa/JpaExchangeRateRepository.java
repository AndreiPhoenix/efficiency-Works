package com.example.demo.infrastructure.jpa;

import com.example.demo.infrastructure.jpa.entity.ExchangeRateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaExchangeRateRepository extends JpaRepository<ExchangeRateEntity, String> {

    List<ExchangeRateEntity> findByBaseCurrencyAndTargetCurrency(String baseCurrency, String targetCurrency);

    List<ExchangeRateEntity> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<ExchangeRateEntity> findBySource(String source);

    List<ExchangeRateEntity> findBySourceAndTimestampBetween(String source, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(e) FROM ExchangeRateEntity e WHERE e.timestamp >= :since")
    long countSince(@Param("since") LocalDateTime since);

    boolean existsByBaseCurrencyAndTargetCurrencyAndTimestamp(
            String baseCurrency, String targetCurrency, LocalDateTime timestamp);
}