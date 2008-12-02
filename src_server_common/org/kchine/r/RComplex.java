/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

/**
 * RComplex.java
 * 
 * This java class corresponds to R Character Vector 
 */
package org.kchine.r;
import java.util.Arrays;

public class RComplex extends RVector {
    protected double[] real=new double[0];
    protected double[] imaginary=new double[0];
    protected int[] indexNA;
    
    public RComplex() { 
    }
    
    public RComplex(double[] real, double[] imaginary, int[] indexNA, String[] names) {
            super(names);
            this.real=real;
            this.imaginary=imaginary;
	    this.indexNA=indexNA;
    }
    
    /**
     * Sets the real for this RComplex.
     *
     * @param real
     */
    public void setReal (double... real) {
        this.real=real;
    }
    
    /**
     * Sets the real for this RComplex.
     *
     * @param real
     */
    public void setRealI(Double real) {
        this.real=new double[]{real.doubleValue()};
    }
    
    /**
     * Gets the real for this RComplex.
     *
     * @return real
     */
    public double[] getReal () {
        return real;
    }    
    
    /**
     * Sets the imaginary for this RComplex.
     *
     * @param imaginary
     */
    public void setImaginary (double... imaginary) {
        this.imaginary=imaginary;
    }
    
    /**
     * Sets the imaginary for this RComplex.
     *
     * @param imaginary
     */
    public void setImaginaryI (Double imaginary) {
        this.imaginary=new double[]{imaginary.doubleValue()};
    }
    
    /**
     * Gets the imaginary for this RComplex.
     *
     * @return imaginary
     */
    public double[] getImaginary () {
        return imaginary;
    } 
 
    /**
     * Sets the NA indices for this RComplex, start from 0
     *
     * @param indexNA
     */
    public void setIndexNA (int[] indexNA) {
        this.indexNA=indexNA;
    }

    /**
     * Gets the NA indices for this RComplex.
     *
     * @return indexNA
     */
    public int[] getIndexNA () {
        return indexNA;
    }

 
    public int length() {
        int res=0;
        if (imaginary!=null)
                res= imaginary.length;
        return res;
    }
 
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RComplex obj=(RComplex)inputObject;
	    int[] objIndexNA=obj.getIndexNA();
            res=res && Arrays.equals(indexNA, objIndexNA);
	    double[] objReal=obj.getReal();
	    double[] objImaginary=obj.getImaginary();
            String[] objNames=obj.getNames();
	    if (res&&(indexNA!=null)) {
                if((objReal!=null)&&(real!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objReal[indexNA[i]]=real[indexNA[i]];
                }
                if((objImaginary!=null)&&(imaginary!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objImaginary[indexNA[i]]=imaginary[indexNA[i]];
                }
                if((objNames!=null)&&(names!=null)){
                    for(int i=0; i<indexNA.length; i++)
                        objNames[indexNA[i]]=names[indexNA[i]];
                }
            }
            res=res && Arrays.equals(real, objReal);
            res=res && Arrays.equals(imaginary, objImaginary);
            res=res && Arrays.equals(names, objNames);
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RComplex {");
        res.append("name= "+Arrays.toString(names));
        res.append(", value= ");
        if ((real==null)||(imaginary==null))
            res.append("null");
        else {
            res.append("[ ");
	    boolean[] isNA=new boolean[real.length];
	    for (int i=0; i<real.length; i++)
		isNA[i]=false;
	    if (indexNA!=null) {
		for (int i=0; i<indexNA.length; i++)
			isNA[indexNA[i]]=true;
	    }
            for (int i=0; i<real.length; i++) { 
		if (isNA[i])
		    res.append("NA ");
		else
                    res.append(real[i]).append("+(").append(imaginary[i]).append(")i ");
	    }
            res.append("]");
        }
        res.append(" }");
        return res.toString();
    }  

}
        
