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
import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Vector;
import mapping.RPackage;
import mapping.ReferenceInterface;
import org.apache.commons.logging.Log;
import org.bioconductor.packages.rservices.RObject;
import org.rosuda.JRI.Rengine;
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
	
	private  HashMap<Integer,GDDeviceImpl> _deviceHashMap=new HashMap<Integer, GDDeviceImpl>();

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
			
		
			_assignInterface = new AssignInterfaceImpl(this);
			DirectJNI.getInstance().setAssignInterface(
					(AssignInterface) java.rmi.server.RemoteObject.toStub(_assignInterface));

			
			_remoteRni = new RNIImpl(_log);

			
			_graphicNotifier = new GraphicNotifierImpl();
			
			
			_rim = new HashMap<String, RPackage>();

			
			
			for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
				String shortClassName = className.substring(className.lastIndexOf('.') + 1);
				log.info(shortClassName);
				System.out.println("Going to load : "+className + "ImplRemote");
				_rim.put(shortClassName, (RPackage) DirectJNI.class.getClassLoader().loadClass(className + "ImplRemote")
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

			Vector<Integer> devices=new Vector<Integer>();
			for (Integer d:_deviceHashMap.keySet()) devices.add(d);
			
			System.out.println("devices before reset:"+devices);			
			
			for (Integer d:devices) {
				try {_deviceHashMap.get(d).dispose();} catch (Exception ex) {ex.printStackTrace();}
			}

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
		return new GDDeviceImpl(w, h, _deviceHashMap);
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


	// --------------


	/*
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
	*/
}