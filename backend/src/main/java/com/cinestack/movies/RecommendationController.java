package com.cinestack.movies;

import com.cinestack.auth.CurrentUser;
import com.cinestack.movies.MovieDtos.MovieResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecommendationController {
    private final CurrentUser currentUser;
    private final RecommendationService recommendationService;

    public RecommendationController(CurrentUser currentUser, RecommendationService recommendationService) {
        this.currentUser = currentUser;
        this.recommendationService = recommendationService;
    }

    @GetMapping("/api/recommendations")
    List<MovieResponse> recommendations(Authentication authentication) {
        return recommendationService.forUser(currentUser.require(authentication));
    }
}
