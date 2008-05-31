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

	public void discardCache() {
		localListener.discardCache();
	}
	public void discardCacheRange(CellRange range) {
		localListener.discardCacheRange(range);
	}
	public void discardCacheCell(int row, int col) {
		localListener.discardCacheCell(row, col);
	}
	public void discardColumnCount() {
		localListener.discardColumnCount();
	}
	public void discardRowCount() {
		localListener.discardRowCount();
	}
	public void setSelection(String origin, CellRange sel) {
		localListener.setSelection(origin, sel);
	}
	public void updateRedoAction() {
		localListener.updateRedoAction();
	}
	public void updateUndoAction() {
		localListener.updateUndoAction();
	}
	
	public void removeColumns(int removeNum) throws RemoteException {	
		localListener.removeColumns(removeNum);
	}
	
	public void insertColumn(int insertNum, int startCol) throws RemoteException {
		localListener.insertColumn(insertNum, startCol);		
	}

}
