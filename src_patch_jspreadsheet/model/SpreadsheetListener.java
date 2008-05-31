package model;

import net.java.dev.jspreadsheet.CellRange;

public interface SpreadsheetListener {
	public void setSelection(String origin,CellRange sel);
	public void updateUndoAction();
	public void updateRedoAction();
	
	
	public void discardRowCount();
	public void discardColumnCount();	
	public void discardCache();
	public void discardCacheCell(int row, int col);
	public void discardCacheRange(CellRange range);
	
}
