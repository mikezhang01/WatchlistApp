package ui;

import model.Event;
import model.EventLog;

import model.Movie;
import model.WatchList;
import persistence.Reader;
import persistence.Writer;

import java.awt.*;
import java.awt.event.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

//inspired by ListDemo.java from https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html
// represents a GUI for watchlist application
public class GUI extends JPanel implements ListSelectionListener {

    private JList list;
    private DefaultListModel listModel;

    private JButton removeButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton watchedButton;
    private JButton rateButton;
    private JButton sortButton;
    private JButton infoButton;
    private JButton addButton;
    private JLabel titleLabel;
    private JLabel yearLabel;
    private JLabel lengthLabel;
    private JLabel infoLabel;
    private JLabel addMovie;

    private JTextField movieTitle;
    private JTextField year;
    private JTextField length;
    private JTextField description;
    private JPanel buttonPanel;
    private JPanel addPanel;
    private AddListener addListener;
    private JScrollPane listScrollPane;

    private WatchList watchList;
    private Writer jsonWriter;
    private Reader jsonReader;
    private static final String JSON_LOCATION = "./data/watchList.json";

    // MODIFIES: this
    // EFFECTS: constructs the GUI, creates and adds all the panels, buttons, text fields
    // and designs the layout
    public GUI() {
        super(new BorderLayout());
        createJson();
        createWatchList();
        displayList();
        createButton();
        createTextBox();
        addButtonToPanel();
        createAddFunction();
        add(addPanel);
        add(listScrollPane, BorderLayout.WEST);
        add(buttonPanel, BorderLayout.PAGE_END);
    }

    // MODIFIES: this, buttonPanel
    // EFFECTS: creates bottom panel, set color and border,
    // and adds all the buttons to be displayed on the bottom panel
    private void addButtonToPanel() {
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.PINK);

        buttonPanel.add(removeButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(watchedButton);
        buttonPanel.add(rateButton);
        buttonPanel.add(sortButton);
        buttonPanel.add(infoButton);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    }

    // MODIFIES: this, addPanel
    // EFFECTS: creates panel for adding movie, constructs all the JLabels for adding movie
    // and adds them to the panel
    private void createAddFunction() {
        addMovie = new JLabel("Add a movie to watchlist");
        addMovie.setFont(new Font("Zapfino", Font.BOLD,20));
        titleLabel = new JLabel("Title: ");
        yearLabel = new JLabel("Year: ");
        lengthLabel = new JLabel("Duration: ");
        infoLabel = new JLabel("Description: ");

        addPanel = new JPanel();
        addPanel.setBounds(410,0,100,100);
        addPanel.setBackground(Color.orange);
        addPanel.add(addMovie);
        addPanel.add(titleLabel);
        addPanel.add(movieTitle);
        addPanel.add(yearLabel);
        addPanel.add(year);
        addPanel.add(lengthLabel);
        addPanel.add(length);
        addPanel.add(infoLabel);
        addPanel.add(description);
        addPanel.add(addButton);
    }

    // MODIFIES: this, watchList, listModel
    // EFFECTS: creates new watchList and listModel
    // and adds each movie's display info from watchList to list Model
    private void createWatchList() {

        watchList = new WatchList();

        listModel = new DefaultListModel();
        for (Movie m : watchList.getFullList()) {
            String printOut = m.getTitle().toUpperCase() + ", " + "Year: "
                    + m.getYear() + ", " + m.getDuration() + "min";
            listModel.addElement(printOut);
        }
    }

    // MODIFIES: this
    // EFFECTS: creates JSon Writer and Reader for saving and loading
    private void createJson() {
        jsonWriter = new Writer(JSON_LOCATION);
        jsonReader = new Reader(JSON_LOCATION);
    }

    // MODIFIES: this
    // EFFECTS: creates all the buttons used in this GUI
    private void createButton() {
        addButton = new JButton("Add");
        addListener = new AddListener(addButton);
        addButton.addActionListener(addListener);
        addButton.setEnabled(false);

        removeButton = new JButton("Remove");
        removeButton.addActionListener(new RemoveListener());

        saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveListener());

        loadButton = new JButton("Load");
        loadButton.addActionListener(new LoadListener());

        watchedButton = new JButton("Mark as Watched");
        watchedButton.addActionListener(new WatchedListener());

        rateButton = new JButton("Rate");
        rateButton.addActionListener(new RateListener());

        sortButton = new JButton("Sort");
        sortButton.addActionListener(new SortListener());

