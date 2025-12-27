package com.example.demo.infrastructure.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_rates",
        indexes = {
                @Index(name = "idx_currency_pair", columnList = "baseCurrency,targetCurrency"),
                @Index(name = "idx_timestamp", columnList = "timestamp"),
                @Index(name = "idx_source", columnList = "source")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_currency_pair_timestamp",
                        columnNames = {"baseCurrency", "targetCurrency", "timestamp"})
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 3)
    private String baseCurrency;

    @Column(nullable = false, length = 3)
    private String targetCurrency;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal rate;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 50)
    private String source;

    @Column(precision = 19, scale = 8)
    private BigDecimal bid;

    @Column(precision = 19, scale = 8)
    private BigDecimal ask;

    @Column(precision = 19, scale = 8)
    private BigDecimal high;

    @Column(precision = 19, scale = 8)
    private BigDecimal low;

    private Long volume;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}