package model;

import java.util.EventListener;
import java.util.HashMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

public class ModelUtils {
	public static AbstractTableModel getTableModelWrapper(final TableModelRemote modelRemote) {
		return new AbstractTableModel(){
			private HashMap<TableModelListener, TableModelListenerRemoteImpl> modelListenerHashMap=new HashMap<TableModelListener, TableModelListenerRemoteImpl>();
			
			public void addTableModelListener(TableModelListener l) {			
				try {
					TableModelListenerRemoteImpl tableModelListenerRemoteImpl=new TableModelListenerRemoteImpl(l);
					modelRemote.addTableModelListener((TableModelListenerRemote)java.rmi.server.RemoteObject.toStub(tableModelListenerRemoteImpl));
					modelListenerHashMap.put(l, tableModelListenerRemoteImpl);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}							
			}

			public Class<?> getColumnClass(int columnIndex) {
				try {					
					return modelRemote.getColumnClass(columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getColumnCount() {
				try {					
					return modelRemote.getColumnCount();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public String getColumnName(int columnIndex) {
				try {					
					return modelRemote.getColumnName(columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getRowCount() {
				try {					
					return modelRemote.getRowCount();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				try {					
					return modelRemote.getValueAt(rowIndex, columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				try {					
					return modelRemote.isCellEditable(rowIndex, columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void removeTableModelListener(TableModelListener l) {
				try {					
					TableModelListenerRemoteImpl tableModelListenerRemoteImpl=modelListenerHashMap.get(l);
					modelRemote.removeTableModelListener(tableModelListenerRemoteImpl);
					modelListenerHashMap.remove(l);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}

			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				try {					
					 modelRemote.setValueAt(value, rowIndex, columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}
			
			
			public int findColumn(String columnName) {
				try {					
					 return modelRemote.findColumn(columnName);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	

			}
			
			public void fireTableCellUpdated(int row, int column)  {
				try {					
					 modelRemote.fireTableCellUpdated(row,column);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}
			
			public void fireTableChanged(TableModelEvent e) {
				try {					
					 modelRemote.fireTableChanged(e);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}			
			
			public void fireTableDataChanged()  {
				try {					
					 modelRemote.fireTableDataChanged();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
					
			public void fireTableRowsDeleted(int firstRow, int lastRow) {
				try {					
					 modelRemote.fireTableRowsDeleted(firstRow,lastRow);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}		
			
			public void fireTableRowsInserted(int firstRow, int lastRow)  {
				try {					
					 modelRemote.fireTableRowsInserted(firstRow,lastRow);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public void fireTableRowsUpdated(int firstRow, int lastRow)  {
				try {					
					 modelRemote.fireTableRowsUpdated(firstRow,lastRow);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public void fireTableStructureChanged()  {
				try {					
					 modelRemote.fireTableStructureChanged();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public <T extends EventListener> T[] getListeners(Class<T> listenerType)  {
				try {					
					 return modelRemote.getListeners(listenerType);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public TableModelListener[] getTableModelListeners() {
				try {					
					 return modelRemote.getTableModelListeners();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}		
			}		
			
		};
	}
}
