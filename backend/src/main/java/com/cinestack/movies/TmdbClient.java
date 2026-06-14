package com.cinestack.movies;

import com.cinestack.movies.MovieDtos.MovieSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

@Component
public class TmdbClient {
    private final RestClient restClient;
    private final String apiKey;

    public TmdbClient(@Value("${app.tmdb.base-url}") String baseUrl, @Value("${app.tmdb.api-key}") String apiKey) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public List<MovieSearchResult> search(String query, int page) {
        if (apiKey == null || apiKey.isBlank()) {
            return List.of();
        }
        TmdbSearchResponse response = restClient.get()
                .uri(uriBuilder -> searchUri(uriBuilder, query, page))
                .retrieve()
                .body(TmdbSearchResponse.class);
        if (response == null || response.results() == null) {
            return List.of();
        }
        return response.results().stream()
                .map(r -> new MovieSearchResult(r.id(), r.title(), r.overview(), parseDate(r.release_date()), r.poster_path()))
                .toList();
    }

    public Optional<TmdbMovieDetails> details(Long tmdbId) {
        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }
        TmdbMovieDetails response = restClient.get()
                .uri(builder -> builder.path("/movie/{id}")
                        .queryParam("api_key", apiKey)
                        .build(tmdbId))
                .retrieve()
                .body(TmdbMovieDetails.class);
        return Optional.ofNullable(response);
    }

    private java.net.URI searchUri(UriBuilder builder, String query, int page) {
        return builder.path("/search/movie")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("page", page)
                .build();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    public record TmdbSearchResponse(List<TmdbMovie> results) {
    }

    public record TmdbMovie(Long id, String title, String overview, String release_date, String poster_path) {
    }

    public record TmdbMovieDetails(
            Long id,
            String title,
            String original_title,
            String overview,
            String release_date,
            Integer runtime,
            String poster_path,
            String backdrop_path,
            BigDecimal popularity,
            BigDecimal vote_average,
            List<TmdbGenre> genres
    ) {
        public LocalDate parsedReleaseDate() {
            return release_date == null || release_date.isBlank() ? null : LocalDate.parse(release_date);
        }
    }

    public record TmdbGenre(Long id, String name) {
    }
}
