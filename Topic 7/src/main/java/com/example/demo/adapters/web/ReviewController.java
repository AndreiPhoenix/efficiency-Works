package com.example.demo.adapters.web;

import com.example.demo.core.domain.Review;
import com.example.demo.core.ports.input.ReviewAnalyzerUseCase;
import com.example.demo.core.ports.output.ReviewFetcherPort;
import com.example.demo.core.ports.output.ResultPublisherPort;
import com.example.demo.core.domain.AnalysisResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewAnalyzerUseCase reviewAnalyzer;
    private final ReviewFetcherPort reviewFetcher;
    private final ResultPublisherPort resultPublisher;
    private AnalysisResult lastResult; // Локальное хранение последнего результата

    public ReviewController(ReviewAnalyzerUseCase reviewAnalyzer,
                            ReviewFetcherPort reviewFetcher,
                            ResultPublisherPort resultPublisher) {
        this.reviewAnalyzer = reviewAnalyzer;
        this.reviewFetcher = reviewFetcher;
        this.resultPublisher = resultPublisher;
    }

    @PostMapping("/analyze/{productId}")
    public ResponseEntity<Map<String, Object>> analyzeProductReviews(@PathVariable String productId) {
        double averageRating = reviewAnalyzer.analyzeProductReviews(productId);

        // Сохраняем последний результат для возврата в ответе
        // В реальном приложении это нужно получать из БД или кэша

        return ResponseEntity.ok(Map.of(
                "productId", productId,
                "averageRating", averageRating,
                "message", "Analysis completed successfully"
        ));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable String productId) {
        List<Review> reviews = reviewFetcher.fetchReviews(productId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/sentiment")
    public ResponseEntity<Map<String, String>> analyzeSentiment(@RequestParam String text) {
        var sentiment = reviewAnalyzer.analyzeSentiment(text);
        return ResponseEntity.ok(Map.of(
                "text", text,
                "sentiment", sentiment.name()
        ));
    }
}