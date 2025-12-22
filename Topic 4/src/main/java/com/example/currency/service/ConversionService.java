package com.example.currency.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class ConversionService {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();
    private final Map<String, RateInfo> rates = new ConcurrentHashMap<>();

    // ❌ Пример плохой практики (для демонстрации)
    private final List<Runnable> leakedCallbacks = new ArrayList<>();

    public ConversionService() {
        // Инициализация тестовых данных
        initializeTestData();
    }

    private void initializeTestData() {
        accounts.put("user1", new Account("user1",
                Map.of("USD", BigDecimal.valueOf(1000),
                        "EUR", BigDecimal.valueOf(500),
                        "GBP", BigDecimal.valueOf(300))));

        accounts.put("user2", new Account("user2",
                Map.of("USD", BigDecimal.valueOf(2000),
                        "JPY", BigDecimal.valueOf(100000),
                        "CNY", BigDecimal.valueOf(5000))));

        // Инициализация курсов
        updateAllRates();
    }

    // ❌ Проблемный метод: создает много потоков
    @Async
    public CompletableFuture<Void> badUpdateAllRatesAsync() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String baseCurrency : Arrays.asList("USD", "EUR", "GBP", "JPY")) {
            for (String targetCurrency : Arrays.asList("USD", "EUR", "GBP", "JPY", "CNY", "RUB")) {
                if (!baseCurrency.equals(targetCurrency)) {
                    // Создаем отдельную задачу для каждой пары - ПЛОХО!
                    futures.add(CompletableFuture.runAsync(() -> {
                        updateRate(baseCurrency, targetCurrency);
                    }));
                }
            }
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    // ✅ Хороший метод: использует ограниченный пул
    @Async("rateUpdateExecutor")
    public CompletableFuture<Void> goodUpdateAllRatesAsync() {
        return CompletableFuture.runAsync(this::updateAllRates);
    }

    @Scheduled(fixedRate = 60000) // Каждую минуту
    public void updateAllRates() {
        log.info("Starting scheduled rate update");
        long startTime = System.currentTimeMillis();

        List<String> currencies = Arrays.asList("USD", "EUR", "GBP", "JPY", "CNY", "RUB");

        for (String base : currencies) {
            for (String target : currencies) {
                if (!base.equals(target)) {
                    updateRate(base, target);
                }
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Rate update completed in {} ms", duration);

        // Логируем проблему если обновление заняло слишком много времени
        if (duration > 10000) {
            log.warn("Rate update took too long: {} ms. Possible performance issue.", duration);
        }
    }

    @Cacheable(value = "exchangeRates", key = "#baseCurrency + '-' + #targetCurrency")
    public BigDecimal getRate(String baseCurrency, String targetCurrency) {
        log.debug("Cache miss for rate {}/{}", baseCurrency, targetCurrency);

        // Имитация запроса к внешнему API
        try {
            Thread.sleep(50 + ThreadLocalRandom.current().nextInt(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Rate request interrupted", e);
        }

        BigDecimal rate = generateRate(baseCurrency, targetCurrency);
        rates.put(baseCurrency + "-" + targetCurrency,
                new RateInfo(rate, LocalDateTime.now()));

        return rate;
    }

    @CacheEvict(value = "exchangeRates", allEntries = true)
    public void clearRateCache() {
        log.info("Exchange rates cache cleared");
    }

    @Transactional
    public ConversionResult convert(String userId, String fromCurrency,
                                    String toCurrency, BigDecimal amount) {

        log.info("Starting conversion for user {}: {} {} -> {}",
                userId, amount, fromCurrency, toCurrency);

        long startTime = System.nanoTime();

        try {
            Account account = accounts.get(userId);
            if (account == null) {
                throw new IllegalArgumentException("Account not found: " + userId);
            }

            // Проверка баланса
            if (account.getBalance(fromCurrency).compareTo(amount) < 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }

            // Получение курса
            BigDecimal rate = getRate(fromCurrency, toCurrency);

            // Расчет комиссии (1%)
            BigDecimal commission = amount.multiply(BigDecimal.valueOf(0.01))
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal amountAfterCommission = amount.subtract(commission);

            // Конвертация
            BigDecimal convertedAmount = amountAfterCommission.multiply(rate)
                    .setScale(2, RoundingMode.HALF_UP);

            // Обновление балансов
            account.subtract(fromCurrency, amount);
            account.add(toCurrency, convertedAmount);

            // Сохранение транзакции
            Transaction transaction = new Transaction(
                    UUID.randomUUID().toString(),
                    userId,
                    fromCurrency,
                    toCurrency,
                    amount,
                    convertedAmount,
                    rate,
                    commission,
                    LocalDateTime.now()
            );

            account.addTransaction(transaction);

            ConversionResult result = new ConversionResult(
                    transaction.getId(),
                    amount,
                    convertedAmount,
                    rate,
                    commission,
                    fromCurrency,
                    toCurrency,
                    account.getBalance(fromCurrency),
                    account.getBalance(toCurrency)
            );

            long duration = (System.nanoTime() - startTime) / 1_000_000;
            log.info("Conversion completed in {} ms", duration);

            // Мониторинг длительных операций
            if (duration > 500) {
                log.warn("Slow conversion detected: {} ms for user {}", duration, userId);
            }

            return result;

        } catch (Exception e) {
            log.error("Conversion failed for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    // Метод для демонстрации утечки памяти
    public void registerLeakyCallback(Runnable callback) {
        leakedCallbacks.add(callback); // Утечка! Callbacks никогда не удаляются
    }

    private BigDecimal generateRate(String from, String to) {
        // Детерминированная генерация курса для тестов
        int hash = Math.abs((from + to).hashCode());
        double baseRate = 0.5 + (hash % 100) / 100.0;
        return BigDecimal.valueOf(baseRate).setScale(4, RoundingMode.HALF_UP);
    }

    private void updateRate(String baseCurrency, String targetCurrency) {
        try {
            BigDecimal newRate = generateRate(baseCurrency, targetCurrency);
            rates.put(baseCurrency + "-" + targetCurrency,
                    new RateInfo(newRate, LocalDateTime.now()));
            log.trace("Updated rate {}/{}: {}", baseCurrency, targetCurrency, newRate);
        } catch (Exception e) {
            log.error("Failed to update rate {}/{}", baseCurrency, targetCurrency, e);
        }
    }

    // Вложенные классы моделей
    public static class Account {
        private final String userId;
        private final Map<String, BigDecimal> balances;
        private final List<Transaction> transactions = new ArrayList<>();

        public Account(String userId, Map<String, BigDecimal> initialBalances) {
            this.userId = userId;
            this.balances = new ConcurrentHashMap<>(initialBalances);
        }

        public BigDecimal getBalance(String currency) {
            return balances.getOrDefault(currency, BigDecimal.ZERO);
        }

        public void add(String currency, BigDecimal amount) {
            balances.merge(currency, amount, BigDecimal::add);
        }

        public void subtract(String currency, BigDecimal amount) {
            balances.merge(currency, amount, (old, sub) -> {
                BigDecimal newBalance = old.subtract(sub);
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Insufficient funds");
                }
                return newBalance;
            });
        }

        public void addTransaction(Transaction transaction) {
            transactions.add(transaction);
        }

        public List<Transaction> getTransactions() {
            return Collections.unmodifiableList(transactions);
        }
    }

    public static class RateInfo {
        private final BigDecimal rate;
        private final LocalDateTime timestamp;

        public RateInfo(BigDecimal rate, LocalDateTime timestamp) {
            this.rate = rate;
            this.timestamp = timestamp;
        }

        public BigDecimal getRate() { return rate; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class Transaction {
        private final String id;
        private final String userId;
        private final String fromCurrency;
        private final String toCurrency;
        private final BigDecimal amount;
        private final BigDecimal convertedAmount;
        private final BigDecimal rate;
        private final BigDecimal commission;
        private final LocalDateTime timestamp;

        public Transaction(String id, String userId, String fromCurrency,
                           String toCurrency, BigDecimal amount, BigDecimal convertedAmount,
                           BigDecimal rate, BigDecimal commission, LocalDateTime timestamp) {
            this.id = id;
            this.userId = userId;
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
            this.amount = amount;
            this.convertedAmount = convertedAmount;
            this.rate = rate;
            this.commission = commission;
            this.timestamp = timestamp;
        }

        // Getters
        public String getId() { return id; }
        public String getUserId() { return userId; }
        public String getFromCurrency() { return fromCurrency; }
        public String getToCurrency() { return toCurrency; }
        public BigDecimal getAmount() { return amount; }
        public BigDecimal getConvertedAmount() { return convertedAmount; }
        public BigDecimal getRate() { return rate; }
        public BigDecimal getCommission() { return commission; }
        public LocalDateTime getTimestamp() { return timestamp; }
    }

    public static class ConversionResult {
        private final String transactionId;
        private final BigDecimal originalAmount;
        private final BigDecimal convertedAmount;
        private final BigDecimal rate;
        private final BigDecimal commission;
        private final String fromCurrency;
        private final String toCurrency;
        private final BigDecimal fromBalanceAfter;
        private final BigDecimal toBalanceAfter;

        public ConversionResult(String transactionId, BigDecimal originalAmount,
                                BigDecimal convertedAmount, BigDecimal rate,
                                BigDecimal commission, String fromCurrency,
                                String toCurrency, BigDecimal fromBalanceAfter,
                                BigDecimal toBalanceAfter) {
            this.transactionId = transactionId;
            this.originalAmount = originalAmount;
            this.convertedAmount = convertedAmount;
            this.rate = rate;
            this.commission = commission;
            this.fromCurrency = fromCurrency;
            this.toCurrency = toCurrency;
            this.fromBalanceAfter = fromBalanceAfter;
            this.toBalanceAfter = toBalanceAfter;
        }

        // Getters
        public String getTransactionId() { return transactionId; }
        public BigDecimal getOriginalAmount() { return originalAmount; }
        public BigDecimal getConvertedAmount() { return convertedAmount; }
        public BigDecimal getRate() { return rate; }
        public BigDecimal getCommission() { return commission; }
        public String getFromCurrency() { return fromCurrency; }
        public String getToCurrency() { return toCurrency; }
        public BigDecimal getFromBalanceAfter() { return fromBalanceAfter; }
        public BigDecimal getToBalanceAfter() { return toBalanceAfter; }
    }
}