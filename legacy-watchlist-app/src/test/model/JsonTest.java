package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

// inspired by JsonSerializationDemo JsonTest Class
// Represents a class that tests if Movie attributes are correct
public class JsonTest {
    protected void checkMovieAttributes(int score, boolean watched, Movie movie) {
        assertEquals(score, movie.getScore());
        assertEquals(watched, movie.isWatched());
    }
}

