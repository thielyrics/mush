package simpleview;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

class ExecutePanel extends JPanel implements ActionListener {
	private ArrayList<ActionListener> listeners;
	private ArrayList<String> backHistory;
	private ArrayList<String> forwardHistory;
	private JButton back;
	private JButton forward;
	private JButton execute;
	private JTextPane editor;

	public ExecutePanel() {
		super(new BorderLayout());

		listeners = new ArrayList<ActionListener>();
		backHistory = new ArrayList<String>();
		forwardHistory = new ArrayList<String>();
		back = createButton("<");
		forward = createButton(">");
		execute = createButton("Execute");
		editor = new JTextPane();

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
		buttons.add(back);
		buttons.add(forward);
		buttons.add(execute);
		updateButtons();

		JScrollPane editorPane = new JScrollPane(editor,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(buttons, BorderLayout.NORTH);
		add(editorPane, BorderLayout.CENTER);
	}

	private JButton createButton(String label) {
		JButton ret = new JButton(label);
		ret.addActionListener(this);
		return ret;
	}

	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		listeners.remove(l);
	}

	public String getQueryString() {
		return editor.getText();
	}

	public void addToHistory(String query) {
		String last = backHistory.isEmpty() ? null : backHistory.get(backHistory.size() - 1);
		if(!query.equals(last)) {
			backHistory.add(getQueryString());
			forwardHistory.clear();
			updateButtons();
		}
	}

	public void actionPerformed(ActionEvent e) {
		Object src = e.getSource();
		if(src == back) {
			scrollInto(backHistory, forwardHistory);
		} else if(src == forward) {
			scrollInto(forwardHistory, backHistory);
		} else if(src == execute || src == editor) {
			ActionEvent event = new ActionEvent(this, 0, "execute");
			for(int i = 0, n = listeners.size(); i < n; i++) {
				listeners.get(i).actionPerformed(event);
			}
		}
	}

	private void scrollInto(ArrayList<String> src, ArrayList<String> dst) {
		if(src.isEmpty()) return;

		String query = src.remove(src.size() - 1);
		dst.add(query);
		if(query.equals(editor.getText())) {
			if(src.isEmpty()) return;
			query = src.remove(src.size() - 1);
			dst.add(query);
		}
		editor.setText(query);
		updateButtons();
	}

	private void updateButtons() {
		back.setEnabled(!backHistory.isEmpty());
		forward.setEnabled(!forwardHistory.isEmpty());
	}
}
