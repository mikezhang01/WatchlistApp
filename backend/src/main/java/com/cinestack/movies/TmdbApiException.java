package com.cinestack.movies;

public class TmdbApiException extends RuntimeException {
    private final Integer retryAfterSeconds;

    public TmdbApiException(String message, Integer retryAfterSeconds, Throwable cause) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public Integer getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

