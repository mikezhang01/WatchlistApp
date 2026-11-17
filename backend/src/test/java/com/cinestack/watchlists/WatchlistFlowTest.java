package com.cinestack.watchlists;

import com.cinestack.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The core loop of the app: build a list, add titles, track them, clean up. */
class WatchlistFlowTest extends IntegrationTest {
    @Test
    void createListAddTitleTrackItThenRemoveEverything() throws Exception {
        TestUser user = register();
        UUID listId = createWatchlist(user, "Weekend", "PRIVATE");
        UUID movieId = importMovie(user, "Heat");

        UUID itemId = addItem(user, listId, movieId);

        // The list of my watchlists carries each list's titles, including genres.
        mvc.perform(as(user, get("/api/watchlists")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Weekend"))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[0].items[0].status").value("PLANNED"))
                .andExpect(jsonPath("$[0].items[0].movie.title").value("Heat"))
                .andExpect(jsonPath("$[0].items[0].movie.genres", containsInAnyOrder("Crime", "Drama")));

        mvc.perform(as(user, get("/api/watchlists/" + listId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(itemId.toString()));

        Map<String, Object> watched = new HashMap<>();
        watched.put("status", "WATCHED");
        watched.put("personalRating", 4);
        watched.put("notes", "Great heist");
        mvc.perform(send(patch("/api/watchlists/" + listId + "/items/" + itemId), user, watched))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WATCHED"))
                .andExpect(jsonPath("$.personalRating").value(4))
                .andExpect(jsonPath("$.watchedAt", notNullValue()));

        mvc.perform(as(user, delete("/api/watchlists/" + listId + "/items/" + itemId)))
                .andExpect(status().isOk());
        mvc.perform(as(user, get("/api/watchlists/" + listId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)));

        mvc.perform(as(user, delete("/api/watchlists/" + listId))).andExpect(status().isOk());
        mvc.perform(as(user, get("/api/watchlists/" + listId))).andExpect(status().isNotFound());
    }

    @Test
    void deletingAListRemovesItsItemsButKeepsTheTitle() throws Exception {
        TestUser user = register();
        UUID listId = createWatchlist(user, "Temporary", "PRIVATE");
        UUID movieId = importMovie(user, "Ronin");
        addItem(user, listId, movieId);

        mvc.perform(as(user, delete("/api/watchlists/" + listId))).andExpect(status().isOk());

        mvc.perform(get("/api/movies/" + movieId)).andExpect(status().isOk());
    }

    @Test
    void sameTitleCannotBeAddedToAListTwice() throws Exception {
        TestUser user = register();
        UUID listId = createWatchlist(user, "Once", "PRIVATE");
        UUID movieId = importMovie(user, "Collateral");
        addItem(user, listId, movieId);

        mvc.perform(send(post("/api/watchlists/" + listId + "/items"), user,
                        Map.of("movieId", movieId, "status", "PLANNED")))
                .andExpect(status().isConflict());
    }

    @Test
    void ratingOutsideOneToFiveIsRejected() throws Exception {
        TestUser user = register();
        UUID listId = createWatchlist(user, "Ratings", "PRIVATE");
        UUID itemId = addItem(user, listId, importMovie(user, "Thief"));

        mvc.perform(send(patch("/api/watchlists/" + listId + "/items/" + itemId), user,
                        Map.of("status", "WATCHED", "personalRating", 9)))
                .andExpect(status().isBadRequest());
    }
}
