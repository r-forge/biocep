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
