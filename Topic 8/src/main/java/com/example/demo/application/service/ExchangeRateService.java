package com.example.demo.application.service;

import com.example.demo.domain.model.ExchangeRate;
import com.example.demo.domain.repository.ExchangeRateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;

    @Transactional
    public ExchangeRate save(ExchangeRate rate) {
        if (!rate.isValid()) {
            throw new IllegalArgumentException("Invalid exchange rate");
        }
        log.info("Saving exchange rate: {}", rate.getCurrencyPair());
        return exchangeRateRepository.save(rate);
    }

    @Transactional
    public List<ExchangeRate> saveAll(List<ExchangeRate> rates) {
        rates.forEach(rate -> {
            if (!rate.isValid()) {
                throw new IllegalArgumentException("Invalid exchange rate in batch");
            }
        });
        log.info("Batch saving {} exchange rates", rates.size());
        return exchangeRateRepository.saveAll(rates);
    }

    @Transactional(readOnly = true)
    public Optional<ExchangeRate> findById(String id) {
        return exchangeRateRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRate> findByCurrencyPair(Currency base, Currency target) {
        return exchangeRateRepository.findByCurrencyPair(base, target);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRate> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return exchangeRateRepository.findByDateRange(start, end);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRate> findBySource(String source) {
        return exchangeRateRepository.findBySource(source);
    }

    @Transactional(readOnly = true)
    public List<ExchangeRate> findBySourceAndDateRange(String source, LocalDateTime start, LocalDateTime end) {
        return exchangeRateRepository.findBySourceAndDateRange(source, start, end);
    }

    @Transactional(readOnly = true)
    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        List<ExchangeRate> rates = exchangeRateRepository.findByCurrencyPair(from, to);

        if (rates.isEmpty()) {
            throw new IllegalArgumentException("No exchange rate found for currency pair: "
                    + from + "/" + to);
        }

        ExchangeRate latest = rates.stream()
                .max((r1, r2) -> r1.getTimestamp().compareTo(r2.getTimestamp()))
                .orElseThrow();

        return latest.convert(amount);
    }

    @Transactional(readOnly = true)
    public long count() {
        return exchangeRateRepository.count();
    }

    @Transactional
    public void clear() {
        exchangeRateRepository.deleteAll();
    }
}