package com.cinestack.movies;

import com.cinestack.common.ApiException;
import com.cinestack.movies.MovieDtos.MovieResponse;
import com.cinestack.movies.MovieDtos.MovieSearchResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class MovieService {
    private static final Logger log = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movies;
    private final GenreRepository genres;
    private final TmdbClient tmdbClient;
    private final long cacheRefreshDays;

    public MovieService(MovieRepository movies, GenreRepository genres, TmdbClient tmdbClient,
                        @Value("${app.tmdb.cache-refresh-days:30}") long cacheRefreshDays) {
        this.movies = movies;
        this.genres = genres;
        this.tmdbClient = tmdbClient;
        this.cacheRefreshDays = cacheRefreshDays;
    }

    public List<MovieSearchResult> search(String query, int page) {
        try {
            List<MovieSearchResult> external = tmdbClient.search(query, page);
            if (!external.isEmpty()) {
                return external;
            }
        } catch (TmdbApiException ex) {
            log.warn("TMDB search unavailable; using local cache: {}", ex.getMessage());
        }
        return movies.findTop20ByTitleContainingIgnoreCaseOrderByPopularityDesc(query).stream()
                .map(movie -> new MovieSearchResult(
                        movie.getTmdbId(),
                        movie.getTitle(),
                        movie.getOverview(),
                        movie.getReleaseDate(),
                        movie.getPosterPath()
                ))
                .toList();
    }

    public Movie requireMovie(UUID movieId) {
        return movies.findById(movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    @Transactional
    public MovieResponse get(UUID movieId) {
        Movie movie = requireMovie(movieId);
        refreshIfStale(movie);
        return MovieResponse.from(movie);
    }

    public List<MovieResponse> popular() {
        return movies.findTop20ByOrderByPopularityDesc().stream().map(MovieResponse::from).toList();
    }

    @Transactional
    public MovieResponse importTmdb(Long tmdbId) {
        Movie cached = movies.findByTmdbId(tmdbId).orElse(null);
        if (cached != null) {
            refreshIfStale(cached);
            return MovieResponse.from(cached);
        }

        TmdbClient.TmdbMovieDetails details = loadDetails(tmdbId);
        Movie movie = Movie.fromTmdb(details.id(), details.title(), details.original_title(),
                details.overview(), details.parsedReleaseDate(), details.runtime(),
                details.poster_path(), details.backdrop_path(), details.popularity(),
                details.vote_average());
        for (Genre genre : resolveGenres(details)) {
            movie.addGenre(genre);
        }
        return MovieResponse.from(movies.save(movie));
    }

    void refreshTmdbMovie(Movie movie) {
        if (movie.getTmdbId() == null) {
            return;
        }
        TmdbClient.TmdbMovieDetails details = loadDetails(movie.getTmdbId());
        movie.refreshFromTmdb(details.title(), details.original_title(), details.overview(),
                details.parsedReleaseDate(), details.runtime(), details.poster_path(),
                details.backdrop_path(), details.popularity(), details.vote_average(), resolveGenres(details));
    }

    private void refreshIfStale(Movie movie) {
        Instant cutoff = Instant.now().minus(cacheRefreshDays, ChronoUnit.DAYS);
        if (!movie.needsTmdbRefresh(cutoff)) {
            return;
        }
        try {
            refreshTmdbMovie(movie);
        } catch (ApiException ex) {
            log.warn("Could not refresh TMDB movie {}: {}", movie.getId(), ex.getMessage());
        }
    }

    private TmdbClient.TmdbMovieDetails loadDetails(Long tmdbId) {
        try {
            return tmdbClient.details(tmdbId)
                    .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                            "TMDB is not configured. Add TMDB_API_KEY and retry."));
        } catch (TmdbApiException ex) {
            String retry = ex.getRetryAfterSeconds() == null ? "" : " Retry after " + ex.getRetryAfterSeconds() + " seconds.";
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage() + retry);
        }
    }

    private java.util.Set<Genre> resolveGenres(TmdbClient.TmdbMovieDetails details) {
        java.util.Set<Genre> resolved = new HashSet<>();
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
