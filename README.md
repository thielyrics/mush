In this assignment, we'll explore how you can interface with a database in Java using JDBC, the standard API for database access using Java. Its classes and interfaces are defined in the java.sql package. Most prominent DBMSs have a database driver that is compatible with JDBC; we'll be using SQLite for our DBMS, and we'll use SQLiteJDBC for our JDBC database driver.

    Setting up the assignment with Eclipse
    Sample Java program using SQLiteJDBC
    SQLite documentation
    SQLite JDBC home page 

We'll practice using JDBC by completing a MUSH. A MUSH is a multi-user game that among other things allows its participants to explore areas and to build new areas into the game. I have created the user interface for a very rudimentary MUSH, which you can execute in the mush.Frame class (downloaded in mush.zip as described on the setup page); however, right now it doesn't do much, since it doesn't yet have the pieces that communicate with the database.

The MUSH code uses mush.DbInterface to retrieve information about the shared data structures, but right now DbInterface is little more than a set of stubs. Your assignment is to set up SQLite to store this data structure and to complete the mush.DbInterface class so that it retrieves and updates information through SimpleDB. After doing this, you'll be able to run multiple instances of the MUSH application in different windows on your computer, and you'll be able to see how the environment changes for one user based on the actions of another user.

You will want to create three tables to support this, as described in the schemas below. Of the attributes listed, userloc, roomid, sourceid, and destid are integers, while userid, creator, description, and action are strings.

    users(userid, userloc)
    rooms(roomid, creator, description)
    passages(sourceid, action, destid) 

To create the tables, you can simply start the SQLite application (as in Assignment 1) and issue the appropriate SQL. As with Assignment 1, I recommend actually writing the SQL into a text file and then using copy-and-paste to issue the query to SQLite.

DbInterface is the only Java class you should modify for this assignment. Its methods should be defined as follows.

public DbInterface()

    Connects to SimpleDB so that SQL statements can be issued by other methods. I've already implemented this.
public void close()

    Closes the connection to SimpleDB. No methods will work after this is called. I've already implemented this.
public boolean userExists(String userId)

    Returns true if there is a registered user with the provided user ID.
public boolean createUser(String userId)

    Creates a new user with the given user ID, returning true on success. The method returns false only if there are problems communicating with SimpleDB.)
public Room getCurrentRoom(String userId)

    Constructs and returns a Room object representing all the information that can be retrieved from the database about the room that the user currently occupies.
public boolean doAction(String userId, String action)

    Modifies the user's location based on the indicated action. Returns true if successful; false may result if, say, the action is not an option in the current room.
public boolean createRoom(String userId, String action, String description)

    Creates a new room with the given description, which can be reached from the user's current room using the provided action. As created initially, there are no actions in this new room that allow one to leave it. Returns true on success; possible reasons for failure include the user occupying an invalid room and database communication problems.

    One of the problems you face here is allocating a room identifier that distinguishes the room from all others in the database. You can do this by repeatedly generating random numbers between 0 and 9,999 until you find a room ID that isn't being used.
public boolean createPassage(String userId, String action, int dst)

    Creates a passage from the user's current room to another room using the indicated action identifier. Returns true on success; possible reasons for failure include the destination room ID being invalid, the user occupying an invalid room, and database communication problems.