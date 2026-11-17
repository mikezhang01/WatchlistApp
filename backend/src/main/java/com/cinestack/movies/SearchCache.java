package com.cinestack.movies;

import com.cinestack.movies.MovieDtos.SearchPage;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers recent TMDB search results for a few minutes, so the same search by several
 * people (or one person paging back and forth) costs one TMDB request instead of many.
 * Kept in memory: it is lost on restart and not shared between servers, which is fine
 * for results that are only a convenience.
 */
class SearchCache {
    private static final int MAX_ENTRIES = 500;

    private record Entry(SearchPage page, long expiresAt) {
    }

    private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
    private final long ttlMs;

    SearchCache(long ttlSeconds) {
        this.ttlMs = ttlSeconds * 1000;
    }

    SearchPage get(String query, int page, long nowMs) {
        Entry entry = entries.get(key(query, page));
        return entry == null || entry.expiresAt() <= nowMs ? null : entry.page();
    }

    void put(String query, int page, SearchPage value, long nowMs) {
        if (ttlMs <= 0) {
            return;
        }
        if (entries.size() >= MAX_ENTRIES) {
            entries.entrySet().removeIf(entry -> entry.getValue().expiresAt() <= nowMs);
            if (entries.size() >= MAX_ENTRIES) {
                entries.clear();
            }
        }
        entries.put(key(query, page), new Entry(value, nowMs + ttlMs));
    }

    /** "Heat", " heat " and "HEAT" are the same search. */
    private static String key(String query, int page) {
        return query.trim().toLowerCase(Locale.ROOT) + "|" + page;
    }
}
