package org.kchine.r.server.iplots;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.rosuda.ibase.SMarkerInterfaceRemote;

public interface SVarSetInterfaceRemote extends Remote{
	
	public int count() throws RemoteException;
	public SVarInterfaceRemote at(int i) throws RemoteException;
	public SVarInterfaceRemote byName(String nam) throws RemoteException;
	public int indexOf(String nam) throws RemoteException;	
	public String getName() throws RemoteException;	
    public void setName(String s) throws RemoteException;        
    public SMarkerInterfaceRemote getMarker() throws RemoteException;    
    
}
