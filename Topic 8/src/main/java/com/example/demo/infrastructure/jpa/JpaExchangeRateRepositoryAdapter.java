package com.example.demo.infrastructure.jpa;

import com.example.demo.domain.model.ExchangeRate;
import com.example.demo.domain.repository.ExchangeRateRepository;
import com.example.demo.infrastructure.jpa.entity.ExchangeRateEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JpaExchangeRateRepositoryAdapter implements ExchangeRateRepository {

    private final JpaExchangeRateRepository repository;
    private final JpaExchangeRateMapper mapper;

    @Override
    @Transactional
    public ExchangeRate save(ExchangeRate rate) {
        log.debug("Saving to PostgreSQL: {}", rate.getCurrencyPair());
        ExchangeRateEntity entity = mapper.toEntity(rate);
        entity = repository.save(entity);
        return mapper.toDomain(entity);
    }

    @Override
    @Transactional
    public List<ExchangeRate> saveAll(List<ExchangeRate> rates) {
        log.debug("Batch saving {} rates to PostgreSQL", rates.size());
        List<ExchangeRateEntity> entities = rates.stream()
                .map(mapper::toEntity)
                .toList();

        List<ExchangeRateEntity> savedEntities = repository.saveAll(entities);

        return savedEntities.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExchangeRate> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> findByCurrencyPair(Currency base, Currency target) {
        return repository.findByBaseCurrencyAndTargetCurrency(
                        base.getCurrencyCode(),
                        target.getCurrencyCode()
                ).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByTimestampBetween(start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> findBySource(String source) {
        return repository.findBySource(source)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExchangeRate> findBySourceAndDateRange(String source, LocalDateTime start, LocalDateTime end) {
        return repository.findBySourceAndTimestampBetween(source, start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }

    @Override
    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        return repository.existsById(id);
    }
}