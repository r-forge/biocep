package org.rosuda.ibase;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

public class DependentRemoteImpl extends UnicastRemoteObject implements DependentRemote{
	Dependent _dep;
	public DependentRemoteImpl(Dependent dep) throws RemoteException{
		super();
		_dep=dep;
	}
	public void Notifying(NotifyMsg msg, Object src, Vector path) throws RemoteException {
		_dep.Notifying(msg, src, path);		
	}
}
