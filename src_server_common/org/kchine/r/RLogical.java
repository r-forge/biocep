/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

/**
 * RLogical.java
 * 
 * This java class corresponds to R Character Vector 
 */
package org.kchine.r;
import java.util.Arrays;

public class RLogical extends RVector {
    protected boolean[] value=new boolean[0];
    protected int[] indexNA;
    
    public RLogical() { 
    }

    public RLogical(boolean... value) {
	    this.value=value;
    }
    
    public RLogical(boolean[] value, int[] indexNA, String[] names) {
            super(names);
            this.value=value;
            this.indexNA=indexNA;
    }
    
    /**
     * Sets the value for this RLogical.
     *
     * @param value
     */
    public void setValue (boolean[] value) {
        this.value=value;
    }
    
    /**
     * Sets the value for this RLogical.
     *
     * @param value
     */
    public void setValueI (Boolean value) {
        this.value=new boolean[]{value.booleanValue()};
    }
    
    /**
     * Gets the value for this RLogical.
     *
     * @return value
     */
    public boolean[] getValue () {
        return value;
    }   
    
    /**
     * Sets the NA indices for this RLogical, start from 0
     *
     * @param indexNA
     */
    public void setIndexNA (int[] indexNA) {
        this.indexNA=indexNA;
    }
    
    /**
     * Gets the NA indices for this RLogical.
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
            RLogical obj=(RLogical)inputObject;
            int[] objIndexNA=obj.getIndexNA();
            res=res && Arrays.equals(indexNA, objIndexNA);
            boolean[] objValue=obj.getValue();
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
        StringBuffer res=new StringBuffer("RLogical {");
        res.append("value= "+Arrays.toString(value));
        res.append(", name= "+Arrays.toString(names));
        res.append(", NA indices= "+Arrays.toString(indexNA));
        res.append(" }");
        return res.toString();
    }  
       
}
        
