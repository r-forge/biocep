/**
 * Author : Karim Chine 
 * License : GPLv3
 */

package org.kchine.r;
import java.util.Arrays;

public class RFunction extends RRaw { 
	
    public RFunction() {
		super();
	}

	public RFunction(int[] value) {
		super(value);
	}

	public String toString() {
        StringBuffer res=new StringBuffer("RFunction {");
        res.append("value= "+Arrays.toString(value));
        res.append(" }");
        return res.toString();
    }  
}
        
