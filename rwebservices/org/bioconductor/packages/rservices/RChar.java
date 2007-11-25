/**
 * RChar.java
 * 
 * This java class corresponds to R Character Vector 
 */
package org.bioconductor.packages.rservices;
import java.util.Arrays;

public class RChar extends RVector {
    protected String[] value=new String[0];
    protected int[] indexNA;
    
    public RChar() { 
    }

    public RChar(String... value) {
            this.value=value;
    }

    public RChar(String[] value, int[] indexNA, String[] names) {
            super(names);
            this.value=value;
	    this.indexNA=indexNA;
    }
    
    /**
     * Sets the value for this RChar.
     *
     * @param value
     */
    public void setValue (String[] value) {
        this.value=value;
    }
    
    /**
     * Sets the value for this RChar.
     *
     * @param value
     */
    public void setValueI (String value) {
        this.value=new String[]{value};
    }
    
    /**
     * Gets the value for this RChar.
     *
     * @return value
     */
    public String[] getValue () {
        return value;
    }  
        
    /**
     * Sets the NA indices for this RChar, start from 0
     *
     * @param indexNA
     */
    public void setIndexNA (int[] indexNA) {
        this.indexNA=indexNA;
    }
    
    /**
     * Gets the NA indices for this RChar.
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
	    RChar obj=(RChar)inputObject;
	    int[] objIndexNA=obj.getIndexNA();
	    res=res && Arrays.equals(indexNA, objIndexNA);
	    String[] objValue=obj.getValue();
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
        StringBuffer res=new StringBuffer("RChar {");
        res.append("value= "+Arrays.toString(value));
        res.append(", name= "+Arrays.toString(names));
        res.append(", NA indices= "+Arrays.toString(indexNA));
        res.append(" }");
        return res.toString();
    }      

}
        
