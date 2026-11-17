package com.cinestack.movies;

import com.cinestack.movies.MovieDtos.SearchPage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class SearchCacheTest {
    private final SearchPage page = new SearchPage(List.of(), 1, 1);

    @Test
    void returnsAStoredPageUntilItExpires() {
        SearchCache cache = new SearchCache(300);
        cache.put("heat", 1, page, 0);

        assertSame(page, cache.get("heat", 1, 299_000));
        assertNull(cache.get("heat", 1, 300_000));
    }

    @Test
    void ignoresCaseAndSurroundingSpacesButNotThePageNumber() {
        SearchCache cache = new SearchCache(300);
        cache.put("Heat", 1, page, 0);

        assertSame(page, cache.get("  heat ", 1, 1_000));
        assertNull(cache.get("heat", 2, 1_000));
        assertNull(cache.get("heat 2", 1, 1_000));
    }
}
