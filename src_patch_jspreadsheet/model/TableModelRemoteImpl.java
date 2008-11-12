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
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

public  class TableModelRemoteImpl extends UnicastRemoteObject implements TableModelRemote {
	protected AbstractTableModel m;
	private HashMap<TableModelListenerRemote, TableModelListener> modelListenerHashMap=new HashMap<TableModelListenerRemote, TableModelListener>();
	public TableModelRemoteImpl(int rowCount, int colCount) throws RemoteException {
		super();
		m=new DefaultTableModel(rowCount,colCount);
	}
	
	public TableModelRemoteImpl(Object[] columnName, int rowCount) throws RemoteException {
		super();
		m=new DefaultTableModel(columnName,rowCount);
	}
	
	
	public TableModelRemoteImpl(Object[][] data, Object[] columnName) throws RemoteException {
		super();
		m=new DefaultTableModel(data,columnName);
	}
	
	public TableModelRemoteImpl(AbstractTableModel m) throws RemoteException {
		super();
		this.m=m;
	}
	
	public void addTableModelListener(final TableModelListenerRemote l)  {
		TableModelListener listener=new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				try {
					l.tableChanged(e);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		modelListenerHashMap.put(l,listener);
		m.addTableModelListener(listener);			
	}

	public Class<?> getColumnClass(int columnIndex)  {
		return m.getColumnClass(columnIndex);
	}

	public int getColumnCount()  {
		return m.getColumnCount();
	}

	public String getColumnName(int columnIndex)  {
		return m.getColumnName(columnIndex);
	}

	public int getRowCount() {
		return m.getRowCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex)  {
		return m.getValueAt(rowIndex, columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex)  {
		return m.isCellEditable(rowIndex, columnIndex);
	}

	public void removeTableModelListener(TableModelListenerRemote l) {
		TableModelListener listener=modelListenerHashMap.get(l);
		if (listener!=null) {
			m.removeTableModelListener(listener);
			modelListenerHashMap.remove(l);
		}
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex)  {
		m.setValueAt(value, rowIndex, columnIndex);
		System.out.println("value="+value);
		System.out.println(new Date()+"value="+value+" value class="+value.getClass().getName());
		
	}
	
	public int findColumn(String columnName)  {
		return m.findColumn(columnName);
	}
	
	public void fireTableCellUpdated(int row, int column) {
		m.fireTableCellUpdated(row,column);
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
	
	public void fireTableRowsInserted(int firstRow, int lastRow){
		m.fireTableRowsInserted(firstRow, lastRow);
	}
	
	public void fireTableRowsUpdated(int firstRow, int lastRow)  {
		m.fireTableRowsUpdated(firstRow, lastRow);
	}
	
	public void fireTableStructureChanged()  {
		m.fireTableStructureChanged();
	}
	
	public <T extends EventListener> T[] getListeners(Class<T> listenerType)  {
		return m.getListeners(listenerType);
	}
	
	public TableModelListener[] getTableModelListeners() {
		return m.getTableModelListeners();			
	}		
}