package org.rosuda.ibase;

import java.io.Serializable;

import org.kchine.r.server.iplots.SVarSetInterfaceRemote;


public interface SVarSetInterface extends Serializable{	
	public SVarInterface at(int i);
    public int count();
    public void setName(String s);
    public int indexOf(String nam);
    public String getName();
    public SMarkerInterface getMarker();    
    public SVarInterface byName(String nam);
    public SVarSetInterfaceRemote getRemote();    
    //public void setMarker(SMarkerInterface m);    
    //public int add(SVarInterface v);
}
