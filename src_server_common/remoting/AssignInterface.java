/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
 * Copyright (C) 2007 - 2008  Karim Chine
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.bioconductor.packages.rservices.RList;
import org.bioconductor.packages.rservices.RObject;
import org.bioconductor.packages.rservices.RVector;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public interface AssignInterface extends Remote {

	long assign(long rObjectId, String slotsPath, RObject robj) throws RemoteException;

	String[] getNames(long rObjectId, String slotsPath) throws RemoteException;

	long setNames(long rObjectId, String slotsPath, String[] names) throws RemoteException;

	public int length(long rObjectId, String slotsPath) throws RemoteException;

	String[] getValueStringArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueStringArray(long rObjectId, String slotsPath, String[] value) throws RemoteException;

	boolean[] getValueBoolArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueBoolArray(long rObjectId, String slotsPath, boolean[] value) throws RemoteException;

	double[] getValueDoubleArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueDoubleArray(long rObjectId, String slotsPath, double[] value) throws RemoteException;

	int[] getValueIntArray(long rObjectId, String slotsPath) throws RemoteException;

	long setValueIntArray(long rObjectId, String slotsPath, int[] value) throws RemoteException;

	double[] getValueCPReal(long rObjectId, String slotsPath) throws RemoteException;

	double[] getValueCPImaginary(long rObjectId, String slotsPath) throws RemoteException;

	long setValueCP(long rObjectId, String slotsPath, double[] real, double[] imaginary) throws RemoteException;

	public int[] getIndexNA(long rObjectId, String slotsPath) throws RemoteException;

	public long setIndexNA(long rObjectId, String slotsPath, int[] indexNA) throws RemoteException;

	public String getOutputMsg(long rObjectId, String slotsPath) throws RemoteException;

	public long setOutputMsg(long rObjectId, String slotsPath, String msg) throws RemoteException;

	public RObject getObjectFromReference(RObject refObj) throws RemoteException;

	public RNI getRNI() throws RemoteException;

	public String getName() throws RemoteException;

	// Arrays
	public RVector getArrayValue(long rObjectId, String slotsPath) throws RemoteException;

	public long setArrayValue(long rObjectId, String slotsPath, RVector value) throws RemoteException;

	public int[] getArrayDim(long rObjectId, String slotsPath) throws RemoteException;

	public long setArrayDim(long rObjectId, String slotsPath, int[] dim) throws RemoteException;

	public RList getArrayDimnames(long rObjectId, String slotsPath) throws RemoteException;

	public long setArrayDimnames(long rObjectId, String slotsPath, RList dimnames) throws RemoteException;

	// Factors
	public String[] factorAsData(long rObjectId, String slotsPath) throws RemoteException;

	public int[] getFactorCode(long rObjectId, String slotsPath) throws RemoteException;

	public String[] getFactorLevels(long rObjectId, String slotsPath) throws RemoteException;

	public long setFactorCode(long rObjectId, String slotsPath, int[] code) throws RemoteException;

	public long setFactorLevels(long rObjectId, String slotsPath, String[] levels) throws RemoteException;

	// Dataframes
	public RList getDataframeData(long rObjectId, String slotsPath) throws RemoteException;

	public String[] getDataframeRowNames(long rObjectId, String slotsPath) throws RemoteException;

	public long setDataframeData(long rObjectId, String slotsPath, RList data) throws RemoteException;

	public long setDataframeRowNames(long rObjectId, String slotsPath, String[] rowNames) throws RemoteException;

	// Lists
	public RObject[] getListValue(long rObjectId, String slotsPath) throws RemoteException;

	public long setListValue(long rObjectId, String slotsPath, RObject[] value) throws RemoteException;

	// Env
	public HashMap getEnvData(long rObjectId, String slotsPath) throws RemoteException;

	public long putEnv(long rObjectId, String slotsPath, String theKey, RObject theValue) throws RemoteException;

	public long setEnvData(long rObjectId, String slotsPath, HashMap<String, RObject> data) throws RemoteException;

}
