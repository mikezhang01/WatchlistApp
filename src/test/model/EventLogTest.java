package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the EventLog class
 */
// inspired by EventLogTest class in AlarmSystem
public class EventLogTest {

    private WatchList watchListTest;
    private Movie testMovie1;
    private Movie testMovie2;

    @BeforeEach
    public void loadEvents() {
        EventLog eventLog = EventLog.getInstance();

        watchListTest = new WatchList();
        testMovie1 = new Movie("Tenet", 2020, 150, "Action/Sci-fi");
        testMovie2 = new Movie("Nope", 2022, 135, "Horror/Sci-fi");
    }

    @Test
    public void testLogEvent() {
        watchListTest.addToList(testMovie1);
        watchListTest.addToList(testMovie2);
        watchListTest.removeFromList(testMovie1);
        watchListTest.getListInAlphabetical();
        testMovie1.markAsWatched();
        testMovie2.setScore(5);

        List<Event> l = new ArrayList<Event>();

        EventLog el = EventLog.getInstance();
        for (Event next : el) {
            l.add(next);
        }
        assertEquals(l.size(), 6);
        assertEquals("Tenet added to the watchlist.", l.get(0).getDescription());
        assertEquals("Nope added to the watchlist.", l.get(1).getDescription());
        assertEquals("Tenet is removed from the watchlist.", l.get(2).getDescription());
        assertEquals("Watchlist sorted in alphabetical order.", l.get(3).getDescription());
        assertEquals("Tenet is marked as watched.", l.get(4).getDescription());
        assertEquals("Nope is rated 5 out of 5.", l.get(5).getDescription());
    }

    @Test
    public void testClear() {
        EventLog el = EventLog.getInstance();
        el.clear();
        Iterator<Event> itr = el.iterator();
        assertTrue(itr.hasNext());   // After log is cleared, the clear log event is added
        assertEquals("Event log cleared.", itr.next().getDescription());
        assertFalse(itr.hasNext());
    }
}
