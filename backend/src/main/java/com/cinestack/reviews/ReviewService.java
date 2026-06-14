package com.cinestack.reviews;

import com.cinestack.common.ApiException;
import com.cinestack.common.Visibility;
import com.cinestack.movies.Movie;
import com.cinestack.movies.MovieService;
import com.cinestack.reviews.ReviewDtos.ReviewRequest;
import com.cinestack.reviews.ReviewDtos.ReviewResponse;
import com.cinestack.users.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewService {
    private final ReviewRepository reviews;
    private final MovieService movieService;

    public ReviewService(ReviewRepository reviews, MovieService movieService) {
        this.reviews = reviews;
        this.movieService = movieService;
    }

    public List<ReviewResponse> publicForMovie(UUID movieId) {
        return reviews.findByMovie_IdAndVisibilityOrderByCreatedAtDesc(movieId, Visibility.PUBLIC)
                .stream().map(ReviewResponse::from).toList();
    }

    public List<ReviewResponse> mine(User user) {
        return reviews.findByUserOrderByCreatedAtDesc(user).stream().map(ReviewResponse::from).toList();
    }

    public List<ReviewResponse> publicForUser(UUID userId) {
        return reviews.findByUser_IdAndVisibilityOrderByCreatedAtDesc(userId, Visibility.PUBLIC)
                .stream().map(ReviewResponse::from).toList();
    }

    @Transactional
    public ReviewResponse create(User user, UUID movieId, ReviewRequest request) {
        Movie movie = movieService.requireMovie(movieId);
        if (reviews.existsByUser_IdAndMovie_Id(user.getId(), movieId)) {
            throw new ApiException(HttpStatus.CONFLICT, "You already reviewed this movie");
        }
        return ReviewResponse.from(reviews.save(new Review(user, movie, request.rating(), request.body(), request.visibility())));
    }

    @Transactional
    public ReviewResponse update(User user, UUID reviewId, ReviewRequest request) {
        Review review = requireOwned(user, reviewId);
        review.update(request.rating(), request.body(), request.visibility());
        return ReviewResponse.from(review);
    }

    @Transactional
    public void delete(User user, UUID reviewId) {
        reviews.delete(requireOwned(user, reviewId));
    }

    private Review requireOwned(User user, UUID reviewId) {
        Review review = reviews.findById(reviewId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.isOwnedBy(user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this review");
        }
        return review;
    }
}
