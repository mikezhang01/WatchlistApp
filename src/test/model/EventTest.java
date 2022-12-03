package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the Event class
 */
// inspired by EventTest class in AlarmSystem
public class EventTest {
    private Event e;
    private Date d;

    private WatchList watchListTest;
    private Movie testMovie1;
    private Movie testMovie2;

    @BeforeEach
    public void runBefore() {
        e = new Event("added to watchlist");
        d = Calendar.getInstance().getTime();

        EventLog.getInstance().clear();

        watchListTest = new WatchList();
        testMovie1 = new Movie("Tenet", 2020, 150, "Action/Sci-fi");
        testMovie2 = new Movie("Nope", 2022, 135, "Horror/Sci-fi");
    }

    @Test
    public void testEvent() {
        assertEquals("added to watchlist", e.getDescription());
    }

    @Test
    public void testToString() {
        assertEquals(d.toString() + "\n" + "added to watchlist", e.toString());
    }

    @Test
    public void testAddRemoveAndSort() {
        watchListTest.addToList(testMovie1);
        watchListTest.addToList(testMovie2);
        watchListTest.removeFromList(testMovie1);
        watchListTest.getListInAlphabetical();

        List<Event> l = new ArrayList<Event>();

        EventLog eventLog = EventLog.getInstance();
        for (Event next : eventLog) {
            l.add(next);
        }

        assertEquals("Event log cleared.",l.get(0).getDescription());
        assertEquals("Tenet added to the watchlist.", l.get(1).getDescription());
        assertEquals("Nope added to the watchlist.", l.get(2).getDescription());
        assertEquals("Tenet is removed from the watchlist.", l.get(3).getDescription());
        assertEquals("Watchlist sorted in alphabetical order.", l.get(4).getDescription());
    }

    @Test
    public void testRateAndMark() {
        testMovie1.markAsWatched();
        testMovie2.setScore(5);

        List<Event> l = new ArrayList<Event>();

        EventLog eventLog = EventLog.getInstance();
        for (Event next : eventLog) {
            l.add(next);
        }

        assertEquals("Tenet is marked as watched.", l.get(1).getDescription());
        assertEquals("Nope is rated 5 out of 5.", l.get(2).getDescription());
    }

}
