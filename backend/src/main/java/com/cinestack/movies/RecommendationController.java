package com.cinestack.movies;

import com.cinestack.auth.CurrentUser;
import com.cinestack.movies.MovieDtos.MovieResponse;
import com.cinestack.users.User;
import com.cinestack.watchlists.WatchlistItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class RecommendationController {
    private final CurrentUser currentUser;
    private final WatchlistItemRepository items;
    private final MovieRepository movies;

    public RecommendationController(CurrentUser currentUser, WatchlistItemRepository items, MovieRepository movies) {
        this.currentUser = currentUser;
        this.items = items;
        this.movies = movies;
    }

    @GetMapping("/api/recommendations")
    List<MovieResponse> recommendations(Authentication authentication) {
        User user = currentUser.require(authentication);
        Set<UUID> alreadyAdded = items.findByWatchlist_Owner_Id(user.getId()).stream()
                .map(item -> item.getMovie().getId())
                .collect(Collectors.toSet());
        return movies.findTop20ByOrderByPopularityDesc().stream()
                .filter(movie -> !alreadyAdded.contains(movie.getId()))
                .limit(10)
                .map(MovieResponse::from)
                .toList();
    }
}
