package model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.java.dev.jspreadsheet.CellRange;

public class SpreadsheetListenerRemoteImpl extends UnicastRemoteObject implements SpreadsheetListenerRemote {
	SpreadsheetListener localListener;
	SpreadsheetListenerRemoteImpl(SpreadsheetListener localListener) throws RemoteException{
		super();
		this.localListener=localListener;
	}
	public void setSelection(CellRange sel) throws RemoteException {
		localListener.setSelection(sel);			
	}
	public void updateRedoAction() throws RemoteException {
		localListener.updateRedoAction();			
	}
	public void updateUndoAction() throws RemoteException {
		localListener.updateUndoAction();			
	}
}
