package simpleview;

import java.sql.*;

import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "SqliteJDBC not found. Perhaps "
					+ "you need to set the build path?",
					"Library Missing", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
			
		Connection conn = null;
		try {
			conn = DriverManager.getConnection("jdbc:sqlite:mush.db");
		} catch(SQLException e) {
			JOptionPane.showMessageDialog(null,
					"Could not connect to DBMS. Maybe DBMS isn't running?",
					"Could Not Connect", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		Frame frame = new Frame(conn);
		frame.pack();
		frame.setVisible(true);
	}
}