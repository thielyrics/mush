package simpleview;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

class DataTable extends JTable {
	private Connection sqlConnection;
	private Statement sqlStatement;

	public DataTable(Connection sqlConnection) {
		this.sqlConnection = sqlConnection;
		this.sqlStatement = null;
	}
	
	public DataModel getDataModel() {
		Object ret = getModel();
		return ret instanceof DataModel ? (DataModel) ret : null;
	}
	
	public void viewSql(String query) throws SQLException {
		Statement stmt = getStatement();
		ResultSet data;
		try {
			data = stmt.executeQuery(query);
		} catch(SQLException e) {
			throw e;
		}
		
		TableDescription desc;
		try {
			ResultSetMetaData meta = data.getMetaData();
			ArrayList<ColumnDescription> c = new ArrayList<ColumnDescription>();
			int colCount = meta.getColumnCount();
			for(int i = 1; i <= colCount; i++) {
				c.add(new ColumnDescription(meta.getColumnName(i)));
			}
			desc = new TableDescription(query, c);
		} catch(SQLException e) {
			System.err.println("Error finding info about result");
			e.printStackTrace();
			setModel(new DefaultTableModel());
			try { data.close(); } catch(SQLException e2) { }
			return;
		}
		
		loadResultSet(desc, data);
	}

	public void viewTable(String tableName) {
		if(tableName == null || tableName.equals("")) {
			 setModel(new DefaultTableModel());
			 return;
		}
		Statement stmt = getStatement();

		TableDescription desc;
		try {
			desc = TableDescription.compute(tableName, stmt);
		} catch(SQLException e) {
			System.err.println("Could not retrieve meta-information about table");
			e.printStackTrace();
			setModel(new DefaultTableModel());
			return;
		}

		int cols = desc.getColumnCount();
		StringBuilder sql = new StringBuilder("select ");
		for(int i = 0; i < cols; i++) {
			if(i > 0) sql.append(", ");
			sql.append(desc.getColumnName(i));
		}
		sql.append(" from " + tableName);
		
		try {
			ResultSet data = stmt.executeQuery(sql.toString());
			loadResultSet(desc, data);
		} catch(SQLException e) {
			System.err.println("Error retrieving table contents");
			e.printStackTrace();
			setModel(new DefaultTableModel());
		}
	}
	
	private Statement getStatement() {
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
		if(stmt == null) setModel(new DefaultTableModel());
		return stmt;
	}
	
	private void loadResultSet(TableDescription desc, ResultSet data) {
		String[] cols = new String[desc.getColumnCount()];
		for(int i = 0; i < cols.length; i++) {
			cols[i] = desc.getColumnName(i);
		}
		
		ResultSetMetaData meta = null;
		try {
			meta = data.getMetaData();
		} catch(SQLException e) { }
		ArrayList<Object[]> rows = new ArrayList<Object[]>();
		try {
			while(data.next()) {
				Object[] row = new Object[cols.length];
				for(int i = 0; i < cols.length; i++) {
					int type = meta == null ? Types.VARCHAR : meta.getColumnType(i + 1);
					if(type == Types.INTEGER) {
						row[i] = Integer.valueOf(data.getInt(i + 1));
					} else {
						row[i] = data.getString(i + 1);
					}
				}
				rows.add(row);
			}
		} catch(SQLException e) {
			System.err.println("Error while retrieving table data");
			e.printStackTrace();
		}
		try { data.close(); } catch(SQLException e2) { }
		setModel(new DataModel(desc, rows));
	}
}