package uk.ac.ebi.microarray.pools;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class CreationCallBack extends UnicastRemoteObject implements ServantCreationListener {
	ManagedServant[] _managedServantHolder;
	RemoteException[] _remoteExceptionHolder;

	public CreationCallBack(ManagedServant[] managedServantHolder, RemoteException[] remoteExceptionHolder) throws RemoteException {
		super();
		_managedServantHolder = managedServantHolder;
		_remoteExceptionHolder = remoteExceptionHolder;
	}

	public void setServantStub(ManagedServant servant) throws RemoteException {
		System.out.println("received:" + PoolUtils.stubToHex(servant));
		_managedServantHolder[0] = servant;
	}
	
	public void setRemoteException(RemoteException remoteException) throws RemoteException {
		System.out.println("received:" + PoolUtils.getStackTraceAsString(remoteException));
		_remoteExceptionHolder[0] = remoteException;
	}
	
}