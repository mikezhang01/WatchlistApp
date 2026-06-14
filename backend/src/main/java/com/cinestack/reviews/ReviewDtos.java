package com.cinestack.reviews;

import com.cinestack.common.Visibility;
import com.cinestack.movies.MovieDtos.MovieResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public final class ReviewDtos {
    private ReviewDtos() {
    }

    public record ReviewRequest(
            @Min(1) @Max(5) int rating,
            String body,
            @NotNull Visibility visibility
    ) {
    }

    public record ReviewResponse(
            UUID id,
            UUID userId,
            String displayName,
            MovieResponse movie,
            int rating,
            String body,
            Visibility visibility
    ) {
        public static ReviewResponse from(Review review) {
            return new ReviewResponse(
                    review.getId(),
                    review.getUser().getId(),
                    review.getUser().getDisplayName(),
                    MovieResponse.from(review.getMovie()),
                    review.getRating(),
                    review.getBody(),
                    review.getVisibility()
            );
        }
    }
}

