package com.cinestack.users;

import com.cinestack.auth.CurrentUser;
import com.cinestack.watchlists.LegacyWatchlistDtos.LegacyWatchlist;
import com.cinestack.watchlists.LegacyWatchlistService;
import com.cinestack.watchlists.WatchlistDtos.WatchlistResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class MeController {
    private final CurrentUser currentUser;
    private final LegacyWatchlistService legacyWatchlistService;

    public MeController(CurrentUser currentUser, LegacyWatchlistService legacyWatchlistService) {
        this.currentUser = currentUser;
        this.legacyWatchlistService = legacyWatchlistService;
    }

    @GetMapping("/export")
    LegacyWatchlist exportLegacy(Authentication authentication) {
        return legacyWatchlistService.exportAll(currentUser.require(authentication));
    }

    @PostMapping("/import/legacy-watchlist")
    WatchlistResponse importLegacy(Authentication authentication,
                                   @Valid @RequestBody LegacyWatchlist legacyWatchlist) {
        return legacyWatchlistService.importAsNewList(currentUser.require(authentication), legacyWatchlist);
    }
}
