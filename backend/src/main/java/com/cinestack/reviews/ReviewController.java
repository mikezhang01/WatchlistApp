package com.cinestack.reviews;

import com.cinestack.auth.CurrentUser;
import com.cinestack.reviews.ReviewDtos.ReviewRequest;
import com.cinestack.reviews.ReviewDtos.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class ReviewController {
    private final ReviewService reviewService;
    private final CurrentUser currentUser;

    public ReviewController(ReviewService reviewService, CurrentUser currentUser) {
        this.reviewService = reviewService;
        this.currentUser = currentUser;
    }

    @GetMapping("/api/movies/{movieId}/reviews")
    List<ReviewResponse> publicForMovie(@PathVariable UUID movieId) {
        return reviewService.publicForMovie(movieId);
    }

    @PostMapping("/api/movies/{movieId}/reviews")
    ReviewResponse create(Authentication authentication, @PathVariable UUID movieId,
                          @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(currentUser.require(authentication), movieId, request);
    }

    @PatchMapping("/api/reviews/{reviewId}")
    ReviewResponse update(Authentication authentication, @PathVariable UUID reviewId,
                          @Valid @RequestBody ReviewRequest request) {
        return reviewService.update(currentUser.require(authentication), reviewId, request);
    }

    @DeleteMapping("/api/reviews/{reviewId}")
    void delete(Authentication authentication, @PathVariable UUID reviewId) {
        reviewService.delete(currentUser.require(authentication), reviewId);
    }

    @GetMapping("/api/me/reviews")
    List<ReviewResponse> mine(Authentication authentication) {
        return reviewService.mine(currentUser.require(authentication));
    }
}

