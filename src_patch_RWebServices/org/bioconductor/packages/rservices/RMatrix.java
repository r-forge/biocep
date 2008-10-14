/**
 * RMatrix.java
 * 
 * This java class corresponds to R matrix 
 */
package org.bioconductor.packages.rservices;

public class RMatrix extends RArray { 
   public RMatrix() {
	super();
	dim=new int[]{0,0};
   } 
}
