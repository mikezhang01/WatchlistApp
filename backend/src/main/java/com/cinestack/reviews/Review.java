package com.cinestack.reviews;

import com.cinestack.common.Visibility;
import com.cinestack.movies.Movie;
import com.cinestack.users.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(nullable = false)
    private int rating;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Review() {
    }

    public Review(User user, Movie movie, int rating, String body, Visibility visibility) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.movie = movie;
        this.rating = rating;
        this.body = body;
        this.visibility = visibility;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public boolean isOwnedBy(User candidate) {
        return user.getId().equals(candidate.getId());
    }

    public void update(int rating, String body, Visibility visibility) {
        this.rating = rating;
        this.body = body;
        this.visibility = visibility;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Movie getMovie() {
        return movie;
    }

    public int getRating() {
        return rating;
    }

    public String getBody() {
        return body;
    }

    public Visibility getVisibility() {
        return visibility;
    }
}

