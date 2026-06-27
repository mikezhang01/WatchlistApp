package com.cinestack.movies;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GenreRepository extends JpaRepository<Genre, Long> {
    @Modifying
    @Query(value = "DELETE FROM genres g WHERE NOT EXISTS "
            + "(SELECT 1 FROM movie_genres mg WHERE mg.genre_id = g.id)", nativeQuery = true)
    void deleteOrphanedGenres();
}
