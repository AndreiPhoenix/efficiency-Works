package com.example.demo.core.ports.input;

import com.example.demo.core.domain.Sentiment;
import java.util.List;

public interface ReviewAnalyzerUseCase {
    double calculateAverageRating(List<Double> ratings);
    Sentiment analyzeSentiment(String reviewText);
    double analyzeProductReviews(String productId);
}