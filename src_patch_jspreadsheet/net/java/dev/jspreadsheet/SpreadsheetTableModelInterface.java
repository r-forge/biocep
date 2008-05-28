package net.java.dev.jspreadsheet;

import remoting.RServices;

public interface SpreadsheetTableModelInterface {
	public RServices getR();
	public Cell getCellAt(int aRow, int aColumn) ;
	public Number getNumericValueAt(int row, int col) throws ParserException;
	public boolean isEmptyCell(int row, int col);
}
