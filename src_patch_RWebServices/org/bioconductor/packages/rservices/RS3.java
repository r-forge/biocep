package org.bioconductor.packages.rservices;

import java.util.Arrays;

public class RS3 extends RList{
	
	private String[] classAttribute=null;
	
	public RS3() {
		super();
	}

	public RS3(RObject[] value, String[] names) {
		super(value, names);
	}

	public RS3(RObject[] value, String[] names, String[] classAttribute) {
		super(value, names);
		this.classAttribute=classAttribute;
	}

	public String[] getClassAttribute() {
		return classAttribute;
	}

	public void setClassAttribute(String[] classAttribute) {
		this.classAttribute = classAttribute;
	}
	
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
        	RS3 obj=(RS3)inputObject;
            Object[] objValue=obj.getValue();
            res=res && Arrays.deepEquals(value, objValue);
            String[] objNames=obj.getNames();
            
            boolean ce=false;
            if (classAttribute==null && obj.classAttribute==null) ce=true;
            else if (classAttribute==null && obj.classAttribute!=null) ce=false;
            else if (classAttribute!=null && obj.classAttribute==null) ce=false;
            else if (classAttribute.length!=obj.classAttribute.length) ce=false; 
            else {            	
            	ce=true;
            	for (int i=0; i< classAttribute.length; ++i) {
            		if (!classAttribute[i].equals(obj.classAttribute[i])) ce=false; break;
            	}
            }
            res=res && Arrays.equals(names, objNames) && ce;
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RS3 {");
        res.append("name= "+Arrays.toString(names));
        res.append(", value= "+Arrays.deepToString(value));
        res.append(", class= "+Arrays.toString(classAttribute));
        res.append(" }");
        return res.toString();
    } 

	
}
