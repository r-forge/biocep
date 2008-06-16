package org.rosuda.ibase;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

public class SMarkerInterfaceRemoteImpl extends UnicastRemoteObject implements SMarkerInterfaceRemote {
	SMarkerInterface _smarker;
	public SMarkerInterfaceRemoteImpl(SMarkerInterface smarker) throws RemoteException {
		super();
		_smarker=smarker;		
	}
	public void addDepend(Dependent c) {
		_smarker.addDepend(c);
	}
	public boolean at(int pos) {
		return _smarker.at(pos);
	}
	public void beginBatch() {
		_smarker.beginBatch();
	}
	public void delDepend(Dependent c) {
		_smarker.delDepend(c);
	}
	public Enumeration elements() {
		return _smarker.elements();
	}
	public void endBatch() {
		_smarker.endBatch();
	}
	public int get(int pos) {
		return _smarker.get(pos);
	}
	public Vector getList() {
		return _smarker.getList();
	}
	public int[] getMaskCopy(int maskType) {
		return _smarker.getMaskCopy(maskType);
	}
	public SVarSetInterfaceRemote getMasterSet() {
		return _smarker.getMasterSet().getRemote();
	}
	public int getMaxMark() {
		return _smarker.getMaxMark();
	}
	public int getSec(int pos) {
		return _smarker.getSec(pos);
	}
	public int getSecCount() {
		return _smarker.getSecCount();
	}
	public int[] getSelectedIDs() {
		return _smarker.getSelectedIDs();
	}
	public int marked() {
		return _smarker.marked();
	}
	public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) {
		_smarker.NotifyAll(msg, c, path);
	}
	public void NotifyAll(NotifyMsg msg, Dependent c) {
		_smarker.NotifyAll(msg, c);
	}
	public void NotifyAll(NotifyMsg msg, Vector path) {
		_smarker.NotifyAll(msg, path);
	}
	public void NotifyAll(NotifyMsg msg) {
		_smarker.NotifyAll(msg);
	}
	public void resetSec() {
		_smarker.resetSec();
	}
	public void resize(int newsize) {
		_smarker.resize(newsize);
	}
	public Object run(Object o, String cmd) {
		return _smarker.run(o, cmd);
	}
	public void selectAll() {
		_smarker.selectAll();
	}
	public void selectInverse() {
		_smarker.selectInverse();
	}
	public void selectNone() {
		_smarker.selectNone();
	}
	public void set(int pos, boolean mark) {
		_smarker.set(pos, mark);
	}
	
	public void setSec(int pos, int mark) {
		_smarker.setSec(pos, mark);
	}
	public void setSecBySelection(int markSel, int markNonsel) {
		_smarker.setSecBySelection(markSel, markNonsel);
	}
	public void setSelected(int mark) {
		_smarker.setSelected(mark);
	}
	public int size() {
		return _smarker.size();
	}
	public void startCascadedNotifyAll(NotifyMsg msg) {
		_smarker.startCascadedNotifyAll(msg);
	}
	
	
	private HashMap<DependentRemote, Dependent> modelListenerHashMap=new HashMap<DependentRemote, Dependent>();
	public void addDepend(final DependentRemote l)  {
		Dependent listener=new Dependent() {
			public void Notifying(NotifyMsg msg, Object src, Vector path) {
				try {

					if (src instanceof SMarker) {
						src=((SMarker)src).getRemote();
					}
					l.Notifying(msg, null, path);
					
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		modelListenerHashMap.put(l,listener);
		_smarker.addDepend(listener);			
	}
	
	public void delDepend(DependentRemote l) {
		Dependent listener=modelListenerHashMap.get(l);
		if (listener!=null) {
			_smarker.delDepend(listener);
			modelListenerHashMap.remove(l);
		}
	}
	
}
