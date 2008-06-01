package model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.java.dev.jspreadsheet.CellRange;

public interface SpreadsheetListenerRemote extends Remote{
	public void setSelection(String origin,CellRange sel) throws RemoteException;
	public void updateUndoAction() throws RemoteException;
	public void updateRedoAction() throws RemoteException;
	
	public void discardRowCount() throws RemoteException;
	public void discardColumnCount() throws RemoteException;	
	public void discardCache() throws RemoteException;
	public void discardCacheCell(int row, int col) throws RemoteException;
	public void discardCacheRange(CellRange range) throws RemoteException;
	
	public void removeColumns(int rangeNum) throws RemoteException;
	public void insertColumn(int insertNum, int startCol) throws RemoteException;
	public void removeRows(int removeNum) throws RemoteException;
	public void insertRow(int insertNum, int startRow) throws RemoteException;
	
	
}
