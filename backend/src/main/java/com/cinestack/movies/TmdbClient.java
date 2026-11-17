package com.cinestack.movies;

import com.cinestack.movies.MovieDtos.MovieSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.time.LocalDate;
import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

@Component
public class TmdbClient {
    private final RestClient restClient;
    private final String apiKey;
    private final TmdbRequestGate requestGate;

    public TmdbClient(@Value("${app.tmdb.base-url}") String baseUrl,
                      @Value("${app.tmdb.api-key}") String apiKey,
                      TmdbRequestGate requestGate) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.requestGate = requestGate;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public List<MovieSearchResult> search(String query, int page) {
        if (!isConfigured()) {
            return List.of();
        }
        TmdbSearchResponse response = execute(() -> restClient.get()
                .uri(uriBuilder -> searchUri(uriBuilder, query, page))
                .retrieve()
                .body(TmdbSearchResponse.class));
        if (response == null || response.results() == null) {
            return List.of();
        }
        return response.results().stream()
                .filter(TmdbSearchResult::isSupportedTitle)
                .map(r -> new MovieSearchResult(
                        r.id(),
                        r.mediaType(),
                        r.displayTitle(),
                        r.overview(),
                        parseDate(r.displayDate()),
                        r.poster_path()
                ))
                .toList();
    }

    public Optional<TmdbMovieDetails> details(Long tmdbId, MediaType mediaType) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        TmdbMovieDetails response = execute(() -> restClient.get()
                .uri(builder -> builder.path("/{mediaType}/{id}")
                        .queryParam("api_key", apiKey)
                        .build(mediaType == MediaType.TV ? "tv" : "movie", tmdbId))
                .retrieve()
                .body(TmdbMovieDetails.class));
        return Optional.ofNullable(response);
    }

    private <T> T execute(java.util.function.Supplier<T> request) {
        requestGate.acquire();
        try {
            return request.get();
        } catch (HttpClientErrorException.TooManyRequests ex) {
            throw new TmdbApiException("TMDB rate limit reached. Retry shortly.", retryAfter(ex), ex);
        } catch (RestClientResponseException ex) {
            throw new TmdbApiException("TMDB returned HTTP " + ex.getStatusCode().value() + ".", null, ex);
        } catch (ResourceAccessException ex) {
            throw new TmdbApiException("TMDB is temporarily unreachable.", null, ex);
        }
    }

    private Integer retryAfter(HttpClientErrorException.TooManyRequests ex) {
        String retryAfter = ex.getResponseHeaders() == null
                ? null
                : ex.getResponseHeaders().getFirst(HttpHeaders.RETRY_AFTER);
        if (retryAfter == null) {
            return 5;
        }
        try {
            return Integer.parseInt(retryAfter);
        } catch (NumberFormatException ignored) {
            return 5;
        }
    }

    private java.net.URI searchUri(UriBuilder builder, String query, int page) {
        return builder.path("/search/multi")
                .queryParam("api_key", apiKey)
                .queryParam("query", query)
                .queryParam("page", page)
                .build();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return parseDateOrNull(value);
    }

    /** TMDB occasionally sends a date that is not a valid date; treat it as unknown. */
    static LocalDate parseDateOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException ex) {
            return null;
        }
    }

    public record TmdbSearchResponse(List<TmdbSearchResult> results) {
    }

    public record TmdbSearchResult(
            Long id,
            String media_type,
            String title,
            String name,
            String overview,
            String release_date,
            String first_air_date,
            String poster_path
    ) {
        public boolean isSupportedTitle() {
            return mediaType() != null;
        }

        public MediaType mediaType() {
            if ("movie".equals(media_type)) {
                return MediaType.MOVIE;
            }
            if ("tv".equals(media_type)) {
                return MediaType.TV;
            }
            return null;
        }

        public String displayTitle() {
            return mediaType() == MediaType.TV ? name : title;
        }

        public String displayDate() {
            return mediaType() == MediaType.TV ? first_air_date : release_date;
        }
    }

    public record TmdbMovieDetails(
            Long id,
            String title,
            String name,
            String original_title,
            String original_name,
            String overview,
            String release_date,
            String first_air_date,
            Integer runtime,
            List<Integer> episode_run_time,
            String poster_path,
            String backdrop_path,
            BigDecimal popularity,
            BigDecimal vote_average,
            List<TmdbGenre> genres
    ) {
        public String displayTitle(MediaType mediaType) {
            return mediaType == MediaType.TV ? name : title;
        }

        public String displayOriginalTitle(MediaType mediaType) {
            return mediaType == MediaType.TV ? original_name : original_title;
        }

        public LocalDate parsedReleaseDate(MediaType mediaType) {
            String value = mediaType == MediaType.TV ? first_air_date : release_date;
            return parseDateOrNull(value);
        }

        public Integer runtimeMinutes(MediaType mediaType) {
            if (mediaType == MediaType.MOVIE) {
                return runtime;
            }
            if (episode_run_time == null || episode_run_time.isEmpty()) {
                return null;
            }
            return episode_run_time.get(0);
        }
    }

    public record TmdbGenre(Long id, String name) {
    }
}
