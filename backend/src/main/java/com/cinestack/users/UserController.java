package com.cinestack.users;

import com.cinestack.common.ApiException;
import com.cinestack.reviews.ReviewDtos.ReviewResponse;
import com.cinestack.reviews.ReviewService;
import com.cinestack.watchlists.WatchlistDtos.WatchlistResponse;
import com.cinestack.watchlists.WatchlistService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.cinestack.users.UserDtos.UserResponse;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    private final WatchlistService watchlistService;
    private final ReviewService reviewService;

    public UserController(UserRepository users, WatchlistService watchlistService, ReviewService reviewService) {
        this.users = users;
        this.watchlistService = watchlistService;
        this.reviewService = reviewService;
    }

    @GetMapping("/{userId}")
    UserResponse get(@PathVariable UUID userId) {
        return users.findById(userId).map(UserResponse::from)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @GetMapping("/{userId}/public-watchlists")
    List<WatchlistResponse> publicWatchlists(@PathVariable UUID userId) {
        return watchlistService.publicForUser(userId);
    }

    @GetMapping("/{userId}/reviews")
    List<ReviewResponse> reviews(@PathVariable UUID userId) {
        return reviewService.publicForUser(userId);
    }
}
