package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MovieTest {

    private Movie testMovie1;
    private Movie testMovie2;

    @BeforeEach
    public void setUp() {

        testMovie1 = new Movie("Tenet", 2020, 150, "Action/Sci-fi");
        testMovie2 = new Movie("Nope", 2022, 135, "Horror/Sci-fi");
    }

    @Test
    public void testConstuctor() {

        assertEquals(testMovie1.getTitle(), "Tenet");
        assertEquals(testMovie1.getYear(), 2020);
        assertEquals(testMovie1.getDuration(), 150);
        assertEquals(testMovie1.getDescription(), "Action/Sci-fi");
    }

    @Test
    public void testMarkAsWatched() {
        testMovie1.markAsWatched();
        assertTrue(testMovie1.isWatched());
        assertFalse(testMovie2.isWatched());
    }

    @Test
    public void testSetScore() {
        testMovie1.setScore(5);
        testMovie2.setScore(3);
        assertEquals(testMovie1.getScore(), 5);
        assertEquals(testMovie2.getScore(), 3);
    }

}
