package model;

import http.HttpMarker;
import http.NotLoggedInException;
import java.rmi.RemoteException;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;
import net.java.dev.jspreadsheet.SpreadsheetClipboard;
import remoting.RAction;

public class SpreadsheetModelRemoteProxy implements SpreadsheetModelRemote , HttpMarker {
	SpreadsheetModelDevice _device;
	Thread popThread = null;
	boolean _stopThreads = false;
	Vector<SpreadsheetListenerRemote> _spreadsheetListeners = new Vector<SpreadsheetListenerRemote>();
	Vector<TableModelListenerRemote> _tableModelListeners = new Vector<TableModelListenerRemote>();
	Integer _rowCount;
	Integer _colCount;
	HashMap<Integer, Object> _cellCache=new HashMap<Integer, Object>();

	public SpreadsheetModelRemoteProxy(SpreadsheetModelDevice device) {
		this._device = device;

		popThread = new Thread(new Runnable() {
			public void run() {
				while (true && !_stopThreads) {
					popActions();

					try {
						Thread.sleep(30);
					} catch (Exception e) {
					}
				}
			}
		});
		popThread.start();

	}

	public synchronized void popActions() {
		try {
			Vector<RAction> ractions = _device.popRActions();
			if (ractions != null) {
				for (int i = 0; i < ractions.size(); ++i) {
					final RAction action = ractions.elementAt(i);
					if (action.getActionName().equals("updateUndoAction")) {
						for (int j = 0; j < _spreadsheetListeners.size(); ++j) {
							_spreadsheetListeners.elementAt(j).updateUndoAction();
						}
					} else if (action.getActionName().equals("updateRedoAction")) {
						for (int j = 0; j < _spreadsheetListeners.size(); ++j) {
							_spreadsheetListeners.elementAt(j).updateRedoAction();
						}
					} else if (action.getActionName().equals("setSelection")) {
						String origin = (String) action.getAttributes().get("origin");
						CellRange sel = (CellRange) action.getAttributes().get("sel");
						for (int j = 0; j < _spreadsheetListeners.size(); ++j) {
							_spreadsheetListeners.elementAt(j).setSelection(origin, sel);
						}
					} else if (action.getActionName().equals("removeColumns")) {
						int removeNum= (Integer)action.getAttributes().get("removeNum");
						for (int j = 0; j < _spreadsheetListeners.size(); ++j) {
							_spreadsheetListeners.elementAt(j).removeColumns(removeNum);
						}
					} else if (action.getActionName().equals("insertColumn")) {
												
						int insertNum= (Integer)action.getAttributes().get("insertNum");
						int startCol= (Integer)action.getAttributes().get("startCol");
						for (int j = 0; j < _spreadsheetListeners.size(); ++j) {
							_spreadsheetListeners.elementAt(j).insertColumn(insertNum, startCol);
						}						
					} else if (action.getActionName().equals("removeRows")) {
						int removeNum= (Integer)action.getAttributes().get("removeNum");
						for (int j = 0; j < _spreadsheetListeners.size(); ++j) {
							_spreadsheetListeners.elementAt(j).removeRows(removeNum);
						}
					} else if (action.getActionName().equals("insertRow")) {												
						int insertNum= (Integer)action.getAttributes().get("insertNum");
						int startRow= (Integer)action.getAttributes().get("startRow");
						for (int j = 0; j < _spreadsheetListeners.size(); ++j) {
							_spreadsheetListeners.elementAt(j).insertRow(insertNum, startRow);
						}						
					} else if (action.getActionName().equals("tableChanged")) {
						TableModelEvent e = (TableModelEvent) action.getAttributes().get("e");
						for (int j = 0; j < _tableModelListeners.size(); ++j) {
							_tableModelListeners.elementAt(j).tableChanged(e);
						}
					} else if (action.getActionName().equals("discardRowCount")) {						
						_rowCount = null;						
					} else if (action.getActionName().equals("discardColumnCount")) {						
						_colCount = null;						
					} else if (action.getActionName().equals("discardCache")) {
						_cellCache=new HashMap<Integer, Object>();
					} else if (action.getActionName().equals("discardCacheCell")) {
						int row= (Integer)action.getAttributes().get("row");
						int col= (Integer)action.getAttributes().get("col");
						_cellCache.remove(row*65536+col);
					} else if (action.getActionName().equals("discardCacheRange")) {
						CellRange range= (CellRange)action.getAttributes().get("range");
						for (int l=range.getStartRow(); l<=range.getEndRow();++l) {
							for (int k=range.getStartCol(); k<=range.getEndCol(); ++k) {
								_cellCache.remove(l*65536+k);								
							}
						}
					} 
				}
			}
			
		} catch (NotLoggedInException nle) {
			nle.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void addSpreadsheetListener(SpreadsheetListenerRemote l) throws RemoteException {
		//System.out.println("addSpreadsheetListener");
		_spreadsheetListeners.add(l);
	}

	public void removeSpreadsheetListener(SpreadsheetListenerRemote l) throws RemoteException {
		//System.out.println("removeSpreadsheetListener");
		_spreadsheetListeners.remove(l);
	}

	public void addTableModelListener(TableModelListenerRemote l) throws RemoteException {
		//System.out.println("addTableModelListener");
		_tableModelListeners.add(l);
	}

	public void removeTableModelListener(TableModelListenerRemote l) throws RemoteException {
		//System.out.println("removeTableModelListener");
		_tableModelListeners.remove(l);
	}

	public TableModelListener[] getTableModelListeners() throws RemoteException {
		//System.out.println("getTableModelListeners");
		// return
		// (TableModelListener[])_tableModelListeners.toArray(a)_device.getTableModelListeners();
		return null;
	}

	public void removeAllSpreadsheetListeners() throws RemoteException {
		//System.out.println("removeAllSpreadsheetListeners");
		_spreadsheetListeners.removeAllElements();
	}

	public boolean canRedo() throws RemoteException {
		//System.out.println("canRedo");
		return _device.canRedo();
	}

	public boolean canUndo() throws RemoteException {
		//System.out.println("canUndo");
		return _device.canUndo();
	}

	public void dispose() throws RemoteException {
		//System.out.println("dispose");
		_device.dispose();
	}

	public void fill(CellRange range, Object input) throws RemoteException {
		//System.out.println("fill");
		_device.fill(range, input);
	}

	public int findColumn(String columnName) throws RemoteException {
		//System.out.println("findColumn");
		return _device.findColumn(columnName);
	}

	public void fireTableCellUpdated(int row, int column) throws RemoteException {
		//System.out.println("fireTableCellUpdated");
		_device.fireTableCellUpdated(row, column);
	}

	public void fireTableChanged(TableModelEvent e) throws RemoteException {
		//System.out.println("fireTableChanged");
		_device.fireTableChanged(e);
	}

	public void fireTableDataChanged() throws RemoteException {
		//System.out.println("fireTableDataChanged");
		_device.fireTableDataChanged();
	}

	public void fireTableRowsDeleted(int firstRow, int lastRow) throws RemoteException {
		//System.out.println("fireTableRowsDeleted");
		_device.fireTableRowsDeleted(firstRow, lastRow);
	}

	public void fireTableRowsInserted(int firstRow, int lastRow) throws RemoteException {
		//System.out.println("fireTableRowsInserted");
		_device.fireTableRowsInserted(firstRow, lastRow);
	}

	public void fireTableRowsUpdated(int firstRow, int lastRow) throws RemoteException {
		//System.out.println("fireTableRowsUpdated");
		_device.fireTableRowsUpdated(firstRow, lastRow);
	}

	public void fireTableStructureChanged() throws RemoteException {
		//System.out.println("fireTableStructureChanged");
		_device.fireTableStructureChanged();
	}

	public void fromString(String text, char delim, int rowOff, int colOff, CellRange range) throws RemoteException {
		//System.out.println("fromString");
		_device.fromString(text, delim, rowOff, colOff, range);
	}

	public Cell getCellAt(int row, int column) throws RemoteException {
		//System.out.println("getCellAt");
		return _device.getCellAt(row, column);
	}

	public Class<?> getColumnClass(int columnIndex) throws RemoteException {
		//System.out.println("getColumnClass");
		return _device.getColumnClass(columnIndex);
	}

	public int getColumnCount() throws RemoteException {
		//System.out.println("getColumnCount");
		if (_colCount==null) _colCount=_device.getColumnCount();
		return _colCount;
	}

	public String getColumnName(int columnIndex) throws RemoteException {
		//System.out.println("getColumnName");
		return _device.getColumnName(columnIndex);
	}

	public String getId() throws RemoteException {
		//System.out.println("getId");
		return _device.getId();
	}

	public <T extends EventListener> T[] getListeners(Class<T> listenerType) throws RemoteException {
		//System.out.println("getListeners");
		return _device.getListeners(listenerType);
	}

	public int getRowCount() throws RemoteException {
		//System.out.println("getRowCount");
		if (_rowCount==null) _rowCount=_device.getRowCount();
		return _rowCount;
	}

	public Object getValueAt(int rowIndex, int columnIndex) throws RemoteException {
		if (_cellCache.size()==0) {
			_cellCache=_device.getRangeHashMap(new CellRange(new CellPoint(rowIndex,columnIndex), new CellPoint(rowIndex+52,columnIndex+17)));
		}
				
		//System.out.print("getValueAt");
		Object result=_cellCache.get(rowIndex*65536+columnIndex);
		if (result==null) {
			result=_device.getValueAt(rowIndex, columnIndex);
			_cellCache.put(rowIndex*65536+columnIndex, result);
		} else {
			//System.out.print(" ---> cached ");
		}
		//System.out.println();
		return result;
	}

	public void historyAdd(CellRange range, int type) throws RemoteException {
		//System.out.println("historyAdd");
		_device.historyAdd(range, type);
	}

	public void historyAdd(CellRange range) throws RemoteException {
		//System.out.println("historyAdd");
		_device.historyAdd(range);
	}

	public void historyAdd(SpreadsheetClipboard clip) throws RemoteException {
		//System.out.println("historyAdd");
		_device.historyAdd(clip);
	}

	public void insertColumn(CellRange insertRange) throws RemoteException {
		//System.out.println("insertColumn");
		_device.insertColumn(insertRange);
	}

	public void insertRow(CellRange insertRange) throws RemoteException {
		//System.out.println("insertRow");
		_device.insertRow(insertRange);
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) throws RemoteException {
		//System.out.println("isCellEditable");
		return _device.isCellEditable(rowIndex, columnIndex);
	}

	public boolean isDeletionSafe(CellRange range, boolean byRow) throws RemoteException {
		//System.out.println("isDeletionSafe");
		return _device.isDeletionSafe(range, byRow);
	}

	public boolean isModified() throws RemoteException {
		//System.out.println("isModified");
		return _device.isModified();
	}

	public SpreadsheetModelDevice[] listSpreadsheetModelDevice() throws RemoteException {
		//System.out.println("listSpreadsheetModelDevice");
		return _device.listSpreadsheetModelDevice();
	}

	public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell) throws RemoteException {
		//System.out.println("look");
		return _device.look(begin, goal, matchCase, matchCell);
	}

	public SpreadsheetModelDevice newSpreadsheetModelDevice() throws RemoteException {
		//System.out.println("newSpreadsheetModelDevice");
		return _device.newSpreadsheetModelDevice();
	}

	public Vector<RAction> popRActions() throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}

