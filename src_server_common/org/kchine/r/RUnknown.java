/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

/**
 * RUnknown.java
 * 
 * This java class corresponds to R objects that have no corresponding covert functions
 */
package org.kchine.r;

public class RUnknown extends RObject {
    protected String rclass;
    protected int length;
    protected String contents;
    
    public RUnknown() { 
    }
      
    /**
     * Sets the rclass name for this RUnknown.
     *
     * @param rclass
     */
    public void setRclass (String rclass) {
        this.rclass=rclass;
    }
    
    /**
     * Gets the rclass name for this RUnknown.
     *
     * @return rclass
     */
     public String getRclass () {
        return rclass;
     }
    
    /**
     * Sets the length for this RUnknown.
     *
     * @param length
     */
    public void setLength (int length) {
        this.length=length;
    }
    
    /**
     * Gets the length of this RUnknown.
     *
     * @return length
     */
    public int getLength () {
        return length;
    }   
    
    /**
     * Sets the contents for this RUnknown.
     *
     * @param contents
     */
    public void setContents (String contents) {
        this.contents=contents;
    }
    
    /**
     * Gets the contents for this RUnknown.
     *
     * @return contents
     */
     public String getContents () {
        return contents;
     } 
     
    public String toString() {
        StringBuffer res=new StringBuffer("RUnknown { ");
        res.append(rclass);
        res.append(" (").append(length).append(")\n");
        res.append(contents);
        res.append("\n}");
        return res.toString();
    }    
       
}
        