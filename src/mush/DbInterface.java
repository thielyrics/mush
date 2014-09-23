package mush;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DbInterface {
	private Connection conn;
	
	
	public DbInterface() {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			System.err.println("SqliteJDBC library not found. "
					+ "Perhaps you need to set the build path?");
			System.exit(-1);
		}
			
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:mush.db");
		} catch (SQLException e) {
			System.err.println("Could not connect to DBMS. Maybe DBMS isn't running?");
			System.exit(-1);
		}
	}

	public void close() {
		try {
			conn.close();
		} catch (SQLException e) {
			System.err.println("Error closing connection");
		}
	}
	
	
	public boolean userExists(String userId) {
		try{
			PreparedStatement statement = conn.prepareStatement("SELECT userid FROM users WHERE userid = '"+ userId + "'" );		     
			boolean empty = true;
			ResultSet rows;		
			rows = statement.executeQuery();		
			while(rows.next()){
				empty = false;
			}
			if(empty){
				return false;
			}
			else{
				return true;
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean createUser(String userId) {
		try{
			PreparedStatement statement = conn.prepareStatement("INSERT INTO users(userid, userloc) VALUES (?, ?)");			
			statement.setString(1, userId);
			statement.setInt(2, 1);
			statement.executeUpdate();			
		}
		catch(Exception e){	
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Room getCurrentRoom(String userId) {	
		ArrayList<String> userlist = new ArrayList<String>();
		ArrayList<String> actionlist = new ArrayList<String>();
		try{			
			PreparedStatement location = conn.prepareStatement("SELECT userloc FROM users WHERE userid =  '"+ userId + "'" );
			ResultSet rows = location.executeQuery();
			int userloc = rows.getInt(1);
			PreparedStatement statement = conn.prepareStatement("SELECT roomId, description, creator FROM rooms WHERE roomId" +
					" = '"+ userloc+"' ");
			PreparedStatement users = conn.prepareStatement("SELECT userid FROM users WHERE userloc =  '"+ userloc+"'");
			PreparedStatement actions = conn.prepareStatement("SELECT action FROM passages WHERE sourceid =  '"+ userloc+"'");
			ResultSet allUsers = users.executeQuery();
			ResultSet allActions = actions.executeQuery();
			while(allUsers.next()){
				if(allUsers.getString(1).equals(userId)){}
				else{
					userlist.add(allUsers.getString(1));
				}
			}
			while(allActions.next()){
				actionlist.add(allActions.getString(1));
			}
			
			ResultSet room = statement.executeQuery();			
			System.out.println(room.getInt(1) + " " + room.getString(2) + " " + room.getString(3));
			return new Room(room.getInt(1), room.getString(2), room.getString(3), userlist, actionlist);						
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return new Room(-1, "You are dead.", "none", new ArrayList<String>(), new ArrayList<String>());
	}

	public boolean doAction(String userId, String action) {
		try{
			PreparedStatement destination = conn.prepareStatement("SELECT destid FROM passages, users WHERE action = '"+ action +"'");
			ResultSet rows = destination.executeQuery();
			PreparedStatement statement = conn.prepareStatement("UPDATE users SET userloc = '"+ rows.getInt(1) +"'" +
					" WHERE userid = '"+ userId + "'");			
			statement.executeUpdate();
			return true;
		}
		catch(Exception e){	
			e.printStackTrace();
		}		
		return false;
	}

	public boolean createRoom(String userId, String action, String description) {
		int min = 1;
		int max = 9999;		
		int randoId = min + (int)(Math.random()*max);
		boolean noRoom = false;  //checks to see if the randomly assigned roomid already exists.
		try{
			while(!noRoom){
				PreparedStatement statement = conn.prepareStatement("SELECT roomid FROM rooms WHERE roomid = '"+ randoId + "'" );		     
				boolean empty = true;
				ResultSet rows;		
				rows = statement.executeQuery();		
				while(rows.next()){
					empty = false;
					randoId = min + (int)(Math.random()*max);
					System.out.println("RoomID already used...selecting a new ID");
				}
				if(empty){
					noRoom = true;						
					int loc = getUserLoc(userId);
					PreparedStatement newpassage = conn.prepareStatement("INSERT INTO passages(sourceid, action, destid) VALUES(?, ?, ?)");
					newpassage.setInt(1, loc);
					newpassage.setString(2, action);
					newpassage.setInt(3, randoId);
					newpassage.executeUpdate();
					System.out.println("Created room " + randoId +  "which can be accessed from room " + loc);
					PreparedStatement putUser = conn.prepareStatement("UPDATE users SET userloc = '"+ randoId +"'" +
							" WHERE userid = '"+ userId + "'");
					putUser.executeUpdate();			
			}
		}
			PreparedStatement statement = conn.prepareStatement("INSERT INTO rooms(roomid, creator, description) VALUES(?, ?, ?)");
			statement.setInt(1, randoId);
			statement.setString(2, userId);
			statement.setString(3, description);
			statement.executeUpdate();					
			return true;
	}
		catch(Exception e){
			e.printStackTrace();
		}	
		return false;
	}

	public boolean createPassage(String userId, String action, int dst) { //still need to check if user is in an invalid room, such as dead
		try{
			boolean exist = false;
			PreparedStatement checkId = conn.prepareStatement("SELECT roomid FROM rooms WHERE roomid = '"+ dst + "'");
			ResultSet rightrows = checkId.executeQuery();
			while(rightrows.next()){
				exist = true;
			}
			if(exist){				
				int loc = getUserLoc(userId);
				if(loc == -1 ){
					return false;
				}
				else{
					PreparedStatement statement = conn.prepareStatement("INSERT INTO passages(sourceid, action, destid) VALUES(?, ?, ?)");
					statement.setInt(1, loc);
					statement.setString(2, action);
					statement.setInt(3, dst);
					statement.executeUpdate();
					System.out.println(loc + action + dst);
					return true;	
				}
			}
		}
		catch(Exception e){
		}
		return false;
	}
	
	public int getUserLoc(String userId){
		try{
			PreparedStatement location = conn.prepareStatement("SELECT userloc FROM users WHERE userid =  '"+ userId + "'" );
			ResultSet rows = location.executeQuery();
			int loc = rows.getInt(1);
			return loc;
		}
		catch(Exception e){}
		return 0;		
	}
}