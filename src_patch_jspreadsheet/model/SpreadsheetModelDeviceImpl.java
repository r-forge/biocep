package model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import remoting.RAction;
import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;
import net.java.dev.jspreadsheet.SpreadsheetClipboard;

public class SpreadsheetModelDeviceImpl extends UnicastRemoteObject implements SpreadsheetModelDevice{
	
	private SpreadsheetModelRemote m=null;
	private HashMap<String, SpreadsheetModelDevice> _spreadsheetDeviceHashMap;
	private Vector<RAction> _rActions = new Vector<RAction>();
	private static int _spreadsheetDeviceCounter=0;
	private String _id="SpreadsheetDevice_"+(_spreadsheetDeviceCounter++);

	
	public SpreadsheetModelDeviceImpl(SpreadsheetModelRemote m, HashMap<String, SpreadsheetModelDevice> map) throws RemoteException{
		super();
		this.m=m;
		this._spreadsheetDeviceHashMap=map;
		map.put(_id, this);
	}
	
	
	
	public boolean canRedo() throws RemoteException {
		return m.canRedo();
	}
	
	public boolean canUndo() throws RemoteException {
		return m.canUndo();
	}
	
	public void fill(CellRange range, Object input) throws RemoteException {
		m.fill(range, input);		
	}
	
	public void fromString(String text, char delim, int rowOff, int colOff, CellRange range) throws RemoteException {
		m.fromString(text, delim, rowOff, colOff, range);		
	}
	
	public Cell getCellAt(int row, int column) throws RemoteException {
		return m.getCellAt(row, column);
	}
	
	public void historyAdd(CellRange range, int type) throws RemoteException {
		m.historyAdd(range, type);
	}
	
	public void historyAdd(CellRange range) throws RemoteException {
		m.historyAdd(range);
	}
	
	public void historyAdd(SpreadsheetClipboard clip) throws RemoteException {
		m.historyAdd(clip);
	}
	
	public void insertColumn(CellRange insertRange) throws RemoteException {
		m.insertColumn(insertRange);
	}
	
	public void insertRow(CellRange insertRange) throws RemoteException {
		m.insertRow(insertRange);
	}
	
	public boolean isDeletionSafe(CellRange range, boolean byRow) throws RemoteException {
		return m.isDeletionSafe(range, byRow);
	}
	
	public boolean isModified() throws RemoteException {
		return m.isModified();
	}
	
	public CellPoint look(CellPoint begin, Object goal, boolean matchCase, boolean matchCell) throws RemoteException {
		return m.look(begin, goal, matchCase, matchCell);
	}
	
	public void addSpreadsheetListener(SpreadsheetListenerRemote l) throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public SpreadsheetModelDevice newSpreadsheetModelDevice() throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public SpreadsheetModelDevice[] listSpreadsheetModelDevice() throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public void redo() throws RemoteException {
		m.redo();		
	}
	
	public void removeAllSpreadsheetListeners() throws RemoteException {
		throw new RuntimeException("Shouldn't be called");	
	}
	
	public void removeColumn(CellRange deletionRange) throws RemoteException {
		m.removeColumn(deletionRange);
	}
	
	public void removeRow(CellRange deletionRange) throws RemoteException {
		m.removeRow(deletionRange);
	}
	
	public void removeSpreadsheetListener(SpreadsheetListenerRemote l) throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public void setModified(boolean modified) throws RemoteException {
		m.setModified(modified);		
	}
	
	public void setPasswordModified(boolean modified) throws RemoteException {
		m.setPasswordModified(modified);
	}
	
	public void setRange(CellRange range, Cell[][] data, boolean byValue) throws RemoteException {
		m.setRange(range, data);		
	}
	
	public void setRange(CellRange range, Object[][] data) throws RemoteException {
		m.setRange(range, data);		
	}
	
	public void setSpreadsheetSelection(String origin, CellRange sel) throws RemoteException {
		m.setSpreadsheetSelection(origin, sel);	
	}
	
	public void sort(CellRange area, int primary, int second, boolean isRow, boolean ascend, boolean tiebreaker) throws RemoteException {
		m.sort(area, primary, second, isRow, ascend, tiebreaker);
	}
	
	public String toString(CellRange range, boolean byValue, char delim) throws RemoteException {
		return m.toString(range, byValue, delim);
	}
	
	public void undo() throws RemoteException {
		m.undo();
	}
	
	public void addTableModelListener(TableModelListenerRemote l) throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public int findColumn(String columnName) throws RemoteException {
		return m.findColumn(columnName);
	}
	
