package model;

import net.java.dev.jspreadsheet.CellRange;

public interface SpreadsheetListener {
	public void setSelection(CellRange sel);
	public void updateUndoAction();
	public void updateRedoAction();
}
