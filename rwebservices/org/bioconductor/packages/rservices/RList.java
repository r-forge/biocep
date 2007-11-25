/**
 * RList.java
 * 
 * This java class corresponds to R List 
 */
package org.bioconductor.packages.rservices;
import java.util.Arrays;

public class RList extends RObject {
    protected RObject[] value=new RObject[0];
    protected String[] names;
    
    public RList() { 
    }
    
    public RList(RObject[] value, String[] names) {
            this.value=value;
            this.names=names;
    }
    
    /**
     * Sets the value for this RList.
     *
     * @param value
     */
    public void setValue (RObject[] value) {
        this.value=value;
    }
    
    /**
     * Gets the value for this RList.
     *
     * @return value
     */
     public RObject[] getValue () {
        return value;
     }
   
    /**
     * Sets the names for this RList.
     *
     * @param names
     */
    public void setNames (String[] names) {
        this.names=names;
    }
    
    
    public void setNamesI (String names) {
        this.names=new String[]{names};
    }
    
    
    /**
     * Gets the names for this RList.
     *
     * @return value
     */
    public String[] getNames () {
        return names;
    } 
   
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
            RList obj=(RList)inputObject;
            Object[] objValue=obj.getValue();
            res=res && Arrays.deepEquals(value, objValue);
            String[] objNames=obj.getNames();
            res=res && Arrays.equals(names, objNames);
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RList {");
        res.append("name= "+Arrays.toString(names));
        res.append(", value= "+Arrays.deepToString(value));
        res.append(" }");
        return res.toString();
    } 
      
}
        
