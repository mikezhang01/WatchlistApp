package com.cinestack;

import com.cinestack.movies.MediaType;
import com.cinestack.movies.TmdbClient;
import com.cinestack.movies.TmdbClient.TmdbGenre;
import com.cinestack.movies.TmdbClient.TmdbMovieDetails;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for tests that exercise the real HTTP API against a real PostgreSQL.
 *
 * The container is started once and shared by every test class, and Flyway runs the
 * actual migrations against it, so schema and mapping mistakes fail here instead of in
 * production. Tests never clean the database; each one registers its own users with
 * unique emails so they cannot see each other's data.
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class IntegrationTest {
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Every test registers users from the same address, so lift the sign-up limit.
        registry.add("app.rate-limit.auth-per-minute", () -> "100000");
    }

    private static final AtomicLong NEXT_TMDB_ID = new AtomicLong(1_000_000);

    /** TMDB is always replaced by a mock so tests never call the real service. */
    @MockBean
    protected TmdbClient tmdb;

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper json;

    public record TestUser(UUID id, String email, String password, String token) {
    }

    protected TestUser register() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@example.com";
        String password = "correct-horse-battery";
        JsonNode body = read(mvc.perform(send(post("/api/auth/register"), null,
                        Map.of("email", email, "password", password, "displayName", "Test User")))
                .andExpect(status().isOk()));
        return new TestUser(UUID.fromString(body.get("user").get("id").asText()), email, password,
                body.get("token").asText());
    }

    /** Adds the bearer token (when a user is given) and a JSON body (when one is given). */
    protected MockHttpServletRequestBuilder send(MockHttpServletRequestBuilder request, TestUser user, Object body)
            throws Exception {
        if (user != null) {
            request.header("Authorization", "Bearer " + user.token());
        }
        if (body != null) {
            request.contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .content(json.writeValueAsString(body));
        }
        return request;
    }

    protected MockHttpServletRequestBuilder as(TestUser user, MockHttpServletRequestBuilder request) throws Exception {
        return send(request, user, null);
    }

    protected JsonNode read(ResultActions actions) throws Exception {
        MvcResult result = actions.andReturn();
        return json.readTree(result.getResponse().getContentAsString());
    }

    protected UUID createWatchlist(TestUser owner, String name, String visibility) throws Exception {
        JsonNode body = read(mvc.perform(send(post("/api/watchlists"), owner,
                        Map.of("name", name, "description", "", "visibility", visibility)))
                .andExpect(status().isOk()));
        return UUID.fromString(body.get("id").asText());
    }

    protected long nextTmdbId() {
        return NEXT_TMDB_ID.incrementAndGet();
    }

    /** Makes the TMDB mock return a movie for this id, the way the real API would. */
    protected void stubTmdbMovie(long tmdbId, String title) {
        when(tmdb.details(tmdbId, MediaType.MOVIE)).thenReturn(Optional.of(new TmdbMovieDetails(
                tmdbId, title, null, title, null, "Overview of " + title, "1995-12-15", null, 170, null,
                "/poster.jpg", "/backdrop.jpg", BigDecimal.valueOf(42.5), BigDecimal.valueOf(8.3),
                List.of(new TmdbGenre(80L, "Crime"), new TmdbGenre(18L, "Drama")))));
    }

    /** Makes the TMDB mock return a TV show, which uses different field names than a movie. */
    protected void stubTmdbShow(long tmdbId, String name) {
        when(tmdb.details(tmdbId, MediaType.TV)).thenReturn(Optional.of(new TmdbMovieDetails(
                tmdbId, null, name, null, name, "Overview of " + name, null, "2008-01-20", null, List.of(47),
                "/poster.jpg", "/backdrop.jpg", BigDecimal.valueOf(99.1), BigDecimal.valueOf(8.9),
                List.of(new TmdbGenre(18L, "Drama")))));
    }

    /** Imports a stubbed movie through the API and returns its CineStack id. */
    protected UUID importMovie(TestUser user, String title) throws Exception {
        long tmdbId = nextTmdbId();
        stubTmdbMovie(tmdbId, title);
        JsonNode body = read(mvc.perform(as(user, post("/api/movies/import/tmdb/" + tmdbId)))
                .andExpect(status().isOk()));
        return UUID.fromString(body.get("id").asText());
    }

    /** Adds a title to a list and returns the new item's id. */
    protected UUID addItem(TestUser user, UUID watchlistId, UUID movieId) throws Exception {
        JsonNode body = read(mvc.perform(send(post("/api/watchlists/" + watchlistId + "/items"), user,
                        Map.of("movieId", movieId, "status", "PLANNED")))
                .andExpect(status().isOk()));
        return UUID.fromString(body.get("id").asText());
    }
}
