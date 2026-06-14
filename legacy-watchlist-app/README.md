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

## Phase 4: Task 2
Sample event log:

Fri Dec 02 01:59:37 PST 2022\
Tenet added to the watchlist.

Fri Dec 02 02:00:21 PST 2022\
Nope added to the watchlist.

Fri Dec 02 02:01:00 PST 2022\
Avengers added to the watchlist.

Fri Dec 02 02:01:17 PST 2022\
Nope is removed from the watchlist.

Fri Dec 02 02:01:24 PST 2022\
Tenet is marked as watched.

Fri Dec 02 02:01:38 PST 2022\
Tenet is rated 5 out of 5.

Fri Dec 02 02:01:44 PST 2022\
Watchlist sorted in alphabetical order.

Fri Dec 02 02:01:49 PST 2022\
Watchlist Saved to file.

When loading watchlist from file, the event log will log adding all loaded movies to the watchlist,
along with logging the score of each movie and also log if the movie has been watched. 
This is because Json reader is 'reconstructing' each movie when parsing from Json object to movie and watchlist. 
Therefore, the methods of adding movies to the watchlist, as well as setting the score and watched for the movies
will be logged. The movies are logged in the right order; however, the log display might show a movie is
rated before it is added to the watchlist. This randomness is due to parsing from Json to movie/watchlist, but all
the data are stored and displayed properly on the GUI. 

Here is an example of the event log when loading watchlist from a saved file, continuing
from the example above:

Fri Dec 02 02:16:05 PST 2022\
Avengers is rated 0 out of 5.

Fri Dec 02 02:16:05 PST 2022\
Avengers added to the watchlist.

Fri Dec 02 02:16:05 PST 2022\
Tenet is rated 5 out of 5.

Fri Dec 02 02:16:05 PST 2022\
Tenet is marked as watched.

Fri Dec 02 02:16:05 PST 2022\
Tenet added to the watchlist.

## Phase 4: Task 3

Refactorings I would do to improve my design:

- The first one I noticed is a small code duplication in the GUI Class where each time I add to 
watchlist I would reuse the same lines of codes where I get the Movie's title, year, duration, and description
as a string then store it as a variable to be printed out. This duplication can be seen in the createWatchList() method, 
and in the SortListener, AddListener, and LoadListener classes. To avoid this duplicate code use, I can create a new method
in GUI that does this and call on that method everytime. Another way is to add a method in Movie class that
stores and returns the string I need for each movie and call that method in GUI instead.
- Another refactoring I would do is to add exception handling. I have set the year and duration as int
in the Movie class. When adding a movie if the input for these fields does not match the type it would 
raise an exception which is not handled. The program continues to run but the exception is still displayed.
To fix this, I can check the input type from the user and throw exceptions if it does not match
the required type for the field.
- When looking into the GUI class, it contains many nested classes that are listeners which perform their action
when the corresponding buttons are pressed. Having many nested classes within GUI goes against the
Single Responsibility Principle. To increase cohesion, I could make each nested class as their own class outside GUI, 
and call on method within each class in GUI when the corresponding button is pressed. Since many of the nested classes
call on methods in GUI to perform their action, I could put the method in the nested class when
extracting from GUI. For example, LoadListener and SaveListener call loadWatchList and saveWatchList respectively.
However, I would also need to make sure the nested classes extracted have access to the required fields passed in 
as parameters for the constructor or the methods. This is because many of the nested classes requires access to the fields of GUI, mainly the JList variable
and the listModel. 
- Another refactoring I would do is to try and improve readability of the GUI, specifically about all the
buttons, panels, labels, and textboxes. I have many small methods that does one step in the creation
of the GUI, for example method that creates the buttons, method that creates the textboxes, method that creates
the labels, and methods that added the corresponding buttons and labels to each panel. This makes the beginning parts
of the code in GUI a little difficult to read as many methods are called in the constructor to construct the GUI display. 
One way I am thinking of to improve this design is to group all these methods by panel. Within the frame there are
3 panels, one for displaying the watchlist, one for adding movie to watchlist, and one at the bottom that contains most
of the buttons. I can group all the methods related to displaying the watchlist, and create a new method that calls
upon these smaller methods I already made. Similar, I can group all the methods related to adding movie, which
includes all the textboxes for input, labels, and the add button, then create another method that calls on them. 
The same can be done with the buttons panel on the bottom. This way, in the constructor I would only need
to call the 3 methods for the 3 panels. These 3 methods would call on the smaller methods that they need. This
way the codes are more organized and easier to read.

## Resources

- GUI's ListSelectionListener is inspired by ListDemo.java from https://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html
- JSON Reader, Writer, and Tests are inspired by JsonSerializationDemo  provided
- Non-Copyrighted images used in GUI are downloaded from https://pixabay.com/
  - popcornF.png is downloaded at https://pixabay.com/vectors/popcorn-food-snack-movie-cinema-7525406/
  - movie.png is downloaded at https://pixabay.com/vectors/camera-video-icon-video-camera-2008489/
  - clapperboard.png is downloaded at https://pixabay.com/vectors/clapperboard-film-movie-cut-video-311792/