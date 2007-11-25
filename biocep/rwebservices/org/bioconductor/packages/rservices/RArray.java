/**
 * RArray.java
 * 
 * This java class corresponds to R array 
 */
package org.bioconductor.packages.rservices;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.lang.reflect.Array;

public class RArray extends RObject{
    protected RVector value=new RNumeric();
    protected int[] dim=new int[]{0};
    protected RList dimnames;    
    
    public RArray() { 
    }
   
    public RArray(RVector value) {
	    this.value=value;
	    int valueLength=value.length();
	    this.dim=new int[]{valueLength};
    }
 
    public RArray(RVector value, int[]dim, RList dimnames) {
            this.value=value;
            this.dim=dim;
            this.dimnames=dimnames; 
    }
    
    /**
     * Sets the value for this RArray.
     *
     * @param value
     */
    public void setValue (RVector value) {
        this.value=value;
    }
    
    /**
     * Gets the value for this RArray.
     *
     * @return value
     */
     public RVector getValue () {
        return value;
     }
     
     /**
     * Sets the dim value for this RArray.
     *
     * @param dim
     */
    public void setDim (int[] dim) {
        this.dim=dim;
    }
 
    /**
     * Gets the dim value for this RArray.
     *
     * @return dim
     */
     public int[] getDim () {
         return dim;
     }
    
    /**
     * Sets the dimnames value for this RArray.
     *
     * @param dimanmes
     */
    public void setDimnames (RList dimnames) throws Exception {
        this.dimnames=dimnames;
    }
    
    /**
     * Gets the dimnames value for this RArray.
     *
     * @return dimnames
     */
     public RList getDimnames () {
        return dimnames;
     }

     public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RArray obj=(RArray)inputObject;
	    RVector objVal=obj.getValue();
	    if ((value==null)||(objVal==null))
		res=res&&(value==objVal);
	    else
            	res=res&&(value.equals(objVal));
	    RList objDimnames=obj.getDimnames();
	    if ((dimnames==null)||(objDimnames==null))
		res=res&&(dimnames==objDimnames);
	    else
	    	res=res&&(dimnames.equals(objDimnames));
	    int[] objDim=(int[])obj.getDim();
            res=res && Arrays.equals(dim, objDim);
        }
        return res;
    }
   
     public String toString() {
        StringBuffer res=new StringBuffer(this.getClass().getName());
        res.append(" { value= "+String.valueOf(value));
        res.append(", dim= "+Arrays.toString(dim));
        res.append(", dimnames= "+String.valueOf(dimnames));
        res.append(" }");
        return res.toString();
    }   
      
}
        
