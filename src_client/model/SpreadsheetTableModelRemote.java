package model;

import java.rmi.RemoteException;
import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;

public interface SpreadsheetTableModelRemote extends TableModelRemote {
	 public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker) throws RemoteException;
	 
	 public CellRange removeRow(CellRange deletionRange) throws RemoteException;
	 public CellRange removeColumn(CellRange deletionRange) throws RemoteException;
	 public CellRange insertColumn(CellRange insertRange) throws RemoteException;
	 public CellRange insertRow(CellRange insertRange) throws RemoteException;
	 
	 public Cell getCellAt(int aRow, int aColumn) throws RemoteException;
	 public void setRange(CellRange range, Cell[][] data, boolean byValue) throws RemoteException;
	 public void setRange(CellRange range, Object[][] data) throws RemoteException;
	 public void fromString(String text, char delim, int rowOff, int colOff, CellRange range) throws RemoteException;
	 public void fill(CellRange range, Object input) throws RemoteException;
	 public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell) throws RemoteException;	
	 public String toString(CellRange range, boolean byValue, char delim) throws RemoteException;
	 public void setModified(boolean modified) throws RemoteException;
	 public boolean isModified() throws RemoteException;	
	 public void setPasswordModified(boolean modified) throws RemoteException;
	 public boolean isDeletionSafe(CellRange range, boolean byRow) throws RemoteException;		
}
