package org.kchine.r.server.iplots;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

import org.rosuda.ibase.Dependent;
import org.rosuda.ibase.DependentRemote;
import org.rosuda.ibase.NotifyMsg;

public interface NotifierInterfaceRemote extends Remote{	
    public void addDepend(DependentRemote c) throws RemoteException;
    public void delDepend(DependentRemote c) throws RemoteException;
    public void NotifyAll(NotifyMsg msg, Dependent c) throws RemoteException;
    public void NotifyAll(NotifyMsg msg, Vector path) throws RemoteException;
    public void startCascadedNotifyAll(NotifyMsg msg) throws RemoteException;
    public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) throws RemoteException;
    public void NotifyAll(NotifyMsg msg) throws RemoteException;
    public void beginBatch() throws RemoteException;
    public void endBatch() throws RemoteException;
}
