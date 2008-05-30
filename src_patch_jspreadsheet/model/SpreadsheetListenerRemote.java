package model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import net.java.dev.jspreadsheet.CellRange;

public interface SpreadsheetListenerRemote extends Remote{
	public void setSelection(String origin,CellRange sel) throws RemoteException;
	public void updateUndoAction() throws RemoteException;
	public void updateRedoAction() throws RemoteException;
}
