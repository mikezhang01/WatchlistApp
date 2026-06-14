package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WatchListTest {

    private WatchList watchListTest;
    private Movie testMovie1;
    private Movie testMovie2;
    private Movie testMovie3;

    @BeforeEach
    public void setUp() {

        watchListTest = new WatchList();
        testMovie1 = new Movie("Tenet", 2020, 150, "Action/Sci-fi");
        testMovie2 = new Movie("Nope", 2022, 135, "Horror/Sci-fi");
        testMovie3 = new Movie("Avengers: Endgame", 2019, 181, "Action/Sci-fi");
    }

    @Test
    public void testAddToList() {

        watchListTest.addToList(testMovie1);
        assertEquals(watchListTest.getListSize(), 1);
        watchListTest.addToList(testMovie2);
        assertEquals(watchListTest.getListSize(), 2);
        assertTrue(watchListTest.getListOfTitles().contains(testMovie1.getTitle()));
        assertTrue(watchListTest.getListOfTitles().contains(testMovie2.getTitle()));

    }

    @Test
    public void testRemoveFromList() {

        watchListTest.addToList(testMovie1);
        assertFalse(watchListTest.removeFromList(testMovie2));

        watchListTest.addToList(testMovie2);
        assertTrue(watchListTest.removeFromList(testMovie2));
        assertEquals(watchListTest.getListSize(), 1);
        assertTrue(watchListTest.getListOfTitles().contains(testMovie1.getTitle()));
        assertFalse(watchListTest.getListOfTitles().contains(testMovie2.getTitle()));
    }

    @Test
    public void testGetFullList() {

        watchListTest.addToList(testMovie1);
        watchListTest.addToList(testMovie2);

        List<Movie> fullList = watchListTest.getFullList();

        assertEquals(fullList.get(0), testMovie1);
        assertEquals(fullList.get(1), testMovie2);
    }

    @Test
    public void testGetListInAlphabetical() {

        watchListTest.addToList(testMovie1);
        watchListTest.addToList(testMovie3);
        watchListTest.addToList(testMovie2);

        List<Movie> fullList = watchListTest.getListInAlphabetical();
        assertEquals(fullList.get(0), testMovie3);
        assertEquals(fullList.get(1), testMovie2);
        assertEquals(fullList.get(2), testMovie1);

    }

}
