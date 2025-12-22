package com.example.caching.service;

import com.example.caching.model.Product;
import com.example.caching.repository.CacheStats;
import com.example.caching.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CacheStats cacheStats;

    // Время симуляции медленного запроса
    private static final long SLOW_QUERY_DELAY = 2000;

    public ProductService(ProductRepository productRepository, CacheStats cacheStats) {
        this.productRepository = productRepository;
        this.cacheStats = cacheStats;
    }

    @Cacheable(value = "productDetails", key = "#id", unless = "#result == null")
    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        simulateSlowQuery();
        cacheStats.recordMiss();
        log.info("Fetching product {} from database", id);
        return productRepository.findById(id)
                .map(product -> {
                    product.setViewsCount(product.getViewsCount() + 1);
                    return productRepository.save(product);
                })
                .orElse(null);
    }

    @Cacheable(value = "products", key = "'all'", unless = "#result.isEmpty()")
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        simulateSlowQuery();
        cacheStats.recordMiss();
        log.info("Fetching all products from database");
        return productRepository.findAll();
    }

    @Cacheable(value = "products", key = "#category")
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(String category) {
        simulateSlowQuery();
        cacheStats.recordMiss();
        log.info("Fetching products by category {} from database", category);
        return productRepository.findByCategory(category);
    }

    @Cacheable(value = "expensiveProducts", key = "#minPrice")
    @Transactional(readOnly = true)
    public List<Product> getExpensiveProducts(BigDecimal minPrice) {
        simulateSlowQuery();
        cacheStats.recordMiss();
        log.info("Fetching expensive products (>{}) from database", minPrice);
        return productRepository.findByPriceGreaterThan(minPrice);
    }

    @Cacheable(value = "products", key = "'search_' + #keyword")
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        simulateSlowQuery();
        cacheStats.recordMiss();
        log.info("Searching products with keyword: {} in database", keyword);
        return productRepository.searchByKeyword(keyword);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "products", key = "'all'"),
                    @CacheEvict(value = "products", allEntries = true),
                    @CacheEvict(value = "expensiveProducts", allEntries = true)
            },
            put = @CachePut(value = "productDetails", key = "#result.id")
    )
    @Transactional
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        return productRepository.save(product);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "products", key = "'all'"),
                    @CacheEvict(value = "products", allEntries = true),
                    @CacheEvict(value = "expensiveProducts", allEntries = true)
            },
            put = @CachePut(value = "productDetails", key = "#product.id")
    )
    @Transactional
    public Product updateProduct(Product product) {
        log.info("Updating product: {}", product.getId());
        return productRepository.save(product);
    }

    @Caching(
            evict = {
                    @CacheEvict(value = "productDetails", key = "#id"),
                    @CacheEvict(value = "products", key = "'all'"),
                    @CacheEvict(value = "products", allEntries = true),
                    @CacheEvict(value = "expensiveProducts", allEntries = true)
            }
    )
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        productRepository.deleteById(id);
    }

    @Cacheable(value = "products", key = "'top10'")
    @Transactional(readOnly = true)
    public List<Product> getTopProducts() {
        simulateSlowQuery();
        cacheStats.recordMiss();
        log.info("Fetching top 10 products from database");
        return productRepository.findTop10ByViews();
    }

    @CacheEvict(value = {"products", "productDetails", "expensiveProducts"}, allEntries = true)
    @Scheduled(fixedRate = 3600000) // Каждый час
    public void clearAllCaches() {
        log.info("Scheduled cache clearance executed");
    }

    @CacheEvict(value = "products", allEntries = true)
    public void clearProductsCache() {
        log.info("Products cache cleared");
    }

    @CacheEvict(value = "productDetails", key = "#id")
    public void clearProductCache(Long id) {
        log.info("Product {} cache cleared", id);
    }

    private void simulateSlowQuery() {
        try {
            Thread.sleep(SLOW_QUERY_DELAY);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Метод для проверки кэша
    public Product getProductWithCacheCheck(Long id) {
        // В реальном проекте здесь была бы проверка кэша
        return getProductById(id);
    }
}