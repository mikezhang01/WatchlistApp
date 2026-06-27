package com.cinestack.movies;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TmdbRequestGate {
    private final long minimumIntervalMs;
    private final long maximumWaitMs;
    private long nextRequestAtMs;

    public TmdbRequestGate(
            @Value("${app.tmdb.request-min-interval-ms:250}") long minimumIntervalMs,
            @Value("${app.tmdb.request-max-wait-ms:2000}") long maximumWaitMs
    ) {
        this.minimumIntervalMs = minimumIntervalMs;
        this.maximumWaitMs = maximumWaitMs;
    }

    public void acquire() {
        long waitMs;
        synchronized (this) {
            long now = System.currentTimeMillis();
            long requestAt = Math.max(now, nextRequestAtMs);
            waitMs = requestAt - now;
            if (waitMs > maximumWaitMs) {
                throw new TmdbApiException("TMDB request queue is busy. Retry shortly.", 2, null);
            }
            nextRequestAtMs = requestAt + minimumIntervalMs;
        }

        if (waitMs == 0) {
            return;
        }
        try {
            Thread.sleep(waitMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new TmdbApiException("TMDB request was interrupted.", 1, ex);
        }
    }
}

