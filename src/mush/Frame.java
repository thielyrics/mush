package mush;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.HyperlinkEvent.EventType;

public class Frame extends JFrame implements HyperlinkListener, WindowListener {
	private DbInterface db;
	private String userId;

	private JEditorPane description;

	private Frame(DbInterface db) {
		this.db = db;

		setTitle("SimpleMUSH");
		addWindowListener(this);

		description = new JEditorPane("text/html", "");
		description.setPreferredSize(new Dimension(640, 480));
		description.addHyperlinkListener(this);
		description.setEditable(false);

		getContentPane().add(new JScrollPane(description));
	}

	private void loadRoom() {
		if(userId == null) return;

		Room room = db.getCurrentRoom(userId);

		StringBuilder data = new StringBuilder(room.getDescription());
		if(room.getUserCount() > 0) {
			data.append("\n\n<p>Also here:");
			boolean first = true;
			for(int i = 0, n = room.getUserCount(); i < n; i++) {
				String user = room.getUser(i);
				if(!first) data.append(", ");
				data.append(user);
				first = false;
			}
			data.append("</p>");
		}
		if(room.getActionCount() > 0) {
			data.append("\n\n<p>Choices:");
			for(int i = 0, n = room.getActionCount(); i < n; i++) {
				String action = room.getAction(i);
				if(i > 0) data.append(", ");
				data.append("<a href=\"do:" + action + "\">" + action + "</a>");
			}
			data.append("</p>");
		}
		data.append("\n\n<p>[<a href=\"refresh\">Refresh</a>] "
				+ "[<a href=\"build\">Build New Room</a>] "
				+ "[<a href=\"connect\">Connect Rooms</a>] "
				+ "[<a href=\"quit\">Quit</a>]</p>");
		data.append("\n\n<p><small>ID: " + room.getId() + "; creator: " + room.getCreator() + "</small></p>");
		description.setText(data.toString());
	}

	public void hyperlinkUpdate(HyperlinkEvent e) {
		if(!e.getEventType().equals(EventType.ACTIVATED)) return;
		String cmd = e.getDescription();
		if(cmd.startsWith("do:")) {
			db.doAction(userId, cmd.substring(3));
			loadRoom();
		} else if(cmd.equals("refresh")) {
			loadRoom();
		} else if(cmd.equals("build")) {
			String action = getAction();
			if(action == null) return;

			String desc = JOptionPane.showInputDialog(this, "What is the new room's description?", "Input Description", JOptionPane.QUESTION_MESSAGE);
			if(desc == null) return;

			if(!db.createRoom(userId, action, desc)) {
				JOptionPane.showMessageDialog(this, "The room could not be created.", "Build Failed", JOptionPane.ERROR_MESSAGE);
			}
			loadRoom();
		} else if(cmd.equals("connect")) {
			String action = getAction();
			if(action == null) return;

			String roomId;
			while(true) {
				roomId = JOptionPane.showInputDialog(this, "What is the identifier of the connected room?", "Input Room ID", JOptionPane.QUESTION_MESSAGE);
				if(action == null || roomId == null) return;
				if(roomId.matches("^(0|[1-9][0-9]*)$")) break;
				JOptionPane.showMessageDialog(this, "Room ID must be a positive integer.", "Room ID Invalid", JOptionPane.INFORMATION_MESSAGE);
			}

			if(!db.createPassage(userId, action, Integer.parseInt(roomId))) {
				JOptionPane.showMessageDialog(this, "The passage could not be created.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
			}
			loadRoom();
		} else if(cmd.equals("quit")) {
			db.close();
			System.exit(0);
		}
	}

	private String getAction() {
		while(true) {
			String action = JOptionPane.showInputDialog(this, "What action moves into new room?", "Input Action", JOptionPane.QUESTION_MESSAGE);
			if(action == null) return null;
			if(action.matches("^[A-Z][a-z]*( [A-Z][a-z]*)*$")) return action;
			JOptionPane.showMessageDialog(this, "Action must be one or more capitalized words.", "Action Invalid", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void windowActivated(WindowEvent e) { }

	public void windowClosed(WindowEvent e) { }

	public void windowClosing(WindowEvent e) {
		db.close();
		System.exit(0);
	}

	public void windowDeactivated(WindowEvent e) { }

	public void windowDeiconified(WindowEvent e) { }

	public void windowIconified(WindowEvent e) { }

	public void windowOpened(WindowEvent e) { }

	public static void main(String[] args) {
		DbInterface db = new DbInterface();
		Frame frame = new Frame(db);
		frame.pack();
		frame.setVisible(true);

		String userId = JOptionPane.showInputDialog(frame, "What is your name?", "Log In", JOptionPane.QUESTION_MESSAGE);
		if(userId == null || userId.equals("")) {
			System.exit(0);
			return;
		}

		if(!db.userExists(userId)) {
			int input = JOptionPane.showConfirmDialog(frame, "No such user exists. To continue, you'll need to register. Is this OK?",
					"New User?", JOptionPane.YES_NO_OPTION);
			if(input != JOptionPane.YES_OPTION) {
				System.exit(0);
				return;
			}
			if(!db.createUser(userId)) {
				JOptionPane.showMessageDialog(null, "Could not create user; program aborted.", "User Creation Failed",
						JOptionPane.ERROR_MESSAGE);
				System.exit(0);
				return;
			}
		}

		frame.userId = userId;
		frame.loadRoom();
	}
}
