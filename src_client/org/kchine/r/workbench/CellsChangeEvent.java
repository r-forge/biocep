package org.kchine.r.workbench;

import org.kchine.r.server.spreadsheet.CellRange;


public class CellsChangeEvent {
	
	String spreadsheetName;
	CellRange range;
	
	int row,col,height,width;
	String originator;
	RGui rgui;
	
	public CellsChangeEvent(String name, CellRange range, String originator, RGui rgui) {
		super();
		this.spreadsheetName = name;
		this.range = range;
		this.originator = originator;
		this.rgui = rgui;		
	}
	
	public String getSpreadsheetName() {
		return spreadsheetName;
	}
	public CellRange getRange() {
		return range;
	}
	public String getOriginator() {
		return originator;
	}
	public RGui getRGui() {
		return rgui;
	}
}
