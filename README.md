# My Personal Project: Movie/TV Watchlist

## Personal Watchlist Application

This application will function similarly to a wishlist, but for movies and TV shows.
Users will have their own personal **watchlist**, where they will be able to add movies or shows they want to watch to the list.
Other **features** of the watchlist users can use include removing movies from the list, mark a movie as watched,
give a movie a score, and display the list based on user preference. 

Anyone who wants an application to keep track of movies/shows they want to watch can benefit from using this application.
The *functionality* of this application can also be applied to other things like books or video games.

Although many of the big streaming services have a watchlist function on their platform, there are still many limitations. Firstly, 
in order to access watchlist the user must be a subscriber and continues to do so if they wish to access their watchlist. Secondly, 
no streaming platform have all the movies/shows. If the user wants to watch a show that is not available on this platform then they will not be able
to add it to their watchlist. As someone who loves watching movies and TV shows, not having a watchlist I can access anytime and add any movies to can be
frustrating at times. This is why I decided to create this project. 

## User Stories:

- As a user, I want to be able to add a movie to the watchlist
- As a user, I want to be able to view my watchlist with all the movie titles
- As a user, I want to be able to remove a movie from my watchlist
- As a user, I want to be able to mark a movie as watched in my watchlist
- As a user, I want to be able to select a movie on my watchlist and give it a rating out of 5
- As a user, I want to be able to sort my watchlist alphabetically  
- As a user, I want to be able to save my watchlist to a file
- As a user, I want to be able to load a saved watchlist from file

## Instructions for Grader

- You can generate the first required event related to adding Xs to a Y by 
  using the panel on the right to add a movie to the watchlist. First enter the Title, Year, Duration,
  and Description of the movie into the text boxes. Then hit the Add button, a confirmation window
  will pop up, and you can see the added movie displayed in the watchlist on the left panel.
- You can generate the second required event related to removing Xs from a Y by selecting a movie
  from the watchlist that is displayed on the left panel, then click the Remove button. This wll
  remove the selected movie from the watchlist. 
- You can locate my visual component by looking at the top left of the window, where I have changed
  the frame Icon to a custom image clapperboard. 
- Additionally, I also have 2 more visual components. 
  The first one is the frame Icon for the Additional Info display window has also been changed to a 
  different image by me. This can be accessed by selecting a movie then click the Additional Info button. 
  Second one is everytime a movie is added (when the add button is clicked), a message will pop up that confirms
  it, and on that pop-up window I added a custom popcorn image. 
- You can save the state of my application by clicking the Save button at the bottom of the display. 
- You can reload the state of my application by clicking the Load button at the bottom of the display.
- All user stories from above have also been implemented in the GUI. To mark a movie as watched, select a
  movie from the watchlist then click the Mark as Watched button. To rate a movie out of 5, select a
  movie from the watchlist then click the Rate button. A new pop-up window will show up where the user can input
  a score. To sort the watchlist in alphabetical order, clicked the Sort button and the watchlist
  will be displayed alphabetically. 
- The Additional Info will display more information about a selected movie in a new window. This
  includes the description of the movie, if the movie has been watched, and the score of the movie. 
  To see these, select a movie from the watchlist then click the Additional Info button. 

## Resources

- GUI's ListSelectionListener is inspired by ListDemo.java from https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html
- JSON Reader, Writer, and Tests are inspired by JsonSerializationDemo  provided
- Non-Copyrighted images used in GUI are downloaded from https://pixabay.com/
  - popcornF.png is downloaded at https://pixabay.com/vectors/popcorn-food-snack-movie-cinema-7525406/
  - movie.png is downloaded at https://pixabay.com/vectors/camera-video-icon-video-camera-2008489/
  - clapperboard.png is downloaded at https://pixabay.com/vectors/clapperboard-film-movie-cut-video-311792/