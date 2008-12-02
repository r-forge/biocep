/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

/**
 * REnvironment.java
 * 
 * This java class corresponds to R List 
 */
package org.kchine.r;
import java.util.HashMap;

public class REnvironment extends RObject {
    protected HashMap<String, RObject> data = new HashMap<String, RObject>();
    
    public REnvironment() { 
    }
    
    /**
     * Sets the data for this REnvironment.
     *
     * @param data
     */
    public void setData (HashMap<String, RObject> data) {
        this.data=data;
    }
    
    /**
     * Gets the data for this REnvironment.
     *
     * @return data
     */
     public HashMap<String, RObject> getData () {
        return data;
     }
    
     public void put(String theKey, RObject theValue) {
	data.put(theKey, theValue);
     }

     public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            REnvironment obj=(REnvironment)inputObject;
            HashMap objData=obj.getData();
	    if (data==null)
		res=res&(data==objData);
	    else
            	res=res&&data.equals(objData);
        }
        return res;
    }
 
     public String toString() {
        String res="REnvironment {"+String.valueOf(data)+"}";
        return res;
    }    
}
        
