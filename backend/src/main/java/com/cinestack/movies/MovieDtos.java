package com.cinestack.movies;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class MovieDtos {
    private MovieDtos() {
    }

    public record MovieResponse(
            UUID id,
            Long tmdbId,
            String title,
            String overview,
            LocalDate releaseDate,
            Integer runtimeMinutes,
            String posterPath,
            BigDecimal voteAverage,
            List<String> genres
    ) {
        public static MovieResponse from(Movie movie) {
            return new MovieResponse(
                    movie.getId(),
                    movie.getTmdbId(),
                    movie.getTitle(),
                    movie.getOverview(),
                    movie.getReleaseDate(),
                    movie.getRuntimeMinutes(),
                    movie.getPosterPath(),
                    movie.getVoteAverage(),
                    movie.getGenres().stream().map(Genre::getName).sorted().toList()
            );
        }
    }

    public record MovieSearchResult(
            Long tmdbId,
            String title,
            String overview,
            LocalDate releaseDate,
            String posterPath
    ) {
    }
}

