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


import java.io.BufferedReader;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;

import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.RServices;


public class SpreadsheetModelRemoteImpl extends TableModelRemoteImpl implements SpreadsheetModelRemote, SpreadsheetTableModelInterface,
		SpreadsheetTableModelClipboardInterface {

	private History history;
	private HashMap<String, SpreadsheetModelRemoteImpl> map;
	private UndoManager um = new UndoManager() {
		public void undoableEditHappened(UndoableEditEvent e) {
			super.undoableEditHappened(e);
			updateUndoAction();
			updateRedoAction();
		}

		public void undo() {
			super.undo();
			updateUndoAction();
			updateRedoAction();
		}

		public void redo() {
			super.redo();
			updateUndoAction();
			updateRedoAction();
		}
	};

	public SpreadsheetModelRemoteImpl(int rowCount, int colCount, HashMap<String, SpreadsheetModelRemoteImpl> map) throws RemoteException {
		super(new SpreadsheetDefaultTableModel(rowCount, colCount));
		this.map = map;
		init();
		if (map != null)
			map.put(getSpreadsheetModelId(), this);
	}

	public SpreadsheetModelRemoteImpl(Object[] columnName, int rowCount) throws RemoteException {
		super(columnName, rowCount);
		this.map = map;
		init();
		if (map != null)
			map.put(getSpreadsheetModelId(), this);
	}

	public SpreadsheetModelRemoteImpl(Object[][] data, Object[] columnName) throws RemoteException {
		super(data, columnName);
		this.map = map;
		init();
		if (map != null)
			map.put(getSpreadsheetModelId(), this);
	}

	private void init() {
		for (int row = 0; row < m.getRowCount(); row++)
			for (int col = 0; col < m.getColumnCount(); col++)
				m.setValueAt(new Cell(""), row, col);
		history = new History(this);
		history.addUndoableEditListener(um);
		Formula.registerFunctions();
	}

	private boolean modified = false;
	private boolean passwordModified;

	/**
	 * This constructor is convenience for loading objects that are already in
	 * an array. It fills the SharpTableModel with the objects in the array
	 * making Object[0][0] be in cell A1, etc.
	 * <P>
	 * <B>Note: </B> This constructor does not assume that objects are of the
	 * desired form. It will parse a string to see if it is a number or formula.
	 * 
	 * @param sharp
	 *            gui object to associate with this SharpTableModel
	 * @param data
	 *            the array of objects to place into the SharpTableModel
	 */

	/*
	 * public SpreadsheetTableModel(JTable table, Object[][] data, RGui rgui) {
	 * this(table, data.length, data[0].length, rgui); // load the data for (int
	 * i = 0; i < data.length; i++) { for (int j = 0; j < data[i].length; j++) {
	 * doSetValueAt(data[i][j], i, j); } } // initialize state to unmodified and
	 * file to untitled modified = false; }
	 */

	// History getHistory()
	// {
	// return history;
	// }
	/**
	 * This method gets the whole Cell object located at the these coordinates.
	 * This method avoids the casting required when using getValueAt.
	 * 
	 * Note: here we need to make row 1-based.
	 * 
	 * <P>
	 * If the coordinates specify a cell that is out of bounds then it returns
	 * null. If a cell does not exist in the SharpTableModel at that valid
	 * coordinate, it creates an empty cell, places it at that spot and returns
	 * it.
	 * 
	 * @param aRow
	 *            the row of the cell
	 * @param aColumn
	 *            the column of the cell
	 * @return the Cell object at this location
	 */
	public Cell getCellAt(int aRow, int aColumn) {
		/* check for out of bounds */
		if ((aRow < 0) || (aRow >= m.getRowCount()) || (aColumn < 0) || (aColumn >= m.getColumnCount())) {
			return null;
		}

		Cell temp = (Cell) m.getValueAt(aRow, aColumn);

		return temp;
	}

	/**
	 * All Cells other than those in row 0 or column 0 are editable.
	 * 
	 * @param row
	 *            the row coordinate
	 * @param column
	 *            the column coordinate
	 * @return true if cell is editable
	 */
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	/**
	 * JTable uses this method to determine the default renderer editor for each
	 * cell. This method tells JTable to use SharpCellRender and
	 * SharpCellEditor.
	 * 
	 * @param c
	 *            the column for which we need to determine the class
	 * @return Cell class
	 */
	public Class<?> getColumnClass(int c) {
		/* only cell objects in this TableModel */
		return Cell.class;
	}

	/**
	 * This is used to return the column name
	 */
	public String getColumnName(int col) {
		Debug.println("name of column " + col);
		return String.valueOf(Node.translateColumn(col));
	}

	/**
	 * check whether the deletion is safe
	 * 
	 * @param range
	 *            the range to delete
	 * @param byRow
	 *            whether it's deletion by row
	 * @return true if it's safe
	 */
	public boolean isDeletionSafe(CellRange range, boolean byRow) {
		int rowOff;
		int colOff;
		CellRange needCheck;

		if (byRow) {
			rowOff = -range.getHeight();
			colOff = 0;
			if (range.getEndRow() == (m.getRowCount() - 1)) {
				return true;
			}

			needCheck = new CellRange(range.getEndRow() + 1, m.getRowCount() - 1, 0, m.getColumnCount() - 1);
		} else {
			rowOff = 0;
			colOff = -range.getWidth();
			if (range.getEndCol() == (m.getColumnCount() - 1)) {
				return true;
			}
			needCheck = new CellRange(0, m.getRowCount() - 1, range.getEndCol() + 1, m.getColumnCount() - 1);
		}

		for (int i = needCheck.getStartRow(); i <= needCheck.getEndRow(); i++)
			for (int j = needCheck.getStartCol(); j <= needCheck.getEndCol(); j++) {
				Cell cell = getCellAt(i, j);
				if (cell.isFormula() && !Formula.isSafe(cell.getFormula(), rowOff, colOff)) {
					Debug.println("relative addresses become invalid");

					return false;
				}
			}

		return true;
	}

	/**
	 * Determines if a cell at these coordinates is a formula cell
	 * 
	 * @param aRow
	 *            row coordinate
	 * @param aColumn
	 *            column coordinate
	 * @return true only if the cell at those coordinates is a formula
	 */
	public boolean isFormula(int aRow, int aColumn) {
		Cell temp = getCellAt(aRow, aColumn);

		return ((temp != null) && (temp.getType() == Cell.FORMULA));
	}

	/**
	 * Sets modified state of current document
	 * 
	 * @param modified
	 *            true sets state to modified
	 */
	public void setModified(boolean modified) {
		this.modified = modified | passwordModified;

		// enable/disable the "Save" button
		// sharp.checkSaveState();
	}

	/**
	 * Returns modified state of document
	 * 
	 * @return document's modified value - true or false
	 */
	public boolean isModified() {
		return modified;
	}

	/**
	 * This implements a needed class to work with the Formula class. If it is a
	 * data cell, it returns the numerical value of the cell (for a String the
	 * value is 0). If it is a formula, then it recalculates the value and
	 * returns that as an answer.
	 * 
	 * @param row
	 *            row coordinate
	 * @param col
	 *            column coordinate
	 * @throws ParserException
	 *             if the value doesn't parse
	 * @return numerical value of the cell
	 */
	public Number getNumericValueAt(int row, int col) throws ParserException {
		Cell cell = getCellAt(row, col);
		if (cell != null) {
			int type = cell.getType();
			if (type == Cell.FORMULA) {
				Object value = cell.getValue();
				Formula form = cell.getFormula();

				// if need recalc
				if (form.needsRecalc()) {
					try {
						value = Formula.evaluate(this, row, col);
						cell.setValue(value);
					} catch (ParserException e) {
						cell.setValue(e);
						value = e;
					}
				}

				if (value instanceof ParserException) {
					throw (ParserException) value;
				} else {
					return (Number) cell.getValue();
				}
			} else if (type == Cell.NUMBER) {
				return (Number) cell.getValue();
			} else {
				return new Float(0);
			}
		} else {
			// a string or null
			// return new Float(0);
			throw new ParserException("#REFS?");
		}
	}

	/**
	 * Sets modified state of password; can't undo
	 * 
	 * @param modified
	 *            sets state to modified
	 * @see FileOp#setPassword
	 */
	public void setPasswordModified(boolean modified) {
		passwordModified = modified;
		setModified(this.modified);

		// enable/disable the "Save" button
	}

	/**
	 * This method copies the cells in a range into a two-dimensional array of
	 * cells.
	 * 
	 * @param range
	 *            range of cells to copy
	 * @return copy of range
	 */
	public Cell[][] getRange(CellRange range) {
		// get dimensions of range
		Cell[][] board = new Cell[range.getHeight()][range.getWidth()];

		// copy the cells
		for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
			for (int j = range.getStartCol(); j <= range.getEndCol(); j++) {
				// translate to coordinates in copy array
				int x = i - range.getStartRow();
				int y = j - range.getStartCol();

				Cell field = getCellAt(i, j);

				/*
				 * if it is a formula copy both the value and the formula The
				 * value will be useful with a paste by value
				 */
				if (field.isFormula()) {
					try {
						Formula form = new Formula(field.getFormula(), i, j);
						board[x][y] = new Cell(form, field.getValue(), null);
					} catch (ParserException e) {
						/*
						 * if there is a problem, always treat formula as a
						 * string.
						 */
						board[x][y] = new Cell(field.getFormula().toString());
					}
				} else {
					// value cells have immutable objects
					board[x][y] = new Cell(field.getValue());
				}
			}
		}

		return board;
	}

	/**
	 * From a string input determine how many rows/columns it requires for the
	 * table - it corresponds to the number of newlines and tabs.
	 * 
	 * @param input
	 *            the string to analyze
	 */
	static public CellPoint getSize(String input, char delim) {
		BufferedReader in = new BufferedReader(new StringReader(input));
		String line;
		int rowcount = 0;
		int colcount = 0;

		try {
			while ((line = in.readLine()) != null) {
				rowcount++;

				// initialize new tokenizer on line with tab delimiter.
				// tokenizer = new StringTokenizer(line, "\t");
				int index;
				int prev = 0;

				// set col to 1 before each loop
				int col = 0;

				while (true) {
					index = line.indexOf(delim, prev);
					prev = index + 1;

					// increment column number
					col++;

					if (index == -1) {
						break;
					}
				}

				if (colcount < col) {
					colcount = col;
				}
			}
		} catch (Exception e) {
			return null;
		}

		return new CellPoint(rowcount, colcount);
	}

	/**
	 * Determines if a cell is empty
	 * 
	 * @param row
	 *            row coordinate of cell
	 * @param col
	 *            column coordinate of cell
	 * @return true if cell is empty
	 */
	public boolean isEmptyCell(int row, int col) {
		return getCellAt(row, col).getValue().equals("");
	}

	/**
	 * This class returns the cell object at those coordinates. It does exactly
	 * the same thing as getCellAt except that the return type is Object. It is
	 * implemented because TableModel requires this method return an Object.
	 * 
	 * @param aRow
	 *            the row coordinate
	 * @param aColumn
	 *            the column coordinate
	 * @return the Cell
	 */
	public Object getValueAt(int aRow, int aColumn) {
		return getCellAt(aRow, aColumn);
	}

	/**
	 * This method does not recognize formula strings. It is used for dialogue
	 * box input where there will be no formulas inputted or expected to be
	 * inputted.
	 * 
	 * @param input
	 *            input string to parse
	 * @return appropriate object after parsing
	 */
	public static Object fieldParser(String input) {
		if (input == null) {
			return new String("");
		}

		/* try making it a number */
		try {
			return new Float(input);
		} catch (NumberFormatException e) {
			/* all else fails treat as string */
			return input;
		}
	}

	/**
	 * This object assumes that the object passes to it is already the correct
	 * object to set the value of the cell as. For a formula, it also
	 * calculcates the value of the formula and records that in the cell.
	 * 
	 * @param input
	 *            object to set the Cell value as
	 * @param aRow
	 *            row of cell to set
	 * @param aColumn
	 *            column of cell to set
	 */
	public void setCellAt(Object input, int aRow, int aColumn) {
		Cell temp = getCellAt(aRow, aColumn);

		/* if for some reason value out of bounds ignore */
		if (temp != null) {
			// always remove references old formula referred to
			removeRefs(aRow, aColumn);

			// insert new formula
			if (input instanceof Formula) {
				temp.setFormula((Formula) input);

				if (isLoop(new CellPoint(aRow, aColumn))) {
					ParserException loop = new ParserException("#LOOP?");
					Formula form2 = new Formula(input.toString(), aRow, aColumn, loop);
					setCellAt(form2, aRow, aColumn);
					getCellAt(aRow, aColumn).setValue(loop);
					updateRefs(aRow, aColumn);

					return;
				} else {
					addRefs(aRow, aColumn);
					recalculate(aRow, aColumn);
					updateRefs(aRow, aColumn);
				}
			} else {
				// treat as normal data cell
				temp.setData(input);
				updateRefs(aRow, aColumn);
			}
		}

	}

	/**
	 * This method sets the cells given by the range to the cooresponding value
	 * in the Object array. In other words, this method pastes the object array
	 * onto the range. It is assumed that the range and Object array have the
	 * same dimensions. (a "placeAt" method for ranges)
	 * 
	 * @param range
	 *            the range of cells to paste to
	 * @param data
	 *            the data to paste
	 */
	public void setRange(CellRange range, Object[][] data) {
		/* Loop through the paste range */
		for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
			for (int j = range.getStartCol(); j <= range.getEndCol(); j++) {
				// calculate the corresponding entry in data array
				int x = i - range.getStartRow();
				int y = j - range.getStartCol();

				// place data entry at that place
				doSetValueAt(data[x][y], i, j);
			}
		}
	}

	/**
	 * This is a method used to paste cells onto the table. This method is used
	 * by the SharpClipboard class. It's feature is that it can paste only the
	 * old evaluated values or it can be told to paste the data cells and
	 * formulas.
	 * 
	 * @param range
	 *            range to paste to
	 * @param data
	 *            cells that need to be pasted
	 * @param byValue
	 *            true if only paste values if there are formula
	 */
	public void setRange(CellRange range, Cell[][] data, boolean byValue) {
		/*
		 * there may be formula so if byValue is true paste evaluated formula
		 * value into the range as a data cell
		 */
		if (byValue) {
			for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
				for (int j = range.getStartCol(); j <= range.getEndCol(); j++) {
					int x = i - range.getStartRow();
					int y = j - range.getStartCol();

					// get only value of a formula cell not formula
					doSetValueAt(data[x][y].getValue(), i, j);
				}
			}
		} else {
			for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
				for (int j = range.getStartCol(); j <= range.getEndCol(); j++) {
					int x = i - range.getStartRow();
					int y = j - range.getStartCol();
					Cell info = data[x][y];

					// paste new formula to recalculate
					if (info.isFormula()) {
						doSetValueAt(info.getFormula(), i, j);
					} else {
						doSetValueAt(info.getValue(), i, j);
					}
				}
			}
		}
	}

	/**
	 * Sets the value of the cell. It takes care of formulas and data. If aValue
	 * is a string, it parses it to see if it is a formula (begins with an "=")
	 * or a number. It then sets the value of the cell accordingly.
	 * <P>
	 * This function is called by JTable automatically, which means user has
	 * manually input something. Thus, it records the previous value of the cell
	 * into the History object associated with this SharpTableModel.
	 * <p>
	 * We should never call it directly (use doSetValueAt instead).
	 * 
	 * @param aValue
	 *            the formula or data you want to set cell to
	 * @param aRow
	 *            row coordinate
	 * @param aColumn
	 *            column coordinate
	 */
	public void setValueAt(Object aValue, int aRow, int aColumn) {
		CellPoint point = new CellPoint(aRow, aColumn);
		history.add(new CellRange(point, point));
		doSetValueAt(aValue, aRow, aColumn);
		history.fireCellsChangeEvent(new CellRange(point, point));
	}

	private void fireDiscardRowCount() {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).discardRowCount();
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireDiscardColumnCount() {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).discardColumnCount();
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireDiscardCell(int aRow, int aColumn) {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).discardCacheCell(aRow, aColumn);
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireDiscardRange(CellRange range) {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).discardCacheRange(range);
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireDiscard() {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).discardCache();
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireRemoveCols(int removeNum) {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).removeColumns(removeNum);
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireInsertCols(int insertNum, int startCol) {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).insertColumn(insertNum, startCol);
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireRemoveRows(int removeNum) {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).removeRows(removeNum);
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void fireInsertRows(int insertNum, int startRow) {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).insertRow(insertNum, startRow);
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	/**
	 * This method should be called with a cell is set as a formula cell
	 * (although it does nothing if it is not a formula). When a formula is
	 * entered, it may reference other cells. These cells need to be notified
	 * that if they are changed, to notify this formula cell. This method adds
	 * the cell coordinates to the reference list of each cell that the formula
	 * references.
	 * 
	 * @param aRow
	 *            row of formula cell
	 * @param aColumn
	 *            column of formula cell
	 */
	public void addRefs(int aRow, int aColumn) {
		if (isFormula(aRow, aColumn)) {
			Formula temp = getCellAt(aRow, aColumn).getFormula();
			TreeSet list = temp.getDependency();

			/*
			 * use formula's dependency to find cells that need to notify it if
			 * their values change
			 */
			Iterator it = list.iterator();
			CellPoint thisRef = new CellPoint(aRow, aColumn);
			while (it.hasNext()) {
				CellPoint update = (CellPoint) it.next();
				Cell field = getCellAt(update.getRow(), update.getCol());

				// test of cell found was out of bounds
				if (field != null) {
					field.addRef(thisRef);
				}
			}
		}
	}

	/**
	 * This method clears all cells in the range but leaves the reference lists
	 * alone.
	 * 
	 * @param range
	 *            range to clear
	 */
	public void clearRange(CellRange range) {
		fill(range, null);
	}

	/**
	 * This method sets the value of the cell specified with these coordinates
	 * to aValue. It does the parsing of string objects to see if they are
	 * numbers or formulas. If you do not want any parsing at all, use
	 * setCellAt.
	 * 
	 * @param aValue
	 *            value to set cell to
	 * @param aRow
	 *            row coordinate of cell
	 * @param aColumn
	 *            column coordinate of cell
	 */
	public void doSetValueAt(Object aValue, int aRow, int aColumn) {

		if (aValue == null) {
			aValue = "";
		}

		if (aValue instanceof String) {
			String input = (String) aValue;

			/* try making it a formula */
			if (input.startsWith("=")) {
				Formula form = null;

				// create formula and its value and put in a cell
				try {
					form = new Formula(input.substring(1), aRow, aColumn);
					setCellAt(form, aRow, aColumn);
				} catch (ParserException e) {
					// no parsing
					form = new Formula(input.substring(1), aRow, aColumn, e);
					setCellAt(form, aRow, aColumn);
					getCellAt(aRow, aColumn).setValue(e);
				}
			} else {
				// try {
				// Integer idata = new Integer(input);
				// setCellAt(idata, aRow, aColumn);
				// }

				/*
				 * if it begins with "=" but invalid just treat as a string
				 */

				// catch (NumberFormatException e) {
				/* try making it a number */
				try {
					Float data = new Float(input);
					setCellAt(data, aRow, aColumn);
				}

				/*
				 * if it begins with "=" but invalid just treat as a string
				 */
				catch (NumberFormatException e2) {
					/* all else fails treat as string */
					setCellAt(aValue, aRow, aColumn);
				}

				// }
			}
		} else {
			/*
			 * it is an object, assume it is exactly what should the cell be set
			 * to
			 */
			if (aValue instanceof Formula) {
				try {
					Formula form = (Formula) aValue;
					setCellAt(new Formula(form, aRow, aColumn), aRow, aColumn);
				} catch (ParserException e) {
					// errorMessage(e.toString());
					Formula form2 = (Formula) aValue;
					form2 = new Formula(form2.toString(), aRow, aColumn, e);
					setCellAt(form2, aRow, aColumn);
					getCellAt(aRow, aColumn).setValue(e);
				}
			} else {
				setCellAt(aValue, aRow, aColumn);
			}
		}
	}

	/**
	 * This method inserts columns to the left of the range with the number of
	 * new columns equal to the number of columns in the range.
	 * 
	 * @param insertRange
	 *            range of cells to add new columns to the left of creates the
	 *            same number of new columns as range has
	 */
	public void insertColumn(CellRange insertRange) {
		long t1 = System.currentTimeMillis();
		System.out.println("SpreadsheetModelRemoteImpl.insertColumn t1=" + t1);

		fireDiscardColumnCount();
		fireDiscard();
		/*
		 * since the insertion point is given by a selected cell there will
		 * never be an out of bounds error
		 */
		Debug.println("insertRange: " + insertRange);

		/* start inserting at this coordinate */
		int col = insertRange.getStartCol();

		/* number of columns to insert including col */
		int insertNum = insertRange.getWidth();

		// the coordinates of the last cell in table
		int lastRow = getRowCount() - 1;
		int lastCol = getColumnCount() - 1;

		/*
		 * everything right to new columns must be shifted right so cut them to
		 * the clipboard. So if col is "C" then it is also copied. The max is a
		 * guard for inserting before the label column.
		 */
		CellRange range = new CellRange(0, lastRow, Math.max(col, 0), lastCol);
		SpreadsheetClipboard scrap = new SpreadsheetClipboard(this, range, true);

		// add the new columns to the end
		for (int i = 0; i < insertNum; i++) {
			int curCol = lastCol + i + 1;
			addColumn();
		}

		fireInsertCols(insertNum, col);

		// shift relevant columns left
		scrap.paste(this, new CellPoint(0, col + insertNum));

		recalculateAll();

		// set selection

		CellRange selectionRange = new CellRange(0, 0, col, col);
		fireSetSelection(null, selectionRange);
		// return selectionRange;

		// sharp.setBaseColumnWidth();
		// fireTableStructureChanged();
		// sharp.setBaseColumnWidth();

		long t2 = System.currentTimeMillis();
		System.out.println("SpreadsheetModelRemoteImpl.insertColumn t2=" + t2 + " -> " + (t2 - t1));

	}

	/**
	 * This method inserts rows to the left of range. It adds as many rows as
	 * the range has.
	 * 
	 * @param insertRange
	 *            the range to the left of to add new rows also adds number of
	 *            new rows equal to rows in range
	 */
	public void insertRow(CellRange insertRange) {
		fireDiscardRowCount();
		fireDiscard();
		/*
		 * since the insertion point is given by a selected cell there will
		 * never be an out of bounds error
		 */
		/* insert starting at this coordinate */
		int row = insertRange.getStartRow();

		/* number of rows to insert including row */
		int insertNum = insertRange.getHeight();

		// coordinates of last cell of table
		int lastRow = m.getRowCount() - 1;
		int lastCol = m.getColumnCount() - 1;

		/*
		 * copy things below these new rows The max is to prevent inserts above
		 * the column headers
		 */
		CellRange range = new CellRange(Math.max(row, 0), lastRow, 0, lastCol);
		SpreadsheetClipboard scrap = new SpreadsheetClipboard(this, range, true);

		// add the rows to the end
		for (int i = 0; i < insertNum; i++) {
			addRow();
		}

		fireInsertRows(insertNum, row);

		// shift old rows down
		scrap.paste(this, new CellPoint(row + insertNum, 0));

		recalculateAll();

		CellRange selectionRange = new CellRange(row, row, 0, 0);
		fireSetSelection(null, selectionRange);
		// return selectionRange;

	}

	/**
	 * This searches of an object starting from a cell point.
	 * 
	 * @param begin
	 *            cellpoint to begin search
	 * @param goal
	 *            the object to search for
	 * @param matchCase
	 *            true if case sensitive (only active if goal is a string)
	 * @param matchCell
	 *            true if goal must equal entire content of cell (only active if
	 *            goal is a string)
	 * @return the CellPoint of next occurence of goal
	 */
	public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell) {
		int startRow = begin.getRow();
		int startCol = begin.getCol();

		if ((goal instanceof String) && !matchCase && matchCell) {
			String objective = (String) goal;
			for (int i = startCol; i < m.getColumnCount(); i++) {
				if (objective.equalsIgnoreCase(getCellAt(startRow, i).getValue().toString())) {
					return new CellPoint(startRow, i);
				}
			}

			for (int i = startRow + 1; i < m.getRowCount(); i++) {
				for (int j = 1; j < m.getColumnCount(); j++) {
					if (objective.equalsIgnoreCase(getCellAt(i, j).getValue().toString())) {
						return new CellPoint(i, j);
					}
				}
			}

			return null;
		} else {
			if ((goal instanceof String) && !matchCell) {
				String objective = (String) goal;
				for (int i = startCol; i < m.getColumnCount(); i++) {
					String test = getCellAt(startRow, i).getValue().toString();

					if (!matchCase) {
						objective = objective.toUpperCase();
						test = test.toUpperCase();
					}
					for (int k = 0; k < test.length(); k++) {
						if (test.startsWith(objective, k)) {
							return new CellPoint(startRow, i);
						}
					}
				}

				for (int i = startRow + 1; i < m.getRowCount(); i++) {
					for (int j = 1; j < m.getColumnCount(); j++) {
						String test = getCellAt(i, j).getValue().toString();
						if (!matchCase) {
							objective = objective.toUpperCase();
							test = test.toUpperCase();
						}
						for (int k = 0; k < test.length(); k++) {
							if (test.startsWith(objective, k)) {
								return new CellPoint(i, j);
							}
						}
					}
				}

				return null;
			} else {
				for (int i = startCol; i < m.getColumnCount(); i++) {
					if (goal.equals(getCellAt(startRow, i).getValue())) {
						return new CellPoint(startRow, i);
					}
				}

				for (int i = startRow + 1; i < m.getRowCount(); i++) {
					for (int j = 1; j < m.getColumnCount(); j++) {
						if (goal.equals(getCellAt(i, j).getValue())) {
							return new CellPoint(i, j);
						}
					}
				}

				return null;
			}
		}
	}

	/**
	 * This is a warper method of the Formula class's evaluate method. This
	 * method recalculates the value of a formula at the given coordinates. If
	 * the coordinates do not specify a formula it does nothing.
	 * 
	 * @param aRow
	 *            the row coordinate
	 * @param aColumn
	 *            the column coordinate
	 */
	public void recalculate(int aRow, int aColumn) {
		if (isFormula(aRow, aColumn)) {
			try {
				Debug.println("recalculate");

				Number eVal = Formula.evaluate(this, aRow, aColumn);

				// we set the value here
				getCellAt(aRow, aColumn).setValue(eVal);
			} catch (ParserException e) {
				// set value as the appropriate error message
				getCellAt(aRow, aColumn).setValue(e);
			}
		}
	}

	/**
	 * This is the version of recalculate that takes a CellPoint object as an
	 * argument.
	 * 
	 * @param x
	 *            the coordinates of the cell to be updated
	 */
	public void recalculate(CellPoint x) {
		recalculate(x.getRow(), x.getCol());
	}

	/**
	 * This method recalculates all cells in the table
	 * 
	 * @see #insertRow
	 * @see #insertColumn
	 * @see #removeRow
	 * @see #removeColumn
	 * 
	 */
	public void recalculateAll() {
		for (int i = 1; i < m.getRowCount(); i++)
			for (int j = 1; j < m.getColumnCount(); j++) {
				addRefs(i, j);
				recalculate(i, j);
			}
	}

	/**
	 * This method removes columns in the range.
	 * 
	 * @param deletionRange
	 *            the range that contains the columns to delete
	 */
	public void removeColumn(CellRange deletionRange) {
		fireDiscardColumnCount();
		fireDiscard();
		/*
		 * since the insertion point is given by a selected cell there will
		 * never be an out of bounds error
		 */
		/* first column to delete */
		int col = deletionRange.getStartCol();

		/* number of columns to delete including col */
		int removeNum = deletionRange.getWidth();

		// last entry of table
		int lastRow = m.getRowCount() - 1;
		int lastCol = m.getColumnCount() - 1;

		/*
		 * everything to the right of the columns to remove need to be copied to
		 * be shifted right
		 */
		CellRange range = new CellRange(0, lastRow, col + removeNum, lastCol);

		SpreadsheetClipboard scrap = new SpreadsheetClipboard(this, range, true);

		for (int i = 0; i < removeNum; i++) {
			// delete old column
			removeColumn();
		}

		fireRemoveCols(removeNum);

		// shift clipboard elements right
		scrap.paste(this, new CellPoint(0, col));

		// updateRefs(refs);
		recalculateAll();

		// set selection

		CellRange selectionRange = new CellRange(0, 0, col, col);
		fireSetSelection(null, selectionRange);
		// return selectionRange;

		// fireTableStructureChanged();
		// sharp.setBaseColumnWidth();
	}

	/**
	 * This method removes this formula cell from the reference lists of all
	 * cells it references. If this is not a formula cell, it does nothing. This
	 * method should be called with a formula cell is being changed to a
	 * non-Formula cell.
	 * 
	 * @param aRow
	 *            row of cell to remove from reference list
	 * @param aColumn
	 *            column of cell to remove from reference list
	 */
	public void removeRefs(int aRow, int aColumn) {
		if (isFormula(aRow, aColumn)) {
			Formula temp = getCellAt(aRow, aColumn).getFormula();
			TreeSet list = temp.getDependency();

			/*
			 * use formula dependcy list to go to cells that it references then
			 * remove its entry form their reference list
			 */
			Iterator it = list.iterator();
			CellPoint thisRef = new CellPoint(aRow, aColumn);
			while (it.hasNext()) {
				CellPoint update = (CellPoint) it.next();
				Cell field = getCellAt(update.getRow(), update.getCol());

				if (field != null) {
					field.removeRef(thisRef);
				}
			}
		}
	}

	/**
	 * This method removes rows in the range.
	 * 
	 * @param deletionRange
	 *            CellRange that contains the rows to delete
	 */
	public void removeRow(CellRange deletionRange) {

		fireDiscardRowCount();
		fireDiscard();

		/*
		 * since the insertion point is given by a selected cell there will
		 * never be an out of bounds error
		 */
		clearRange(deletionRange);

		/* first row to delete */
		int row = deletionRange.getStartRow();

		/* number of rows to delete including the first */
		int removeNum = deletionRange.getHeight();

		// coordinates of last cell in spreadsheet
		int lastRow = m.getRowCount() - 1;
		int lastCol = m.getColumnCount() - 1;

		// everything lower than rows to remove must be copied to be shifted
		CellRange range = new CellRange(row + removeNum, lastRow, 0, lastCol);
		SpreadsheetClipboard scrap = new SpreadsheetClipboard(this, range, true);

		for (int i = 0; i < removeNum; i++) {
			((SpreadsheetDefaultTableModel) m).removeRow(m.getRowCount() - 1);
		}

		fireRemoveRows(removeNum);

		// shift relevent rows up
		scrap.paste(this, new CellPoint(row, 0));

		recalculateAll();

		CellRange selectionRange = new CellRange(row, row, 0, 0);
		fireSetSelection(null, selectionRange);
		// return selectionRange;

	}

	/**
	 * This method sorts an arbitrary range in the table.
	 * 
	 * @param area
	 *            range to sort
	 * @param primary
	 *            primary row/column to sort by
	 * @param second
	 *            second row/column to sort by (set equal to primary if there is
	 *            no secondary criteria specified.
	 * @param isRow
	 *            true if primary and secondary are row numbers
	 * @param ascend
	 *            true if sorting in ascending order by primary criteria
	 * @param tiebreaker
	 *            true if sorting in ascending order by secondary criteria data
	 *            structure
	 */
	public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker) {
		/*
		 * original data order will be saved here and placed on clipboard for
		 * undo
		 */
		SpreadsheetClipboard[] data;
		if (isRow) {
			data = new SpreadsheetClipboard[area.getWidth()];
			for (int i = 0; i < data.length; i++) {
				CellRange temp = new CellRange(area.getStartRow(), area.getEndRow(), area.getStartCol() + i, area.getStartCol() + i);
				data[i] = new SpreadsheetClipboard(this, temp, false);
			}
		} else {
			data = new SpreadsheetClipboard[area.getHeight()];
			for (int i = 0; i < data.length; i++) {
				CellRange temp = new CellRange(area.getStartRow() + i, area.getStartRow() + i, area.getStartCol(), area.getEndCol());
				data[i] = new SpreadsheetClipboard(this, temp, false);
			}
		}

		/*
		 * We are going to do the sort within the world of the data array First,
		 * we do index sorting to create an index array. Then according to the
		 * index array, we paste the entries in data back in the sorted order.
		 */

		// do index sorting
		int[] indices = internalSort(area, primary, second, isRow, ascend, tiebreaker);

		// paste accordingly
		if (isRow) {
			for (int i = area.getStartCol(); i <= area.getEndCol(); i++) {
				// point to paste at
				CellPoint point = new CellPoint(area.getStartRow(), i);

				int y = i - area.getStartCol();
				data[indices[y] - area.getStartCol()].paste(this, point);
			}
		} else {
			for (int i = area.getStartRow(); i <= area.getEndRow(); i++) {
				// point to paste at
				CellPoint point = new CellPoint(i, area.getStartCol());

				int y = i - area.getStartRow();
				data[indices[y] - area.getStartRow()].paste(this, point);
			}
		}
	}

	/**
	 * toString is used to convert a range of cells into a string. One row per
	 * line, and each column is tab-delimited.
	 * 
	 * @param range
	 *            the range in the table
	 * @param byValue
	 *            get the value instead of a formula
	 * @return a string
	 * @see Formula#fixRelAddr
	 * @see FileOp#saveTableModel
	 * @see EditOp#cut
	 * @see EditOp#copy
	 * @see SpreadsheetClipboard
	 */
	public String toString(CellRange range, boolean byValue, char delim) {
		StringBuffer sbf = new StringBuffer();

		for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
			for (int j = range.getStartCol(); j <= range.getEndCol(); j++) {
				if (byValue) {
					sbf.append(getValueAt(i, j));
				} else {
					Cell cell = getCellAt(i, j);

					// if cell is not empty
					if (cell != null) {
						sbf.append(cell.toString());
					}
				}

				if (j < range.getEndCol()) {
					sbf.append(delim);
				}
			}
			sbf.append("\n");
		}

		String text = sbf.toString();

		// Debug.println(text);
		return text;
	}

	/**
	 * convert the whole table to a string.
	 * 
	 * @return a string
	 * 
	 * @see FileOp
	 */
	public String toString(char delim) {
		return toString(new CellRange(0, m.getRowCount() - 1, 0, m.getColumnCount() - 1), false, delim);
	}

	public String toString() {
		return toString(new CellRange(0, m.getRowCount() - 1, 0, m.getColumnCount() - 1), false, '\t');
	}

	/**
	 * This method updates the values of all cells that reference this one. It
	 * recursively updates all cells that depend on this one and cells that
	 * depend on those, etc.
	 * 
	 * @param aRow
	 *            row of cell to update
	 * @param aColumn
	 *            column of cell to update
	 */
	public void updateRefs(int aRow, int aColumn) {
		Cell temp = getCellAt(aRow, aColumn);
		if (temp == null) {
			return;
		}

		TreeSet set = getRefs(aRow, aColumn);

		// mark it as "needsRecalc";
		Iterator it = set.iterator();
		while (it.hasNext()) {
			CellPoint point = (CellPoint) it.next();
			Formula formula = getCellAt(point.getRow(), point.getCol()).getFormula();

			formula.setNeedsRecalc(true);

			// make sure JTable refreshes it
			// fireTableCellUpdated(point.getRow(), point.getCol());
		}

		// recalculate
		it = set.iterator();
		while (it.hasNext()) {
			CellPoint point = (CellPoint) it.next();
			try {
				getNumericValueAt(point.getRow(), point.getCol());
			} catch (ParserException e) {
			}

			// make sure JTable refreshes it
			fireDiscardCell(point.getRow(), point.getCol());
			m.fireTableCellUpdated(point.getRow(), point.getCol());

		}

		// make sure to tell JTable things have changed
		fireDiscardCell(aRow, aColumn);
		m.fireTableCellUpdated(aRow, aColumn);
	}

	/**
	 * This method is used to implement the fills of the spreadsheet. It takes a
	 * range and fills the range with the object. For formula, it is equivalent
	 * to pasting the formula on every cell in the range.
	 * 
	 * @param range
	 *            range to fill
	 * @param input
	 *            object to fill range with
	 */
	public void fill(CellRange range, Object input) {
		// loop through range
		for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
			for (int j = range.getStartCol(); j <= range.getEndCol(); j++) {
				doSetValueAt(input, i, j);
			}
		}
	}

	public void fillRange(CellRange range, String s) {
		fill(range, fieldParser(s, range.getminCorner()));
	}

	/**
	 * This method associated the proper undo object to this SharpTableModel. It
	 * must be called right after the constructor.
	 * 
	 * @param h
	 *            the History object to associate with this SharpTableModel
	 */
	public void setHistory(History h) {
		history = h;
	}

	/**
	 * fromString is used to convert a string to valus in a range of cells. One
	 * row per line, and each column is tab-delimited.
	 * 
	 * @param text
	 *            the string
	 * @param rowOff
	 *            the row offset
	 * @param colOff
	 *            the column offset
	 * @param range
	 *            the range to paste
	 * @see Formula#fixRelAddr
	 * @see FileOp#openTableModel
	 * @see EditOp#paste
	 * @see SpreadsheetClipboard
	 */
	public void fromString(String text, char delim, int rowOff, int colOff, CellRange range) {
		try {
			BufferedReader in = new BufferedReader(new StringReader(text));
			String line;
			int row = range.getStartRow();

			while (row <= range.getEndRow()) {
				line = in.readLine();

				int index;
				int prev = 0;

				// set col to startCol before each loop
				int col = range.getStartCol();
				String value;

				while (col <= range.getEndCol()) {
					index = line.indexOf(delim, prev);
					if (index >= 0) {
						value = line.substring(prev, index);
					} else {
						value = line.substring(prev);
					}

					if (value.startsWith("=")) {
						// need to fix relative address
						value = Formula.fixRelAddr(value.substring(1), rowOff, colOff);
						if (value == null) {
							value = new String("=$REFS$0");
						} else {
							value = "=" + value;
						}
					}

					doSetValueAt(value, row, col);

					prev = index + 1;

					// increment column number
					col++;

					if (index == -1) {
						break;
					}
				}

				row++;
			}
		} catch (Exception e) {
		}
	}

	/**
	 * This is a static method that parses string input passed to it from
	 * somewhere else. It parses the input and returns the appropriate object
	 * including formula object. It is used to create appropriate objects to
	 * pass to the other methods in table model.
	 * 
	 * @param input
	 *            the input string to parse
	 * @param c
	 *            the point in table where this input to be placed
	 * @return the appropriate object after parsing
	 */
	private static Object fieldParser(String input, CellPoint c) {
		if (input == null) {
			return new String("");
		}

		int row = c.getRow();
		int col = c.getCol();

		/* try making it a formula */
		if (input.startsWith("=")) {
			Formula form = null;

			// create formula and its value and put in a cell
			try {
				return new Formula(input.substring(1), row, col);
			} catch (ParserException e) {
				// no parsing
				return new Formula(input.substring(1), row, col, e);
			}
		} else {
			/* try making it a number */
			try {
				// try {
				// return new Integer(input);
				// }
				// catch (NumberFormatException e) {
				return new Float(input);

				// }
			} catch (NumberFormatException e2) {
				/* all else fails treat as string */
				return input;
			}
		}
	}

	/**
	 * used to make sorting method and helper methods for sort treat row sorting
	 * the same as column sorting.
	 * 
	 * @param interest
	 *            criteria coordinate for sort
	 * @param i
	 *            other coordinate
	 * @param isRow
	 *            true if coordinates are in form (row,col)
	 * @return cell at those coordinates
	 */
	private Cell getCriteria(int interest, int i, boolean isRow) {
		if (isRow) {
			return getCellAt(interest, i);
		} else {
			return getCellAt(i, interest);
		}
	}

	/**
	 * Starting from cell, detect potential reference loops.
	 * 
	 * @param cell
	 *            specified CellPoint
	 */
	private boolean isLoop(CellPoint cell) {
		return isLoop(cell, new TreeSet());
	}

	/**
	 * Starting from cell, detect potential reference loops.
	 * 
	 * @param cell
	 *            specified CellPoint
	 */
	private boolean isLoop(CellPoint cell, TreeSet set) {
		if (set.contains(cell)) {
			return true;
		}

		Cell objCell = getCellAt(cell.getRow(), cell.getCol());
		if (objCell == null) {
			return false;
		}

		Formula formula = objCell.getFormula();
		if (formula == null) {
			return false;
		}

		set.add(cell);

		Iterator it = formula.getDependency().iterator();
		while (it.hasNext()) {
			CellPoint newCell = (CellPoint) it.next();
			boolean ret = isLoop(newCell, set);
			if (ret) {
				return true;
			}
		}

		set.remove(cell);

		return false;
	}

	/**
	 * This method gets the set of cells that will be affects by a value change
	 * for the cpecified cell.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the column
	 * 
	 */
	private TreeSet getRefs(int row, int col) {
		TreeSet set = new TreeSet();
		getRefs(row, col, set);

		return set;
	}

	/**
	 * This method is a helper method for getReds(int, int). It recursively gets
	 * refs for each cell and merges it into the set.
	 * 
	 * @param row
	 *            the row
	 * @param col
	 *            the column
	 * @param set
	 *            the current of cells
	 * 
	 */
	private void getRefs(int row, int col, TreeSet set) {
		Cell cell = getCellAt(row, col);
		if ((cell == null) || !cell.hasRefs()) {
			return;
		}

		Iterator it = cell.getRefs().iterator();
		while (it.hasNext()) {
			CellPoint point = (CellPoint) it.next();
			set.add(point);
			getRefs(point.getRow(), point.getCol(), set);
		}
	}

	/**
	 * Helper method for insertColumn. This method will not send the appropriate
	 * notification to JTable. Please use insertColumn method instead.
	 */
	private void addColumn() {

		((SpreadsheetDefaultTableModel) m).getColumnIdentifiers().addElement(null);

		/* Initialize the new column */
		Iterator it = ((SpreadsheetDefaultTableModel) m).getDataVector().iterator();

		// Give column the appropriate label
		if (it.hasNext()) {
			Cell temp = new Cell(Node.translateColumn(m.getColumnCount() - 1));
			((Vector) it.next()).addElement(temp);
		}

		// initialize cells
		while (it.hasNext()) {
			((Vector) it.next()).addElement(new Cell(""));
		}

		// Generate notification

		/*
		 * newColumnsAdded(new TableModelEvent(this, 0, getRowCount() - 1,
		 * getColumnCount() - 1, TableModelEvent.INSERT));
		 */
	}

	/**
	 * Adds row to end of table
	 */
	private void addRow() {

		// create a new row with appropriate label
		Vector rowData = new Vector();
		rowData.add(0, new Cell(new Integer(m.getRowCount() + 1)));

		// add it to the table
		((SpreadsheetDefaultTableModel) m).addRow(rowData);

		for (int i = 1; i < m.getColumnCount(); i++) {
			m.setValueAt(new Cell(""), m.getRowCount() - 1, i);
		}
	}

	/**
	 * This is a helper function that compares rows or columns
	 * 
	 * @param data
	 *            an array of cells
	 * @param primary
	 *            first criteria to sort by
	 * @param secondary
	 *            second criteria to sort by (set to -1 if not specified)
	 * @param isRow
	 *            true if the criteria are rows
	 * @param i
	 *            column or row you are comparing
	 * @param j
	 *            column or row to compare i to
	 * @return -1 if i < j, 0 if i = j, 1 if i > j
	 */
	private int compareLines(int primary, boolean isRow, boolean ascending, int i, int j) {
		Cell x = getCriteria(primary, i, isRow);
		Cell y = getCriteria(primary, j, isRow);

		return x.compare(y, ascending);
	}

	/**
	 * Helper for sort that does the sorting. To implement different algorithms
	 * for sorting modify this method. Returns an index array after index
	 * sorting
	 * 
	 * @param area
	 *            area to sort in
	 * @param primary
	 *            primary criteria to sort
	 * @param second
	 *            secondary criteria (set equal to primary if not specified)
	 * @param isRow
	 *            true if criteria are rows
	 * @param ascend
	 *            true if sort ascending by primary
	 * @param tiebreaker
	 *            true if sort ascending by secondary
	 * @return index array with row/col numbers of how cells should be arranged.
	 */
	private int[] internalSort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker) {
		// initialize index array
		int[] index;
		if (isRow) {
			index = new int[area.getWidth()];
			for (int i = 0; i < index.length; i++) {
				index[i] = i + area.getStartCol();
			}
		} else {
			index = new int[area.getHeight()];
			for (int i = 0; i < index.length; i++) {
				index[i] = i + area.getStartRow();
			}
		}

		int j;

		for (int p = 1; p < index.length; p++) {
			int tmp = index[p];

			for (j = p; ((j > 0) && rightOrder(primary, second, isRow, tmp, index[j - 1], ascend, tiebreaker)); j--) {
				index[j] = index[j - 1];
			}
			index[j] = tmp;
		}

		return index;
	}

	private void removeColumn() {

		int lastRow = m.getRowCount() - 1;
		int lastCol = m.getColumnCount() - 1;

		// remove the data from cells to delete to maintain references
		clearRange(new CellRange(0, lastRow, lastCol, lastCol));

		Iterator it = ((SpreadsheetDefaultTableModel) m).getDataVector().iterator();
		while (it.hasNext()) {
			/*
			 * Since deleting B makes C the new B, the reference lists in cells
			 * of old "B" should not change. So, we only shift the data in cells
			 * right and deleted the last columns.
			 */
			Vector temp = (Vector) it.next();
			temp.removeElementAt(m.getColumnCount() - 1);
		}

		// update inherited field from DefaultTableModel
		Vector columnIdentifiers = ((SpreadsheetDefaultTableModel) m).getColumnIdentifiers();
		columnIdentifiers.removeElementAt(columnIdentifiers.size() - 1);

		// Notification is generated within the GUI

	}

	/**
	 * Determines if cells are in the wrong order Used only as helper method for
	 * sort.
	 */
	private boolean rightOrder(int primary, int second, boolean isRow, int i, int j, boolean ascend, boolean order) {
		// compare by first criteria
		int result = compareLines(primary, isRow, ascend, i, j);

		// if equal, use second as tiebreaker
		if (result == 0) {
			result = compareLines(second, isRow, order, i, j);

			if (order) {
				return (result < 0);
			} else {
				return (result > 0);
			}

			// otherwise just return results from primary criteria
		} else {
			if (ascend) {
				return (result < 0);
			} else {
				return (result > 0);
			}
		}
	}

	public void historyAdd(CellRange range) {
		history.add(range);
	}

	public void historyAdd(SpreadsheetClipboard clip) {
		history.add(clip);
	}

	public void historyAdd(CellRange range, int type) {
		history.add(range, type);
	}
	
	
	public void cellsChangeAdd(CellRange range) {
		history.fireCellsChangeEvent(range);		
	}
	
	public void cellsChangeAdd(CellRange range, int type) {
		history.fireCellsChangeEvent(range);		
	}
	
	public void cellsChangeAdd(SpreadsheetClipboard clip) {

	}

	public void undo() {
		um.undo();
	}

	public boolean canUndo() {
		return um.canUndo();
	}

	public boolean canRedo() {
		return um.canRedo();
	}

	public void redo() {
		um.redo();
	}

	public RServices getR() {
		return DirectJNI.getInstance().getRServices();
	}

	private void updateUndoAction() {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).updateUndoAction();
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	private void updateRedoAction() {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).updateRedoAction();
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	public void fireSetSelection(String origin, CellRange sel) {
		Vector<SpreadsheetListenerRemote> spreadsheetListenersToRemove = new Vector<SpreadsheetListenerRemote>();
		for (int i = 0; i < _spreadsheetListeners.size(); ++i) {
			try {
				_spreadsheetListeners.elementAt(i).setSelection(origin, sel);
			} catch (Exception e) {
				e.printStackTrace();
				spreadsheetListenersToRemove.add(_spreadsheetListeners.elementAt(i));
			}
		}
		_spreadsheetListeners.removeAll(spreadsheetListenersToRemove);
	}

	public void setSpreadsheetSelection(String origin, CellRange sel) throws RemoteException {
		fireSetSelection(origin, sel);
	}

	Vector<SpreadsheetListenerRemote> _spreadsheetListeners = new Vector<SpreadsheetListenerRemote>();

	public void addSpreadsheetListener(SpreadsheetListenerRemote l) {
		_spreadsheetListeners.add(l);

	}

	public void removeSpreadsheetListener(SpreadsheetListenerRemote l) {
		_spreadsheetListeners.remove(l);
	}

	public void removeAllSpreadsheetListeners() {
		_spreadsheetListeners.removeAllElements();
	}

	public void addSpreadsheetListener(SpreadsheetListener l) {
		throw new RuntimeException("Shouldn't be called");
	}

	public void removeSpreadsheetListener(SpreadsheetListener l) {
		throw new RuntimeException("Shouldn't be called");
	}

	public void addTableModelListener(TableModelListenerRemote l) {
		// TODO Auto-generated method stub
		super.addTableModelListener(l);
	}

	@Override
	public int findColumn(String columnName) {
		// TODO Auto-generated method stub
		return super.findColumn(columnName);
	}

	private static int spreadsheetTableModelRemoteCounter = 0;
	
	
	public static String SPREADSHEET_NAME_PREFIX="SS_";
	private String _id =  SPREADSHEET_NAME_PREFIX + (spreadsheetTableModelRemoteCounter++);

	public String getSpreadsheetModelId() throws RemoteException {
		return _id;
	}

	public String getName() {
		return _id;
	}
	
	public void dispose() throws RemoteException {

	}

	private HashMap<String, SpreadsheetModelDevice> _spreadsheetDeviceHashMap = new HashMap<String, SpreadsheetModelDevice>();

	public SpreadsheetModelDevice newSpreadsheetModelDevice() throws RemoteException {
		SpreadsheetModelDeviceImpl result = new SpreadsheetModelDeviceImpl((SpreadsheetModelRemote) java.rmi.server.RemoteObject.toStub(this),
				_spreadsheetDeviceHashMap);
		addSpreadsheetListener((SpreadsheetListenerRemote) java.rmi.server.RemoteObject.toStub(result));
		addTableModelListener((TableModelListenerRemote) java.rmi.server.RemoteObject.toStub(result));
		return result;
	}

	public SpreadsheetModelDevice[] listSpreadsheetModelDevice() throws RemoteException {
		SpreadsheetModelDevice[] result = new SpreadsheetModelDevice[_spreadsheetDeviceHashMap.size()];
		int i = 0;
		for (SpreadsheetModelDevice d : _spreadsheetDeviceHashMap.values())
			result[i++] = d;
		return result;
	}

	public HashMap<Integer, Object> getRangeHashMap(CellRange range) throws RemoteException {
		int endRow = Math.min(range.getEndRow(), getRowCount() - 1);
		int endCol = Math.min(range.getEndCol(), getColumnCount() - 1);
		HashMap<Integer, Object> result = new HashMap<Integer, Object>();
		for (int l = range.getStartRow(); l <= endRow; ++l) {
			for (int k = range.getStartCol(); k <= endCol; ++k) {
				result.put(l * 65536 + k, getValueAt(l, k));
			}
		}
		return result;
	}


	public void paste(int startRow, int startCol, String trstring) throws RemoteException {
		CellPoint copyPoint = new CellPoint(0, 0);

		int rowOff = startRow - copyPoint.getRow();
		int colOff = startCol - copyPoint.getCol();

		try {
			CellPoint size = SpreadsheetTableModel.getSize(trstring, '\t');
			/*
			 * if ( (startRow + size.getRow()) > getRowCount() || startCol +
			 * size.getCol()>getColumnCount()) {
			 * Toolkit.getDefaultToolkit().beep(); }
			 */

			int endRow = Math.min(getRowCount() - 1, (startRow + size.getRow()) - 1);
			int endCol = Math.min(getColumnCount() - 1, (startCol + size.getCol()) - 1);

			CellRange affectedRange = new CellRange(startRow, endRow, startCol, endCol);

			// add to history
			historyAdd(affectedRange);
			fromString(trstring, '\t', rowOff, colOff, affectedRange);
			fireSetSelection(null, affectedRange);
			cellsChangeAdd(affectedRange);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
