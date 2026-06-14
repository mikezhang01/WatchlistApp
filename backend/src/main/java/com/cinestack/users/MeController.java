package com.cinestack.users;

import com.cinestack.auth.CurrentUser;
import com.cinestack.common.Visibility;
import com.cinestack.movies.Movie;
import com.cinestack.movies.MovieService;
import com.cinestack.watchlists.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final CurrentUser currentUser;
    private final WatchlistService watchlistService;
    private final WatchlistRepository watchlists;
    private final MovieService movieService;

    public MeController(CurrentUser currentUser, WatchlistService watchlistService,
                        WatchlistRepository watchlists, MovieService movieService) {
        this.currentUser = currentUser;
        this.watchlistService = watchlistService;
        this.watchlists = watchlists;
        this.movieService = movieService;
    }

    @GetMapping("/export")
    LegacyWatchlist exportLegacy(Authentication authentication) {
        User user = currentUser.require(authentication);
        List<LegacyMovie> movies = watchlists.findByOwnerOrderByCreatedAtDesc(user).stream()
                .flatMap(watchlist -> watchlist.getItems().stream())
                .map(item -> new LegacyMovie(
                        item.getMovie().getTitle(),
                        item.getMovie().getReleaseDate() == null ? null : item.getMovie().getReleaseDate().getYear(),
                        item.getMovie().getRuntimeMinutes(),
                        item.getMovie().getOverview(),
                        item.getStatus() == WatchStatus.WATCHED,
                        item.getPersonalRating() == null ? 0 : item.getPersonalRating()
                ))
                .toList();
        return new LegacyWatchlist(movies);
    }

    @PostMapping("/import/legacy-watchlist")
    WatchlistDtos.WatchlistResponse importLegacy(Authentication authentication,
                                                @Valid @RequestBody LegacyWatchlist legacyWatchlist) {
        User user = currentUser.require(authentication);
        WatchlistDtos.WatchlistResponse imported = watchlistService.create(user,
                new WatchlistDtos.WatchlistRequest("Imported Legacy Watchlist", "Imported from the original Java app.",
                        Visibility.PRIVATE));
        for (LegacyMovie legacyMovie : legacyWatchlist.Movies()) {
            Movie movie = movieService.createManual(legacyMovie.title(), legacyMovie.duration(), legacyMovie.description());
            watchlistService.addItem(user, imported.id(), new WatchlistDtos.AddItemRequest(
                    movie.getId(),
                    Boolean.TRUE.equals(legacyMovie.watched()) ? WatchStatus.WATCHED : WatchStatus.PLANNED
            ));
        }
        return watchlistService.get(imported.id(), user);
    }

    public record LegacyWatchlist(@NotNull List<LegacyMovie> Movies) {
    }

    public record LegacyMovie(
            String title,
            Integer year,
            Integer duration,
            String description,
            Boolean watched,
            Integer score
    ) {
    }
}
