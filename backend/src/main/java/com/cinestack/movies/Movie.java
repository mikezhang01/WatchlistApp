package com.cinestack.movies;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(
        name = "movies",
        uniqueConstraints = @UniqueConstraint(name = "uq_movies_tmdb_media_type", columnNames = {"tmdb_id", "media_type"})
)
public class Movie {
    @Id
    private UUID id;

    @Column(name = "tmdb_id")
    private Long tmdbId;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false)
    private MediaType mediaType = MediaType.MOVIE;

    @Column(nullable = false)
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "backdrop_path")
    private String backdropPath;

    @Column(precision = 10, scale = 3)
    private BigDecimal popularity;

    @Column(name = "vote_average", precision = 4, scale = 2)
    private BigDecimal voteAverage;

    @ManyToMany
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "tmdb_synced_at")
    private Instant tmdbSyncedAt;

    protected Movie() {
    }

    public Movie(String title, Integer runtimeMinutes, String overview) {
        this.id = UUID.randomUUID();
        this.mediaType = MediaType.MOVIE;
        this.title = title;
        this.runtimeMinutes = runtimeMinutes;
        this.overview = overview;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public static Movie fromTmdb(Long tmdbId, MediaType mediaType, String title, String originalTitle, String overview,
                                 LocalDate releaseDate, Integer runtimeMinutes, String posterPath,
                                 String backdropPath, BigDecimal popularity, BigDecimal voteAverage) {
        Movie movie = new Movie(title, runtimeMinutes, overview);
        movie.tmdbId = tmdbId;
        movie.mediaType = mediaType;
        movie.originalTitle = originalTitle;
        movie.releaseDate = releaseDate;
        movie.posterPath = posterPath;
        movie.backdropPath = backdropPath;
        movie.popularity = popularity;
        movie.voteAverage = voteAverage;
        movie.tmdbSyncedAt = Instant.now();
        return movie;
    }

    public void refreshFromTmdb(String title, String originalTitle, String overview,
                                LocalDate releaseDate, Integer runtimeMinutes, String posterPath,
                                String backdropPath, BigDecimal popularity, BigDecimal voteAverage,
                                Set<Genre> refreshedGenres) {
        this.title = title;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.runtimeMinutes = runtimeMinutes;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.popularity = popularity;
        this.voteAverage = voteAverage;
        this.genres.clear();
        this.genres.addAll(refreshedGenres);
        this.tmdbSyncedAt = Instant.now();
        this.updatedAt = this.tmdbSyncedAt;
    }

    public boolean needsTmdbRefresh(Instant refreshCutoff) {
        return tmdbId != null && (tmdbSyncedAt == null || tmdbSyncedAt.isBefore(refreshCutoff));
    }

    public void purgeTmdbContent() {
        this.tmdbId = null;
        this.title = "Unavailable title";
        this.originalTitle = null;
        this.overview = null;
        this.releaseDate = null;
        this.runtimeMinutes = null;
        this.posterPath = null;
        this.backdropPath = null;
        this.popularity = null;
        this.voteAverage = null;
        this.genres.clear();
        this.tmdbSyncedAt = null;
        this.updatedAt = Instant.now();
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public Integer getRuntimeMinutes() {
        return runtimeMinutes;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public BigDecimal getPopularity() {
        return popularity;
    }

    public BigDecimal getVoteAverage() {
        return voteAverage;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public Instant getTmdbSyncedAt() {
        return tmdbSyncedAt;
    }
}
