package com.cinestack.watchlists;

import com.cinestack.auth.CurrentUser;
import com.cinestack.users.User;
import com.cinestack.watchlists.WatchlistDtos.*;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/watchlists")
public class WatchlistController {
    private final WatchlistService watchlistService;
    private final CurrentUser currentUser;

    public WatchlistController(WatchlistService watchlistService, CurrentUser currentUser) {
        this.watchlistService = watchlistService;
        this.currentUser = currentUser;
    }

    @GetMapping
    List<WatchlistResponse> mine(Authentication authentication) {
        return watchlistService.mine(currentUser.require(authentication));
    }

    @PostMapping
    WatchlistResponse create(Authentication authentication, @Valid @RequestBody WatchlistRequest request) {
        return watchlistService.create(currentUser.require(authentication), request);
    }

    @GetMapping("/{watchlistId}")
    WatchlistResponse get(Authentication authentication, @PathVariable UUID watchlistId) {
        User viewer = authentication == null ? null : currentUser.require(authentication);
        return watchlistService.get(watchlistId, viewer);
    }

    @PatchMapping("/{watchlistId}")
    WatchlistResponse update(Authentication authentication, @PathVariable UUID watchlistId,
                             @Valid @RequestBody WatchlistRequest request) {
        return watchlistService.update(currentUser.require(authentication), watchlistId, request);
    }

    @DeleteMapping("/{watchlistId}")
    void delete(Authentication authentication, @PathVariable UUID watchlistId) {
        watchlistService.delete(currentUser.require(authentication), watchlistId);
    }

    @PostMapping("/{watchlistId}/items")
    WatchlistItemResponse addItem(Authentication authentication, @PathVariable UUID watchlistId,
                                  @Valid @RequestBody AddItemRequest request) {
        return watchlistService.addItem(currentUser.require(authentication), watchlistId, request);
    }

    @PatchMapping("/{watchlistId}/items/{itemId}")
    WatchlistItemResponse updateItem(Authentication authentication, @PathVariable UUID watchlistId,
                                     @PathVariable UUID itemId, @Valid @RequestBody UpdateItemRequest request) {
        return watchlistService.updateItem(currentUser.require(authentication), watchlistId, itemId, request);
    }

    @DeleteMapping("/{watchlistId}/items/{itemId}")
    void deleteItem(Authentication authentication, @PathVariable UUID watchlistId, @PathVariable UUID itemId) {
        watchlistService.deleteItem(currentUser.require(authentication), watchlistId, itemId);
    }
}

