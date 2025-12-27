package com.example.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_data", indexes = {
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_value", columnList = "value")
})
@Data
public class DataModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String data;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "processing_time")
    private Long processingTime;

    @Version
    private Long version;
}