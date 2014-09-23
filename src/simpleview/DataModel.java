package simpleview;

import java.sql.Types;
import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

class DataModel extends AbstractTableModel {
	private TableDescription desc;
	private ArrayList<Object[]> data;

	public DataModel(TableDescription desc, ArrayList<Object[]> data) {
		this.desc = desc;
		this.data = data;
	}
	
	public String getName() {
		return desc.getTableName();
	}

	public int getColumnCount() {
		return desc.getColumnCount();
	}

	public int getRowCount() {
		return data.size();
	}
	
	public String getColumnName(int column) {
		return desc.getColumnName(column);
	}
	
	public Class<?> getColumnClass(int column) {
		return Object.class;
		/*
		switch(desc.getColumnType(column)) {
		case Types.INTEGER: return Integer.class;
		case Types.VARCHAR: return String.class;
		default: return Object.class;
		}
		*/
	}

	public Object getValueAt(int row, int column) {
		return data.get(row)[column];
	}
}