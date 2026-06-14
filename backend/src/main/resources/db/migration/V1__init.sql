CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE movies (
    id UUID PRIMARY KEY,
    tmdb_id BIGINT UNIQUE,
    title VARCHAR(255) NOT NULL,
    original_title VARCHAR(255),
    overview TEXT,
    release_date DATE,
    runtime_minutes INT,
    poster_path VARCHAR(255),
    backdrop_path VARCHAR(255),
    popularity NUMERIC(10, 3),
    vote_average NUMERIC(4, 2),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE genres (
    id BIGINT PRIMARY KEY,
    name VARCHAR(120) NOT NULL
);

CREATE TABLE movie_genres (
    movie_id UUID NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE CASCADE,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE watchlists (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE watchlist_items (
    id UUID PRIMARY KEY,
    watchlist_id UUID NOT NULL REFERENCES watchlists(id) ON DELETE CASCADE,
    movie_id UUID NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',
    added_at TIMESTAMPTZ NOT NULL,
    watched_at TIMESTAMPTZ,
    personal_rating INT,
    notes TEXT,
    CONSTRAINT uq_watchlist_movie UNIQUE (watchlist_id, movie_id),
    CONSTRAINT chk_item_rating CHECK (personal_rating IS NULL OR personal_rating BETWEEN 1 AND 5)
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id UUID NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    rating INT NOT NULL,
    body TEXT,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_user_movie_review UNIQUE (user_id, movie_id),
    CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_watchlists_owner ON watchlists(owner_id);
CREATE INDEX idx_watchlist_items_watchlist ON watchlist_items(watchlist_id);
CREATE INDEX idx_reviews_movie ON reviews(movie_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
