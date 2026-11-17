package com.cinestack.movies;

import com.cinestack.movies.MovieDtos.MovieResponse;
import com.cinestack.users.User;
import com.cinestack.watchlists.WatchlistItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {
    private final WatchlistItemRepository items;
    private final MovieRepository movies;

    public RecommendationService(WatchlistItemRepository items, MovieRepository movies) {
        this.items = items;
        this.movies = movies;
    }

    /** Placeholder until real recommendations: popular titles the user has not added yet. */
    public List<MovieResponse> forUser(User user) {
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
