package com.example.demo.core.domain;

public class Review {
    private String id;
    private String productId;
    private String text;
    private double rating;

    // Конструктор без параметров для JSON сериализации
    public Review() {}

    public Review(String id, String productId, String text, double rating) {
        this.id = id;
        this.productId = productId;
        this.text = text;
        this.rating = rating;
    }

    // Getters и Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", text='" + text + '\'' +
                ", rating=" + rating +
                '}';
    }
}