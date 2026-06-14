package com.cinestack.movies;

import com.cinestack.movies.MovieDtos.MovieResponse;
import com.cinestack.movies.MovieDtos.MovieSearchResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/search")
    List<MovieSearchResult> search(@RequestParam String query, @RequestParam(defaultValue = "1") int page) {
        return movieService.search(query, page);
    }

    @GetMapping("/{movieId}")
    MovieResponse get(@PathVariable UUID movieId) {
        return movieService.get(movieId);
    }

    @PostMapping("/import/tmdb/{tmdbId}")
    MovieResponse importTmdb(@PathVariable Long tmdbId) {
        return movieService.importTmdb(tmdbId);
    }

    @GetMapping("/popular")
    List<MovieResponse> popular() {
        return movieService.popular();
    }
}

