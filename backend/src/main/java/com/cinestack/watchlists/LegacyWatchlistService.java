package com.cinestack.watchlists;

import com.cinestack.common.Visibility;
import com.cinestack.movies.Movie;
import com.cinestack.movies.MovieService;
import com.cinestack.users.User;
import com.cinestack.watchlists.LegacyWatchlistDtos.LegacyMovie;
import com.cinestack.watchlists.LegacyWatchlistDtos.LegacyWatchlist;
import com.cinestack.watchlists.WatchlistDtos.WatchlistResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Import and export in the original desktop app's file format. */
@Service
@Transactional(readOnly = true)
public class LegacyWatchlistService {
    private final WatchlistRepository watchlists;
    private final MovieService movieService;

    public LegacyWatchlistService(WatchlistRepository watchlists, MovieService movieService) {
        this.watchlists = watchlists;
        this.movieService = movieService;
    }

    public LegacyWatchlist exportAll(User user) {
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

    /** One transaction: either the whole file is imported or none of it. */
    @Transactional
    public WatchlistResponse importAsNewList(User user, LegacyWatchlist legacyWatchlist) {
        Watchlist watchlist = new Watchlist(user, "Imported Legacy Watchlist",
                "Imported from the original Java app.", Visibility.PRIVATE);
        for (LegacyMovie legacyMovie : legacyWatchlist.Movies()) {
            Movie movie = movieService.createManual(legacyMovie.title(), legacyMovie.duration(), legacyMovie.description());
            watchlist.addItem(movie,
                    Boolean.TRUE.equals(legacyMovie.watched()) ? WatchStatus.WATCHED : WatchStatus.PLANNED);
        }
        // Saving the list also saves its items (cascade on Watchlist.items).
        return WatchlistResponse.from(watchlists.save(watchlist));
    }
}
