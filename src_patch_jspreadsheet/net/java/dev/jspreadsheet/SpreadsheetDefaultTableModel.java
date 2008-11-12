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
