package com.example.demo.infrastructure.mongodb;

import com.example.demo.domain.model.ExchangeRate;
import com.example.demo.domain.repository.ExchangeRateRepository;
import com.example.demo.infrastructure.mongodb.document.ExchangeRateDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoExchangeRateRepositoryAdapter implements ExchangeRateRepository {

    private final MongoExchangeRateRepository repository;
    private final MongoExchangeRateMapper mapper;

    @Override
    public ExchangeRate save(ExchangeRate rate) {
        log.debug("Saving to MongoDB: {}", rate.getCurrencyPair());
        ExchangeRateDocument document = mapper.toDocument(rate);
        document = repository.save(document);
        return mapper.toDomain(document);
    }

    @Override
    public List<ExchangeRate> saveAll(List<ExchangeRate> rates) {
        log.debug("Batch saving {} rates to MongoDB", rates.size());
        List<ExchangeRateDocument> documents = rates.stream()
                .map(mapper::toDocument)
                .toList();

        List<ExchangeRateDocument> savedDocuments = repository.saveAll(documents);

        return savedDocuments.stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<ExchangeRate> findById(String id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<ExchangeRate> findByCurrencyPair(Currency base, Currency target) {
        return repository.findByBaseCurrencyAndTargetCurrency(
                        base.getCurrencyCode(),
                        target.getCurrencyCode()
                ).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ExchangeRate> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return repository.findByTimestampBetween(start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ExchangeRate> findBySource(String source) {
        return repository.findBySource(source)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<ExchangeRate> findBySourceAndDateRange(String source, LocalDateTime start, LocalDateTime end) {
        return repository.findBySourceAndTimestampBetween(source, start, end)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public void deleteAll() {
        repository.deleteAll();
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }
}