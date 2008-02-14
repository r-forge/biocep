/**
 * RVector.java
 * 
 * This java class corresponds to R Vector (exclude list)
 */
package org.bioconductor.packages.rservices;

public abstract class RVector extends RObject {
    protected String[] names;
    
    public RVector() { 
    }
    
    public RVector(String[] names) {
            this.names=names;
    }
    
    /**
     * Sets the names for this RVector.
     *
     * @param names
     */
    public void setNames (String[] names) {
        this.names=names;
    }
    
    /**
     * Gets the names for this RVector.
     *
     * @return names
     */
    public String[] getNames () {
        return names;
    }    
   
    public int length() {
	return 0;
    } 
}
        
