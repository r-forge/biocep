package model;

import net.java.dev.jspreadsheet.CellRange;

public interface SpreadsheetListener {
	public void setSelection(String origin,CellRange sel);
	public void updateUndoAction();
	public void updateRedoAction();
}
