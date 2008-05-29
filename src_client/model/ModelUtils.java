package model;

import java.util.EventListener;
import java.util.HashMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;
import net.java.dev.jspreadsheet.Formula;
import net.java.dev.jspreadsheet.ParserException;
import net.java.dev.jspreadsheet.SpreadsheetTableModel;

public class ModelUtils {
	
	
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
	public static Object fieldParser(String input, CellPoint c) {
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


	public static SpreadsheetAbstractTableModel getSpreadsheetTableModelWrapper(final SpreadsheetTableModelRemote spreadsheetModelRemote) {
		return new SpreadsheetAbstractTableModel(){
			private HashMap<TableModelListener, TableModelListenerRemoteImpl> modelListenerHashMap=new HashMap<TableModelListener, TableModelListenerRemoteImpl>();
			
			public void addTableModelListener(TableModelListener l) {			
				try {
					TableModelListenerRemoteImpl tableModelListenerRemoteImpl=new TableModelListenerRemoteImpl(l);
					spreadsheetModelRemote.addTableModelListener((TableModelListenerRemote)java.rmi.server.RemoteObject.toStub(tableModelListenerRemoteImpl));
					modelListenerHashMap.put(l, tableModelListenerRemoteImpl);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}							
			}

			public Class<?> getColumnClass(int columnIndex) {
				try {					
					return spreadsheetModelRemote.getColumnClass(columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getColumnCount() {
				try {					
					return spreadsheetModelRemote.getColumnCount();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public String getColumnName(int columnIndex) {
				try {					
					return spreadsheetModelRemote.getColumnName(columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getRowCount() {
				try {					
					return spreadsheetModelRemote.getRowCount();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				try {					
					return spreadsheetModelRemote.getValueAt(rowIndex, columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				try {					
					return spreadsheetModelRemote.isCellEditable(rowIndex, columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void removeTableModelListener(TableModelListener l) {
				try {					
					TableModelListenerRemoteImpl tableModelListenerRemoteImpl=modelListenerHashMap.get(l);
					spreadsheetModelRemote.removeTableModelListener(tableModelListenerRemoteImpl);
					modelListenerHashMap.remove(l);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}

			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				try {					
					 spreadsheetModelRemote.setValueAt(value, rowIndex, columnIndex);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}
			
			
			public int findColumn(String columnName) {
				try {					
					 return spreadsheetModelRemote.findColumn(columnName);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	

			}
			
			public void fireTableCellUpdated(int row, int column)  {
				try {					
					 spreadsheetModelRemote.fireTableCellUpdated(row,column);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}
			
			public void fireTableChanged(TableModelEvent e) {
				try {					
					 spreadsheetModelRemote.fireTableChanged(e);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}			
			
			public void fireTableDataChanged()  {
				try {					
					 spreadsheetModelRemote.fireTableDataChanged();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
					
			public void fireTableRowsDeleted(int firstRow, int lastRow) {
				try {					
					 spreadsheetModelRemote.fireTableRowsDeleted(firstRow,lastRow);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}		
			
			public void fireTableRowsInserted(int firstRow, int lastRow)  {
				try {					
					 spreadsheetModelRemote.fireTableRowsInserted(firstRow,lastRow);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public void fireTableRowsUpdated(int firstRow, int lastRow)  {
				try {					
					 spreadsheetModelRemote.fireTableRowsUpdated(firstRow,lastRow);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public void fireTableStructureChanged()  {
				try {					
					 spreadsheetModelRemote.fireTableStructureChanged();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public <T extends EventListener> T[] getListeners(Class<T> listenerType)  {
				try {					
					 return spreadsheetModelRemote.getListeners(listenerType);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			public TableModelListener[] getTableModelListeners() {
				try {					
					 return spreadsheetModelRemote.getTableModelListeners();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}		
			}		
			
			//------------------------------------------------------------------------------
			
			@Override
			public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker) {
				try {					
					 spreadsheetModelRemote.sort( area,  primary,  second,  isRow,ascend,tiebreaker);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void fill(CellRange range, Object input) {
				try {					
					 spreadsheetModelRemote.fill(range,input);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void fromString(String text, char delim, int rowOff, int colOff, CellRange range) {
				try {					
					 spreadsheetModelRemote.fromString(text,delim,rowOff,colOff,range);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public Cell getCellAt(int row, int column) {
				try {					
					 return spreadsheetModelRemote.getCellAt( row,column);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public CellRange insertColumn(CellRange insertRange) {
				try {					
					 return spreadsheetModelRemote.insertColumn(insertRange);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public CellRange insertRow(CellRange insertRange) {
				try {					
					 return spreadsheetModelRemote.insertRow(insertRange);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public boolean isDeletionSafe(CellRange range, boolean byRow) {
				try {					
					 return spreadsheetModelRemote.isDeletionSafe(range, byRow);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public boolean isModified() {
				try {					
					 return spreadsheetModelRemote.isModified();
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell) {
				try {					
					 return spreadsheetModelRemote.look(begin,goal,matchCase,matchCell);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public CellRange removeColumn(CellRange deletionRange) {
				try {					
					 return spreadsheetModelRemote.removeColumn(deletionRange);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public CellRange removeRow(CellRange deletionRange) {
				try {					
					 return spreadsheetModelRemote.removeRow(deletionRange);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void setModified(boolean modified) {
				try {					
					 spreadsheetModelRemote.setModified(modified);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void setPasswordModified(boolean modified) {
				try {					
					 spreadsheetModelRemote.setPasswordModified(modified);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void setRange(CellRange range, Cell[][] data, boolean byValue) {
				try {					
					 spreadsheetModelRemote.setRange(range,data,byValue);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public void setRange(CellRange range, Object[][] data) {
				try {					
					 spreadsheetModelRemote.setRange(range,data);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}

			@Override
			public String toString(CellRange range, boolean byValue, char delim) {
				try {					
					 return spreadsheetModelRemote.toString(range,byValue,delim);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
		};
	}




}