	public void redo() throws RemoteException {
		//System.out.println("redo");
		_device.redo();
	}

	public void removeColumn(CellRange deletionRange) throws RemoteException {
		//System.out.println("removeColumn");
		_device.removeColumn(deletionRange);
	}

	public void removeRow(CellRange deletionRange) throws RemoteException {
		//System.out.println("removeRow");
		_device.removeRow(deletionRange);
	}

	public void setModified(boolean modified) throws RemoteException {
		//System.out.println("setModified");
		_device.setModified(modified);
	}

	public void setPasswordModified(boolean modified) throws RemoteException {
		//System.out.println("setPasswordModified");
		_device.setPasswordModified(modified);
	}

	public void setRange(CellRange range, Cell[][] data, boolean byValue) throws RemoteException {
		//System.out.println("setRange");
		_device.setRange(range, data, byValue);
	}

	public void setRange(CellRange range, Object[][] data) throws RemoteException {
		//System.out.println("setRange");
		_device.setRange(range, data);
	}

	public void setSelection(String origin, CellRange sel) throws RemoteException {
		//System.out.println("setSelection");
		_device.setSelection(origin, sel);
	}

	public void setSpreadsheetSelection(String origin, CellRange sel) throws RemoteException {
		//System.out.println("setSpreadsheetSelection");
		_device.setSpreadsheetSelection(origin, sel);
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) throws RemoteException {
		//System.out.println("setValueAt");
		_device.setValueAt(value, rowIndex, columnIndex);
	}

