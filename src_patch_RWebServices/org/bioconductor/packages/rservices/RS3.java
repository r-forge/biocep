package org.bioconductor.packages.rservices;

import java.util.Arrays;

public class RS3 extends RList{
	
	private String classAttribute=null;
	
	public RS3() {
		super();
	}

	public RS3(RObject[] value, String[] names) {
		super(value, names);
	}

	public RS3(RObject[] value, String[] names, String classAttribute) {
		super(value, names);
		this.classAttribute=classAttribute;
	}

	public String getClassAttribute() {
		return classAttribute;
	}

	public void setClassAttribute(String classAttribute) {
		this.classAttribute = classAttribute;
	}
	
    public boolean equals(Object inputObject) {
        boolean res = getClass().equals(inputObject.getClass());
        if(res) {
        	RS3 obj=(RS3)inputObject;
            Object[] objValue=obj.getValue();
            res=res && Arrays.deepEquals(value, objValue);
            String[] objNames=obj.getNames();
            res=res && Arrays.equals(names, objNames) && (classAttribute.equals(obj.classAttribute));
        }
        return res;
    }
 
    public String toString() {
        StringBuffer res=new StringBuffer("RS3 {");
        res.append("name= "+Arrays.toString(names));
        res.append(", value= "+Arrays.deepToString(value));
        res.append(", class= "+classAttribute);
        res.append(" }");
        return res.toString();
    } 

	
}
