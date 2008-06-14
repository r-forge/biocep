package views;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public 	 class LogView extends DynamicView {
	JTextArea _area;

	public LogView(String title, Icon icon, int id) {
		super(title, icon, new JPanel(), id);
		((JPanel) getComponent()).setLayout(new BorderLayout());
		_area = new JTextArea();
		((JPanel) getComponent()).add(_area);
	}

	public JTextArea getArea() {
		return _area;
	}
}