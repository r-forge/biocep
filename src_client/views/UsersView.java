package views;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.JPanel;


public  class UsersView  extends DynamicView {
	JList _list;
	public UsersView(String title, Icon icon, int id) {		
		super(title, icon, new JPanel(), id);		
		((JPanel) getComponent()).setLayout(new BorderLayout());
		_list = new JList();
		((JPanel) getComponent()).add(_list);
	}

	public JList getList() {
		return _list;
	}
}