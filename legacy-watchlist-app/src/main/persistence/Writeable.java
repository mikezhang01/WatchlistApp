package persistence;

import org.json.JSONObject;

// inspired by JsonSerializationDemo Writable Interface
// Represents data that can be write to file
public interface Writeable {
    // EFFECTS: returns watchlist as a JSONObject
    JSONObject toJson();
}
