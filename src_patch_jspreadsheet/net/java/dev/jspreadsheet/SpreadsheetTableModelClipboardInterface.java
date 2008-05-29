package net.java.dev.jspreadsheet;

public interface SpreadsheetTableModelClipboardInterface {	
	
	public int getColumnCount();
	public int getRowCount();
	public void fromString(String text, char delim, int rowOff, int colOff, CellRange range);
	public void clearRange(CellRange range);
	public String toString(CellRange range, boolean byValue, char delim);
	public void setModified(boolean modified);
	public void setSelection(CellRange sel);
	
	
	public CellRange insertColumn(CellRange insertRange);
	public CellRange insertRow(CellRange insertRange);
	public CellRange removeColumn(CellRange deletionRange);
	public CellRange removeRow(CellRange deletionRange);
		
	public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell);	
	public void fillRange(CellRange range, String s);
	public boolean isModified();
	public boolean isDeletionSafe(CellRange range, boolean byRow);
	public void setPasswordModified(boolean modified);
	void setHistory(History h);
	public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker);
	public Cell getCellAt(int aRow, int aColumn);
	public String toString(char delim); 
	
	
}
	
