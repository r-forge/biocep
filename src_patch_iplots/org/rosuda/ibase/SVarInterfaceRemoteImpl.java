package org.rosuda.ibase;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;

public class SVarInterfaceRemoteImpl extends UnicastRemoteObject implements SVarInterfaceRemote{
	SVarInterface _svarInterface;
	
	private HashMap<DependentRemote, Dependent> modelListenerHashMap=new HashMap<DependentRemote, Dependent>();
	public void addDepend(final DependentRemote l)  {
		Dependent listener=new Dependent() {
			public void Notifying(NotifyMsg msg, Object src, Vector path) {
				try {
					l.Notifying(msg, src, path);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		modelListenerHashMap.put(l,listener);
		_svarInterface.addDepend(listener);			
	}
	
	public void delDepend(DependentRemote l) {
		Dependent listener=modelListenerHashMap.get(l);
		if (listener!=null) {
			_svarInterface.delDepend(listener);
			modelListenerHashMap.remove(l);
		}
	}

	public SVarInterfaceRemoteImpl(SVarInterface svarInterface) throws RemoteException{
		super();
		_svarInterface=svarInterface;
	}
	public boolean add(double d) {
		return _svarInterface.add(d);
	}
	public boolean add(int d) {
		return _svarInterface.add(d);
	}
	public boolean add(Object o) {
		return _svarInterface.add(o);
	}
	
	public Object at(int i) {
		return _svarInterface.at(i);
	}
	public double atD(int i) {
		return _svarInterface.atD(i);
	}
	public double atF(int i) {
		return _svarInterface.atF(i);
	}
	public int atI(int i) {
		return _svarInterface.atI(i);
	}
	public String atS(int i) {
		return _svarInterface.atS(i);
	}
	public void beginBatch() {
		_svarInterface.beginBatch();
	}
	public void categorize() {
		_svarInterface.categorize();
	}
	
	public Object elementAt(int i) {
		return _svarInterface.elementAt(i);
	}
	public void endBatch() {
		_svarInterface.endBatch();
	}
	public Object getCatAt(int i) {
		return _svarInterface.getCatAt(i);
	}
	public Object[] getCategories() {
		return _svarInterface.getCategories();
	}
	public int getCatIndex(int i) {
		return _svarInterface.getCatIndex(i);
	}
	public int getCatIndex(Object o) {
		return _svarInterface.getCatIndex(o);
	}
	public int getContentsType() {
		return _svarInterface.getContentsType();
	}
	public double getMax() {
		return _svarInterface.getMax();
	}
	public double getMin() {
		return _svarInterface.getMin();
	}
	public int getMissingCount() {
		return _svarInterface.getMissingCount();
	}
	public String getName() {
		return _svarInterface.getName();
	}
	public NotifierInterface getNotifier() {
		return _svarInterface.getNotifier();
	}
	public int getNumCats() {
		return _svarInterface.getNumCats();
	}
	public int[] getRanked() {
		return _svarInterface.getRanked();
	}
	public int[] getRanked(SMarkerInterface m, int markspec) {
		return _svarInterface.getRanked(m, markspec);
	}
	public int getSizeCatAt(int i) {
		return _svarInterface.getSizeCatAt(i);
	}
	public boolean hasMissing() {
		return _svarInterface.hasMissing();
	}
	public boolean isCat() {
		return _svarInterface.isCat();
	}
	public boolean isEmpty() {
		return _svarInterface.isEmpty();
	}
	public boolean isLinked() {
		return _svarInterface.isLinked();
	}
	public boolean isMissingAt(int i) {
		return _svarInterface.isMissingAt(i);
	}
	public boolean isNum() {
		return _svarInterface.isNum();
	}
	public boolean isSelected() {
		return _svarInterface.isSelected();
	}
	public SCatSequence mainSeq() {
		return _svarInterface.mainSeq();
	}
	public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) {
		_svarInterface.NotifyAll(msg, c, path);
	}
	public void NotifyAll(NotifyMsg msg, Dependent c) {
		_svarInterface.NotifyAll(msg, c);
	}
	public void NotifyAll(NotifyMsg msg, Vector path) {
		_svarInterface.NotifyAll(msg, path);
	}
	public void NotifyAll(NotifyMsg msg) {
		_svarInterface.NotifyAll(msg);
	}
	public int size() {
		return _svarInterface.size();
	}
	public void startCascadedNotifyAll(NotifyMsg msg) {
		_svarInterface.startCascadedNotifyAll(msg);
	}
	
}