	public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker) throws RemoteException {
		//System.out.println("sort");
		_device.sort(area, primary, second, isRow, ascend, tiebreaker);
	}

	public void tableChanged(TableModelEvent e) throws RemoteException {
		//System.out.println("tableChanged");
		_device.tableChanged(e);
	}

	public String toString(CellRange range, boolean byValue, char delim) throws RemoteException {
		//System.out.println("toString");
		return _device.toString(range, byValue, delim);
	}

	public void undo() throws RemoteException {
		//System.out.println("undo");
		_device.undo();
	}

	public void updateRedoAction() throws RemoteException {
		//System.out.println("updateRedoAction");
		_device.updateRedoAction();
	}

	public void updateUndoAction() throws RemoteException {
		//System.out.println("updateUndoAction");
		_device.updateUndoAction();
	}
	
	public String getSpreadsheetModelId() throws RemoteException {
		return _device.getSpreadsheetModelId();
	}
	
	public HashMap<Integer, Object> getRangeHashMap(CellRange range) throws RemoteException {
		return _device.getRangeHashMap(range);
	}
	
	
	public void stopThreads() {
		_stopThreads=true;
		try {popThread.join();} catch (Exception e) {e.printStackTrace();}
		try {
			// IMPORTANT !!!!!!!!!!!!!!!!!!!!!!!!!!!!
			_device.dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}