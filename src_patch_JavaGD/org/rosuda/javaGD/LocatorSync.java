package org.rosuda.javaGD;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LocatorSync extends Remote {
	public double[] waitForAction() throws RemoteException;

	public void triggerAction(double[] result) throws RemoteException;
}
