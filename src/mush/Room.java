package mush;

import java.util.ArrayList;
import java.util.List;

public class Room {
	private int roomId;
	private String description;
	private ArrayList<String> users;
	private ArrayList<String> actions;
	private String creator;

	public Room(int roomId, String description, String creator, List<String> users, List<String> actions) {
		this.roomId = roomId;
		this.description = description;
		this.creator = creator;
		this.users = new ArrayList<String>(users);
		this.actions = new ArrayList<String>(actions);
	}

	public int getId() {
		return roomId;
	}

	public String getDescription() {
		return description;
	}

	public String getCreator() {
		return creator;
	}

	public int getUserCount() {
		return users.size();
	}

	public String getUser(int index) {
		return users.get(index);
	}

	public int getActionCount() {
		return actions.size();
	}

	public String getAction(int index) {
		return actions.get(index);
	}
}
