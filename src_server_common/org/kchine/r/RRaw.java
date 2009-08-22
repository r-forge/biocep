/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

/**
 * RRaw.java
 * 
 * This java class corresponds to R raw Vector 
 */
package org.kchine.r;
import java.util.Arrays;

public class RRaw extends RObject {
    protected int[] value=new int[0];
    
    public RRaw() { 
    }
    
    public RRaw(int[] value) {
            this.value=value;
    }

    
    /**
     * Sets the value for this RRaw.
     *
     * @param value
     */
    public void setValue (int[] value) {
        this.value=value;
    }
    
    /**
     * Gets the value for this RRaw.
     *
     * @return value
     */
    public int[] getValue () {
        return value;
    }  
  
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RRaw obj=(RRaw)inputObject;
            int[] objValue=obj.getValue();
            res=res && Arrays.equals(value, objValue);
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RRaw {");
        res.append("value= "+Arrays.toString(value));
        res.append(" }");
        return res.toString();
    }  
    
}
        
