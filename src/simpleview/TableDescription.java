package simpleview;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableDescription {
	public static TableDescription compute(String name, Statement stmt) throws SQLException {
		String sql = "SELECT sql FROM sqlite_master "
			+ "WHERE type = 'table' AND name = '" + name + "';";
		ResultSet result = stmt.executeQuery(sql);
		ArrayList<ColumnDescription> cols = new ArrayList<ColumnDescription>();
		try {
			sql = result.getString(1);
			result.close();
			
			int openParen = sql.indexOf('(');
			int closeParen = sql.lastIndexOf(')');
			if (openParen < 0 || closeParen < 0) {
				throw new RuntimeException("Could not find parentheses in SQL");
			}
			String[] toks = sql.substring(openParen + 1, closeParen).split(",");
			Pattern word = Pattern.compile("([^\\s(]+)");
			int parens = 0;
			for (String token : sql.substring(openParen + 1, closeParen).split(",")) {
				if (parens == 0) {
					Matcher m = word.matcher(token);
					if (m.find()) {
						cols.add(new ColumnDescription(m.group()));
					}
				}
				parens += count('(', token) - count(')', token);
			}
		} catch(SQLException e) {
			try { result.close(); } catch(SQLException e2) { }
			throw e;
		}
		return new TableDescription(name, cols);
	}
	
	private static int count(char needle, String haystack) {
		int pos = haystack.indexOf(needle);
		int ret = 0;
		while(pos >= 0) {
			ret++;
			pos = haystack.indexOf(needle, pos + 1);
		}
		return ret;
	}

	private String name;
	private ArrayList<ColumnDescription> columns;

	public TableDescription(String name, ArrayList<ColumnDescription> columns) {
		this.name = name;
		this.columns = columns;
	}

	public String getTableName() {
		return name;
	}

	public int getColumnCount() {
		return columns.size();
	}

	public String getColumnName(int index) {
		return columns.get(index).getName();
	}
}
