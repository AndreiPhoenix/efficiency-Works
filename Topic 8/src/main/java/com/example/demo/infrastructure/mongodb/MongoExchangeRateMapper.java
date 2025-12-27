package com.example.demo.infrastructure.mongodb;

import com.example.demo.domain.model.ExchangeRate;
import com.example.demo.infrastructure.mongodb.document.ExchangeRateDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Currency;

@Mapper(componentModel = "spring")
public interface MongoExchangeRateMapper {

    @Mapping(target = "baseCurrency", expression = "java(domain.getBaseCurrency().getCurrencyCode())")
    @Mapping(target = "targetCurrency", expression = "java(domain.getTargetCurrency().getCurrencyCode())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ExchangeRateDocument toDocument(ExchangeRate domain);

    @Mapping(target = "baseCurrency", expression = "java(toCurrency(document.getBaseCurrency()))")
    @Mapping(target = "targetCurrency", expression = "java(toCurrency(document.getTargetCurrency()))")
    ExchangeRate toDomain(ExchangeRateDocument document);

    default Currency toCurrency(String currencyCode) {
        return Currency.getInstance(currencyCode);
    }
}