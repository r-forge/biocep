/**
 * Author : Karim Chine
 * License : GPL v3
 */

/**
 * RUnknown.java
 * 
 * This java class corresponds to R objects that have no corresponding covert functions
 */
package org.kchine.r;

import java.util.Arrays;

public class RUnknown extends RRaw {

	public RUnknown() {
		super();
	}

	public RUnknown(int[] value) {
		super(value);
	}
	
    public String toString() {
        StringBuffer res=new StringBuffer("RUnknown {");
        res.append("value= "+Arrays.toString(value));
        res.append(" }");
        return res.toString();
    } 
	
}
        