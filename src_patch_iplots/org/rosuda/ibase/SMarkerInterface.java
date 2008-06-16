package org.rosuda.ibase;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

public interface SMarkerInterface extends NotifierInterface, Commander, Serializable{	
    public void resize(int newsize);
    public int size();
    public int marked();
    public int get(int pos);
    public int getSec(int pos);
    public boolean at(int pos);
    public Vector getList();
    public int[] getSelectedIDs();
    public int[] getMaskCopy(int maskType);
    public void set(int pos, boolean pMark);
    public void setSec(int pos, int mark);
    public void setSelected(int mark);
    public int getMaxMark();
	public int getSecCount();
    public Enumeration elements();
    public void selectNone();
    public void selectAll();	
    public void selectInverse();
	public void resetSec();
    public SVarSetInterface getMasterSet();
    public void setMasterSet(SVarSetInterface varset);
    
    public void setSecBySelection(int markSel, int markNonsel);

}
