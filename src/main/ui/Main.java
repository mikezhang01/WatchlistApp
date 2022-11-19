package ui;

//inspired by ListDemo.java from https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html
public class Main {
    public static void main(String[] args) {
        // new WatchListApp();

        //Schedule a job for the event-dispatching thread:
        //creating and showing watchlist app's GUI.
        GUI gui = new GUI();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                gui.createAndShowGUI();
            }
        });

    }
}
