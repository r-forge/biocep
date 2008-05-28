package model;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.swing.event.TableModelEvent;

public interface TableModelListenerRemote extends Remote {		
	public void tableChanged(TableModelEvent e) throws RemoteException;
}