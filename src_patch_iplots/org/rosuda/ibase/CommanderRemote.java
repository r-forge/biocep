package org.rosuda.ibase;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CommanderRemote extends Remote{
	public Object run(Object o, String cmd) throws RemoteException;
}
