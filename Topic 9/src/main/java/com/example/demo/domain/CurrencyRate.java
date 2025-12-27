package com.example.demo.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyRate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String currencyCode;
    private double rate;
    private LocalDateTime timestamp;
    private String baseCurrency;

    public static final int RECORD_SIZE = 100; // Fixed size for binary format

    public byte[] toBytes() {
        // Convert to fixed-size byte array
        StringBuilder sb = new StringBuilder(RECORD_SIZE);
        sb.append(String.format("%-3s", currencyCode != null ? currencyCode : ""));
        sb.append(String.format("%15.6f", rate));
        sb.append(String.format("%-26s", timestamp != null ? timestamp.toString() : ""));
        sb.append(String.format("%-3s", baseCurrency != null ? baseCurrency : ""));

        // Pad to fixed size
        String record = sb.toString();
        if (record.length() < RECORD_SIZE) {
            record = String.format("%-" + RECORD_SIZE + "s", record);
        }

        return record.getBytes();
    }

    public static CurrencyRate fromBytes(byte[] bytes) {
        if (bytes.length < RECORD_SIZE) {
            throw new IllegalArgumentException("Invalid byte array size");
        }

        String record = new String(bytes).trim();
        if (record.length() < RECORD_SIZE) {
            return null;
        }

        try {
            String currencyCode = record.substring(0, 3).trim();
            double rate = Double.parseDouble(record.substring(3, 18).trim());
            String timestampStr = record.substring(18, 44).trim();
            LocalDateTime timestamp = LocalDateTime.parse(timestampStr);
            String baseCurrency = record.substring(44, 47).trim();

            return CurrencyRate.builder()
                    .currencyCode(currencyCode)
                    .rate(rate)
                    .timestamp(timestamp)
                    .baseCurrency(baseCurrency)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }
}