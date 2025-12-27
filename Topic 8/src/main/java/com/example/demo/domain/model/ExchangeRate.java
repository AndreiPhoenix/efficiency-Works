package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRate {
    private String id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
    private LocalDateTime timestamp;
    private String source;
    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal high;
    private BigDecimal low;
    private Long volume;

    public boolean isValid() {
        return baseCurrency != null &&
                targetCurrency != null &&
                rate != null &&
                rate.compareTo(BigDecimal.ZERO) > 0 &&
                timestamp != null;
    }

    public String getCurrencyPair() {
        return baseCurrency.getCurrencyCode() + "/" + targetCurrency.getCurrencyCode();
    }

    public BigDecimal convert(BigDecimal amount) {
        return amount.multiply(rate);
    }
}