package com.cinestack.watchlists;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * The JSON file format of the original desktop app, kept as-is (including the
 * capitalised "Movies" key) so old exports can still be imported.
 */
public final class LegacyWatchlistDtos {
    private LegacyWatchlistDtos() {
    }

    public record LegacyWatchlist(@NotNull List<@Valid LegacyMovie> Movies) {
    }

    public record LegacyMovie(
            @NotBlank String title,
            Integer year,
            Integer duration,
            String description,
            Boolean watched,
            Integer score
    ) {
    }
}