	public void fireTableCellUpdated(int row, int column) throws RemoteException {
		m.fireTableCellUpdated(row, column);		
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
	
	public Class<?> getColumnClass(int columnIndex) throws RemoteException {		
		return m.getColumnClass(columnIndex);
	}
	
	public int getColumnCount() throws RemoteException {
		return m.getColumnCount();
	}
	
	public String getColumnName(int columnIndex) throws RemoteException {
		return m.getColumnName(columnIndex);
	}
	
	public <T extends EventListener> T[] getListeners(Class<T> listenerType) throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public int getRowCount() throws RemoteException {
		return m.getRowCount();
	}
	
	public TableModelListener[] getTableModelListeners() throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) throws RemoteException {
		return m.getValueAt(rowIndex, columnIndex);
	}
	
	public boolean isCellEditable(int rowIndex, int columnIndex) throws RemoteException {
		return m.isCellEditable(rowIndex, columnIndex);
	}
	
	public void removeTableModelListener(TableModelListenerRemote l) throws RemoteException {
		throw new RuntimeException("Shouldn't be called");
	}
	
	public void setValueAt(Object value, int rowIndex, int columnIndex) throws RemoteException {
		m.setValueAt(value, rowIndex, columnIndex);
	}
	
	public String getSpreadsheetModelId() throws RemoteException {	
		return m.getSpreadsheetModelId();
	}
	
	public HashMap<Integer, Object> getRangeHashMap(CellRange range) throws RemoteException {
		return m.getRangeHashMap(range);
	}
	
	//-------------------------------
	
	public String getId() throws RemoteException {
		return _id;
	}
	
	public Vector<RAction> popRActions() throws RemoteException {
		if (_rActions.size() == 0)
			return null;
		Vector<RAction> result = (Vector<RAction>) _rActions.clone();
		for (int i = 0; i < result.size(); ++i)	_rActions.remove(0);
		return result;
	}
	
	public void dispose() throws RemoteException {
		final String id = _id;
		new Thread(new Runnable() {
			public void run() {
				boolean shutdownSucceeded = false;
				while (true) {
					try {
						shutdownSucceeded = unexportObject(SpreadsheetModelDeviceImpl.this, false);
					} catch (Exception e) {
						e.printStackTrace();
						shutdownSucceeded = true;
					}
					System.out.println("-----shutdownSucceeded:" + shutdownSucceeded);
					if (shutdownSucceeded) {
						_spreadsheetDeviceHashMap.remove(id);
						break;
					}
					try {Thread.sleep(200);} catch (Exception e) {}
				}
			}
		}).start();
		
	}
	
	//-----------------------------------
	
	public void updateUndoAction() throws RemoteException {
		RAction action = new RAction("updateUndoAction", null);
		_rActions.add(action);
	}
	
	public void updateRedoAction() throws RemoteException {	
		RAction action = new RAction("updateRedoAction", null);
		_rActions.add(action);
	}
	
	public void setSelection(String origin, CellRange sel) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("origin", origin);
		attributes.put("sel", sel);	
		RAction action = new RAction("setSelection", attributes);
		_rActions.add(action);
	}
	
	
	public void discardCache() throws RemoteException {
		RAction action = new RAction("discardCache", null);
		_rActions.add(action);
	}
	
	public void discardCacheCell(int row, int col) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("row", row);
		attributes.put("col", col);	
		RAction action = new RAction("discardCacheCell", attributes);
		_rActions.add(action);	
	}
	
	public void discardCacheRange(CellRange range) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("range", range);
		RAction action = new RAction("discardCacheRange", attributes);
		_rActions.add(action);	
	}
	
	public void discardColumnCount() throws RemoteException {
		RAction action = new RAction("discardColumnCount", null);
		_rActions.add(action);		
	}
	
	public void discardRowCount() throws RemoteException {
		RAction action = new RAction("discardRowCount", null);
		_rActions.add(action);	
	}
	
	
	public void removeColumns(int removeNum) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("removeNum", removeNum);
		RAction action = new RAction("removeColumns", attributes);
		_rActions.add(action);	
	}
	
	public void insertColumn(int insertNum, int startCol) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("insertNum", insertNum);
		attributes.put("startCol", startCol);
		RAction action = new RAction("insertColumn", attributes);
		_rActions.add(action);
	}
	
	public void removeRows(int removeNum) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("removeNum", removeNum);
		RAction action = new RAction("removeRows", attributes);
		_rActions.add(action);	
	}
	
	public void insertRow(int insertNum, int startRow) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("insertNum", insertNum);
		attributes.put("startRow", startRow);
		RAction action = new RAction("insertRow", attributes);
		_rActions.add(action);	
	}
	
	
	//-------------------------------------
	
	public void tableChanged(TableModelEvent e) throws RemoteException {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("e", e);	
		RAction action = new RAction("tableChanged", attributes);
		_rActions.add(action);
	}
	
	//--------------------------------------
	
	
}
