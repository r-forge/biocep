package server;

import java.rmi.RemoteException;
import java.util.HashMap;

import org.bioconductor.packages.rservices.RList;
import org.bioconductor.packages.rservices.RObject;
import org.bioconductor.packages.rservices.RVector;

import remoting.AssignInterface;
import remoting.RNI;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class AssignInterfaceImpl extends java.rmi.server.UnicastRemoteObject implements AssignInterface {
	RServantImpl _rservantImpl = null;

	public AssignInterfaceImpl(RServantImpl rservantImpl) throws RemoteException {
		super();
		_rservantImpl = rservantImpl;
	}

	public long assign(long rObjectId, String slotsPath, RObject robj) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().assign(rObjectId, slotsPath, robj);
	}

	public RObject getObjectFromReference(RObject refObj) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getObjectFromReference(refObj);
	}

	public String[] getNames(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getNames(rObjectId, slotsPath);
	}

	public long setNames(long rObjectId, String slotsPath, String[] names) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setNames(rObjectId, slotsPath, names);
	}

	public int length(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().length(rObjectId, slotsPath);
	}

	public String[] getValueStringArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueStringArray(rObjectId, slotsPath);
	}

	public long setValueStringArray(long rObjectId, String slotsPath, String[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueStringArray(rObjectId, slotsPath, value);
	}

	public boolean[] getValueBoolArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueBoolArray(rObjectId, slotsPath);
	}

	public long setValueBoolArray(long rObjectId, String slotsPath, boolean[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueBoolArray(rObjectId, slotsPath, value);
	}

	public double[] getValueDoubleArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueDoubleArray(rObjectId, slotsPath);
	}

	public long setValueDoubleArray(long rObjectId, String slotsPath, double[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueDoubleArray(rObjectId, slotsPath, value);
	}

	public int[] getValueIntArray(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueIntArray(rObjectId, slotsPath);
	}

	public long setValueIntArray(long rObjectId, String slotsPath, int[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueIntArray(rObjectId, slotsPath, value);
	}

	public double[] getValueCPImaginary(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueCPImaginary(rObjectId, slotsPath);
	}

	public double[] getValueCPReal(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getValueCPReal(rObjectId, slotsPath);
	}

	public long setValueCP(long rObjectId, String slotsPath, double[] real, double[] imaginary) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setValueCP(rObjectId, slotsPath, real, imaginary);
	}

	public RNI getRNI() throws RemoteException {
		return _rservantImpl.getRNI();
	}

	public String getName() throws RemoteException {
		return _rservantImpl.getServantName();
	}

	public int[] getIndexNA(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getIndexNA(rObjectId, slotsPath);
	}

	public long setIndexNA(long rObjectId, String slotsPath, int[] indexNA) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setIndexNA(rObjectId, slotsPath, indexNA);
	}

	public String getOutputMsg(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getOutputMsg(rObjectId, slotsPath);
	}

	public long setOutputMsg(long rObjectId, String slotsPath, String msg) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setOutputMsg(rObjectId, slotsPath, msg);
	}

	public RVector getArrayValue(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getArrayValue(rObjectId, slotsPath);
	}

	public long setArrayValue(long rObjectId, String slotsPath, RVector value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setArrayValue(rObjectId, slotsPath, value);
	}

	public int[] getArrayDim(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getArrayDim(rObjectId, slotsPath);
	}

	public long setArrayDim(long rObjectId, String slotsPath, int[] dim) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setArrayDim(rObjectId, slotsPath, dim);
	}

	public RList getArrayDimnames(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getArrayDimnames(rObjectId, slotsPath);
	}

	public long setArrayDimnames(long rObjectId, String slotsPath, RList dimnames) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setArrayDimnames(rObjectId, slotsPath, dimnames);
	}

	// Factors
	public String[] factorAsData(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().factorAsData(rObjectId, slotsPath);
	}

	public int[] getFactorCode(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getFactorCode(rObjectId, slotsPath);
	}

	public String[] getFactorLevels(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getFactorLevels(rObjectId, slotsPath);
	}

	public long setFactorCode(long rObjectId, String slotsPath, int[] code) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setFactorCode(rObjectId, slotsPath, code);
	}

	public long setFactorLevels(long rObjectId, String slotsPath, String[] levels) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setFactorLevels(rObjectId, slotsPath, levels);
	}

	// Dataframes
	public RList getDataframeData(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getDataframeData(rObjectId, slotsPath);
	}

	public String[] getDataframeRowNames(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getDataframeRowNames(rObjectId, slotsPath);
	}

	public long setDataframeData(long rObjectId, String slotsPath, RList data) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setDataframeData(rObjectId, slotsPath, data);
	}

	public long setDataframeRowNames(long rObjectId, String slotsPath, String[] rowNames) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setDataframeRowNames(rObjectId, slotsPath, rowNames);
	}

	// Lists
	public RObject[] getListValue(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getListValue(rObjectId, slotsPath);
	}

	public long setListValue(long rObjectId, String slotsPath, RObject[] value) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setListValue(rObjectId, slotsPath, value);
	}

	// env
	public HashMap getEnvData(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getEnvData(rObjectId, slotsPath);
	}

	public long putEnv(long rObjectId, String slotsPath, String theKey, RObject theValue) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().putEnv(rObjectId, slotsPath, theKey, theValue);
	}

	public long setEnvData(long rObjectId, String slotsPath, HashMap<String, RObject> data) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setEnvData(rObjectId, slotsPath, data);
	}
	
	public String getS3ClassAttribute(long rObjectId, String slotsPath) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().getS3ClassAttribute(rObjectId, slotsPath);
	}

	public long setS3ClassAttribute(long rObjectId, String slotsPath, String classAttribute) throws RemoteException {
		return DirectJNI.getInstance().getDefaultAssignInterface().setS3ClassAttribute(rObjectId, slotsPath, classAttribute);
	}	

}