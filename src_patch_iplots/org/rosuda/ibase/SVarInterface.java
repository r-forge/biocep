package org.rosuda.ibase;

import java.io.Serializable;

public interface SVarInterface extends NotifierInterface, Serializable{
	
/*	
public void setAllEmpty(int size) ;
public void setInternalType(int it) ;

public int getInternalType();
public boolean isInternal() ;

public void setSelected(boolean setit) ;
public void categorize();
public NotifierInterface getNotifier() ;
public int getContentsType()  ;
public boolean setContentsType(int ct) ;
public void sortCategories() ;
public void setCategorical(boolean nc)  ;
public boolean add(double d)  ;
public boolean add(int d)  ;
public boolean insert(double d, int index)  ;
public boolean insert(int d, int index) ;
public boolean replace(int index, double d)  ;
public boolean replace(int index, int i) ;

public Object elementAt(int i) ;

public void setName(String nn) ;

public Object[] getCategories() ;
public void setSeq(SCatSequence newSeq) ;
*/
public int[] getRanked()  ;
public int[] getRanked(SMarkerInterface m, int markspec);
public int getContentsType()  ;
public void categorize();
public NotifierInterface getNotifier() ;
public Object elementAt(int i) ;
public boolean isSelected() ;	
public boolean add(Object o);
public boolean add(double d)  ;
public boolean add(int d);  
	
public Object[] getCategories() ;
public double getMin()  ;
public double getMax() ;
public boolean isNum()  ;
public boolean isCat()  ;
public boolean isEmpty()  ;
public String getName() ;
public SCatSequence mainSeq() ;
public boolean hasMissing() ;
public boolean isMissingAt(int i) ;
public int getMissingCount() ;
public int getCatIndex(int i)  ;
public int getCatIndex(Object o);
public int atI(int i)  ;
public double atF(int i)  ;
public double atD(int i) ;
public String atS(int i)  ;
public Object at(int i) ;
public int size() ;
public int getNumCats() ;
public int getSizeCatAt(int i) ;
public Object getCatAt(int i) ;
public boolean isLinked() ;

public SVarInterfaceRemote getSVarRemote();
}
