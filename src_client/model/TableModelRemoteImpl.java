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
	
	public void addTableModelListener(final TableModelListenerRemote l) throws RemoteException {
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

	public Class<?> getColumnClass(int columnIndex) throws RemoteException {
		return m.getColumnClass(columnIndex);
	}

	public int getColumnCount() throws RemoteException {
		return m.getColumnCount();
	}

	public String getColumnName(int columnIndex) throws RemoteException {
		return m.getColumnName(columnIndex);
	}

	public int getRowCount() throws RemoteException {
		return m.getRowCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex) throws RemoteException {
		return m.getValueAt(rowIndex, columnIndex);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) throws RemoteException {
		return m.isCellEditable(rowIndex, columnIndex);
	}

	public void removeTableModelListener(TableModelListenerRemote l) throws RemoteException {
		TableModelListener listener=modelListenerHashMap.get(l);
		if (listener!=null) {
			m.removeTableModelListener(listener);
			modelListenerHashMap.remove(l);
		}
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) throws RemoteException {
		m.setValueAt(value, rowIndex, columnIndex);
		System.out.println("value="+value);
		System.out.println(new Date()+"value="+value+" value class="+value.getClass().getName());
		
	}
	
	public int findColumn(String columnName) throws RemoteException {
		return m.findColumn(columnName);
	}
	
	public void fireTableCellUpdated(int row, int column) throws RemoteException {
		m.fireTableCellUpdated(row,column);
	}
	
	public void fireTableChanged(TableModelEvent e) throws RemoteException {
		m.fireTableChanged(e);
	}
	
	
	public void fireTableDataChanged() throws RemoteException {
		m.fireTableDataChanged();
	}
			
	public void fireTableRowsDeleted(int firstRow, int lastRow) throws RemoteException {
		m.fireTableRowsDeleted(firstRow, lastRow);
	}		
	
	public void fireTableRowsInserted(int firstRow, int lastRow) throws RemoteException {
		m.fireTableRowsInserted(firstRow, lastRow);
	}
	
	public void fireTableRowsUpdated(int firstRow, int lastRow) throws RemoteException {
		m.fireTableRowsUpdated(firstRow, lastRow);
	}
	
	public void fireTableStructureChanged() throws RemoteException {
		m.fireTableStructureChanged();
	}
	
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) throws RemoteException {
		return m.getListeners(listenerType);
	}
	
	public TableModelListener[] getTableModelListeners() throws RemoteException {
		return m.getTableModelListeners();			
	}		
}