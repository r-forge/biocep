/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	
	public void removeColumns(int removeNum)  {	
		localListener.removeColumns(removeNum);
	}
	
	public void insertColumn(int insertNum, int startCol)  {
		localListener.insertColumn(insertNum, startCol);		
	}

	public void insertRow(int insertNum, int startRow) {
		localListener.insertRow(insertNum, startRow);
	}

	public void removeRows(int removeNum) {
		localListener.removeRows(removeNum);
	}

}
