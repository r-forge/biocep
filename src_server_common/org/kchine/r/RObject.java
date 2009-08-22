/**
 * Authors : Nianhua Li, MT Morgan
 * License : caBIG 
 */

package org.kchine.r;

public class RObject implements java.io.Serializable{
	RList attributes;

	public RList getAttributes() {
		return attributes;
	}

	public void setAttributes(RList attributes) {
		this.attributes = attributes;
	}
}
