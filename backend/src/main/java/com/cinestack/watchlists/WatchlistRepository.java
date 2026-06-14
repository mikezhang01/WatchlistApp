package com.cinestack.watchlists;

import com.cinestack.common.Visibility;
import com.cinestack.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WatchlistRepository extends JpaRepository<Watchlist, UUID> {
    List<Watchlist> findByOwnerOrderByCreatedAtDesc(User owner);

    List<Watchlist> findByOwner_IdAndVisibilityOrderByCreatedAtDesc(UUID ownerId, Visibility visibility);
}
