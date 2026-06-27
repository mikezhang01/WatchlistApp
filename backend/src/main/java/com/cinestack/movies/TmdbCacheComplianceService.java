package com.cinestack.movies;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class TmdbCacheComplianceService {
    private static final Logger log = LoggerFactory.getLogger(TmdbCacheComplianceService.class);

    private final MovieRepository movies;
    private final GenreRepository genres;
    private final MovieService movieService;
    private final TmdbClient tmdbClient;
    private final long refreshDays;
    private final long maximumAgeDays;
    private final boolean purgeOnStartup;

    public TmdbCacheComplianceService(
            MovieRepository movies,
            GenreRepository genres,
            MovieService movieService,
            TmdbClient tmdbClient,
            @Value("${app.tmdb.cache-refresh-days:30}") long refreshDays,
            @Value("${app.tmdb.cache-max-age-days:175}") long maximumAgeDays,
            @Value("${app.tmdb.purge-on-startup:false}") boolean purgeOnStartup
    ) {
        this.movies = movies;
        this.genres = genres;
        this.movieService = movieService;
        this.tmdbClient = tmdbClient;
        if (maximumAgeDays <= 0 || maximumAgeDays > 175) {
            throw new IllegalArgumentException("TMDB_CACHE_MAX_AGE_DAYS must be between 1 and 175");
        }
        if (refreshDays <= 0 || refreshDays >= maximumAgeDays) {
            throw new IllegalArgumentException("TMDB_CACHE_REFRESH_DAYS must be positive and below the maximum age");
        }
        this.refreshDays = refreshDays;
        this.maximumAgeDays = maximumAgeDays;
        this.purgeOnStartup = purgeOnStartup;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void handleStartup() {
        if (purgeOnStartup) {
            int purged = purgeAllTmdbContent();
            log.warn("TMDB_PURGE_ON_STARTUP removed cached TMDB content from {} movies", purged);
        }
    }

    @Scheduled(cron = "${app.tmdb.cache-maintenance-cron:0 15 3 * * *}")
    @Transactional
    public void maintainCache() {
        Instant refreshCutoff = Instant.now().minus(refreshDays, ChronoUnit.DAYS);
        if (tmdbClient.isConfigured()) {
            List<Movie> candidates = movies
                    .findTop20ByTmdbIdIsNotNullAndTmdbSyncedAtBeforeOrderByTmdbSyncedAtAsc(refreshCutoff);
            for (Movie movie : candidates) {
                try {
                    movieService.refreshTmdbMovie(movie);
                } catch (RuntimeException ex) {
                    log.warn("Scheduled TMDB refresh failed for movie {}: {}", movie.getId(), ex.getMessage());
                }
            }
        }

        Instant expiryCutoff = Instant.now().minus(maximumAgeDays, ChronoUnit.DAYS);
        List<Movie> expired = movies.findByTmdbIdIsNotNullAndTmdbSyncedAtBefore(expiryCutoff);
        expired.forEach(Movie::purgeTmdbContent);
        movies.flush();
        genres.deleteOrphanedGenres();
        if (!expired.isEmpty()) {
            log.warn("Purged expired TMDB content from {} movies", expired.size());
        }
    }

    int purgeAllTmdbContent() {
        List<Movie> cached = movies.findByTmdbIdIsNotNull();
        cached.forEach(Movie::purgeTmdbContent);
        movies.flush();
        genres.deleteOrphanedGenres();
        return cached.size();
    }
}
