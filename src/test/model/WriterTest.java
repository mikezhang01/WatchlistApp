package model;

import org.junit.jupiter.api.Test;
import persistence.Reader;
import persistence.Writer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.fail;

// inspired by JsonSerializationDemo JsonWriterTest Class
public class WriterTest extends JsonTest {

    @Test
    void testWriteToInvalidFile() {
        try {
            WatchList watchList = new WatchList();
            Writer writer = new Writer("./data/my\0somefile that do not work");
            writer.open();
            fail("IOException was expected");
        } catch (IOException e) {
            // pass
        }
    }

    @Test
    void testWriteEmptyWatchList() {
        try {
            WatchList watchList = new WatchList();
            Writer writer = new Writer("./data/testWriteEmptyWatchList.json");
            writer.open();
            writer.write(watchList);
            writer.close();

            Reader reader = new Reader("./data/testWriteEmptyWatchList.json");
            watchList = reader.read();
            //assertEquals("My work room", wr.getName());
            assertEquals(watchList.getListSize(), 0);

            ArrayList<String> titleList = new ArrayList<String>();
            titleList = watchList.getListOfTitles();
            assertEquals(titleList.size(), 0);

        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

    @Test
    void testWriteNonEmptyWatchList() {
        try {
            WatchList watchList = new WatchList();
            watchList.addToList(new Movie("Tenet", 2020, 150, "Action/Sci-fi"));
            watchList.addToList(new Movie("Nope", 2022, 135, "Horror/Sci-fi"));
            Writer writer = new Writer("./data/testWriteNonEmptyWatchList.json");
            writer.open();
            writer.write(watchList);
            writer.close();

            Reader reader = new Reader("./data/testWriteNonEmptyWatchList.json");
            watchList = reader.read();
            assertEquals(watchList.getListSize(), 2);

            ArrayList<String> titleList = new ArrayList<String>();
            titleList = watchList.getListOfTitles();
            assertEquals(titleList.get(0), "Tenet");
            assertEquals(titleList.get(1), "Nope");
            List<Movie> fullList = watchList.getFullList();
            checkMovieAttributes(0,false, fullList.get(0));

        } catch (IOException e) {
            fail("Exception should not have been thrown");
        }
    }

}
