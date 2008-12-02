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
package org.kchine.r.server.spreadsheet;

import java.rmi.RemoteException;
import java.util.HashMap;


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
	 	 
	 public void cellsChangeAdd(CellRange range) throws RemoteException;
	 public void cellsChangeAdd(SpreadsheetClipboard clip) throws RemoteException;
	 public void cellsChangeAdd(CellRange range, int type) throws RemoteException;
			 
	 public void addSpreadsheetListener(SpreadsheetListenerRemote l) throws RemoteException;
	 public void removeSpreadsheetListener(SpreadsheetListenerRemote l)throws RemoteException;
	 public void removeAllSpreadsheetListeners()throws RemoteException;
	 
	 public String getSpreadsheetModelId() throws RemoteException;
	 public void dispose() throws RemoteException;
	 
	 public void setSpreadsheetSelection(String origin,CellRange sel) throws RemoteException;
	 
	 public SpreadsheetModelDevice newSpreadsheetModelDevice() throws RemoteException;
	 public SpreadsheetModelDevice[] listSpreadsheetModelDevice() throws RemoteException;
	 
	 public HashMap<Integer, Object> getRangeHashMap(CellRange range) throws RemoteException;
	 
	 public void paste(int startRow, int startCol, String trstring) throws RemoteException;
		 
}
