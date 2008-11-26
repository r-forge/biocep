package org.kchine.r.workbench.macros;

import java.awt.Color;

import model.ImportInfo;
import net.java.dev.jspreadsheet.CellRange;

import org.kchine.r.workbench.CellsChangeEvent;
import org.kchine.r.workbench.CellsChangeListener;

public class MacroCellsChangeListener implements CellsChangeListener {
	CellRange range;
	boolean enabled = true;
	MacroInterface m;
	Color color;
	String spreadsheetName;
	
	public MacroCellsChangeListener(String name, CellRange range, Color color,  MacroInterface m) {
		this.range = range;
		this.m=m;
		this.color=color;
		this.spreadsheetName=name;
	}
	
	public MacroCellsChangeListener(String name, String range, Color color, MacroInterface m) throws Exception{			
		this.range = ImportInfo.getRange(range);;
		this.m=m;
		this.color=color;
		this.spreadsheetName=name;
	}

	public Color getColor(String name,int row,int col) {
		if (color==null || !name.equals(this.spreadsheetName)) return null;
		if (row>=range.getStartRow() && row<=range.getEndRow() && col>=range.getStartCol() && col<=range.getEndCol()) {
			return color;
		} else  {
			return null;
		}
	}
	
	public void cellsChanged(CellsChangeEvent event) {
		if (!enabled || !event.getSpreadsheetName().equals(spreadsheetName))
			return;
		
		
		CellRange eventRange = event.getRange();
		if ((eventRange.getStartCol() >= range.getStartCol() && eventRange.getStartCol() <= range.getEndCol()
				&& eventRange.getStartRow() >= range.getStartRow() && eventRange.getStartRow() <= range.getEndRow())
				|| (eventRange.getEndCol() >= range.getStartCol() && eventRange.getEndCol() <= range.getEndCol()
						&& eventRange.getEndRow() >= range.getStartRow() && eventRange.getEndRow() <= range.getEndRow())

				|| (eventRange.getEndCol() >= range.getStartCol() && eventRange.getEndCol() <= range.getEndCol()
						&& eventRange.getStartRow() >= range.getStartRow() && eventRange.getStartRow() <= range.getEndRow())

				|| (eventRange.getStartCol() >= range.getStartCol() && eventRange.getStartCol() <= range.getEndCol()
						&& eventRange.getEndRow() >= range.getStartRow() && eventRange.getEndRow() <= range.getEndRow())

				|| (range.getStartCol() >= eventRange.getStartCol() && range.getStartCol() <= eventRange.getEndCol()
						&& range.getStartRow() >= eventRange.getStartRow() && range.getStartRow() <= eventRange.getEndRow())
				|| (range.getEndCol() >= eventRange.getStartCol() && range.getEndCol() <= eventRange.getEndCol()
						&& range.getEndRow() >= eventRange.getStartRow() && range.getEndRow() <= eventRange.getEndRow())
				|| (range.getEndCol() >= eventRange.getStartCol() && range.getEndCol() <= eventRange.getEndCol()
						&& range.getStartRow() >= eventRange.getStartRow() && range.getStartRow() <= eventRange.getEndRow())

				|| (range.getStartCol() >= eventRange.getStartCol() && range.getStartCol() <= eventRange.getEndCol()
						&& range.getEndRow() >= eventRange.getStartRow() && range.getEndRow() <= eventRange.getEndRow())

		) {

			m.sourceAll(event.getRGui());

		}

	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String toString() {
		return range.toString();
	}
}