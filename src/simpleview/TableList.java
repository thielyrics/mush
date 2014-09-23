package simpleview;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

class TableList extends JList {
	private Connection sqlConnection;
	private Statement sqlStatement;
	private DefaultListModel model;

	public TableList(Connection sqlConnection) {
		this.sqlConnection = sqlConnection;
		this.sqlStatement = null;
		this.model = new DefaultListModel();

		setModel(this.model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public void refresh() {
		Statement sql = sqlStatement;
		if(sql == null) {
			try {
				sql = sqlConnection.createStatement();
				sqlStatement = sql;
			} catch(SQLException e) {
				sql = null;
				System.err.println("Could not create SQL statement for refreshing table list");
			}
		}
		ArrayList<String> names = new ArrayList<String>();
		if(sql != null) {
			ResultSet results = null;
			try {
				results = sql.executeQuery("SELECT name FROM sqlite_master "
						+ "WHERE type='table' ORDER BY name;");
				while(results.next()) {
					names.add(results.getString(1));
				}
			} catch(SQLException e) {
				System.err.println("Error while executing table-refresh query");
				e.printStackTrace();
			}
			try {
				if(results != null) results.close();
			} catch(SQLException e) {
				System.err.println("Error while closing table-refresh query");
			}
		}

		Collections.sort(names);
		int i = 0;
		int j = 0;
		while(true) {
			if(i >= names.size()) {
				if(j < model.getSize()) model.removeRange(j, model.getSize() - 1);
				break;
			} else if(j >= model.getSize()) {
				model.add(j, names.get(i));
				i++;
				j++;
			} else {
				String a = names.get(i);
				Object b = model.get(j);
				if(a.equals(b)) {
					i++;
					j++;
				} else if(a.compareTo((String) b) > 0) {
					model.add(j, a);
					i++;
					j++;
				} else {
					model.remove(j);
				}
			}
		}
	}
}
