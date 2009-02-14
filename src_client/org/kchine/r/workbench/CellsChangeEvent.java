package org.kchine.r.workbench;

import java.util.HashSet;

import org.kchine.r.server.spreadsheet.CellRange;
import org.kchine.r.workbench.macros.Macro;


public class CellsChangeEvent {
	
	String spreadsheetName;
	CellRange range;
	
	int row,col,height,width;
	String originator;
	RGui rgui;
	HashSet<Macro> forbiddenMacros=null;
	
	
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
	
	public HashSet<Macro> getFrobiddenMacros() {
		return forbiddenMacros;
	}
	
	public void setFrobiddenMacros(HashSet<Macro> forbiddenMacros) {
		this.forbiddenMacros=forbiddenMacros;
	}
	
	
}
