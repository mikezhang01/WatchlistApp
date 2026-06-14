package com.cinestack.watchlists;

import com.cinestack.common.Visibility;
import com.cinestack.movies.MovieDtos.MovieResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class WatchlistDtos {
    private WatchlistDtos() {
    }

    public record WatchlistRequest(
            @NotBlank @Size(max = 100) String name,
            String description,
            @NotNull Visibility visibility
    ) {
    }

    public record AddItemRequest(@NotNull UUID movieId, WatchStatus status) {
    }

    public record UpdateItemRequest(
            @NotNull WatchStatus status,
            @Min(1) @Max(5) Integer personalRating,
            String notes
    ) {
    }

    public record WatchlistItemResponse(
            UUID id,
            MovieResponse movie,
            WatchStatus status,
            Instant watchedAt,
            Integer personalRating,
            String notes
    ) {
        public static WatchlistItemResponse from(WatchlistItem item) {
            return new WatchlistItemResponse(
                    item.getId(),
                    MovieResponse.from(item.getMovie()),
                    item.getStatus(),
                    item.getWatchedAt(),
                    item.getPersonalRating(),
                    item.getNotes()
            );
        }
    }

    public record WatchlistResponse(
            UUID id,
            UUID ownerId,
            String ownerDisplayName,
            String name,
            String description,
            Visibility visibility,
            List<WatchlistItemResponse> items
    ) {
        public static WatchlistResponse from(Watchlist watchlist) {
            return new WatchlistResponse(
                    watchlist.getId(),
                    watchlist.getOwner().getId(),
                    watchlist.getOwner().getDisplayName(),
                    watchlist.getName(),
                    watchlist.getDescription(),
                    watchlist.getVisibility(),
                    watchlist.getItems().stream().map(WatchlistItemResponse::from).toList()
            );
        }
    }
}

