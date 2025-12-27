package com.example.demo.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyRateTest {

    @Test
    void testToBytesAndFromBytes() {
        CurrencyRate original = CurrencyRate.builder()
                .currencyCode("USD")
                .rate(1.234567)
                .timestamp(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .baseCurrency("EUR")
                .build();

        byte[] bytes = original.toBytes();
        assertEquals(CurrencyRate.RECORD_SIZE, bytes.length);

        CurrencyRate restored = CurrencyRate.fromBytes(bytes);
        assertNotNull(restored);
        assertEquals(original.getCurrencyCode(), restored.getCurrencyCode());
        assertEquals(original.getRate(), restored.getRate(), 0.000001);
        assertEquals(original.getTimestamp(), restored.getTimestamp());
        assertEquals(original.getBaseCurrency(), restored.getBaseCurrency());
    }

    @Test
    void testInvalidBytes() {
        byte[] invalidBytes = new byte[CurrencyRate.RECORD_SIZE];
        assertNull(CurrencyRate.fromBytes(invalidBytes));

        byte[] shortBytes = new byte[10];
        assertThrows(IllegalArgumentException.class, () -> CurrencyRate.fromBytes(shortBytes));
    }
}