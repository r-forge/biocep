package uk.ac.ebi.microarray.pools;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class CreationCallBack extends UnicastRemoteObject implements ServantCreationListener {
	Remote[] _managedServantHolder;
	RemoteException[] _remoteExceptionHolder;

	public CreationCallBack(Remote[] managedServantHolder, RemoteException[] remoteExceptionHolder) throws RemoteException {
		super();
		_managedServantHolder = managedServantHolder;
		_remoteExceptionHolder = remoteExceptionHolder;
	}

	public void setServantStub(Remote servant) throws RemoteException {
		System.out.println("received:" + PoolUtils.stubToHex(servant));
		_managedServantHolder[0] = servant;
	}

	public void setRemoteException(RemoteException remoteException) throws RemoteException {
		System.out.println("received:" + PoolUtils.getStackTraceAsString(remoteException));
		_remoteExceptionHolder[0] = remoteException;
	}

}