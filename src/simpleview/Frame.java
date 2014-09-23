package simpleview;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

class Frame extends JFrame
		implements WindowListener, ListSelectionListener, ActionListener {
	private Connection connection;
	private JButton tablesRefresh;
	private TableList tables;
	private ExecutePanel execute;
	private DataTable dataView;

	public Frame(Connection connection) {
		this.connection = connection;

		tablesRefresh = new JButton("Refresh");
		tablesRefresh.addActionListener(this);
		tables = new TableList(connection);
		dataView = new DataTable(connection);
		execute = new ExecutePanel();

		JScrollPane tableScroll = new JScrollPane(tables);
		JPanel tablePane = new JPanel(new BorderLayout());
		JPanel tableButtons = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		tableButtons.add(tablesRefresh);
		tablePane.add(tableButtons, BorderLayout.NORTH);
		tablePane.add(tableScroll, BorderLayout.CENTER);
		JScrollPane dataScroll = new JScrollPane(dataView);
		JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				tablePane, execute);
		JSplitPane tableSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				leftSplit, dataScroll);

		Container contents = getContentPane();
		contents.add(tableSplit);

		setTitle("Simple View");
		tables.addListSelectionListener(this);
		execute.addActionListener(this);
		addWindowListener(this);
	}

	public void windowActivated(WindowEvent e) { }

	public void windowClosed(WindowEvent e) { }

	public void windowClosing(WindowEvent e) {
		try {
			connection.close();
		} catch(SQLException e2) {
			System.err.println("Could not close connection to database");
		}
		System.exit(0);
	}

	public void windowDeactivated(WindowEvent e) { }

	public void windowDeiconified(WindowEvent e) { }

	public void windowIconified(WindowEvent e) { }

	public void windowOpened(WindowEvent e) {
		tables.refresh();
	}

	public void valueChanged(ListSelectionEvent e) {
		String table = (String) tables.getSelectedValue();
		if(table == null) return;
		
		DataModel model = dataView.getDataModel();
		if(model == null || !table.equals(model.getName())) {
			dataView.viewTable(table);
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src == tablesRefresh) {
			tables.refresh();
		} else if(src == execute) {
			String query = execute.getQueryString().trim();
			System.err.println("query: " + query);
			try {
				if(query.length() >= 6 && query.substring(0, 6).equalsIgnoreCase("select")) {
					dataView.viewSql(query);
					tables.clearSelection();
				} else {
					Statement stmt = connection.createStatement();
					stmt.executeUpdate(query);
				}
				execute.addToHistory(query);
			} catch(Exception e2) {
				String msg = "SQL is not valid: ";
				msg += e2.getMessage() == null ? e2.getClass().getName() : e2.getMessage();
				JOptionPane.showMessageDialog(this, msg);
			}
		}
	}
}
