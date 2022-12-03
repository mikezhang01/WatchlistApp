package model;

import org.json.JSONObject;
import persistence.Writeable;

// Represents a Movie object
public class Movie implements Writeable {

    private String title;
    private int year;
    private int duration;
    private String description;
    private boolean watched;
    private int score;

    // constructs a new Movie objects with the provided attributes
    public Movie(String title, int year, int duration, String description) {

        this.title = title;
        this.year = year;
        this.duration = duration;
        this.description = description;
    }

    // EFFECTS: returns the title of the movie
    public String getTitle() {
        return title;
    }

    // EFFECTS: returns the year of the movie
    public int getYear() {
        return year;
    }

    // EFFECTS: returns the length of the movie
    public int getDuration() {
        return duration;
    }

    // EFFECTS: returns the movie description
    public String getDescription() {
        return description;
    }

    // MODIFIES: this
    // EFFECTS:  marks this movie as watched
    public void markAsWatched() {
        EventLog.getInstance().logEvent(new Event(this.getTitle() + " is marked as watched."));
        this.watched = true;
    }

    // EFFECTS: returns if the movie has been watched or not
    public boolean isWatched() {
        return watched;
    }

    // MODIFIES: this
    // EFFECTS:  gives the movie a score
    public void setScore(int score) {
        this.score = score;
        EventLog.getInstance().logEvent(new Event(this.getTitle() + " is rated " + score + " out of 5."));
    }

    // EFFECTS: returns the score of the movie
    public int getScore() {
        return score;
    }

    // EFFECTS: returns movie as a JSONObject
    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("title", title);
        json.put("year", year);
        json.put("duration", duration);
        json.put("description", description);
        json.put("watched", watched);
        json.put("score", score);
        return json;
    }
}
