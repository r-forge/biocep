package org.rosuda.ibase;

import java.io.Serializable;


public interface SVarSetInterface extends Serializable{	
	public SVarInterface at(int i);
    public int count();
    public void setName(String s);
    public int indexOf(String nam);
    public String getName();
    public SMarkerInterface getMarker();
    public SMarkerInterfaceRemote getMarkerRemote();    
    public void setMarker(SMarkerInterface m);
    public SVarInterface byName(String nam);
    public int add(SVarInterface v);
}
