package com.cinestack.movies;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
    Optional<Movie> findByTmdbId(Long tmdbId);

    List<Movie> findTop20ByTitleContainingIgnoreCaseOrderByPopularityDesc(String title);

    List<Movie> findTop20ByOrderByPopularityDesc();

    List<Movie> findByTmdbIdIsNotNull();

    List<Movie> findTop20ByTmdbIdIsNotNullAndTmdbSyncedAtBeforeOrderByTmdbSyncedAtAsc(Instant cutoff);

    List<Movie> findByTmdbIdIsNotNullAndTmdbSyncedAtBefore(Instant cutoff);
}
