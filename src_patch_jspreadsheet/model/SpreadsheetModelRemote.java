package model;

import java.rmi.RemoteException;
import java.util.HashMap;

import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;
import net.java.dev.jspreadsheet.SpreadsheetClipboard;

public interface SpreadsheetModelRemote extends TableModelRemote {
	 
	 public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker) throws RemoteException;	 	 
	 public void removeRow(CellRange deletionRange) throws RemoteException;
	 public void removeColumn(CellRange deletionRange) throws RemoteException;
	 public void insertColumn(CellRange insertRange) throws RemoteException;
	 public void insertRow(CellRange insertRange) throws RemoteException;	 	 
	 public void setRange(CellRange range, Cell[][] data, boolean byValue) throws RemoteException;
	 public void setRange(CellRange range, Object[][] data) throws RemoteException;
	 public void fromString(String text, char delim, int rowOff, int colOff, CellRange range) throws RemoteException;
	 public void fill(CellRange range, Object input) throws RemoteException;
	 public void undo() throws RemoteException;	 	
	 public void redo() throws RemoteException;
	 
	 
	 public Cell getCellAt(int aRow, int aColumn) throws RemoteException;
	 public String toString(CellRange range, boolean byValue, char delim) throws RemoteException;	 
	 public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell) throws RemoteException;
	 
	 public void setModified(boolean modified) throws RemoteException;
	 public boolean isModified() throws RemoteException;	
	 public void setPasswordModified(boolean modified) throws RemoteException;
	 public boolean isDeletionSafe(CellRange range, boolean byRow) throws RemoteException;		

	 public boolean canUndo() throws RemoteException;
	 public boolean canRedo() throws RemoteException;		 
	 
	 public void historyAdd(CellRange range) throws RemoteException;
	 public void historyAdd(SpreadsheetClipboard clip) throws RemoteException;
	 public void historyAdd(CellRange range, int type) throws RemoteException;
		
	 
	 public void addSpreadsheetListener(SpreadsheetListenerRemote l) throws RemoteException;
	 public void removeSpreadsheetListener(SpreadsheetListenerRemote l)throws RemoteException;
	 public void removeAllSpreadsheetListeners()throws RemoteException;
	 
	 public String getSpreadsheetModelId() throws RemoteException;
	 public void dispose() throws RemoteException;
	 
	 public void setSpreadsheetSelection(String origin,CellRange sel) throws RemoteException;
	 
	 public SpreadsheetModelDevice newSpreadsheetModelDevice() throws RemoteException;
	 public SpreadsheetModelDevice[] listSpreadsheetModelDevice() throws RemoteException;
	 
	 public HashMap<Integer, Object> getRangeHashMap(CellRange range) throws RemoteException;
		 
}
