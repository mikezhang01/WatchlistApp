ALTER TABLE movies
    ADD COLUMN media_type VARCHAR(20) NOT NULL DEFAULT 'MOVIE';

DO $$
DECLARE
    unique_name TEXT;
BEGIN
    SELECT conname
    INTO unique_name
    FROM pg_constraint
    WHERE conrelid = 'movies'::regclass
      AND contype = 'u'
      AND pg_get_constraintdef(oid) = 'UNIQUE (tmdb_id)'
    LIMIT 1;

    IF unique_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE movies DROP CONSTRAINT %I', unique_name);
    END IF;
END $$;

CREATE UNIQUE INDEX uq_movies_tmdb_media_type
    ON movies(tmdb_id, media_type)
    WHERE tmdb_id IS NOT NULL;
