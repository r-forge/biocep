package model;

import javax.swing.table.AbstractTableModel;
import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;

abstract public class SpreadsheetAbstractTableModel extends AbstractTableModel {	
	abstract public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker);
	abstract public CellRange removeRow(CellRange deletionRange);
	abstract public CellRange removeColumn(CellRange deletionRange);
	abstract public CellRange insertRow(CellRange insertRange);
	abstract public CellRange insertColumn(CellRange insertRange);
	abstract public Cell getCellAt(int aRow, int aColumn);
	abstract public void setRange(CellRange range, Cell[][] data, boolean byValue);
	abstract public void setRange(CellRange range, Object[][] data);
	abstract public void fromString(String text, char delim, int rowOff, int colOff, CellRange range);
	abstract public void fill(CellRange range, Object input);
	abstract public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell);	
	abstract public String toString(CellRange range, boolean byValue, char delim);
	abstract public void setModified(boolean modified);
	abstract public boolean isModified();	
	abstract public void setPasswordModified(boolean modified);
	abstract public boolean isDeletionSafe(CellRange range, boolean byRow);	
}
