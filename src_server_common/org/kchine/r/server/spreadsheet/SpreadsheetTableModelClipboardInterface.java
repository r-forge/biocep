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



public interface SpreadsheetTableModelClipboardInterface {	
	
	public int getColumnCount();
	public int getRowCount();
	public void fromString(String text, char delim, int rowOff, int colOff, CellRange range);
	public void clearRange(CellRange range);
	public String toString(CellRange range, boolean byValue, char delim);
	public void setModified(boolean modified);
	public void fireSetSelection(String origin , CellRange sel);
	
	
	public void insertColumn(CellRange insertRange);
	public void insertRow(CellRange insertRange);
	public void removeColumn(CellRange deletionRange);
	public void removeRow(CellRange deletionRange);
		
	public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell);	
	public void fillRange(CellRange range, String s);
	public boolean isModified();
	public boolean isDeletionSafe(CellRange range, boolean byRow);
	public void setPasswordModified(boolean modified);
	public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker);
	public Cell getCellAt(int aRow, int aColumn);
	public boolean isEmptyCell(int row, int col);
	
	
	public String toString(char delim); 
	
	public void historyAdd(CellRange range);
	public void historyAdd(SpreadsheetClipboard clip);
	public void historyAdd(CellRange range, int type);

	public void cellsChangeAdd(CellRange range);
	public void cellsChangeAdd(SpreadsheetClipboard clip);
	public void cellsChangeAdd(CellRange range, int type);	
	
	public void undo();
	public boolean canUndo();	
	public void redo();
	public boolean canRedo();	
	
	public void addSpreadsheetListener(SpreadsheetListener l);
	public void removeSpreadsheetListener(SpreadsheetListener l);
	public void removeAllSpreadsheetListeners();
	
	public String getName();
	
	
		
}
	
