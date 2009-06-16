package org.kchine.r.workbench.spreadsheet;

import javax.swing.JPanel;

public class EmbeddedPanelDescription {
	String spreadsheetName;
	String range;
	JPanel panel;
	public EmbeddedPanelDescription(String spreadsheetName, String range, JPanel panel) {
		super();
		this.spreadsheetName = spreadsheetName;
		this.range = range;
		this.panel = panel;
	}
	public String getSpreadsheetName() {
		return spreadsheetName;
	}
	public void setSpreadsheetName(String spreadsheetName) {
		this.spreadsheetName = spreadsheetName;
	}
	public String getRange() {
		return range;
	}
	public void setRange(String range) {
		this.range = range;
	}
	public JPanel getPanel() {
		return panel;
	}
	public void setPanel(JPanel panel) {
		this.panel = panel;
	}	
	
}
