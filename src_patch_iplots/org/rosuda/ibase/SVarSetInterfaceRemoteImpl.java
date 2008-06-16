package org.rosuda.ibase;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SVarSetInterfaceRemoteImpl extends UnicastRemoteObject implements SVarSetInterfaceRemote {
	SVarSetInterface _svarset;
	public SVarSetInterfaceRemoteImpl(SVarSetInterface svarset) throws RemoteException{
		super();
		_svarset=svarset;
	}
	
	public SVarInterfaceRemote at(int i) {
		return _svarset.at(i).getRemote();
	}
	
	public SVarInterfaceRemote byName(String nam) {
		return _svarset.byName(nam).getRemote();
	}
	
	public int count() {
		return _svarset.count();
	}
	
	public SMarkerInterfaceRemote getMarker() {
		return _svarset.getMarker().getRemote();
	}
	
	public String getName() {
		return _svarset.getName();
	}
	public int indexOf(String nam) {
		return _svarset.indexOf(nam);
	}
		
	public void setName(String s) {
		_svarset.setName(s);
	}
}
