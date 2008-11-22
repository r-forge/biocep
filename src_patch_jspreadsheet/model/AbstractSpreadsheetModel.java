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

import javax.swing.table.AbstractTableModel;
import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;
import net.java.dev.jspreadsheet.SpreadsheetClipboard;

abstract public class AbstractSpreadsheetModel extends AbstractTableModel {	
	abstract public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker);
	abstract public void removeRow(CellRange deletionRange);
	abstract public void removeColumn(CellRange deletionRange);
	abstract public void insertRow(CellRange insertRange);
	abstract public void insertColumn(CellRange insertRange);
	abstract public Cell getCellAt(int aRow, int aColumn);
	abstract public void setRange(CellRange range, Cell[][] data, boolean byValue);
	abstract public void setRange(CellRange range, Object[][] data);
	abstract public void fromString(String text, char delim, int rowOff, int colOff, CellRange range);
	abstract public void fill(CellRange range, Object input);
	abstract public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell);	
	abstract public String toString(CellRange range, boolean byValue, char delim);
	abstract public void setModified(boolean modified);
	abstract public boolean isModified();	
	abstract public void setPasswordModified(boolean modified);
	abstract public boolean isDeletionSafe(CellRange range, boolean byRow);	
	
	abstract public void historyAdd(CellRange range);
	abstract public void historyAdd(SpreadsheetClipboard clip);
	abstract public void historyAdd(CellRange range, int type);
	
	abstract public void cellsChangeAdd(CellRange range);
	abstract public void cellsChangeAdd(SpreadsheetClipboard clip);
	abstract public void cellsChangeAdd(CellRange range, int type);
		
	abstract public void undo();
	abstract public boolean canUndo();	
	abstract public void redo();
	abstract public boolean canRedo();	
	
	abstract public void addSpreadsheetListener(SpreadsheetListener l);
	abstract public void removeSpreadsheetListener(SpreadsheetListener l);
	abstract public void removeAllSpreadsheetListeners();
	
	abstract public void setSpreadsheetSelection(String origin, CellRange sel);
	
}
