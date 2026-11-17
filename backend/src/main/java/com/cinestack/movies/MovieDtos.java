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
            MediaType mediaType,
            String title,
            String overview,
            LocalDate releaseDate,
            Integer runtimeMinutes,
            String posterPath,
            String backdropPath,
            BigDecimal voteAverage,
            List<String> genres
    ) {
        public static MovieResponse from(Movie movie) {
            return new MovieResponse(
                    movie.getId(),
                    movie.getTmdbId(),
                    movie.getMediaType(),
                    movie.getTitle(),
                    movie.getOverview(),
                    movie.getReleaseDate(),
                    movie.getRuntimeMinutes(),
                    movie.getPosterPath(),
                    movie.getBackdropPath(),
                    movie.getVoteAverage(),
                    movie.getGenres().stream().map(Genre::getName).sorted().toList()
            );
        }
    }

    public record MovieSearchResult(
            Long tmdbId,
            MediaType mediaType,
            String title,
            String overview,
            LocalDate releaseDate,
            String posterPath
    ) {
    }

    /** One page of search results, with the page count so the UI knows when to stop. */
    public record SearchPage(List<MovieSearchResult> results, int page, int totalPages) {
    }
}
