package com.example.demo.core.service;

import com.example.demo.core.domain.AnalysisResult;
import com.example.demo.core.domain.Review;
import com.example.demo.core.domain.Sentiment;
import com.example.demo.core.ports.input.ReviewAnalyzerUseCase;
import com.example.demo.core.ports.output.ReviewFetcherPort;
import com.example.demo.core.ports.output.ResultPublisherPort;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewAnalyzerService implements ReviewAnalyzerUseCase {

    private final ReviewFetcherPort reviewFetcher;
    private final ResultPublisherPort resultPublisher;

    public ReviewAnalyzerService(ReviewFetcherPort reviewFetcher,
                                 ResultPublisherPort resultPublisher) {
        this.reviewFetcher = reviewFetcher;
        this.resultPublisher = resultPublisher;
    }

    @Override
    public double calculateAverageRating(List<Double> ratings) {
        if (ratings == null || ratings.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (Double rating : ratings) {
            sum += rating;
        }
        return sum / ratings.size();
    }

    @Override
    public Sentiment analyzeSentiment(String reviewText) {
        if (reviewText == null || reviewText.trim().isEmpty()) {
            return Sentiment.NEUTRAL;
        }

        String text = reviewText.toLowerCase();
        int positiveWords = countWords(text, "good", "great", "excellent", "love", "best");
        int negativeWords = countWords(text, "bad", "poor", "terrible", "hate", "worst");

        if (positiveWords > negativeWords) {
            return Sentiment.POSITIVE;
        } else if (negativeWords > positiveWords) {
            return Sentiment.NEGATIVE;
        } else {
            return Sentiment.NEUTRAL;
        }
    }

    @Override
    public double analyzeProductReviews(String productId) {
        // Получаем отзывы через порт (не знаем, откуда именно)
        List<Review> reviews = reviewFetcher.fetchReviews(productId);

        // Бизнес-логика: вычисляем средний рейтинг
        List<Double> ratings = reviews.stream()
                .map(Review::getRating)
                .collect(Collectors.toList());

        double averageRating = calculateAverageRating(ratings);

        // Бизнес-логика: определяем общую тональность
        Sentiment overallSentiment = reviews.stream()
                .map(review -> analyzeSentiment(review.getText()))
                .reduce(Sentiment.NEUTRAL, (s1, s2) -> {
                    // Простая логика агрегации
                    if (s1 == s2) return s1;
                    return Sentiment.NEUTRAL;
                });

        // Создаем результат
        AnalysisResult result = new AnalysisResult(productId, averageRating, overallSentiment);

        // Публикуем результат через порт (не знаем, куда именно)
        resultPublisher.publishResult(result);
        resultPublisher.saveResult(result);

        return averageRating;
    }

    private int countWords(String text, String... words) {
        int count = 0;
        for (String word : words) {
            if (text.contains(word)) {
                count++;
            }
        }
        return count;
    }
}