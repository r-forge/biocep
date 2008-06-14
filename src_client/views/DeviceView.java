package views;

import graphics.rmi.JGDPanelPop;

import java.awt.Component;

import javax.swing.Icon;

public class DeviceView extends DynamicView {
	JGDPanelPop _panel;

	public DeviceView(String title, Icon icon, Component component, int id) {
		super(title, icon, component, id);
	}

	public JGDPanelPop getPanel() {
		return _panel;
	}

	public void setPanel(JGDPanelPop panel) {
		this._panel = panel;
	}

}