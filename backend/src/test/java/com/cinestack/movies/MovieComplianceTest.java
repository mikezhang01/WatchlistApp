package com.cinestack.movies;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MovieComplianceTest {
    @Test
    void importedMovieTracksSyncTimeAndRefreshEligibility() {
        Movie movie = Movie.fromTmdb(
                42L,
                "Example",
                "Example Original",
                "Overview",
                LocalDate.of(2024, 1, 1),
                120,
                "/poster.jpg",
                "/backdrop.jpg",
                BigDecimal.TEN,
                BigDecimal.valueOf(7.5)
        );

        assertNotNull(movie.getTmdbSyncedAt());
        assertFalse(movie.needsTmdbRefresh(Instant.now().minusSeconds(60)));
        assertTrue(movie.needsTmdbRefresh(Instant.now().plusSeconds(60)));
    }

    @Test
    void purgeRemovesTmdbContentButKeepsMovieRecord() {
        Movie movie = Movie.fromTmdb(
                42L,
                "Example",
                "Example Original",
                "Overview",
                LocalDate.of(2024, 1, 1),
                120,
                "/poster.jpg",
                "/backdrop.jpg",
                BigDecimal.TEN,
                BigDecimal.valueOf(7.5)
        );
        movie.addGenre(new Genre(1L, "Drama"));

        movie.purgeTmdbContent();

        assertNull(movie.getTmdbId());
        assertEquals("Unavailable movie", movie.getTitle());
        assertNull(movie.getOverview());
        assertNull(movie.getPosterPath());
        assertNull(movie.getBackdropPath());
        assertNull(movie.getTmdbSyncedAt());
        assertEquals(Set.of(), movie.getGenres());
    }
}

