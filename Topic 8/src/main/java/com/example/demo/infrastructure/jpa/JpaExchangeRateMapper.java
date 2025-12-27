package com.example.demo.infrastructure.jpa;

import com.example.demo.domain.model.ExchangeRate;
import com.example.demo.infrastructure.jpa.entity.ExchangeRateEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Currency;

@Mapper(componentModel = "spring")
public interface JpaExchangeRateMapper {

    @Mapping(target = "baseCurrency", expression = "java(domain.getBaseCurrency().getCurrencyCode())")
    @Mapping(target = "targetCurrency", expression = "java(domain.getTargetCurrency().getCurrencyCode())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ExchangeRateEntity toEntity(ExchangeRate domain);

    @Mapping(target = "baseCurrency", expression = "java(toCurrency(entity.getBaseCurrency()))")
    @Mapping(target = "targetCurrency", expression = "java(toCurrency(entity.getTargetCurrency()))")
    ExchangeRate toDomain(ExchangeRateEntity entity);

    default Currency toCurrency(String currencyCode) {
        return Currency.getInstance(currencyCode);
    }
}