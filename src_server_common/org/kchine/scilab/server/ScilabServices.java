package org.kchine.scilab.server;

import java.rmi.RemoteException;

import org.kchine.rpf.ManagedServant;

public interface ScilabServices extends ManagedServant{
	
    public boolean scilabExec(String cmd) throws java.rmi.RemoteException;
    
    public String scilabConsoleSubmit(String cmd) throws java.rmi.RemoteException;    
    public Object scilabGetObject(String expression) throws RemoteException;
    public void scilabPutAndAssign(Object obj, String name) throws RemoteException;
    public void scilabSetWorkingDirectory(String dir) throws java.rmi.RemoteException;
    public String scilabGetStatus() throws java.rmi.RemoteException;    

}
