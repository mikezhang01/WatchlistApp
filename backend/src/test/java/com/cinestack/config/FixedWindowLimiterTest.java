package com.cinestack.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedWindowLimiterTest {
    @Test
    void allowsUpToTheLimitThenBlocksUntilTheWindowPasses() {
        FixedWindowLimiter limiter = new FixedWindowLimiter();

        assertTrue(limiter.tryAcquire("auth:1.2.3.4", 2, 0));
        assertTrue(limiter.tryAcquire("auth:1.2.3.4", 2, 1_000));
        assertFalse(limiter.tryAcquire("auth:1.2.3.4", 2, 2_000));

        assertTrue(limiter.tryAcquire("auth:1.2.3.4", 2, 61_000));
    }

    @Test
    void countsEachKeySeparately() {
        FixedWindowLimiter limiter = new FixedWindowLimiter();

        assertTrue(limiter.tryAcquire("auth:1.2.3.4", 1, 0));
        assertFalse(limiter.tryAcquire("auth:1.2.3.4", 1, 10));
        assertTrue(limiter.tryAcquire("auth:5.6.7.8", 1, 10));
        assertTrue(limiter.tryAcquire("search:1.2.3.4", 1, 10));
    }
}
