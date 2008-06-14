package views;

import java.awt.Component;

import javax.swing.Icon;

import net.infonode.docking.View;

public class DynamicView extends View {
	private int id;

	public DynamicView(String title, Icon icon, Component component, int id) {
		super(title, icon, component);
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
