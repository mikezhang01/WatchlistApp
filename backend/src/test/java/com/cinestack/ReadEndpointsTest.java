package com.cinestack;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every endpoint that returns titles or lists has to load related data (a list's items,
 * a title's genres). These all failed with a 500 before reads were made transactional,
 * so this is one pass over them with real data in place.
 */
class ReadEndpointsTest extends IntegrationTest {
    @Test
    void reviewListsReturnTheReview() throws Exception {
        TestUser user = register();
        UUID movieId = importMovie(user, "Sicario");

        mvc.perform(send(post("/api/movies/" + movieId + "/reviews"), user,
                        Map.of("rating", 5, "body", "Tense", "visibility", "PUBLIC")))
                .andExpect(status().isOk());

        mvc.perform(get("/api/movies/" + movieId + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].movie.title").value("Sicario"));
        mvc.perform(as(user, get("/api/me/reviews")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(get("/api/users/" + user.id() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void recommendationsLeaveOutTitlesAlreadyOnMyLists() throws Exception {
        TestUser user = register();
        UUID listId = createWatchlist(user, "Seen", "PRIVATE");
        UUID mine = importMovie(user, "Arrival");
        addItem(user, listId, mine);

        mvc.perform(get("/api/movies/popular")).andExpect(status().isOk());
        mvc.perform(as(user, get("/api/recommendations")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(mine.toString()))));
    }

    @Test
    void legacyExportAndImportRoundTrip() throws Exception {
        TestUser user = register();
        UUID listId = createWatchlist(user, "Export me", "PRIVATE");
        addItem(user, listId, importMovie(user, "Prisoners"));

        mvc.perform(as(user, get("/api/me/export")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Movies", hasSize(1)))
                .andExpect(jsonPath("$.Movies[0].title").value("Prisoners"));

        Map<String, Object> legacy = Map.of("Movies", List.of(
                Map.of("title", "Alien", "year", 1979, "duration", 117, "description", "In space", "watched", true, "score", 5),
                Map.of("title", "Aliens", "year", 1986, "duration", 137, "description", "More of them", "watched", false, "score", 0)));
        mvc.perform(send(post("/api/me/import/legacy-watchlist"), user, legacy))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(2)));
    }

    @Test
    void badInputGetsAJsonErrorNotAServerError() throws Exception {
        TestUser user = register();

        mvc.perform(as(user, get("/api/watchlists/not-a-uuid")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mvc.perform(as(user, get("/api/watchlists/" + UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Watchlist not found"));
        mvc.perform(as(user, post("/api/watchlists"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{ this is not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
