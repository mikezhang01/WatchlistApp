package com.cinestack.movies;

import com.cinestack.common.ApiException;
import com.cinestack.movies.MovieDtos.MovieResponse;
import com.cinestack.movies.MovieDtos.MovieSearchResult;
import com.cinestack.movies.MovieDtos.SearchPage;
import com.cinestack.movies.TmdbClient.TmdbMovieDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Titles, and keeping them in step with TMDB.
 *
 * A transaction holds a database connection until it ends, and a call to TMDB can take
 * seconds. So the methods that talk to TMDB are not transactional as a whole: they read
 * in one short transaction, call TMDB with no connection held, then write in another.
 */
@Service
public class MovieService {
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movies;
    private final GenreRepository genres;
    private final TmdbClient tmdbClient;
    private final TransactionTemplate transaction;
    private final long cacheRefreshDays;
    private final SearchCache searchCache;

    public MovieService(MovieRepository movies, GenreRepository genres, TmdbClient tmdbClient,
                        PlatformTransactionManager transactionManager,
                        @Value("${app.tmdb.cache-refresh-days:30}") long cacheRefreshDays,
                        @Value("${app.tmdb.search-cache-seconds:300}") long searchCacheSeconds) {
        this.movies = movies;
        this.genres = genres;
        this.tmdbClient = tmdbClient;
        this.transaction = new TransactionTemplate(transactionManager);
        this.cacheRefreshDays = cacheRefreshDays;
        this.searchCache = new SearchCache(searchCacheSeconds);
    }

    /** What we have stored for a title, and whether it is due a refresh from TMDB. */
    private record Cached(MovieResponse response, boolean stale) {
    }

    public SearchPage search(String query, int page) {
        long now = System.currentTimeMillis();
        SearchPage recent = searchCache.get(query, page, now);
        if (recent != null) {
            return recent;
        }
        try {
            SearchPage external = tmdbClient.search(query, page);
            if (external != null && !external.results().isEmpty()) {
                searchCache.put(query, page, external, now);
                return external;
            }
        } catch (TmdbApiException ex) {
            log.warn("TMDB search unavailable; using local cache: {}", ex.getMessage());
        }
        List<MovieSearchResult> local = movies.findTop20ByTitleContainingIgnoreCaseOrderByPopularityDesc(query).stream()
                .map(movie -> new MovieSearchResult(
                        movie.getTmdbId(),
                        movie.getMediaType(),
                        movie.getTitle(),
                        movie.getOverview(),
                        movie.getReleaseDate(),
                        movie.getPosterPath()
                ))
                .toList();
        return new SearchPage(local, 1, 1);
    }

    public Movie requireMovie(UUID movieId) {
        return movies.findById(movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    public MovieResponse get(UUID movieId) {
        Cached cached = inTransaction(() -> cached(requireMovie(movieId)));
        if (!cached.stale()) {
            return cached.response();
        }
        return fetchAndStore(cached.response().tmdbId(), cached.response().mediaType(), cached.response());
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> popular() {
        return movies.findTop20ByOrderByPopularityDesc().stream().map(MovieResponse::from).toList();
    }

    public MovieResponse importTmdb(Long tmdbId, MediaType mediaType) {
        Cached cached = inTransaction(() ->
                movies.findByTmdbIdAndMediaType(tmdbId, mediaType).map(this::cached).orElse(null));
        if (cached != null && !cached.stale()) {
            return cached.response();
        }
        return fetchAndStore(tmdbId, mediaType, cached == null ? null : cached.response());
    }

    /**
     * Calls TMDB with no transaction open, then saves the result. If TMDB fails and we
     * already have a stored copy, that copy is returned instead of an error.
     */
    private MovieResponse fetchAndStore(Long tmdbId, MediaType mediaType, MovieResponse stored) {
        TmdbMovieDetails details;
        try {
            details = loadDetails(tmdbId, mediaType);
        } catch (ApiException ex) {
            if (stored == null) {
                throw ex;
            }
            log.warn("Could not refresh TMDB title {}: {}", stored.id(), ex.getMessage());
            return stored;
        }
        return inTransaction(() -> {
            Movie movie = movies.findByTmdbIdAndMediaType(tmdbId, mediaType).orElse(null);
            if (movie == null) {
                movie = Movie.fromTmdb(details.id(), mediaType, details.displayTitle(mediaType),
                        details.displayOriginalTitle(mediaType),
                        details.overview(), details.parsedReleaseDate(mediaType), details.runtimeMinutes(mediaType),
                        details.poster_path(), details.backdrop_path(), details.popularity(),
                        details.vote_average());
                for (Genre genre : resolveGenres(details)) {
                    movie.addGenre(genre);
                }
                movie = movies.save(movie);
            } else {
                applyDetails(movie, details);
            }
            return MovieResponse.from(movie);
        });
    }

    /** Used by the nightly cache maintenance job, which manages its own transaction. */
    void refreshTmdbMovie(Movie movie) {
        if (movie.getTmdbId() == null) {
            return;
        }
        applyDetails(movie, loadDetails(movie.getTmdbId(), movie.getMediaType()));
    }

    private void applyDetails(Movie movie, TmdbMovieDetails details) {
        MediaType mediaType = movie.getMediaType();
        movie.refreshFromTmdb(details.displayTitle(mediaType), details.displayOriginalTitle(mediaType), details.overview(),
                details.parsedReleaseDate(mediaType), details.runtimeMinutes(mediaType), details.poster_path(),
                details.backdrop_path(), details.popularity(), details.vote_average(), resolveGenres(details));
    }

    private Cached cached(Movie movie) {
        Instant cutoff = Instant.now().minus(cacheRefreshDays, ChronoUnit.DAYS);
        return new Cached(MovieResponse.from(movie), movie.needsTmdbRefresh(cutoff));
    }

    private <T> T inTransaction(Supplier<T> work) {
        return transaction.execute(status -> work.get());
    }

    private TmdbMovieDetails loadDetails(Long tmdbId, MediaType mediaType) {
        try {
            return tmdbClient.details(tmdbId, mediaType)
                    .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                            "TMDB is not configured. Add TMDB_API_KEY and retry."));
        } catch (TmdbApiException ex) {
            String retry = ex.getRetryAfterSeconds() == null ? "" : " Retry after " + ex.getRetryAfterSeconds() + " seconds.";
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage() + retry);
        }
    }

    private Set<Genre> resolveGenres(TmdbMovieDetails details) {
        Set<Genre> resolved = new HashSet<>();
        if (details.genres() == null) {
            return resolved;
        }
        for (TmdbClient.TmdbGenre tmdbGenre : details.genres()) {
            resolved.add(genres.findById(tmdbGenre.id())
                    .orElseGet(() -> genres.save(new Genre(tmdbGenre.id(), tmdbGenre.name()))));
        }
        return resolved;
    }

    @Transactional
    public Movie createManual(String title, Integer runtimeMinutes, String overview) {
        return movies.save(new Movie(title, runtimeMinutes, overview));
    }
}
