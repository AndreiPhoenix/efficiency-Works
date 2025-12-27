package com.example.demo.core.service;

import com.example.demo.core.domain.AnalysisResult;
import com.example.demo.core.domain.Review;
import com.example.demo.core.domain.Sentiment;
import com.example.demo.core.ports.output.ReviewFetcherPort;
import com.example.demo.core.ports.output.ResultPublisherPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewAnalyzerServiceTest {

    @Mock
    private ReviewFetcherPort reviewFetcher;

    @Mock
    private ResultPublisherPort resultPublisher;

    @Test
    void calculateAverageRating_shouldReturnCorrectAverage() {
        // Arrange
        ReviewAnalyzerService service = new ReviewAnalyzerService(reviewFetcher, resultPublisher);
        List<Double> ratings = List.of(4.0, 5.0, 3.0, 4.0);

        // Act
        double result = service.calculateAverageRating(ratings);

        // Assert
        assertEquals(4.0, result, 0.001);
    }

    @Test
    void calculateAverageRating_emptyList_shouldReturnZero() {
        // Arrange
        ReviewAnalyzerService service = new ReviewAnalyzerService(reviewFetcher, resultPublisher);

        // Act
        double result = service.calculateAverageRating(List.of());

        // Assert
        assertEquals(0.0, result, 0.001);
    }

    @Test
    void analyzeSentiment_positiveText_shouldReturnPositive() {
        // Arrange
        ReviewAnalyzerService service = new ReviewAnalyzerService(reviewFetcher, resultPublisher);
        String text = "This is a great product! I love it!";

        // Act
        Sentiment result = service.analyzeSentiment(text);

        // Assert
        assertEquals(Sentiment.POSITIVE, result);
    }

    @Test
    void analyzeSentiment_negativeText_shouldReturnNegative() {
        // Arrange
        ReviewAnalyzerService service = new ReviewAnalyzerService(reviewFetcher, resultPublisher);
        String text = "This is a terrible product. The worst I've ever bought.";

        // Act
        Sentiment result = service.analyzeSentiment(text);

        // Assert
        assertEquals(Sentiment.NEGATIVE, result);
    }

    @Test
    void analyzeSentiment_neutralText_shouldReturnNeutral() {
        // Arrange
        ReviewAnalyzerService service = new ReviewAnalyzerService(reviewFetcher, resultPublisher);
        String text = "The product arrived on time.";

        // Act
        Sentiment result = service.analyzeSentiment(text);

        // Assert
        assertEquals(Sentiment.NEUTRAL, result);
    }

    @Test
    void analyzeProductReviews_shouldCallPortsAndReturnAverage() {
        // Arrange
        ReviewAnalyzerService service = new ReviewAnalyzerService(reviewFetcher, resultPublisher);

        List<Review> mockReviews = List.of(
                new Review("1", "product-1", "Great product!", 5.0),
                new Review("2", "product-1", "Not bad", 3.0),
                new Review("3", "product-1", "Excellent quality", 4.0)
        );

        when(reviewFetcher.fetchReviews("product-1")).thenReturn(mockReviews);
        doNothing().when(resultPublisher).publishResult(any(AnalysisResult.class));
        doNothing().when(resultPublisher).saveResult(any(AnalysisResult.class));

        // Act
        double result = service.analyzeProductReviews("product-1");

        // Assert
        assertEquals(4.0, result, 0.001);
        verify(reviewFetcher, times(1)).fetchReviews("product-1");
        verify(resultPublisher, times(1)).publishResult(any(AnalysisResult.class));
        verify(resultPublisher, times(1)).saveResult(any(AnalysisResult.class));
    }
}