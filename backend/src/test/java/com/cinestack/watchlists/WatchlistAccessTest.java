package com.cinestack.watchlists;

import com.cinestack.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Who is allowed to see and change what. */
class WatchlistAccessTest extends IntegrationTest {
    @Test
    void privateListIsVisibleOnlyToItsOwner() throws Exception {
        TestUser owner = register();
        TestUser stranger = register();
        UUID listId = createWatchlist(owner, "Secret", "PRIVATE");

        mvc.perform(as(owner, get("/api/watchlists/" + listId))).andExpect(status().isOk());
        mvc.perform(as(stranger, get("/api/watchlists/" + listId))).andExpect(status().isForbidden());
        mvc.perform(get("/api/watchlists/" + listId)).andExpect(status().isForbidden());

        mvc.perform(get("/api/users/" + owner.id() + "/public-watchlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void publicListIsReadableByAnyoneIncludingVisitors() throws Exception {
        TestUser owner = register();
        TestUser stranger = register();
        UUID listId = createWatchlist(owner, "Shared", "PUBLIC");
        addItem(owner, listId, importMovie(owner, "Manhunter"));

        mvc.perform(get("/api/watchlists/" + listId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)));
        mvc.perform(as(stranger, get("/api/watchlists/" + listId))).andExpect(status().isOk());

        mvc.perform(get("/api/users/" + owner.id() + "/public-watchlists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void strangerCannotChangeSomeoneElsesList() throws Exception {
        TestUser owner = register();
        TestUser stranger = register();
        UUID listId = createWatchlist(owner, "Mine", "PUBLIC");
        UUID movieId = importMovie(owner, "The Insider");
        UUID itemId = addItem(owner, listId, movieId);
        UUID otherMovieId = importMovie(stranger, "Ali");

        mvc.perform(send(patch("/api/watchlists/" + listId), stranger,
                        Map.of("name", "Hijacked", "description", "", "visibility", "PUBLIC")))
                .andExpect(status().isForbidden());
        mvc.perform(send(post("/api/watchlists/" + listId + "/items"), stranger,
                        Map.of("movieId", otherMovieId, "status", "PLANNED")))
                .andExpect(status().isForbidden());
        mvc.perform(send(patch("/api/watchlists/" + listId + "/items/" + itemId), stranger,
                        Map.of("status", "DROPPED")))
                .andExpect(status().isForbidden());
        mvc.perform(as(stranger, delete("/api/watchlists/" + listId + "/items/" + itemId)))
                .andExpect(status().isForbidden());
        mvc.perform(as(stranger, delete("/api/watchlists/" + listId))).andExpect(status().isForbidden());

        mvc.perform(as(owner, get("/api/watchlists/" + listId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mine"))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    /**
     * Item ids are visible on public lists. Owning some list must not be enough to
     * change an item that lives in a different list.
     */
    @Test
    void itemCannotBeChangedThroughADifferentList() throws Exception {
        TestUser owner = register();
        TestUser attacker = register();
        UUID ownersList = createWatchlist(owner, "Target", "PUBLIC");
        UUID ownersItem = addItem(owner, ownersList, importMovie(owner, "Miami Vice"));
        UUID attackersList = createWatchlist(attacker, "Decoy", "PRIVATE");

        mvc.perform(as(attacker, delete("/api/watchlists/" + attackersList + "/items/" + ownersItem)))
                .andExpect(status().isNotFound());
        mvc.perform(send(patch("/api/watchlists/" + attackersList + "/items/" + ownersItem), attacker,
                        Map.of("status", "DROPPED")))
                .andExpect(status().isNotFound());

        mvc.perform(as(owner, get("/api/watchlists/" + ownersList)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].status").value("PLANNED"));
    }

    @Test
    void publicProfileDoesNotExposeEmail() throws Exception {
        TestUser user = register();

        mvc.perform(get("/api/users/" + user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Test User"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }
}
