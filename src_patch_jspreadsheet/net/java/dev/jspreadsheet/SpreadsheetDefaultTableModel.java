package net.java.dev.jspreadsheet;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class SpreadsheetDefaultTableModel extends DefaultTableModel implements TableModel {
	
	 public SpreadsheetDefaultTableModel() {
		super();
	}

	public SpreadsheetDefaultTableModel(int rowCount, int columnCount) {
		super(rowCount, columnCount);
	}

	public SpreadsheetDefaultTableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	public SpreadsheetDefaultTableModel(Object[][] data, Object[] columnNames) {
		super(data, columnNames);
	}

	public SpreadsheetDefaultTableModel(Vector columnNames, int rowCount) {
		super(columnNames, rowCount);
	}

	public SpreadsheetDefaultTableModel(Vector data, Vector columnNames) {
		super(data, columnNames);
	}

	public Vector getColumnIdentifiers() {
         return columnIdentifiers;
    }

}
