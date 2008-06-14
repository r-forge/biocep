package views;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public  class ServerLogView extends DynamicView {
	JTextArea _area;
	JScrollPane _scrollPane;
	RemoteLogListenerImpl _rll;

	public ServerLogView(String title, Icon icon, int id) {
		super(title, icon, new JPanel(), id);
		_area = new JTextArea();
		_scrollPane = new JScrollPane(_area);

		((JPanel) getComponent()).setLayout(new BorderLayout());
		((JPanel) getComponent()).add(_scrollPane);
		try {
			_rll = new RemoteLogListenerImpl(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JTextArea getArea() {
		return _area;
	}

	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	public RemoteLogListenerImpl getRemoteLogListenerImpl() {
		return _rll;
	}

	public void recreateRemoteLogListenerImpl() {
		try {
			_rll = new RemoteLogListenerImpl(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

