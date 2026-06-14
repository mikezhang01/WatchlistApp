package ui;

import model.WatchList;
import model.Movie;
import persistence.Reader;
import persistence.Writer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Represents the watchlist application
public class WatchListApp {

    private static final String JSON_LOCATION = "./data/watchList.json";
    private WatchList watchList;
    private Movie movie;
    private Scanner input;
    private boolean contiune = true;
    private Writer jsonWriter;
    private Reader jsonReader;

    // EFFECTS: runs the watchlist application
    public WatchListApp() {

        jsonWriter = new Writer(JSON_LOCATION);
        jsonReader = new Reader(JSON_LOCATION);
        runApplication();
    }

    // EFFECTS: main loop of the application, takes user input and go to the corresponding response
    //          contiunes to run until contiune becomes false
    private void runApplication() {

        String action;
        input = new Scanner(System.in);

        watchList = new WatchList();

        while (contiune) {
            displayMenu();

            action = input.next();
            actionResponse(action);
        }

        System.out.println("Application is closed");
    }

    // EFFECTS: respond to user inputs
    private void actionResponse(String action) {

        if (action.equals("1")) {
            displayWatchList();
        } else if (action.equals("2")) {
            addToList();
        } else if (action.equals("3")) {
            removeFromList();
        } else if (action.equals("4")) {
            displaySortedWatchList();
        } else if (action.equals("5")) {
            markAsWatched();
        } else if (action.equals("6")) {
            giveScore();
        } else if (action.equals("0")) {
            displayAdditionInfo();
        } else if (action.equals("8")) {
            saveWatchList();
        } else if (action.equals("9")) {
            loadWatchList();
        } else if (action.equals("q")) {
            contiune = false;
        } else {
            System.out.println("Please enter a valid input");
        }
    }

    // MODIFIES: this
    // EFFECTS: loads watchlist from file
    private void loadWatchList() {

        try {
            watchList = jsonReader.read();
            System.out.println("Loaded WatchList from " + JSON_LOCATION);
        } catch (IOException e) {
            System.out.println("Unable to load from file: " + JSON_LOCATION);
        }
    }

    // EFFECTS: saves the watchlist to file
    private void saveWatchList() {

        try {
            jsonWriter.open();
            jsonWriter.write(watchList);
            jsonWriter.close();
            System.out.println("Saved WatchList to " + JSON_LOCATION);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_LOCATION + " because it doesn't exist");
        }
    }

    // EFFECTS: displays additional info for the selected movie
    private void displayAdditionInfo() {

        System.out.println("Please enter a movie name to view addition information:");
        String name = input.next();
        String watched;

        for (Movie m : watchList.getFullList()) {
            if (m.getTitle().equals(name)) {
                if (m.isWatched()) {
                    watched = "WATCHED";
                } else {
                    watched = "Not Watched";
                }
                System.out.println(m.getDescription() + ", " + watched + ", " + "Score: " + m.getScore() + "/5");
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: give a movie from the watchlist a score based on title
    private void giveScore() {

        System.out.println("Please enter the name of the movie you wish to rate:");
        String name = input.next();

        System.out.println("Please give it a rating out of 5:");
        int score = input.nextInt();

        for (Movie m : watchList.getFullList()) {
            if (m.getTitle().equals(name)) {
                m.setScore(score);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: mark a movie from the watchlist as watched based on title
    private void markAsWatched() {

        System.out.println("Please enter the name of the movie you wish to mark as watched:");
        String name = input.next();

        for (Movie m : watchList.getFullList()) {
            if (m.getTitle().equals(name) && m.isWatched() != true) {
                m.markAsWatched();
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: remove a movie from the watchlist based on title
    private void removeFromList() {

        System.out.println("Please enter the name of the movie you wish to remove:");
        String name = input.next();

        ArrayList<Movie> newList = new ArrayList<Movie>();
        for (Movie m : watchList.getFullList()) {
            newList.add(m);
        }

        for (Movie m : newList) {
            if (m.getTitle().equals(name)) {
                watchList.removeFromList(m);
            }
        }
    }

    // MODIFIES: this
    // EFFECTS: creates a new movie from user inputs, and adds to the watchlist
    private void addToList() {

        System.out.println("Please enter the name of the movie:");
        String name = input.next();
        System.out.println("Which year did the movie release:");
        int year = input.nextInt();
        System.out.println("How long is the movie (in min):");
        int duration = input.nextInt();
        System.out.println("Description of the movie:");
        String description = input.next();

        movie = new Movie(name, year, duration, description);
        watchList.addToList(movie);
    }

    // EFFECTS: displays the movie titles in the watchlist
    private void displayWatchList() {

        for (Movie m : watchList.getFullList()) {
            System.out.println(m.getTitle().toUpperCase() + ", " + "Year: "
                    + m.getYear() + ", " + m.getDuration() + "min");
        }
    }

    // EFFECTS: displays the movie in alphabetical order based on title
    private void displaySortedWatchList() {

        List<Movie> newList = watchList.getListInAlphabetical();

        for (Movie m : newList) {
            System.out.println(m.getTitle().toUpperCase() + ", " + "Year: "
                    + m.getYear() + ", " + m.getDuration() + "min");
        }
    }

    // EFFECTS: displays the menu with selections
    private void displayMenu() {
        System.out.println("\nPlease choose from the following options:");
        System.out.println("\t1 -> view watchlist");
        System.out.println("\t2 -> add movie to watchlist");
        System.out.println("\t3 -> remove movie from watchlist");
        System.out.println("\t4 -> display watchlist in alphabetical order");
        System.out.println("\t5 -> mark a movie as watched");
        System.out.println("\t6 -> give a movie a score");
        System.out.println("\t0 -> view additional info about movie");
        System.out.println("\t8 -> save watchlist");
        System.out.println("\t9 -> load watchlist");
        System.out.println("\tq -> exit application");
    }

}
