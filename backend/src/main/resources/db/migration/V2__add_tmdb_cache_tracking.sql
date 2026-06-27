ALTER TABLE movies ADD COLUMN tmdb_synced_at TIMESTAMPTZ;

UPDATE movies
SET tmdb_synced_at = updated_at
WHERE tmdb_id IS NOT NULL;

CREATE INDEX idx_movies_tmdb_cache_age
    ON movies(tmdb_synced_at)
    WHERE tmdb_id IS NOT NULL;

