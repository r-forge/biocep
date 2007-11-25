/**
 * RMatrix.java
 * 
 * This java class corresponds to R matrix 
 */
package org.bioconductor.packages.rservices;
import java.util.ArrayList;
import java.util.Vector;

public class RMatrix extends RArray { 
   public RMatrix() {
	super();
	dim=new int[]{0,0};
   } 
}
