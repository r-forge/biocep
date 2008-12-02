/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

/**
 * RInteger.java
 * 
 * This java class corresponds to R Character Vector 
 */
package org.kchine.r;
import java.util.Arrays;

public class RInteger extends RVector {
    protected int[] value=new int[0];    
    protected int[] indexNA;
    
    public RInteger() { 
    }
    
    public RInteger(int... value) {
            this.value=value;
    }

    public RInteger(int[] value, int[] indexNA, String[] names) {
            super(names);
            this.value=value;
	    this.indexNA=indexNA;
    }
    
    /**
     * Sets the value for this RInteger.
     *
     * @param value
     */
    public void setValue (int[] value) {
        this.value=value;
    }
    
    /**
     * Sets the value for this RInteger.
     *
     * @param value
     */

    public void setValueI (Integer value) {
        this.value=new int[]{value.intValue()};
    }

    
    /**
     * Gets the value for this RInteger.
     *
     * @return value
     */
    public int[] getValue () {
        return value;
    } 
        
    /**
     * Sets the NA indices for this RInteger, start from 0
     *
     * @param indexNA
     */
    public void setIndexNA (int[] indexNA) {
        this.indexNA=indexNA;
    }
    
    /**
     * Gets the NA indices for this RInteger.
     *
     * @return indexNA
     */
    public int[] getIndexNA () {
        return indexNA;
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
            RInteger obj=(RInteger)inputObject;
            int[] objIndexNA=obj.getIndexNA();
            res=res && Arrays.equals(indexNA, objIndexNA);
            int[] objValue=obj.getValue();
            String[] objNames=obj.getNames();
            if (res&&(indexNA!=null)) {
                if((objValue!=null)&&(value!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objValue[indexNA[i]]=value[indexNA[i]];
                }
                if((objNames!=null)&&(names!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objNames[indexNA[i]]=names[indexNA[i]];
                }
            }
            res=res && Arrays.equals(value, objValue);
            res=res && Arrays.equals(names, objNames);
        }
        return res;
    }
    
    public String toString() {
        StringBuffer res=new StringBuffer("RInteger {");
        res.append("value= "+Arrays.toString(value));
        res.append(", name= "+Arrays.toString(names));
        res.append(", NA indices= "+Arrays.toString(indexNA));
        res.append(" }");
        return res.toString();
    }  
       
}
        
