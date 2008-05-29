package model;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

public class TableModelListenerRemoteImpl extends UnicastRemoteObject implements TableModelListenerRemote {
	TableModelListener _listener;
	public TableModelListenerRemoteImpl(TableModelListener listener) throws RemoteException {
		super();
		_listener=listener;
	}
	
	public void tableChanged(TableModelEvent e) throws RemoteException {
		_listener.tableChanged(e);
	}
}