        infoButton = new JButton("Additional Info");
        infoButton.addActionListener(new InfoListener());
    }

    // MODIFIES: this, list, listScrollPane
    // EFFECTS: create the list and put it in a scroll pane
    private void displayList() {

        list = new JList(listModel);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setSelectedIndex(0);
        list.addListSelectionListener(this);
        list.setVisibleRowCount(9);
        listScrollPane = new JScrollPane(list);
        listScrollPane.setPreferredSize(new Dimension(400,600));
    }

    // MODIFIES: this
    // EFFECTS: create the text fields for adding movie
    private void createTextBox() {
        movieTitle = new JTextField(20);
        movieTitle.addActionListener(addListener);
        movieTitle.getDocument().addDocumentListener(addListener);
        year = new JTextField(20);
        year.addActionListener(addListener);
        year.getDocument().addDocumentListener(addListener);
        length = new JTextField(20);
        length.addActionListener(addListener);
        length.getDocument().addDocumentListener(addListener);
        description = new JTextField(20);
        description.addActionListener(addListener);
        description.getDocument().addDocumentListener(addListener);
    }

    // MODIFIES: this
    // EFFECTS: loads watchList from file
    private void loadWatchList() {
        try {
            watchList = jsonReader.read();
            //System.out.println("Loaded WatchList from " + JSON_LOCATION);
        } catch (IOException e) {
            System.out.println("Unable to load from file: " + JSON_LOCATION);
        }
    }

    // EFFECTS: saves the watchList to file
    private void saveWatchList() {
        try {
            jsonWriter.open();
            jsonWriter.write(watchList);
            jsonWriter.close();
            //System.out.println("Saved WatchList to " + JSON_LOCATION);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to write to file: " + JSON_LOCATION + " because it doesn't exist");
        }
    }

    //inspired by ListDemo.java
    // represents a Removelistener that removes the selected movie from display and watchlist,
    // when the corresponding actionevent happens (button)
    class RemoveListener implements ActionListener {

        // REQUIRES: This method can be called only if there's a valid selection
        // MODIFIES: this
        // EFFECTS: removes selected movie from watchList and listModel
        public void actionPerformed(ActionEvent e) {

            int index = list.getSelectedIndex();
            listModel.remove(index);

            Movie m = watchList.getFullList().get(index);
            watchList.removeFromList(m);

            int size = listModel.getSize();

            if (size == 0) {
                removeButton.setEnabled(false);
            } else {
                if (index == listModel.getSize()) {
                    index--;
                }
                list.setSelectedIndex(index);
                list.ensureIndexIsVisible(index);
            }
        }
    }

    // represents an Infolistener that displays additional info for selected movie
    // in a new window when the corresponding actionevent happens (button)
    class InfoListener implements ActionListener {

        // REQUIRES: This method can be called only if there's a valid selection
        // EFFECTS: displays additional info for movie in new window
        public void actionPerformed(ActionEvent e) {

            int index = list.getSelectedIndex();

            Movie m = watchList.getFullList().get(index);

            AdditionalInfo additionalInfo = new AdditionalInfo(m);
        }
    }

    // represents a Sortlistener that displays watchlist in alphabetical order
    // when the corresponding actionevent happens (button)
    class SortListener implements ActionListener {

        // MODIFIES: this
        // EFFECTS: sort watchlist in alphabetical order and displays it
        public void actionPerformed(ActionEvent e) {

            List<Movie> newList = new ArrayList<>();
            newList = watchList.getListInAlphabetical();

            listModel.clear();
            for (Movie m : newList) {
                String printOut = m.getTitle().toUpperCase() + ", " + "Year: "
                        + m.getYear() + ", " + m.getDuration() + "min";
                listModel.addElement(printOut);
            }
        }
    }

    // represents a Ratelistener that allows user to rate a movie
    // when the corresponding actionevent happens (button)
    class RateListener implements ActionListener {

        // REQUIRES: This method can be called only if there's a valid selection
        // MODIFIES: this
        // EFFECTS: changes selected movie's score to user input (out of 5)
        public void actionPerformed(ActionEvent e) {

            int index = list.getSelectedIndex();

            Movie m = watchList.getFullList().get(index);

            String rating = JOptionPane.showInputDialog("Rate the movie out of 5: ");
            int score = Integer.parseInt(rating);
            m.setScore(score);
        }
    }

    // represents a Watchedlistener that allows user to mark a movie as watched
    // when the corresponding actionevent happens (button)
    class WatchedListener implements ActionListener {

        // REQUIRES: This method can be called only if there's a valid selection
        // MODIFIES: this
        // EFFECTS: mark selected movie as watched
        public void actionPerformed(ActionEvent e) {

            int index = list.getSelectedIndex();

            Movie m = watchList.getFullList().get(index);
            m.markAsWatched();
        }
    }

    // represents a Loadlistener that loads watchlist from a file
    // when the corresponding actionevent happens (button)
    class LoadListener implements ActionListener {

        // MODIFIES: this
        // EFFECTS: changes watchlist and listModel based on the watchlist loaded
        public void actionPerformed(ActionEvent e) {
            loadWatchList();

            listModel.clear();
            for (Movie m : watchList.getFullList()) {
                String printOut = m.getTitle().toUpperCase() + ", " + "Year: "
                        + m.getYear() + ", " + m.getDuration() + "min";
                listModel.addElement(printOut);
            }

        }
    }

    // represents a Savelistener that saves watchlist to a file
    // when the corresponding actionevent happens (button)
    class SaveListener implements ActionListener {

        // EFFECTS: saves watchlist to file
        public void actionPerformed(ActionEvent e) {
            saveWatchList();
        }
    }

    //inspired by ListDemo.java
    // represents an Addlistener that allows user to add movie to watchlist,
    // when the corresponding actionevent happens (button)
    class AddListener implements ActionListener, DocumentListener {
        private boolean alreadyEnabled = false;
        private JButton button;

        // MODIFIES: this
        // EFFECTS: set button to the corresponding button that is passed in
        public AddListener(JButton button) {
            this.button = button;
        }

        // MODIFIES: this
        // EFFECTS: adds movie to watchlist and displays it
        //          based on user input of movie title, year, length, and description
        //          sets the text fields to empty once a movie has been added
        //          displays a pop-up window with a custom image as logo that confirms
        // movie is added (visual component)
        public void actionPerformed(ActionEvent e) {
            String name = movieTitle.getText();
            String yearIn = year.getText();
            String duration = length.getText();
            String info = description.getText();

            int index = list.getSelectedIndex();
            int yearR = Integer.parseInt(yearIn);
            int runtime = Integer.parseInt(duration);

            Movie m = new Movie(name,yearR,runtime,info);
            watchList.addToList(m);
            String message = m.getTitle().toUpperCase() + ", " + "Year: "
                    + m.getYear() + ", " + m.getDuration() + "min";

            listModel.addElement(message);

            //Reset the text fields
            movieTitle.setText("");
            year.setText("");
            length.setText("");
            description.setText("");

            //Select the new item and make it visible
            list.setSelectedIndex(index);
            list.ensureIndexIsVisible(index);

            // changes the image to the pop-up window to popcorn - visual component
            ImageIcon image2 = new
                    ImageIcon("C:\\Users\\mike\\Desktop\\CPSC 210\\project_s3x1x\\data\\popcornF.png");
            JOptionPane.showMessageDialog(null, "Added " + m.getTitle(),
                    "Success!", JOptionPane.PLAIN_MESSAGE, image2);
        }

        //Required by DocumentListener
        // EFFECTS: enables button
        public void insertUpdate(DocumentEvent e) {
            enableButton();
        }

        //Required by DocumentListener
        // EFFECTS: enables button when handleEmptyTextField returns true
        public void removeUpdate(DocumentEvent e) {
            handleEmptyTextField(e);
        }

        //Required by DocumentListener
        // EFFECTS: enables button when handleEmptyTextField returns false
        public void changedUpdate(DocumentEvent e) {
            if (!handleEmptyTextField(e)) {
                enableButton();
            }
        }

        // MODIFIES: this
        // EFFECTS: enables button
        private void enableButton() {
            if (!alreadyEnabled) {
                button.setEnabled(true);
            }
        }

        // MODIFIES: this
        // EFFECTS: enables button if text fields are not empty, return true
        //          else return false
        private boolean handleEmptyTextField(DocumentEvent e) {
            if (e.getDocument().getLength() <= 0) {
                button.setEnabled(false);
                alreadyEnabled = false;
                return true;
            }
            return false;
        }
    }

    //This method is required by ListSelectionListener
    // MODIFIES: this
    // EFFECTS: enables the removeButton if there is movie in the watchlist
    //          else disable the removeButton
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == false) {

            if (list.getSelectedIndex() == -1) {
                removeButton.setEnabled(false);

            } else {
                removeButton.setEnabled(true);
            }
        }
    }

    //inspired by ListDemo.java
    // MODIFIES: this
    // EFFECTS: Create the frame, adds all components to GUI and show it
    //          changes the frame Icon to custom image - visual component
    public static void createAndShowGUI() {
        //Create and set up the window
        JFrame frame = new JFrame("WatchList App");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(656,690);
        frame.setResizable(false);

        //Create and set up the content pane
        JComponent newContentPane = new GUI();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        // changes the frame Icon to a custom image clapperboard - visual component
        ImageIcon image1 = new
                ImageIcon("C:\\Users\\mike\\Desktop\\CPSC 210\\project_s3x1x\\data\\clapperboard.png");
        frame.setIconImage(image1.getImage());
        frame.setVisible(true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                for (Event next : EventLog.getInstance()) {
                    System.out.println(next.toString() + "\n");
                }

                System.exit(0);
            }
        });
    }

}

