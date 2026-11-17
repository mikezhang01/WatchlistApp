package com.cinestack.watchlists;

import com.cinestack.common.ApiException;
import com.cinestack.common.Visibility;
import com.cinestack.movies.Movie;
import com.cinestack.movies.MovieService;
import com.cinestack.users.User;
import com.cinestack.watchlists.WatchlistDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Every method runs in a transaction. A list's items and a title's genres are loaded
 * lazily, which only works while the transaction (and its database session) is open,
 * so entities are turned into response DTOs here and never leave this class.
 * Reads use the class-level read-only transaction; methods that write override it.
 */
@Service
@Transactional(readOnly = true)
public class WatchlistService {
    private final WatchlistRepository watchlists;
    private final WatchlistItemRepository items;
    private final MovieService movieService;

    public WatchlistService(WatchlistRepository watchlists, WatchlistItemRepository items, MovieService movieService) {
        this.watchlists = watchlists;
        this.items = items;
        this.movieService = movieService;
    }

    public List<WatchlistResponse> mine(User user) {
        return watchlists.findByOwnerOrderByCreatedAtDesc(user).stream().map(WatchlistResponse::from).toList();
    }

    public List<WatchlistResponse> publicForUser(UUID userId) {
        return watchlists.findByOwner_IdAndVisibilityOrderByCreatedAtDesc(userId, Visibility.PUBLIC)
                .stream().map(WatchlistResponse::from).toList();
    }

    public WatchlistResponse get(UUID id, User viewer) {
        Watchlist watchlist = requireVisible(id, viewer);
        return WatchlistResponse.from(watchlist);
    }

    @Transactional
    public WatchlistResponse create(User user, WatchlistRequest request) {
        Watchlist watchlist = watchlists.save(new Watchlist(
                user,
                request.name(),
                request.description(),
                request.visibility()
        ));
        return WatchlistResponse.from(watchlist);
    }

    @Transactional
    public WatchlistResponse update(User user, UUID id, WatchlistRequest request) {
        Watchlist watchlist = requireOwned(id, user);
        watchlist.update(request.name(), request.description(), request.visibility());
        return WatchlistResponse.from(watchlist);
    }

    @Transactional
    public void delete(User user, UUID id) {
        Watchlist watchlist = requireOwned(id, user);
        watchlists.delete(watchlist);
    }

    @Transactional
    public WatchlistItemResponse addItem(User user, UUID watchlistId, AddItemRequest request) {
        Watchlist watchlist = requireOwned(watchlistId, user);
        Movie movie = movieService.requireMovie(request.movieId());
        if (items.existsByWatchlist_IdAndMovie_Id(watchlistId, movie.getId())) {
            throw new ApiException(HttpStatus.CONFLICT, "This title is already in this watchlist");
        }
        WatchlistItem item = watchlist.addItem(movie,
                request.status() == null ? WatchStatus.PLANNED : request.status());
        return WatchlistItemResponse.from(items.save(item));
    }

    @Transactional
    public WatchlistItemResponse updateItem(User user, UUID watchlistId, UUID itemId, UpdateItemRequest request) {
        requireOwned(watchlistId, user);
        WatchlistItem item = requireItem(watchlistId, itemId);
        item.update(request.status(), request.personalRating(), request.notes());
        return WatchlistItemResponse.from(item);
    }

    @Transactional
    public void deleteItem(User user, UUID watchlistId, UUID itemId) {
        Watchlist watchlist = requireOwned(watchlistId, user);
        watchlist.removeItem(requireItem(watchlistId, itemId));
    }

    private Watchlist requireVisible(UUID id, User viewer) {
        Watchlist watchlist = watchlists.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Watchlist not found"));
        if (watchlist.getVisibility() == Visibility.PUBLIC || (viewer != null && watchlist.isOwnedBy(viewer))) {
            return watchlist;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "This watchlist is private");
    }

    private Watchlist requireOwned(UUID id, User user) {
        Watchlist watchlist = watchlists.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Watchlist not found"));
        if (!watchlist.isOwnedBy(user)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not own this watchlist");
        }
        return watchlist;
    }

    /**
     * The item must belong to the list named in the URL. Looking it up by id alone would
     * let the owner of any list change items in someone else's.
     */
    private WatchlistItem requireItem(UUID watchlistId, UUID itemId) {
        return items.findByIdAndWatchlist_Id(itemId, watchlistId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Watchlist item not found"));
    }
}
