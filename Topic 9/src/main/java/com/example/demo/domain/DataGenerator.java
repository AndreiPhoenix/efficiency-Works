package com.example.demo.domain;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataGenerator {

    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "NZD", "RUB"};
    private static final Random random = new Random();

    public static List<CurrencyRate> generateCurrencyRates(int count) {
        List<CurrencyRate> rates = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(count);

        for (int i = 0; i < count; i++) {
            String currency = CURRENCIES[random.nextInt(CURRENCIES.length)];
            double rate = 0.5 + random.nextDouble() * 150;
            LocalDateTime timestamp = baseTime.plusHours(i);

            CurrencyRate currencyRate = CurrencyRate.builder()
                    .currencyCode(currency)
                    .rate(rate)
                    .timestamp(timestamp)
                    .baseCurrency("USD")
                    .build();

            rates.add(currencyRate);
        }

        return rates;
    }

    public static void generateTextFile(String filePath, int recordCount) throws IOException {
        List<CurrencyRate> rates = generateCurrencyRates(recordCount);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (CurrencyRate rate : rates) {
                writer.write(String.format("%s,%.6f,%s,%s",
                        rate.getCurrencyCode(),
                        rate.getRate(),
                        rate.getTimestamp(),
                        rate.getBaseCurrency()));
                writer.newLine();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // Generate sample files
        generateTextFile("currency_rates_10000.txt", 10000);
        generateTextFile("currency_rates_50000.txt", 50000);
        generateTextFile("currency_rates_100000.txt", 100000);

        System.out.println("Data files generated successfully!");
    }
}