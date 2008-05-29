package net.java.dev.jspreadsheet;

import graphics.rmi.RGui;
import java.util.EventListener;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.undo.UndoManager;

import model.ModelUtils;
import model.SpreadsheetAbstractTableModel;
import remoting.RServices;

/**
 * This is the table data structure of the spreadsheet (i.e. the heart of the
 * backend). Although this class implements the methods needed for
 * <code>TableModel</code>, it also has methods for maintaining dependencies
 * and some data manipulation.
 * <P>
 * Note: This structure only holds Cell objects! It's methods take into account
 * the Cell object. If you modified the cell class please check the methods in
 * this class.
 * 
 * @author Ricky Chin
 * @version $Revision: 1.1 $
 */
public class SpreadsheetTableModelBis extends AbstractTableModel implements  SpreadsheetTableModelClipboardInterface {



	/** Stores file name of current document */
	private JTable table;


	private RGui rgui;

	private SpreadsheetAbstractTableModel m;


	/**
	 * This constructor creates an empty table with numRow rows and numCol
	 * columns including the row and column label row and column.
	 * <P>
	 * The zeroth column are headers. The rows are numbered 1, 2, ... and
	 * columns are labeled A, B, ... . Thus, it really holds numRow X numCol-1
	 * of data. The table is initialized with no cell objects. Each coordinate
	 * that will have data will have a cell object created for it later by the
	 * getCell method.
	 * 
	 * @param sharp
	 *            gui object to associated with this SharpTableModel
	 * @param numRows
	 *            total number of rows including row header
	 * @param numColumns
	 *            total number of columns including column header
	 */
	public SpreadsheetTableModelBis(JTable table, SpreadsheetAbstractTableModel m, RGui rgui) {
		// initialize state to unmodified and file to untitled
		this.table = table;
		this.rgui = rgui;
		this.m = m;
	}

	public void addTableModelListener(TableModelListener l) {
		m.addTableModelListener(l);
	}

	public void removeTableModelListener(TableModelListener l) {
		m.removeTableModelListener(l);
	}

	public int getColumnCount() {
		return m.getColumnCount();
	}

	public int getRowCount() {
		return m.getRowCount();
	}
		

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
		return m.getCellAt(aRow, aColumn);
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
		return m.isDeletionSafe(range, byRow);
	}

	/**
	 * Sets modified state of current document
	 * 
	 * @param modified
	 *            true sets state to modified
	 */
	public void setModified(boolean modified) {
		m.setModified(modified);
	}

	/**
	 * Returns modified state of document
	 * 
	 * @return document's modified value - true or false
	 */
	public boolean isModified() {
		return m.isModified();
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
		throw new RuntimeException("Shouldn't be Called");
	}

	/**
	 * Sets modified state of password; can't undo
	 * 
	 * @param modified
	 *            sets state to modified
	 * @see FileOp#setPassword
	 */
	public void setPasswordModified(boolean modified) {
		m.setPasswordModified(modified);
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
		return m.getValueAt(aRow, aColumn);
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
		m.setRange(range, data);
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
		m.setRange(range, data, byValue);
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
		m.setValueAt(aValue, aRow, aColumn);
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
	 * This method inserts columns to the left of the range with the number of
	 * new columns equal to the number of columns in the range.
	 * 
	 * @param insertRange
	 *            range of cells to add new columns to the left of creates the
	 *            same number of new columns as range has
	 */
	public CellRange insertColumn(CellRange insertRange) {
		return m.insertColumn(insertRange);
	}

	/**
	 * This method inserts rows to the left of range. It adds as many rows as
	 * the range has.
	 * 
	 * @param insertRange
	 *            the range to the left of to add new rows also adds number of
	 *            new rows equal to rows in range
	 */
	public CellRange insertRow(CellRange insertRange) {
		return m.insertRow(insertRange);
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
		return m.look(begin, goal, matchCase, matchCell);
	}

	/**
	 * This method removes columns in the range.
	 * 
	 * @param deletionRange
	 *            the range that contains the columns to delete
	 */
	public CellRange removeColumn(CellRange deletionRange) {
		return m.removeColumn(deletionRange);
	}

	/**
	 * This method removes rows in the range.
	 *
	 * @param deletionRange
	 *            CellRange that contains the rows to delete
	 */
	public CellRange removeRow(CellRange deletionRange) {
		return m.removeRow(deletionRange);
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
		m.sort(area, primary, second, isRow, ascend, tiebreaker);
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
		return m.toString(range, byValue, delim);
	}

	/**
	 * convert the whole table to a string.
	 * 
	 * @return a string
	 * 
	 * @see FileOp
	 */
	public String toString(char delim) {
		return toString(new CellRange(0, getRowCount() - 1, 0, getColumnCount() - 1), false, delim);
	}

	public String toString() {
		return toString(new CellRange(0, getRowCount() - 1, 0, getColumnCount() - 1), false, '\t');
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
		m.fill(range, input);
	}

	public void fillRange(CellRange range, String s) {
		fill(range, ModelUtils.fieldParser(s, range.getminCorner()));
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
		m.fromString(text, delim, rowOff, colOff, range);
	}
	
	public int findColumn(String columnName) {
		return m.findColumn(columnName);
	}

	public void fireTableCellUpdated(int row, int column) {
		m.fireTableCellUpdated(row, column);
	}

	public void fireTableChanged(TableModelEvent e) {
		m.fireTableChanged(e);
	}

	public void fireTableDataChanged() {
		m.fireTableDataChanged();
	}

	public void fireTableRowsDeleted(int firstRow, int lastRow) {

		m.fireTableRowsDeleted(firstRow, lastRow);

	}

	public void fireTableRowsInserted(int firstRow, int lastRow) {

		m.fireTableRowsInserted(firstRow, lastRow);

	}

	public void fireTableRowsUpdated(int firstRow, int lastRow) {

		m.fireTableRowsUpdated(firstRow, lastRow);

	}

	public void fireTableStructureChanged() {

		m.fireTableStructureChanged();

	}

	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {

		return m.getListeners(listenerType);

	}

	public TableModelListener[] getTableModelListeners() {

		return m.getTableModelListeners();
	}

	public RServices getR() {
		return rgui.getR();
	}

	public RGui getRGui() {
		return rgui;
	}
	
	/**
	 * set table selection to the range sel
	 * 
	 * @param sel
	 *            the range to be selected
	 */
	public void setSelection(CellRange sel) {
		// validate sel
		int maxRow = table.getRowCount() - 1;
		int maxCol = table.getColumnCount() - 1;

		int startRow = sel.getStartRow();
		int startCol = sel.getStartCol();
		int endRow = sel.getEndRow();
		int endCol = sel.getEndCol();

		table.setColumnSelectionInterval(Math.min(startCol, maxCol), Math.min(endCol, maxCol));
		table.setRowSelectionInterval(Math.min(startRow, maxRow), Math.min(endRow, maxRow));
	}

	/**
	 * Returns JTable
	 * 
	 * @return JTable
	 */
	public JTable getTable() {
		return table;
	}
	
	public void historyAdd(CellRange range) {
		m.historyAdd(range);
	}
	public void historyAdd(SpreadsheetClipboard clip) {
		m.historyAdd(clip);
	}
	public void historyAdd(CellRange range, int type) {
		m.historyAdd(range,type);
	}
	
	public void undo() {
		m.undo();
	}
	
	public boolean canUndo() {
		return m.canUndo();
	}
	
	public boolean canRedo() {
		return m.canRedo();
	}
	
	public void redo() {
		m.redo();
	}

}
