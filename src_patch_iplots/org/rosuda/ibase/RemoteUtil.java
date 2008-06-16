package org.rosuda.ibase;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;


public abstract class RemoteUtil {
	public static SVarInterface getSVarWrapper(final SVarInterfaceRemote _svarRemote) {
		return new SVarInterface() {

			private HashMap<Dependent, DependentRemoteImpl> modelListenerHashMap=new HashMap<Dependent, DependentRemoteImpl>();
			
			public void addDepend(Dependent l) {			
				try {
					DependentRemoteImpl tableModelListenerRemoteImpl=new DependentRemoteImpl(l);
					_svarRemote.addDepend((DependentRemote)java.rmi.server.RemoteObject.toStub(tableModelListenerRemoteImpl));
					modelListenerHashMap.put(l, tableModelListenerRemoteImpl);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}							
			}

			public void delDepend(Dependent l) {
				try {					
					DependentRemoteImpl tableModelListenerRemoteImpl=modelListenerHashMap.get(l);
					_svarRemote.delDepend(tableModelListenerRemoteImpl);
					modelListenerHashMap.remove(l);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}
			
			
			public boolean add(double d) {
				try {
					return _svarRemote.add(d);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean add(int d) {
				try {
					return _svarRemote.add(d);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean add(Object o) {
				try {
					return _svarRemote.add(o);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Object at(int i) {
				try {
					return _svarRemote.at(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public double atD(int i) {
				try {
					return _svarRemote.atD(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public double atF(int i) {
				try {
					return _svarRemote.atF(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int atI(int i) {
				try {
					return _svarRemote.atI(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public String atS(int i) {
				try {
					return _svarRemote.atS(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void beginBatch() {
				try {
					_svarRemote.beginBatch();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void categorize() {
				try {
					_svarRemote.categorize();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Object elementAt(int i) {
				try {
					return _svarRemote.elementAt(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void endBatch() {
				try {
					_svarRemote.endBatch();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Object getCatAt(int i) {
				try {
					return _svarRemote.getCatAt(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Object[] getCategories() {
				try {
					return _svarRemote.getCategories();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getCatIndex(int i) {
				try {
					return _svarRemote.getCatIndex(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getCatIndex(Object o) {
				try {
					return _svarRemote.getCatIndex(o);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getContentsType() {
				try {
					return _svarRemote.getContentsType();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public double getMax() {
				try {
					return _svarRemote.getMax();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public double getMin() {
				try {
					return _svarRemote.getMin();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getMissingCount() {
				try {
					return _svarRemote.getMissingCount();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public String getName() {
				try {
					return _svarRemote.getName();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public NotifierInterface getNotifier() {
				try {
					return _svarRemote.getNotifier();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getNumCats() {
				try {
					return _svarRemote.getNumCats();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int[] getRanked() {
				try {
					return _svarRemote.getRanked();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int[] getRanked(SMarkerInterface m, int markspec) {
				try {
					return _svarRemote.getRanked(m, markspec);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getSizeCatAt(int i) {
				try {
					return _svarRemote.getSizeCatAt(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean hasMissing() {
				try {
					return _svarRemote.hasMissing();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isCat() {
				try {
					return _svarRemote.isCat();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isEmpty() {
				try {
					return _svarRemote.isEmpty();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isLinked() {
				try {
					return _svarRemote.isLinked();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isMissingAt(int i) {
				try {
					return _svarRemote.isMissingAt(i);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isNum() {
				try {
					return _svarRemote.isNum();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public boolean isSelected() {
				try {
					return _svarRemote.isSelected();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public SCatSequence mainSeq() {
				try {
					return _svarRemote.mainSeq();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) {
				try {
					_svarRemote.NotifyAll(msg, c, path);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void NotifyAll(NotifyMsg msg, Dependent c) {
				try {
					_svarRemote.NotifyAll(msg, c);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void NotifyAll(NotifyMsg msg, Vector path) {
				try {
					_svarRemote.NotifyAll(msg, path);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void NotifyAll(NotifyMsg msg) {
				try {
					_svarRemote.NotifyAll(msg);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int size() {
				try {
					return _svarRemote.size();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void startCascadedNotifyAll(NotifyMsg msg) {
				try {
					_svarRemote.startCascadedNotifyAll(msg);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			public SVarInterfaceRemote getSVarRemote() {
				throw new UnsupportedOperationException("Shouldn't be called");
			}

		};
	}
	
	public static SMarkerInterface getSMarkerWrapper(final SMarkerInterfaceRemote _smarkerRemote) {
		return new SMarkerInterface() {

			
			
			public boolean at(int pos) {
				try {return _smarkerRemote.at(pos);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Enumeration elements()  {
				try {return _smarkerRemote.elements();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int get(int pos)  {
				try {return _smarkerRemote.get(pos);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public Vector getList()  {
				try {return _smarkerRemote.getList();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int[] getMaskCopy(int maskType)  {
				try {return _smarkerRemote.getMaskCopy(maskType);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public SVarSetInterface getMasterSet() {
				try {return _smarkerRemote.getMasterSet();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getMaxMark() {
				try {return _smarkerRemote.getMaxMark();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getSec(int pos)  {
				try {return _smarkerRemote.getSec(pos);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int getSecCount()  {
				try {return _smarkerRemote.getSecCount();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int[] getSelectedIDs()  {
				try {return _smarkerRemote.getSelectedIDs();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int marked()  {
				try {return _smarkerRemote.marked();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void resetSec()  {
				try {_smarkerRemote.resetSec();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void resize(int newsize)  {
				try {_smarkerRemote.resize(newsize);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void selectAll()  {
				try {_smarkerRemote.selectAll();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void selectInverse()  {
				try {_smarkerRemote.selectInverse();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void selectNone()  {
				try {_smarkerRemote.selectNone();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void set(int pos, boolean mark)  {
				try {_smarkerRemote.set(pos, mark);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void setMasterSet(SVarSetInterface varset) {
				try {_smarkerRemote.setMasterSet(varset);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void setSec(int pos, int mark)  {
				try {_smarkerRemote.setSec(pos, mark);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void setSecBySelection(int markSel, int markNonsel)  {
				try {_smarkerRemote.setSecBySelection(markSel, markNonsel);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public void setSelected(int mark) {
				try {_smarkerRemote.setSelected(mark);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			public int size() {
				try {return _smarkerRemote.size();} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			

			private HashMap<Dependent, DependentRemoteImpl> modelListenerHashMap=new HashMap<Dependent, DependentRemoteImpl>();
			
			public void addDepend(Dependent l) {			
				try {
					DependentRemoteImpl tableModelListenerRemoteImpl=new DependentRemoteImpl(l);
					_smarkerRemote.addDepend((DependentRemote)java.rmi.server.RemoteObject.toStub(tableModelListenerRemoteImpl));
					modelListenerHashMap.put(l, tableModelListenerRemoteImpl);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}							
			}

			public void delDepend(Dependent l) {
				try {					
					DependentRemoteImpl tableModelListenerRemoteImpl=modelListenerHashMap.get(l);
					_smarkerRemote.delDepend(tableModelListenerRemoteImpl);
					modelListenerHashMap.remove(l);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}	
			}
			
			public void beginBatch() {
				try {_smarkerRemote.beginBatch();} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			public void endBatch() {
				try {_smarkerRemote.endBatch();	} catch (Exception e) {
					throw new RuntimeException(e);
				}			
			}
			
			public void NotifyAll(NotifyMsg msg) {
				try {_smarkerRemote.NotifyAll(msg);} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			public void NotifyAll(NotifyMsg msg, Dependent c) {
				try {_smarkerRemote.NotifyAll(msg, c);} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			public void NotifyAll(NotifyMsg msg, Dependent c, Vector path) {
				try {_smarkerRemote.NotifyAll(msg, c,path);} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			public void NotifyAll(NotifyMsg msg, Vector path) {
				try {_smarkerRemote.NotifyAll(msg, path);} catch (Exception e) {
					throw new RuntimeException(e);
				}
				
			}
			
			public Object run(Object o, String cmd) {
				try {return _smarkerRemote.run(o,cmd);} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			
			public void startCascadedNotifyAll(NotifyMsg msg) {
				try {_smarkerRemote.startCascadedNotifyAll(msg);} catch (Exception e) {
					throw new RuntimeException(e);
				}				
			}
			
			
			
		};
	}
}
