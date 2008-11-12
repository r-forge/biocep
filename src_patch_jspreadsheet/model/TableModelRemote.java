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
