/**
 * RDataFrame.java
 * 
 * This java class corresponds to R data frame 
 */
package org.bioconductor.packages.rservices;
import java.util.Arrays;

public class RDataFrame extends RObject {
    protected RList data=new RList();
    protected String[] rowNames=new String[0];
    
    public RDataFrame () {
    }
    
    public RDataFrame (RList data, String[] rowNames) {
        this.data=data;
        this.rowNames=rowNames;
    }

    
    /**
     * Sets the data for this RDataFrame.
     *
     * @param data
     */
    public void setData (RList data) {
        this.data=data;
    }
    
    /**
     * Gets the data for this RDataFrame.
     *
     * @return data
     */
     public RList getData () {
        return data;
     }
     
     /**
     * Sets the rowNames for this RDataFrame.
     *
     * @param rowNames
     */
    public void setRowNames (String[] rowNames) {
        this.rowNames=rowNames;
    }
    
     /**
     * Sets the rowNames for this RDataFrame.
     *
     * @param rowNames
     */
    public void setRowNamesI (String rowNames) {
        this.rowNames=new String[]{rowNames};
    }
    
    /**
     * Gets the rowNames value for this RDataFrame.
     *
     * @return rowNames
     */
     public String[] getRowNames () {
         return rowNames;
     }


    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
	    RDataFrame obj=(RDataFrame)inputObject;
            RList objData=obj.getData();
            String[] objRowNames=obj.getRowNames();
	    if (data==null)
		res=res&&(data==objData);
	    else
            	res=res&&data.equals(objData);
            res=res && Arrays.equals(rowNames, objRowNames);
        }
        return res;
    }
     
     public String toString() {
        StringBuffer res=new StringBuffer("RDdataFrame {\ndata=");
        res.append(String.valueOf(data));
        res.append("\nrownames="+Arrays.toString(rowNames));
        res.append("\n}");
        return res.toString();
    } 
}
        
