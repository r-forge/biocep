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

public class RRaw extends RVector {
    protected byte[] value=new byte[0];
    
    public RRaw() { 
    }
    
    public RRaw(byte[] value, String[] names) {
            super(names);
            this.value=value;
    }
    
    /**
     * Sets the value for this RRaw.
     *
     * @param value
     */
    public void setValue (byte[] value) {
        this.value=value;
    }
    
    /**
     * Gets the value for this RRaw.
     *
     * @return value
     */
    public byte[] getValue () {
        return value;
    }  
  
    public int length() {
        int res=0;
        if (value!=null)
                res= value.length;
        return res;
    }
 
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RRaw obj=(RRaw)inputObject;
            byte[] objValue=obj.getValue();
            res=res && Arrays.equals(value, objValue);
            String[] objNames=obj.getNames();
            res=res && Arrays.equals(names, objNames);
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RRaw {");
        res.append("value= "+Arrays.toString(value));
        res.append(", name= "+Arrays.toString(names));
        res.append(" }");
        return res.toString();
    }  
    
}
        
