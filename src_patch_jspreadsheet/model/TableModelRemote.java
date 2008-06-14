package model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public interface TableModelRemote extends Remote {		
	public void addTableModelListener(TableModelListenerRemote l) throws RemoteException;
	public void removeTableModelListener(TableModelListenerRemote l)throws RemoteException;
	
	public Class<?> getColumnClass(int columnIndex) throws RemoteException;	
	public int getColumnCount() throws RemoteException;
	public String getColumnName(int columnIndex) throws RemoteException; 
	public int getRowCount() throws RemoteException;
	public Object getValueAt(int rowIndex, int columnIndex) throws RemoteException;
	public boolean isCellEditable(int rowIndex, int columnIndex) throws RemoteException ;	
	public void setValueAt(Object value, int rowIndex, int columnIndex) throws RemoteException;

	public int findColumn(String columnName) throws RemoteException;	
	public void fireTableCellUpdated(int row, int column) throws RemoteException;
	public void fireTableChanged(TableModelEvent e) throws RemoteException;
	public void fireTableDataChanged() throws RemoteException;
	public void fireTableRowsDeleted(int firstRow, int lastRow) throws RemoteException;
	public void fireTableRowsInserted(int firstRow, int lastRow) throws RemoteException;
	public void fireTableRowsUpdated(int firstRow, int lastRow) throws RemoteException;
	public void fireTableStructureChanged() throws RemoteException;
	
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) throws RemoteException;
	public TableModelListener[] getTableModelListeners() throws RemoteException;
	
	
}
