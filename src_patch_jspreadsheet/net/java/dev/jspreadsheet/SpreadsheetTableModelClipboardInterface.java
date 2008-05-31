package net.java.dev.jspreadsheet;

import java.rmi.RemoteException;

import model.SpreadsheetListener;
import model.SpreadsheetListenerRemote;

public interface SpreadsheetTableModelClipboardInterface {	
	
	public int getColumnCount();
	public int getRowCount();
	public void fromString(String text, char delim, int rowOff, int colOff, CellRange range);
	public void clearRange(CellRange range);
	public String toString(CellRange range, boolean byValue, char delim);
	public void setModified(boolean modified);
	public void setSelection(String origin , CellRange sel);
	
	
	public void insertColumn(CellRange insertRange);
	public void insertRow(CellRange insertRange);
	public void removeColumn(CellRange deletionRange);
	public void removeRow(CellRange deletionRange);
		
	public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell);	
	public void fillRange(CellRange range, String s);
	public boolean isModified();
	public boolean isDeletionSafe(CellRange range, boolean byRow);
	public void setPasswordModified(boolean modified);
	public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker);
	public Cell getCellAt(int aRow, int aColumn);
	public String toString(char delim); 
	
	public void historyAdd(CellRange range);
	public void historyAdd(SpreadsheetClipboard clip);
	public void historyAdd(CellRange range, int type);
	
	public void undo();
	public boolean canUndo();	
	public void redo();
	public boolean canRedo();	
	
	public void addSpreadsheetListener(SpreadsheetListener l);
	public void removeSpreadsheetListener(SpreadsheetListener l);
	public void removeAllSpreadsheetListeners();
	
		
}
	
