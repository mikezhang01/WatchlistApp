package com.cinestack.watchlists;

import com.cinestack.movies.Movie;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "watchlist_items")
public class WatchlistItem {
    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "watchlist_id")
    private Watchlist watchlist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WatchStatus status = WatchStatus.PLANNED;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    @Column(name = "watched_at")
    private Instant watchedAt;

    @Column(name = "personal_rating")
    private Integer personalRating;

    @Column(columnDefinition = "TEXT")
    private String notes;

    protected WatchlistItem() {
    }

    public WatchlistItem(Watchlist watchlist, Movie movie, WatchStatus status) {
        this.id = UUID.randomUUID();
        this.watchlist = watchlist;
        this.movie = movie;
        this.status = status;
        this.addedAt = Instant.now();
        this.watchedAt = status == WatchStatus.WATCHED ? this.addedAt : null;
    }

    public void update(WatchStatus status, Integer personalRating, String notes) {
        this.status = status;
        this.personalRating = personalRating;
        this.notes = notes;
        this.watchedAt = status == WatchStatus.WATCHED && watchedAt == null ? Instant.now() : watchedAt;
    }

    public UUID getId() {
        return id;
    }

    public Movie getMovie() {
        return movie;
    }

    public WatchStatus getStatus() {
        return status;
    }

    public Instant getWatchedAt() {
        return watchedAt;
    }

    public Integer getPersonalRating() {
        return personalRating;
    }

    public String getNotes() {
        return notes;
    }
}

