package ui;

import model.WatchList;
import model.Movie;
import java.util.List;
import java.util.Scanner;

// Represents the watchlist application
public class WatchListApp {

    private WatchList watchList;
    private Movie movie;
    private Scanner input;
    private boolean contiune = true;

    // EFFECTS: runs the watchlist application
    public WatchListApp() {
        runApplication();
    }

    // EFFECTS: main loop of the application, takes user input and go to the corresponding response
    //          contiunes to run until contiune because false
    private void runApplication() {

        String action;
        input = new Scanner(System.in);

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
        } else if (action.equals("q")) {
            contiune = false;
        } else {
            System.out.println("Please enter a valid input");
        }
    }

    // MODIFIES: this
    // EFFECTS: remove a movie from to the watchlist based on title
    private void removeFromList() {

        System.out.println("Please enter the name of the movie you wish to remove:");
        String name = input.nextLine();

        for (Movie m : watchList.getFullList()) {
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

        for (String s : watchList.getListOfTitles()) {
            System.out.println(s);
        }
    }

    // EFFECTS: displays the movie titles in alphabetical order
    private void displaySortedWatchList() {

        List<Movie> fullList = watchList.getListInAlphabetical();
        for (Movie m : fullList) {
            System.out.println(m.getTitle());
        }
    }

    // EFFECTS: displays the menu with selections
    private void displayMenu() {
        System.out.println("\nPlease choose from the following options:");
        System.out.println("\t1 -> view watchlist");
        System.out.println("\t2 -> add movie to watchlist");
        System.out.println("\t3 -> remove movie from watchlist");
        System.out.println("\t4 -> display watchlist in alphabetical order");
        System.out.println("\tq -> exit application");
    }





}
