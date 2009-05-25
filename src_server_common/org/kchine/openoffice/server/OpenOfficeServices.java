package org.kchine.openoffice.server;

import java.rmi.RemoteException;

import org.kchine.rpf.ManagedServant;

public interface OpenOfficeServices extends ManagedServant {
	
	public void convertFile(String inputFile,  String outputFile, String conversionFilter, boolean useserver) throws RemoteException;

}
