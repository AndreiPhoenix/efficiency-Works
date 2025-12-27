package com.example.demo.infrastructure.mongodb.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "exchange_rates")
@CompoundIndex(name = "currency_pair_timestamp_idx",
        def = "{'baseCurrency': 1, 'targetCurrency': 1, 'timestamp': 1}",
        unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateDocument {

    @Id
    private String id;

    @Indexed
    private String baseCurrency;

    @Indexed
    private String targetCurrency;

    private BigDecimal rate;

    @Indexed
    private LocalDateTime timestamp;

    @Indexed
    private String source;

    private BigDecimal bid;
    private BigDecimal ask;
    private BigDecimal high;
    private BigDecimal low;
    private Long volume;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}