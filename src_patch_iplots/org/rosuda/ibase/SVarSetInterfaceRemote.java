package org.rosuda.ibase;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SVarSetInterfaceRemote extends Remote{
	public SVarInterfaceRemote at(int i) throws RemoteException;
    public int count() throws RemoteException;
    public void setName(String s) throws RemoteException;
    public int indexOf(String nam) throws RemoteException;
    public String getName() throws RemoteException;
    public SMarkerInterfaceRemote getMarker() throws RemoteException;    
    public SVarInterfaceRemote byName(String nam) throws RemoteException;
}
