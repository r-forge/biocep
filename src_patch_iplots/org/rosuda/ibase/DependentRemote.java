package org.rosuda.ibase;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface DependentRemote extends Remote {
	 public void Notifying(NotifyMsg msg, Object src, Vector path) throws RemoteException;
}
