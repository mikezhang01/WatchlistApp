package ui;

import model.Movie;

import javax.swing.*;
import java.awt.*;

// represents a class that display additional info about a movie on a new window
public class AdditionalInfo {

    private JFrame frame;
    private JLabel label;
    private JPanel panel;

    // MODIFIES: this
    // EFFECTS: constructs and displays AdditionInfo for the given movie in new window
    //          changes the frame Icon to custom image - visual component
    public AdditionalInfo(Movie movie) {

        frame = new JFrame("Additional Info About Movie");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(600,200);
        frame.setResizable(false);
        panel = new JPanel();

        String watched;
        if (movie.isWatched()) {
            watched = "WATCHED";
        } else {
            watched = "Not Watched";
        }

        String output = movie.getDescription() + ",  " + watched + ",  " + "Score: " + movie.getScore() + "/5";
        label = new JLabel(output);
        label.setFont(new Font("Zapfino", Font.BOLD,20));
        ImageIcon image = new ImageIcon("C:\\Users\\paulj\\Desktop\\CPSC 210\\project_s3x1x\\data\\movie.png");
        frame.setIconImage(image.getImage());   // changes frame Icon to custom image - visual component

        panel.add(label);
        frame.add(panel);
        frame.setVisible(true);
    }
}
