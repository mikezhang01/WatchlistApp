package com.cinestack.movies;

import com.cinestack.IntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MovieImportTest extends IntegrationTest {
    @Test
    void importsAMovieWithItsGenres() throws Exception {
        TestUser user = register();
        long tmdbId = nextTmdbId();
        stubTmdbMovie(tmdbId, "Heat");

        mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tmdbId").value(tmdbId))
                .andExpect(jsonPath("$.mediaType").value("MOVIE"))
                .andExpect(jsonPath("$.title").value("Heat"))
                .andExpect(jsonPath("$.releaseDate").value("1995-12-15"))
                .andExpect(jsonPath("$.runtimeMinutes").value(170))
                .andExpect(jsonPath("$.posterPath").value("/poster.jpg"))
                .andExpect(jsonPath("$.backdropPath").value("/backdrop.jpg"))
                .andExpect(jsonPath("$.genres", containsInAnyOrder("Crime", "Drama")));
    }

    @Test
    void importsATvShowFromItsOwnFieldNames() throws Exception {
        TestUser user = register();
        long tmdbId = nextTmdbId();
        stubTmdbShow(tmdbId, "Breaking Bad");

        mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId + "?mediaType=TV")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mediaType").value("TV"))
                .andExpect(jsonPath("$.title").value("Breaking Bad"))
                .andExpect(jsonPath("$.releaseDate").value("2008-01-20"))
                .andExpect(jsonPath("$.runtimeMinutes").value(47));
    }

    @Test
    void importingTheSameTitleTwiceReusesTheRow() throws Exception {
        TestUser user = register();
        long tmdbId = nextTmdbId();
        stubTmdbMovie(tmdbId, "Heat");

        JsonNode first = read(mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId))));
        JsonNode second = read(mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId))));

        assertThat(second.get("id").asText()).isEqualTo(first.get("id").asText());
    }

    /** TMDB numbers movies and shows separately, so one id can mean two different titles. */
    @Test
    void aMovieAndAShowCanShareATmdbId() throws Exception {
        TestUser user = register();
        long tmdbId = nextTmdbId();
        stubTmdbMovie(tmdbId, "Fargo");
        stubTmdbShow(tmdbId, "Fargo (series)");

        JsonNode movie = read(mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId)))
                .andExpect(status().isOk()));
        JsonNode show = read(mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId + "?mediaType=TV")))
                .andExpect(status().isOk()));

        assertThat(show.get("id").asText()).isNotEqualTo(movie.get("id").asText());
    }

    @Test
    void importNeedsLoginAndReportsWhenTmdbIsUnavailable() throws Exception {
        TestUser user = register();
        long tmdbId = nextTmdbId();

        mvc.perform(post("/api/movies/import/tmdb/" + tmdbId)).andExpect(status().isUnauthorized());
        // No stub: the mock answers like a TMDB client with no API key configured.
        mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId)))
                .andExpect(status().isServiceUnavailable());
    }
}
