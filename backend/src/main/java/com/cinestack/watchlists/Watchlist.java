package com.cinestack.watchlists;

import com.cinestack.common.Visibility;
import com.cinestack.users.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "watchlists")
public class Watchlist {
    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PRIVATE;

    @OneToMany(mappedBy = "watchlist", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WatchlistItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Watchlist() {
    }

    public Watchlist(User owner, String name, String description, Visibility visibility) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public boolean isOwnedBy(User user) {
        return owner.getId().equals(user.getId());
    }

    public void update(String name, String description, Visibility visibility) {
        this.name = name;
        this.description = description;
        this.visibility = visibility;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public List<WatchlistItem> getItems() {
        return items;
    }
}

