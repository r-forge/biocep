package org.rosuda.ibase;

import java.rmi.RemoteException;

public interface SVarInterfaceRemote extends NotifierInterfaceRemote {
	public int[] getRanked()  throws RemoteException;
	public int[] getRanked(SMarkerInterface m, int markspec) throws RemoteException;
	public int getContentsType() throws RemoteException;
	public void categorize() throws RemoteException;
	public NotifierInterface getNotifier() throws RemoteException;
	public Object elementAt(int i) throws RemoteException;
	public boolean isSelected() throws RemoteException;	
	public boolean add(Object o) throws RemoteException;
	public boolean add(double d) throws RemoteException;
	public boolean add(int d) throws RemoteException;  		
	public Object[] getCategories() throws RemoteException;
	public double getMin() throws RemoteException;
	public double getMax() throws RemoteException;
	public boolean isNum() throws RemoteException;
	public boolean isCat() throws RemoteException;
	public boolean isEmpty() throws RemoteException;
	public String getName() throws RemoteException;
	public SCatSequence mainSeq() throws RemoteException;
	public boolean hasMissing() throws RemoteException;
	public boolean isMissingAt(int i) throws RemoteException;
	public int getMissingCount() throws RemoteException;
	public int getCatIndex(int i) throws RemoteException;
	public int getCatIndex(Object o) throws RemoteException;
	public int atI(int i) throws RemoteException;
	public double atF(int i) throws RemoteException;
	public double atD(int i) throws RemoteException;
	public String atS(int i) throws RemoteException;
	public Object at(int i) throws RemoteException;
	public Object[] at(int start, int end) throws RemoteException;
	public int size() throws RemoteException;
	public int getNumCats() throws RemoteException;
	public int getSizeCatAt(int i) throws RemoteException;
	public Object getCatAt(int i) throws RemoteException;
	public boolean isLinked() throws RemoteException;
}
