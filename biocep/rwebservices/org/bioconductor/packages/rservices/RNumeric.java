/**
 * RNumeric.java
 * 
 * This java class corresponds to R Character Vector 
 */
package org.bioconductor.packages.rservices;
import java.util.Arrays;

public class RNumeric extends RVector {
    protected double[] value=new double[0];
    protected int[] indexNA;
    
    public RNumeric() { 
    }
    
    public RNumeric(double... value) {
    	this.value=value;
    }
    
    public RNumeric(double[] value, int[] indexNA, String[] names) {
            super(names);
            this.value=value;
	    this.indexNA=indexNA;
    }
    
    /**
     * Sets the value for this RNumeric.
     *
     * @param value
     */
    public void setValue (double[] value) {
        this.value=value;
    }
    
    /**
     * Sets the value for this RNumeric.
     *
     * @param value
     */
    public void setValueI (Double value) {
        this.value=new double[]{value.doubleValue()};
    }
    
    /**
     * Gets the value for this RNumeric.
     *
     * @return value
     */
    public double[] getValue () {
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
            RNumeric obj=(RNumeric)inputObject;
	    int[] objIndexNA=obj.getIndexNA();
            res=res && Arrays.equals(indexNA, objIndexNA);
            double[] objValue=obj.getValue();
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
        StringBuffer res=new StringBuffer("RNumeric {");
        res.append("value= "+Arrays.toString(value));
        res.append(", name= "+Arrays.toString(names));
	res.append(", NA indices= "+Arrays.toString(indexNA));
        res.append(" }");
        return res.toString();
    }  
    
}
        
