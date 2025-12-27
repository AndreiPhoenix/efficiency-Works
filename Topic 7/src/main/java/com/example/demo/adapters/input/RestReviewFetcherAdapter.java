package com.example.demo.adapters.input;

import com.example.demo.core.domain.Review;
import com.example.demo.core.ports.output.ReviewFetcherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Component
public class RestReviewFetcherAdapter implements ReviewFetcherPort {

    private final RestTemplate restTemplate;

    @Value("${external.api.url:http://localhost:8081}")
    private String apiUrl;

    public RestReviewFetcherAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<Review> fetchReviews(String productId) {
        try {
            // Имитация вызова внешнего API
            // В реальном приложении здесь был бы реальный вызов

            // Для демонстрации возвращаем тестовые данные
            return Arrays.asList(
                    new Review("api-1", productId, "Great product from API!", 4.8),
                    new Review("api-2", productId, "Very good quality", 4.5),
                    new Review("api-3", productId, "Could be better", 3.2)
            );

            /* Реальный вызов выглядел бы так:
            Review[] reviews = restTemplate.getForObject(
                apiUrl + "/api/external/reviews?productId=" + productId,
                Review[].class
            );
            return reviews != null ? Arrays.asList(reviews) : List.of();
            */
        } catch (Exception e) {
            System.err.println("Error fetching from API: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public Review fetchReviewById(String reviewId) {
        try {
            // Имитация вызова
            return new Review(reviewId, "api-product", "Review from external API", 4.0);
        } catch (Exception e) {
            return new Review(reviewId, "unknown", "Error fetching review", 0.0);
        }
    }
}