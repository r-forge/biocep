package org.bioconductor.packages.rservices;
public class RObject implements java.io.Serializable{
	String outputMsg;
	public void setOutputMsg(String msg) {
		outputMsg=msg;
	}

	public String getOutputMsg() {
		return outputMsg;
	}
}
