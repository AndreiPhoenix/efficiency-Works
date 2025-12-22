package com.example.caching.controller;

import com.example.caching.model.Product;
import com.example.caching.service.CacheService;
import com.example.caching.service.ProductService;
import com.example.caching.repository.CacheStats;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CacheService cacheService;
    private final CacheStats cacheStats;

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return product != null ?
                ResponseEntity.ok(product) :
                ResponseEntity.notFound().build();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("/expensive")
    public ResponseEntity<List<Product>> getExpensiveProducts(
            @RequestParam(defaultValue = "1000") BigDecimal minPrice) {
        return ResponseEntity.ok(productService.getExpensiveProducts(minPrice));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @GetMapping("/top")
    public ResponseEntity<List<Product>> getTopProducts() {
        return ResponseEntity.ok(productService.getTopProducts());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        return ResponseEntity.ok(productService.updateProduct(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }

    // Эндпоинты для управления кэшем
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRequests", cacheStats.getTotalRequests());
        stats.put("cacheHits", cacheStats.getCacheHits());
        stats.put("cacheMisses", cacheStats.getCacheMisses());
        stats.put("hitRate", cacheStats.getHitRate());
        stats.put("cacheSize", cacheService.getCacheSize());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/cache/keys")
    public ResponseEntity<Set<String>> getCacheKeys() {
        return ResponseEntity.ok(cacheService.getAllKeys());
    }

    @DeleteMapping("/cache/clear")
    public ResponseEntity<Void> clearCache() {
        cacheService.clearCache();
        cacheStats.clearStats();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/cache/{key}")
    public ResponseEntity<Object> getCacheValue(@PathVariable String key) {
        Object value = cacheService.get(key);
        Long ttl = cacheService.getRemainingTTL(key);

        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("ttl", ttl);
        response.put("exists", value != null);

        return ResponseEntity.ok(response);
    }

    // Эндпоинт для тестирования производительности
    @GetMapping("/performance/{id}")
    public ResponseEntity<Map<String, Object>> testPerformance(@PathVariable Long id) {
        long startTime = System.currentTimeMillis();
        Product product = productService.getProductById(id);
        long endTime = System.currentTimeMillis();

        Map<String, Object> response = new HashMap<>();
        response.put("product", product);
        response.put("responseTime", endTime - startTime);
        response.put("fromCache", endTime - startTime < 1000); // Если < 1 сек, считаем из кэша

        return ResponseEntity.ok(response);
    }
}