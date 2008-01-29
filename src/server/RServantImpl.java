/*
 * Copyright (C) 2007 EMBL-EBI
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
package server;

import graphics.pop.GDDevice;
import graphics.rmi.GraphicNotifier;
import graphics.rmi.JGDPanel;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import mapping.RPackage;
import mapping.ReferenceInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.PropertyConfigurator;
import org.bioconductor.packages.rservices.RList;
import org.bioconductor.packages.rservices.RObject;
import org.bioconductor.packages.rservices.RVector;
import org.rosuda.JRI.Rengine;
import org.rosuda.javaGD.GDContainer;
import remoting.AssignInterface;
import remoting.FileDescription;
import remoting.RAction;
import remoting.RCallback;
import remoting.RNI;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.InitializingException;
import uk.ac.ebi.microarray.pools.ManagedServantAbstract;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemotePanel;
import util.Utils;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class RServantImpl extends ManagedServantAbstract implements RServices {

	private StringBuffer _log = new StringBuffer();

	private HashMap<String, RPackage> _rim = null;

	private RNI _remoteRni = null;

	private AssignInterface _assignInterface = null;

	private GraphicNotifier _graphicNotifier = null;

	private boolean _isReady = false;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(RServantImpl.class);

	public String runR(ExecutionUnit eu) {
		return DirectJNI.getInstance().runR(eu);
	}

	public RServantImpl(String name, String prefix, Registry registry) throws RemoteException {
		super(name, prefix, registry);
		init();
		log.info("Stub:" + PoolUtils.stubToHex(this));
	}

	public void init() throws RemoteException {
		try {
			System.setProperty("wks.persitent", "false");

			DirectJNI.init(getServantName());
			_assignInterface = new AssignInterfaceImpl();
			DirectJNI.getInstance().setAssignInterface(
					(AssignInterface) java.rmi.server.RemoteObject.toStub(_assignInterface));

			_remoteRni = new RNIImpl();

			_graphicNotifier = new GraphicNotifierImpl();

			_rim = new HashMap<String, RPackage>();

			for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
				String shortClassName = className.substring(className.lastIndexOf('.') + 1);
				log.info(shortClassName);
				_rim.put(shortClassName, (RPackage) DirectJNI._mappingClassLoader.loadClass(className + "ImplRemote")
						.newInstance());
			}

			if (System.getProperty("preprocess.help") != null
					&& System.getProperty("preprocess.help").equalsIgnoreCase("true")) {
				new Thread(new Runnable() {
					public void run() {
						DirectJNI.getInstance().preprocessHelp();
					}
				}).start();
			}

			if (System.getProperty("apply.sandbox") != null
					&& System.getProperty("apply.sandbox").equalsIgnoreCase("true")) {
				DirectJNI.getInstance().applySandbox();
			}
			_isReady = true;

		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RemoteException("<" + Utils.getStackTraceAsString(ex) + ">");
		}
	}

	public static class GraphicNotifierImpl extends UnicastRemoteObject implements GraphicNotifier {
		public GraphicNotifierImpl() throws RemoteException {
			super();
		}

		public void fireSizeChangedEvent(final int devNr) throws RemoteException {
			DirectJNI.getInstance().getGraphicNotifier().fireSizeChangedEvent(devNr);
		}

		public void registerContainer(GDContainer container) throws RemoteException {
			DirectJNI.getInstance().getGraphicNotifier().registerContainer(container);
		}

		public void executeDevOff(int devNr) throws RemoteException {
			DirectJNI.getInstance().getGraphicNotifier().executeDevOff(devNr);
		}

		public void putLocation(Point p) throws RemoteException {
			DirectJNI.getInstance().getGraphicNotifier().putLocation(p);
		}

	}

	@Override
	public void logInfo(String message) throws RemoteException {
		log.info("Name:" + this.getServantName());
		log.info("Stub:" + PoolUtils.stubToHex(this));
		log.info(message);
	}

	public void stop() throws RemoteException {
		DirectJNI.getInstance().getRServices().stop();
	}

	public RNI getRNI() throws RemoteException {
		return _remoteRni;
	}

	public String getStatus() throws RemoteException {
		return DirectJNI.getInstance().getRServices().getStatus();
	}

	public boolean isReference(RObject obj) throws RemoteException {
		return DirectJNI.getInstance().getRServices().isReference(obj);
	}

	public RObject call(String methodName, RObject... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().call(methodName, args);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public RObject callAsReference(String methodName, RObject... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().callAsReference(methodName, args);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void callAndAssignName(String varName, String methodName, RObject... args) throws RemoteException {
		DirectJNI.getInstance().getRServices().callAndAssignName(varName, methodName, args);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public void freeReference(RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().freeReference(refObj);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject getObjectFromReference(RObject refObj) throws RemoteException {

		ReferenceInterface refObjCast = (ReferenceInterface) refObj;
		if (refObjCast.getAssignInterface().equals(_assignInterface)) {
			RObject result = DirectJNI.getInstance().getRServices().getObjectFromReference(refObj);
			_log.append(DirectJNI.getInstance().getRServices().getStatus());
			return result;
		} else {
			return refObjCast.extractRObject();
		}
	};

	public RObject putObjectAndGetReference(RObject obj) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().putObjectAndGetReference(obj);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void putObjectAndAssignName(RObject obj, String name) throws RemoteException {
		DirectJNI.getInstance().getRServices().putObjectAndAssignName(obj, name);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject evalAndGetObject(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().evalAndGetObject(expression);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public RObject evalAndGetObjectAsReference(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().evalAndGetObjectAsReference(expression);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void assignNameToObjectReference(String varname, RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().assignNameToObjectReference(varname, refObj);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public String evaluate(String expression) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluate(expression);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String evaluateExpressions(final String expression, final int n) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluateExpressions(expression, n);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromResource(String resource) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromResource(resource);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromBuffer(StringBuffer buffer) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromBuffer(buffer);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String print(String expression) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().print(expression);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String printExpressions(String[] expressions) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().printExpressions(expressions);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void setCallBack(RCallback callback) throws RemoteException {
		DirectJNI.getInstance().getRServices().setCallBack(callback);

	}

	public String[] getAllPackageNames() throws RemoteException {
		return (String[]) _rim.keySet().toArray(new String[0]);
	}

	public RPackage getPackage(String packageName) throws RemoteException {
		return _rim.get(packageName);
	}

	public String getLogs() throws RemoteException {
		return _log.toString();
	}

	public void reset() throws RemoteException {
		if (isResetEnabled()) {

			System.out.println(".....reset called");
			runR(new ExecutionUnit() {
				public void run(Rengine e) {

					String[] allobj = e
							.rniGetStringArray(e.rniEval(e.rniParse(".PrivateEnv$ls(all.names=TRUE)", 1), 0));
					if (allobj != null) {
						for (int i = 0; i < allobj.length; ++i)
							if (DirectJNI.getInstance().getBootStrapRObjects().contains(allobj[i])) {
							} else
								e.rniEval(e.rniParse("rm(" + allobj[i] + ")", 1), 0);
					}

					DirectJNI.getInstance().unprotectAll();
					_log.setLength(0);

				}
			});

			RListener.stopAllClusters();
			try {
				DirectJNI.getInstance().regenerateWorkingDirectory(true);
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

	}

	public String ping() throws java.rmi.RemoteException {
		if (_isReady)
			return "pong";
		else {
			throw new InitializingException();
		}
	}

	public void die() throws java.rmi.RemoteException {
		System.exit(0);
	}

	@Override
	public boolean hasConsoleMode() throws RemoteException {
		return true;
	}

	@Override
	public String consoleSubmit(String cmd) throws RemoteException {
		return DirectJNI.getInstance().getRServices().consoleSubmit(cmd);
	}

	@Override
	public boolean hasPushPopMode() throws RemoteException {
		return true;
	}

	@Override
	public Serializable pop(String symbol) throws RemoteException {
		Serializable result = DirectJNI.getInstance().getRServices().evalAndGetObject(symbol);
		System.out.println("result for " + symbol + " : " + result);
		return result;
	}

	@Override
	public void push(String symbol, Serializable object) throws RemoteException {
		DirectJNI.getInstance().getRServices().putObjectAndAssignName((RObject) object, symbol);
	}

	@Override
	public String[] listSymbols() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listSymbols();
	}

	@Override
	public boolean hasGraphicMode() throws RemoteException {
		return true;
	}

	@Override
	public RemotePanel getPanel(int w, int h) throws RemoteException {
		return new JGDPanel(w, h, _graphicNotifier);
	}

	public GDDevice newDevice(int w, int h) throws RemoteException {
		return new GDDeviceImpl(w, h);
	}

	public String[] getWorkingDirectoryFileNames() throws java.rmi.RemoteException {
		return null;
	}

	public FileDescription[] getWorkingDirectoryFileDescriptions() throws java.rmi.RemoteException {
		return DirectJNI.getInstance().getRServices().getWorkingDirectoryFileDescriptions();
	}

	public FileDescription getWorkingDirectoryFileDescription(String fileName) throws java.rmi.RemoteException {
		return DirectJNI.getInstance().getRServices().getWorkingDirectoryFileDescription(fileName);
	}

	public void createWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
		DirectJNI.getInstance().getRServices().createWorkingDirectoryFile(fileName);
	}

	public void removeWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
		DirectJNI.getInstance().getRServices().removeWorkingDirectoryFile(fileName);
	}

	public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize)
			throws java.rmi.RemoteException {
		return DirectJNI.getInstance().getRServices().readWorkingDirectoryFileBlock(fileName, offset, blocksize);
	}

	public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws java.rmi.RemoteException {
		DirectJNI.getInstance().getRServices().appendBlockToWorkingDirectoryFile(fileName, block);
	}

	public byte[] getRHelpFile(String uri) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getRHelpFile(uri);
	}

	public String getRHelpFileUri(String topic, String pack) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getRHelpFileUri(topic, pack);
	}

	public String[] listDemos() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listDemos();
	}

	public StringBuffer getDemoSource(String demoName) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getDemoSource(demoName);
	}

	public void setProgressiveConsoleLogEnabled(boolean progressiveLog) throws RemoteException {
		DirectJNI.getInstance().getRServices().setProgressiveConsoleLogEnabled(progressiveLog);

	}

	public boolean isProgressiveConsoleLogEnabled() throws RemoteException {
		return DirectJNI.getInstance().getRServices().isProgressiveConsoleLogEnabled();
	}

	public Vector<RAction> popRActions() throws java.rmi.RemoteException {
		return DirectJNI.getInstance().getRServices().popRActions();
	}

	public static class GDDeviceImpl extends UnicastRemoteObject implements GDDevice {
		GDDevice _localGdDevice = null;

		public GDDeviceImpl(int w, int h) throws RemoteException {
			super();
			_localGdDevice = new DirectJNI.GDDeviceLocal(w, h);
		}

		public Vector<org.rosuda.javaGD.GDObject> popAllGraphicObjects() throws RemoteException {
			return _localGdDevice.popAllGraphicObjects();
		};

		public void fireSizeChangedEvent(int w, int h) throws RemoteException {
			_localGdDevice.fireSizeChangedEvent(w, h);
		};

		public void dispose() throws RemoteException {
			_localGdDevice.dispose();
		};
		
		@Override
		public int getDeviceNumber() throws RemoteException {
			return _localGdDevice.getDeviceNumber();
		}
		
		@Override
		public boolean isCurrentDevice() throws RemoteException {
			return _localGdDevice.isCurrentDevice();
		}
		
		@Override
		public void setAsCurrentDevice() throws RemoteException {
			_localGdDevice.setAsCurrentDevice();
		}

		public Dimension getSize() throws RemoteException {
			return _localGdDevice.getSize();
		}

		public void putLocation(Point2D p) throws RemoteException {
			_localGdDevice.putLocation(p);
		}

		public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
			return _localGdDevice.getRealPoints(points);
		}
	}

	class AssignInterfaceImpl extends java.rmi.server.UnicastRemoteObject implements AssignInterface {
		public AssignInterfaceImpl() throws RemoteException {
			super();
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

		public long setValueCP(long rObjectId, String slotsPath, double[] real, double[] imaginary)
				throws RemoteException {
			return DirectJNI.getInstance().getDefaultAssignInterface()
					.setValueCP(rObjectId, slotsPath, real, imaginary);
		}

		public RNI getRNI() throws RemoteException {
			return _remoteRni;
		}

		public String getName() throws RemoteException {
			return RServantImpl.this.getServantName();
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

		//Dataframes	
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
			return DirectJNI.getInstance().getDefaultAssignInterface().setDataframeRowNames(rObjectId, slotsPath,
					rowNames);
		}

		//Lists
		public RObject[] getListValue(long rObjectId, String slotsPath) throws RemoteException {
			return DirectJNI.getInstance().getDefaultAssignInterface().getListValue(rObjectId, slotsPath);
		}

		public long setListValue(long rObjectId, String slotsPath, RObject[] value) throws RemoteException {
			return DirectJNI.getInstance().getDefaultAssignInterface().setListValue(rObjectId, slotsPath, value);
		}

		//env		
		public HashMap getEnvData(long rObjectId, String slotsPath) throws RemoteException {
			return DirectJNI.getInstance().getDefaultAssignInterface().getEnvData(rObjectId, slotsPath);
		}

		public long putEnv(long rObjectId, String slotsPath, String theKey, RObject theValue) throws RemoteException {
			return DirectJNI.getInstance().getDefaultAssignInterface().putEnv(rObjectId, slotsPath, theKey, theValue);
		}

		public long setEnvData(long rObjectId, String slotsPath, HashMap<String, RObject> data) throws RemoteException {
			return DirectJNI.getInstance().getDefaultAssignInterface().setEnvData(rObjectId, slotsPath, data);
		}

	}

	// --------------

	class RNIImpl extends java.rmi.server.UnicastRemoteObject implements RNI {

		private String _status;

		public RNIImpl() throws RemoteException {
			super();
		}

		public void rniAssign(String name, long exp, long rho) throws RemoteException {
			DirectJNI.getInstance().getRServices().getRNI().rniAssign(name, exp, rho);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		}

		public long rniCAR(long exp) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniCAR(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniCDR(long exp) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniCDR(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniCons(long head, long tail) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniCons(head, tail);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniEval(long exp, long rho) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniEval(exp, rho);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public int rniExpType(long exp) throws RemoteException {
			int result = DirectJNI.getInstance().getRServices().getRNI().rniExpType(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniGetAttr(long exp, String name) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniGetAttr(exp, name);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public int[] rniGetBoolArrayI(long exp) throws RemoteException {
			int[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetBoolArrayI(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public double[] rniGetDoubleArray(long exp) throws RemoteException {
			double[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetDoubleArray(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public int[] rniGetIntArray(long exp) throws RemoteException {
			int[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetIntArray(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long[] rniGetList(long exp) throws RemoteException {
			long[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetList(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public String rniGetString(long exp) throws RemoteException {
			String result = DirectJNI.getInstance().getRServices().getRNI().rniGetString(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public String[] rniGetStringArray(long exp) throws RemoteException {
			String[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetStringArray(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public String rniGetSymbolName(long sym) throws RemoteException {
			String result = DirectJNI.getInstance().getRServices().getRNI().rniGetSymbolName(sym);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long[] rniGetVector(long exp) throws RemoteException {
			long[] result = DirectJNI.getInstance().getRServices().getRNI().rniGetVector(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniGetVersion() throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniGetVersion();
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public boolean rniInherits(long exp, String cName) throws RemoteException {
			boolean result = DirectJNI.getInstance().getRServices().getRNI().rniInherits(exp, cName);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniInstallSymbol(String sym) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniInstallSymbol(sym);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniParse(String s, int parts) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniParse(s, parts);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public void rniProtect(long exp) throws RemoteException {
			DirectJNI.getInstance().getRServices().getRNI().rniProtect(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		}

		public long rniPutBoolArray(boolean[] a) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutBoolArray(a);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniPutBoolArrayI(int[] a) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutBoolArrayI(a);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniPutDoubleArray(double[] a) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutDoubleArray(a);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniPutIntArray(int[] a) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutIntArray(a);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniPutList(long[] cont) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutList(cont);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniPutString(String s) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutString(s);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniPutStringArray(String[] a) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutStringArray(a);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniPutVector(long[] exps) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniPutVector(exps);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public void rniSetAttr(long exp, String name, long attr) throws RemoteException {
			DirectJNI.getInstance().getRServices().getRNI().rniSetAttr(exp, name, attr);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		}

		public int rniStop(int flag) throws RemoteException {
			int result = DirectJNI.getInstance().getRServices().getRNI().rniStop(flag);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public long rniTAG(long exp) throws RemoteException {
			long result = DirectJNI.getInstance().getRServices().getRNI().rniTAG(exp);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
			return result;
		}

		public void rniUnprotect(int count) throws RemoteException {
			DirectJNI.getInstance().getRServices().getRNI().rniUnprotect(count);
			_log.append(DirectJNI.getInstance().getRServices().getRNI().getStatus());
		}

		public String getStatus() throws RemoteException {
			return _status;
		}
	}

	static {
		if (log instanceof Log4JLogger) {
			Properties log4jProperties = new Properties();
			for (Object sprop : System.getProperties().keySet()) {
				if (((String) sprop).startsWith("log4j.")) {
					log4jProperties.put(sprop, System.getProperties().get(sprop));
				}
			}
			PropertyConfigurator.configure(log4jProperties);
		}
	}
}