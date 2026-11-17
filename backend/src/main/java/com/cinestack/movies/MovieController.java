package com.cinestack.movies;

import com.cinestack.movies.MovieDtos.MovieResponse;
import com.cinestack.movies.MovieDtos.SearchPage;
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
    SearchPage search(@RequestParam String query, @RequestParam(defaultValue = "1") int page) {
        return movieService.search(query, page);
    }

    @GetMapping("/{movieId}")
    MovieResponse get(@PathVariable UUID movieId) {
        return movieService.get(movieId);
    }

    @PostMapping("/import/tmdb/{tmdbId}")
    MovieResponse importTmdb(@PathVariable Long tmdbId,
                             @RequestParam(defaultValue = "MOVIE") MediaType mediaType) {
        return movieService.importTmdb(tmdbId, mediaType);
    }

    @GetMapping("/popular")
    List<MovieResponse> popular() {
        return movieService.popular();
    }
}
