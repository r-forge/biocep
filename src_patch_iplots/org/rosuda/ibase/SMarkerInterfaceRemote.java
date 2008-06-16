package org.rosuda.ibase;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Vector;

public interface SMarkerInterfaceRemote extends NotifierInterfaceRemote, CommanderRemote{
	public void resize(int newsize) throws RemoteException;
    public int size() throws RemoteException;
    public int marked() throws RemoteException;
    public int get(int pos) throws RemoteException;
    public int getSec(int pos) throws RemoteException;
    public boolean at(int pos) throws RemoteException;
    public Vector getList() throws RemoteException;
    public int[] getSelectedIDs() throws RemoteException;
    public int[] getMaskCopy(int maskType) throws RemoteException;
    public void set(int pos, boolean pMark) throws RemoteException;
    public void setSec(int pos, int mark) throws RemoteException;
    public void setSelected(int mark) throws RemoteException;
    public int getMaxMark() throws RemoteException;
	public int getSecCount() throws RemoteException;
    public Enumeration elements() throws RemoteException;
    public void selectNone() throws RemoteException;
    public void selectAll() throws RemoteException;	
    public void selectInverse() throws RemoteException;
	public void resetSec() throws RemoteException;
    public SVarSetInterface getMasterSet() throws RemoteException;
    public void setMasterSet(SVarSetInterface varset) throws RemoteException;    
    public void setSecBySelection(int markSel, int markNonsel) throws RemoteException;
}
