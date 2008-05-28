package net.java.dev.jspreadsheet;

public interface SpreadsheetTableModelClipboardInterface {	
	public int getColumnCount();
	public int getRowCount();
	public void fromString(String text, char delim, int rowOff, int colOff, CellRange range);
	public void clearRange(CellRange range);
	public String toString(CellRange range, boolean byValue, char delim);
	public void setModified(boolean modified);
	public void setSelection(CellRange sel);
	public void insertColumn(CellRange insertRange);
	public void insertRow(CellRange insertRange);
	public void removeColumn() ;
	public void removeColumn(CellRange deletionRange);
	public void removeRow(CellRange deletionRange);
	
}
	
