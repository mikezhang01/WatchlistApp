package persistence;

import model.Movie;
import model.WatchList;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

// inspired by JsonSerializationDemo JsonReader Class
// Represents a reader that reads watchlist from JSON data stored in a file
public class Reader {

    private String location;

    // EFFECTS: constructs reader to read from file location
    public Reader(String location) {
        this.location = location;
    }

    // EFFECTS: reads watchlist from file and returns it;
    // throws IOException if an error occurs reading data from file
    public WatchList read() throws IOException {

        String jsonData = new String(Files.readAllBytes(Paths.get(location)), StandardCharsets.UTF_8);
        JSONObject jsonObject = new JSONObject(jsonData);
        return parseWatchList(jsonObject);
    }

    // EFFECTS: parses watchlist from JSON object and returns it
    private WatchList parseWatchList(JSONObject jsonObject) {

        WatchList watchList = new WatchList();
        JSONArray jsonArray = jsonObject.getJSONArray("Movies");

        for (Object json : jsonArray) {
            JSONObject nextMovie = (JSONObject) json;
            addMovieToList(watchList, nextMovie);
        }
        return watchList;

    }

    // MODIFIES: watchlist
    // EFFECTS: parses movies from JSON object and adds them to watchlist
    private void addMovieToList(WatchList watchList, JSONObject nextMovie) {

        String title = nextMovie.getString("title");
        int year = nextMovie.getInt("year");
        int duration = nextMovie.getInt("duration");
        String description = nextMovie.getString("description");
        boolean watched = nextMovie.getBoolean("watched");
        int score = nextMovie.getInt("score");

        Movie movie = new Movie(title, year, duration, description);
        movie.setScore(score);
        if (watched == true) {
            movie.markAsWatched();
        }

        watchList.addToList(movie);
    }

}
