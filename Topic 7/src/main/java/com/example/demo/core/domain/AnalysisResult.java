package com.example.demo.core.domain;

import java.time.LocalDateTime;

public class AnalysisResult {
    private String productId;
    private double averageRating;
    private Sentiment overallSentiment;
    private LocalDateTime analyzedAt;

    // Конструктор без параметров для сериализации
    public AnalysisResult() {
        this.analyzedAt = LocalDateTime.now();
    }

    public AnalysisResult(String productId, double averageRating, Sentiment overallSentiment) {
        this.productId = productId;
        this.averageRating = averageRating;
        this.overallSentiment = overallSentiment;
        this.analyzedAt = LocalDateTime.now();
    }

    // Getters и Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public Sentiment getOverallSentiment() { return overallSentiment; }
    public void setOverallSentiment(Sentiment overallSentiment) { this.overallSentiment = overallSentiment; }

    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }

    @Override
    public String toString() {
        return "AnalysisResult{" +
                "productId='" + productId + '\'' +
                ", averageRating=" + averageRating +
                ", overallSentiment=" + overallSentiment +
                ", analyzedAt=" + analyzedAt +
                '}';
    }
}