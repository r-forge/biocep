package model;

import java.rmi.RemoteException;

import net.java.dev.jspreadsheet.Cell;

public interface SpreadsheetModelRemote extends TableModelRemote {
	
	public Cell getCellAt(int aRow, int aColumn) throws RemoteException;
	
	

}
