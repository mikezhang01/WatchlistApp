package com.cinestack.movies;

import com.cinestack.common.ApiException;
import com.cinestack.movies.MovieDtos.MovieResponse;
import com.cinestack.movies.MovieDtos.MovieSearchResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MovieService {
    private final MovieRepository movies;
    private final GenreRepository genres;
    private final TmdbClient tmdbClient;

    public MovieService(MovieRepository movies, GenreRepository genres, TmdbClient tmdbClient) {
        this.movies = movies;
        this.genres = genres;
        this.tmdbClient = tmdbClient;
    }

    public List<MovieSearchResult> search(String query, int page) {
        List<MovieSearchResult> external = tmdbClient.search(query, page);
        if (!external.isEmpty()) {
            return external;
        }
        return movies.findTop20ByTitleContainingIgnoreCaseOrderByPopularityDesc(query).stream()
                .map(movie -> new MovieSearchResult(
                        movie.getTmdbId(),
                        movie.getTitle(),
                        movie.getOverview(),
                        movie.getReleaseDate(),
                        movie.getPosterPath()
                ))
                .toList();
    }

    public Movie requireMovie(UUID movieId) {
        return movies.findById(movieId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Movie not found"));
    }

    public MovieResponse get(UUID movieId) {
        return MovieResponse.from(requireMovie(movieId));
    }

    public List<MovieResponse> popular() {
        return movies.findTop20ByOrderByPopularityDesc().stream().map(MovieResponse::from).toList();
    }

    @Transactional
    public MovieResponse importTmdb(Long tmdbId) {
        return movies.findByTmdbId(tmdbId)
                .map(MovieResponse::from)
                .orElseGet(() -> {
                    TmdbClient.TmdbMovieDetails details = tmdbClient.details(tmdbId)
                            .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                                    "TMDB details are unavailable. Configure TMDB_API_KEY and retry."));
                    Movie movie = Movie.fromTmdb(details.id(), details.title(), details.original_title(),
                            details.overview(), details.parsedReleaseDate(), details.runtime(),
                            details.poster_path(), details.backdrop_path(), details.popularity(),
                            details.vote_average());
                    if (details.genres() != null) {
                        for (TmdbClient.TmdbGenre tmdbGenre : details.genres()) {
                            Genre genre = genres.findById(tmdbGenre.id())
                                    .orElseGet(() -> genres.save(new Genre(tmdbGenre.id(), tmdbGenre.name())));
                            movie.addGenre(genre);
                        }
                    }
                    movie = movies.save(movie);
                    return MovieResponse.from(movie);
                });
    }

    @Transactional
    public Movie createManual(String title, Integer runtimeMinutes, String overview) {
        return movies.save(new Movie(title, runtimeMinutes, overview));
    }
}
