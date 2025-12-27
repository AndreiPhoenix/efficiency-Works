package com.example.demo.adapters.input;

import com.example.demo.core.domain.Review;
import com.example.demo.core.ports.output.ReviewFetcherPort;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileReviewFetcherAdapter implements ReviewFetcherPort {

    @Override
    public List<Review> fetchReviews(String productId) {
        List<Review> reviews = new ArrayList<>();

        // Имитация чтения из файла
        try {
            // Создаем тестовый файл, если его нет
            String fileName = "reviews.txt";
            if (!Files.exists(Paths.get(fileName))) {
                String content = "product-1,Great product! Very satisfied.,5.0\n" +
                        "product-1,Not bad, could be better.,3.0\n" +
                        "product-1,Excellent quality, worth the price.,4.0\n" +
                        "product-2,Very disappointed with this purchase.,2.0\n" +
                        "product-2,Average product, nothing special.,3.0\n" +
                        "product-3,Amazing! Best purchase ever!,5.0";
                Files.write(Paths.get(fileName), content.getBytes());
            }

            // Читаем файл
            List<String> lines = Files.readAllLines(Paths.get(fileName));
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(productId)) {
                    reviews.add(new Review(
                            "review-" + (i + 1),
                            parts[0],
                            parts[1],
                            Double.parseDouble(parts[2])
                    ));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            // Возвращаем тестовые данные
            if (productId.equals("product-1")) {
                reviews.add(new Review("1", productId, "Great product!", 5.0));
                reviews.add(new Review("2", productId, "Good quality", 4.0));
                reviews.add(new Review("3", productId, "Average product", 3.0));
            }
        }

        return reviews;
    }

    @Override
    public Review fetchReviewById(String reviewId) {
        // Простая реализация для примера
        return new Review(reviewId, "product-1", "Sample review from file adapter", 4.5);
    }
}