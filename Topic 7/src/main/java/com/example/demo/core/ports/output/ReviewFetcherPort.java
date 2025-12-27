package com.example.demo.core.ports.output;

import com.example.demo.core.domain.Review;
import java.util.List;

public interface ReviewFetcherPort {
    List<Review> fetchReviews(String productId);
    Review fetchReviewById(String reviewId);
}