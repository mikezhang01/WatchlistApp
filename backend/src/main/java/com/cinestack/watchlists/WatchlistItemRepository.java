package com.cinestack.watchlists;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, UUID> {
    boolean existsByWatchlist_IdAndMovie_Id(UUID watchlistId, UUID movieId);

    List<WatchlistItem> findByWatchlist_Owner_Id(UUID ownerId);

    java.util.Optional<WatchlistItem> findByIdAndWatchlist_Id(UUID itemId, UUID watchlistId);
}
