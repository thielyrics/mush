package simpleview;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

class TableTable extends JTable {
	private Connection sqlConnection;
	private Statement sqlStatement;

	public TableTable(Connection sqlConnection) {
		this.sqlConnection = sqlConnection;
		this.sqlStatement = null;
	}

	public void refresh(String tableName) {
		Statement stmt = sqlStatement;
		if(stmt == null) {
			try {
				stmt = sqlConnection.createStatement();
				sqlStatement = stmt;
			} catch(SQLException e) {
				stmt = null;
				System.err.println("Could not create SQL statement for refreshing table list");
			}
		}
		if(stmt == null || tableName == null || tableName.equals("")) {
			setModel(new DefaultTableModel());
			return;
		}

		TableDescription desc;
		try {
			desc = TableDescription.compute(tableName, stmt);
		} catch(SQLException e) {
			System.err.println("Could not retrieve meta-information about table");
			setModel(new DefaultTableModel());
			return;
		}

		DefaultTableModel model = new DefaultTableModel();
		int cols = desc.getColumnCount();
		StringBuilder sql = new StringBuilder("select ");
		for(int i = 0; i < cols; i++) {
			if(i > 0) sql.append(", ");
			sql.append(desc.getColumnName(i));
			model.addColumn(desc.getColumnName(i));
		}
		sql.append(" from " + tableName);
		try {
			ResultSet rows = stmt.executeQuery(sql.toString());
			ResultSetMetaData meta = null;
			try {
				meta = rows.getMetaData();
			} catch(SQLException e) { }
			for(int row = 0; rows.next(); row++) {
				Object[] data = new Object[cols];
				for(int i = 0; i < cols; i++) {
					int type = meta == null ? Types.VARCHAR : meta.getColumnType(i);
					if(type == Types.INTEGER) {
						int k = rows.getInt(i);
						data[i] = Integer.valueOf(k);
					} else {
						data[i] = rows.getString(i);
					}
				}
				model.addRow(data);
			}
		} catch(SQLException e) {
			System.err.println("Error while retrieving table data");
		}
		setModel(model);
	}
}
