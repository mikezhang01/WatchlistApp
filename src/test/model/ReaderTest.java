package model;

import org.junit.jupiter.api.Test;
import persistence.Reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.fail;


// inspired by JsonSerializationDemo JsonReaderTest Class
public class ReaderTest extends JsonTest {

    @Test
    void testReadInvalidFile() {

        Reader reader = new Reader("./data/nonExistentFile.json");
        try {
            WatchList watchList = reader.read();
            fail("IOException expected");
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    void testReadEmptyWatchList() {

        Reader reader = new Reader("./data/testReadEmptyWatchList.json");
        try {
            WatchList watchList = reader.read();
            assertEquals(watchList.getListSize(), 0);

            ArrayList<String> titleList = new ArrayList<String>();
            titleList = watchList.getListOfTitles();
            assertEquals(titleList.size(), 0);

        } catch (IOException e) {
            fail("Couldn't read from this file");
        }
    }

    @Test
    void testReaderGeneralWorkRoom() {

        Reader reader = new Reader("./data/testReadNonEmptyWatchList.json");

        try {
            WatchList watchList = reader.read();

            assertEquals(watchList.getListSize(), 3);

            ArrayList<String> titleList = new ArrayList<String>();
            titleList = watchList.getListOfTitles();
            assertEquals(titleList.get(0), "Avengers: Endgame");
            assertEquals(titleList.get(1), "Nope");
            assertEquals(titleList.get(2), "Tenet");

            List<Movie> fullList = watchList.getFullList();
            checkMovieAttributes(5,false, fullList.get(0));

        } catch (IOException e) {
            fail("Couldn't read from this file");
        }
    }







}
