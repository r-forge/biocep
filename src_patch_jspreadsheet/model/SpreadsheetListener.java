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
import net.java.dev.jspreadsheet.CellRange;
public interface SpreadsheetListener {
	public void setSelection(String origin,CellRange sel);
	public void updateUndoAction();
	public void updateRedoAction();
	
	
	public void discardRowCount();
	public void discardColumnCount();	
	public void discardCache();
	public void discardCacheCell(int row, int col);
	public void discardCacheRange(CellRange range);
	
	public void removeColumns(int removeNum);
	public void insertColumn(int insertNum, int startCol);
	public void removeRows(int removeNum);
	public void insertRow(int insertNum, int startRow);
	
}
