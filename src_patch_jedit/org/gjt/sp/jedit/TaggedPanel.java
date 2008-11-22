package org.gjt.sp.jedit;

import java.awt.LayoutManager;
import java.util.HashMap;

import javax.swing.JPanel;

public class TaggedPanel extends JPanel {
	HashMap<String, Object> properties = new HashMap<String, Object>();

	public TaggedPanel(boolean isDoubleBuffered) {
		super(isDoubleBuffered);
	}

	public TaggedPanel(LayoutManager layout, boolean isDoubleBuffered) {
		super(layout, isDoubleBuffered);
	}

	public TaggedPanel(LayoutManager layout) {
		super(layout);
	}

	public TaggedPanel() {
		super();
	}

	Object getProperty(String key) {
		return properties.get(key);
	}

	void setProperty(String key, Object value) {
		properties.put(key, value);
	}
}