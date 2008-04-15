package org.bioconductor.packages.rservices;

import java.io.Externalizable;

public interface ObjectNameInterface extends Externalizable{
	public String getRObjectName();
	public void setRObjectName(String name);
	public String getRObjectEnvironment();
	public void setRObjectEnvironment(String env);
}
