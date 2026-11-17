package com.cinestack.config;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Counts requests per key in one-minute windows. Kept in memory, which is enough for a
 * single server; several servers would need a shared store such as Redis.
 */
public class FixedWindowLimiter {
    private static final long WINDOW_MS = 60_000;
    private static final int CLEANUP_THRESHOLD = 10_000;

    private record Window(long startedAt, int count) {
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /** Returns true if this request is within the limit, and counts it. */
    public boolean tryAcquire(String key, int limitPerMinute, long nowMs) {
        if (windows.size() > CLEANUP_THRESHOLD) {
            windows.entrySet().removeIf(entry -> nowMs - entry.getValue().startedAt() >= WINDOW_MS);
        }
        Window updated = windows.compute(key, (ignored, current) ->
                current == null || nowMs - current.startedAt() >= WINDOW_MS
                        ? new Window(nowMs, 1)
                        : new Window(current.startedAt(), current.count() + 1));
        return updated.count() <= limitPerMinute;
    }
}
