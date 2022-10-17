package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Represents a movie watchlist, handles adding, removing and sorting the watchlist
public class WatchList {

    private ArrayList<Movie> watchList;

    // Constructs a new watchlist by creating the new arraylist
    public WatchList() {

        watchList = new ArrayList<Movie>();
    }

    // MODIFIES: this
    // REQUIRES: adds a movie to the watchlist
    public void addToList(Movie movie) {

        watchList.add(movie);
    }

    // EFFECTS: returns the watchlist as a list of movies
    public List<Movie> getFullList() {
        return watchList;
    }

    // EFFECTS: removes the provided movie from the watchlist and returns true, else return false
    public boolean removeFromList(Movie movie) {
        if (watchList.contains(movie)) {
            watchList.remove(movie);
            return true;
        } else {
            return false;
        }
    }

    // EFFECTS: returns the size of the watchlist
    public int getListSize() {
        return watchList.size();
    }

    // EFFECTS: returns a list of all movie titles in the watchlist
    public ArrayList<String> getListOfTitles() {

        ArrayList<String> returnList = new ArrayList<String>();

        for (Movie m : watchList) {
            returnList.add(m.getTitle());
        }

        return returnList;
    }

    // EFFECTS: returns the list of not yet watched movies
    //public List<Movie> getListOfNotWatched() {

        //List<Movie> returnList = new ArrayList<Movie>();

        //for (Movie m : watchList) {
            //if (m.isWatched() == false) {
                //returnList.add(m);
            //}
        //}
        //return returnList;
    //}

    // EFFECTS: returns the watchlist sorted in alphabetical order
    public List<Movie> getListInAlphabetical() {

        Collections.sort(watchList, (Movie m1, Movie m2) -> m1.getTitle().compareTo(m2.getTitle()));

        return watchList;
    }

}
