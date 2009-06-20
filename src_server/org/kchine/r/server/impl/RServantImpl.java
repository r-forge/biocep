/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kchine.r.server.impl;


import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.commons.logging.Log;
import org.kchine.openoffice.server.OpenOfficeServices;
import org.kchine.r.RChar;
import org.kchine.r.RObject;
import org.kchine.r.server.AssignInterface;
import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.ExecutionUnit;
import org.kchine.r.server.ExtendedReentrantLock;
import org.kchine.r.server.FileDescription;
import org.kchine.r.server.GenericCallbackDevice;
import org.kchine.r.server.R;
import org.kchine.r.server.RCallBack;
import org.kchine.r.server.RClustserInterface;
import org.kchine.r.server.RCollaborationListener;
import org.kchine.r.server.RConsoleAction;
import org.kchine.r.server.RConsoleActionListener;
import org.kchine.r.server.RKit;
import org.kchine.r.server.RListener;
import org.kchine.r.server.RNI;
import org.kchine.r.server.RPackage;
import org.kchine.r.server.RServices;
import org.kchine.r.server.RServicesObject;
import org.kchine.r.server.ReferenceInterface;
import org.kchine.r.server.UserStatus;
import org.kchine.r.server.Utils;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.graphics.GraphicNotifier;
import org.kchine.r.server.http.frontend.DiretoryProvider;
import org.kchine.r.server.http.frontend.FreeResourcesListener;
import org.kchine.r.server.http.local.LocalHttpServer;
import org.kchine.r.server.iplots.SVarInterfaceRemote;
import org.kchine.r.server.iplots.SVarSetInterfaceRemote;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.r.server.scripting.GroovyInterpreterSingleton;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemote;
import org.kchine.rpf.InitializingException;
import org.kchine.rpf.LocalRmiRegistry;
import org.kchine.rpf.ManagedServantAbstract;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RemotePanel;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.scilab.server.ScilabServices;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHolder;
import org.rosuda.JRI.Rengine;
import org.rosuda.iplots.Framework;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RServantImpl extends ManagedServantAbstract implements RServices, ScilabServices , OpenOfficeServices {

	public static final int PORT_RANGE_WIDTH=5;
	
	private StringBuffer _log = new StringBuffer();

	private HashMap<String, RPackage> _rim = null;

	private RNI _remoteRni = null;

	private AssignInterface _assignInterface = null;

	private GraphicNotifier _graphicNotifier = null;

	private HashMap<Integer, GDDevice> _deviceHashMap = new HashMap<Integer, GDDevice>();
	
	private HashMap<String, GenericCallbackDevice> _genericCallbackDeviceHashMap = new HashMap<String, GenericCallbackDevice>();
	
	private boolean _isReady = false;

	RServices _rCreationPb = new RServicesObject();
	
	private static int _port=System.getProperty("rmi.port.start")!=null && !System.getProperty("rmi.port.start").equals("") ? Integer.decode(System.getProperty("rmi.port.start")) : 0; 
	
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(RServantImpl.class);
	
	private boolean archiveLog=false;

	public String runR(ExecutionUnit eu) {
		return DirectJNI.getInstance().runR(eu);
	}

	public RServantImpl(String name, String prefix, Registry registry) throws RemoteException {
		super(name, prefix, registry,_port);
		
		log.info("$$>rmi.port.start:"+_port);
		// --------------	
		init();
				
		log.info("Stub:" + PoolUtils.stubToHex(this));
		
		
		if (System.getProperty("preloadall")!=null && System.getProperty("preloadall").equalsIgnoreCase("true") )
		{			
			try {
				Properties props=new Properties();
				props.loadFromXML(this.getClass().getResourceAsStream("/classlist.xml"));
				for (Object c:props.keySet()) {
					this.getClass().getClassLoader().loadClass((String)c); 
				}
				
			} catch (Exception e) {
				//e.printStackTrace();
			}
					
			try {
				Properties props=new Properties();
				props.loadFromXML(this.getClass().getResourceAsStream("/resourcelist.xml"));
				for (Object c:props.keySet()) {
					DirectJNI.getInstance().getResourceAsStream((String)c);
				}			
			} catch (Exception e) {
				//e.printStackTrace();
			}
		}
		
		
		if (System.getProperty("http.port")!=null && !System.getProperty("http.port").equals("")) {
			try {
				final int port= Integer.decode(System.getProperty("http.port"));
				new Thread(new Runnable(){
					public void run() {
						try { startHttpServer(port); } catch (Exception e) {e.printStackTrace();}
					}
				}).start();
			} catch (Exception e) {
				e.printStackTrace();
			}			
		}
		
		
		
		new Thread(new Runnable() {
			public void run() {
				try {					
					GroovyInterpreterSingleton.getInstance().exec("import org.kchine.r.server.R;");
					GroovyInterpreterSingleton.getInstance().exec("R=org.kchine.r.server.R.getInstance();");
					//GroovyInterpreterSingleton.getInstance().exec("SCI=(org.kchine.scilab.server.ScilabServices)org.kchine.r.server.R.getInstance();");
					//GroovyInterpreterSingleton.getInstance().exec("OO=(org.kchine.openoffice.server.OpenOfficeServices)org.kchine.r.server.R.getInstance();");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}).start();
		
		

		
	}

	public void init() throws RemoteException {
		try {
			System.setProperty("wks.persitent", "false");

			DirectJNI.init(getServantName());

			_assignInterface = new AssignInterfaceImpl(this);
			DirectJNI.getInstance().setAssignInterface((AssignInterface) java.rmi.server.RemoteObject.toStub(_assignInterface));

			_remoteRni = new RNIImpl(_log);

			_graphicNotifier = new GraphicNotifierImpl();

			_rim = new HashMap<String, RPackage>();

			for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
				String shortClassName = className.substring(className.lastIndexOf('.') + 1);
				log.info(shortClassName);
				System.out.println("Going to load : " + className + "ImplRemote");
				_rim.put(shortClassName, (RPackage) DirectJNI._mappingClassLoader.loadClass(className + "ImplRemote").newInstance());
			}

			if (System.getProperty("preprocess.help") == null || System.getProperty("preprocess.help").equals("") || System.getProperty("preprocess.help").equalsIgnoreCase("true")) {
				String[] packNames = ((RChar) DirectJNI.getInstance().getRServices().getObject(".packages(all=T)")).getValue();
				DirectJNI.getInstance().preprocessHelp(packNames,true);				
			}

			if (System.getProperty("apply.sandbox") != null && System.getProperty("apply.sandbox").equalsIgnoreCase("true")) {
				DirectJNI.getInstance().applySandbox();
			}

			RListener.setRClusterInterface(new RClustserInterface() {
				public Vector<RServices> createRs(int n, String nodeName) throws Exception {
					System.out.println(" create Rs");

					ExecutorService exec = Executors.newFixedThreadPool(5);
					Future<RServices>[] futures = new Future[n];

					for (int i = 0; i < n; ++i) {

						futures[i] = exec.submit(new Callable<RServices>() {
							public RServices call() {
								try {
									RServices w = cloneServer();
									return w;
								} catch (Exception e) {
									e.printStackTrace();
									return _rCreationPb;
								}
							}
						});

					}
					while (countCreated(futures) < n) {
						try {
							Thread.sleep(20);
						} catch (Exception e) {
						}
					}

					for (int i = 0; i < n; ++i) {
						if (futures[i].get() == _rCreationPb)
							return null;
					}

					Vector<RServices> workers = new Vector<RServices>();
					for (int i = 0; i < n; ++i)
						if (futures[i].get() != _rCreationPb)
							workers.add(futures[i].get());

					for (int i = 0; i < n; ++i) {
						rserverProcessId.put(workers.elementAt(i), workers.elementAt(i).getProcessId());
					}
					return workers;
				}

				private HashMap<RServices, String> rserverProcessId = new HashMap<RServices, String>();

				public void releaseRs(Vector<RServices> rs, int n, String nodeName) throws Exception {
					for (int i = 0; i < n; ++i) {
						try {
							String processId = rserverProcessId.get(rs.elementAt(i));
							if (processId != null) {
								if (PoolUtils.isWindowsOs()) {
									PoolUtils.killLocalWinProcess(processId, true);
								} else {
									PoolUtils.killLocalUnixProcess(processId, true);
								}
								rserverProcessId.remove(rs.elementAt(i));
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			});
			
			RServices rstub=(RServices)java.rmi.server.RemoteObject.toStub(this);
			R._instance=rstub;
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

	public String unsafeGetObjectAsString(String cmd) throws RemoteException {
		return DirectJNI.getInstance().getRServices().unsafeGetObjectAsString(cmd);
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

	public RObject call(String methodName, Object... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().call(methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public RObject callAndGetReference(String methodName, Object... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().callAndGetReference(methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public RObject callAndGetObjectName(String methodName, Object... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().callAndGetObjectName(methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void callAndAssign(String varName, String methodName, Object... args) throws RemoteException {
		DirectJNI.getInstance().getRServices().callAndAssign(varName, methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}
	
	public Object callAndConvert(String methodName, Object... args) throws RemoteException {
		Object result=DirectJNI.getInstance().getRServices().callAndConvert( methodName, args);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void freeReference(RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().freeReference(refObj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject referenceToObject(RObject refObj) throws RemoteException {

		ReferenceInterface refObjCast = (ReferenceInterface) refObj;
		if (refObjCast.getAssignInterface().equals(_assignInterface)) {
			RObject result = DirectJNI.getInstance().getRServices().referenceToObject(refObj);
			if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
			return result;
		} else {
			return refObjCast.extractRObject();
		}
	};

	public RObject putAndGetReference(Object obj) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().putAndGetReference(obj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void putAndAssign(Object obj, String name) throws RemoteException {
		DirectJNI.getInstance().getRServices().putAndAssign(obj, name);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject getObject(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().getObject(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public RObject getReference(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().getReference(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public Object getObjectConverted(String expression) throws RemoteException {
		Object result = DirectJNI.getInstance().getRServices().getObjectConverted(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public RObject getObjectName(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().getObjectName(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	
	public RObject realizeObjectName(RObject objectName) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().realizeObjectName(objectName);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	
	public Object realizeObjectNameConverted(RObject objectName) throws RemoteException {
		Object result = DirectJNI.getInstance().getRServices().realizeObjectNameConverted(objectName);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public void freeAllReferences() throws RemoteException {
		DirectJNI.getInstance().getRServices().freeAllReferences();
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		
	}
	
	public Object convert(RObject obj) throws RemoteException {
		Object result = DirectJNI.getInstance().getRServices().convert(obj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void assignReference(String varname, RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().assignReference(varname, refObj);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public String evaluate(String expression) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluate(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String evaluate(final String expression, final int n) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluate(expression, n);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromResource(String resource) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromResource(resource);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String sourceFromBuffer(String buffer) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromBuffer(buffer);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}
	
	public String sourceFromBuffer(String buffer, HashMap<String, Object> clientProperties) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().sourceFromBuffer(buffer, clientProperties);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String print(String expression) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().print(expression);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String printExpressions(String[] expressions) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().printExpressions(expressions);
		if (archiveLog) _log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public boolean symbolExists(String symbol) throws RemoteException {
		return DirectJNI.getInstance().getRServices().symbolExists(symbol);
	}
	
	public void addRCallback(RCallBack callback) throws RemoteException {
		DirectJNI.getInstance().getRServices().addRCallback(callback);
	}

	public void removeRCallback(RCallBack callback) throws RemoteException {
		DirectJNI.getInstance().getRServices().removeRCallback(callback);
	}

	public void removeAllRCallbacks() throws RemoteException {
		DirectJNI.getInstance().getRServices().removeAllRCallbacks();
	}
		
	public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().addRCollaborationListener(collaborationListener);
	}
	
	public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().removeRCollaborationListener(collaborationListener);
	}
	
	public void removeAllRCollaborationListeners() throws RemoteException {
		DirectJNI.getInstance().getRServices().removeAllRCollaborationListeners();
	}
	
	public boolean hasRCollaborationListeners() throws RemoteException {
		return DirectJNI.getInstance().getRServices().hasRCollaborationListeners();
	}
	
	public void addRConsoleActionListener(RConsoleActionListener helpListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().addRConsoleActionListener(helpListener);
	}	
	
	public void removeRConsoleActionListener(RConsoleActionListener helpListener) throws RemoteException {
		DirectJNI.getInstance().getRServices().removeRConsoleActionListener(helpListener);		
	}
	
	public void removeAllRConsoleActionListeners() throws RemoteException {
		DirectJNI.getInstance().getRServices().removeAllRConsoleActionListeners();		
	}
	
	public void  registerUser(String sourceUID,String user) throws RemoteException {
		DirectJNI.getInstance().getRServices().registerUser(sourceUID,user);
	}
	
	public void  unregisterUser(String sourceUID) throws RemoteException {
		DirectJNI.getInstance().getRServices().unregisterUser(sourceUID);
	}
	
	public void  updateUserStatus(String sourceUID, UserStatus userStatus) throws RemoteException {
		DirectJNI.getInstance().getRServices().updateUserStatus(sourceUID,userStatus);
	}
	
	public UserStatus[] getUserStatusTable() throws RemoteException {
		return DirectJNI.getInstance().getRServices().getUserStatusTable();
	}
	
	public void setUserInput(String userInput) throws RemoteException {
		DirectJNI.getInstance().getRServices().setUserInput(userInput);		
	}
	
	public void setOrginatorUID(String uid) throws RemoteException {
		DirectJNI.getInstance().getRServices().setOrginatorUID(uid);			
	}
	
	public String getOriginatorUID() throws RemoteException {		
		return DirectJNI.getInstance().getRServices().getOriginatorUID();
	}
	
	public void chat(String sourceUID, String user, String message) throws RemoteException {
		DirectJNI.getInstance().getRServices().chat(sourceUID, user, message);
	}
	
	public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
		DirectJNI.getInstance().getRServices().consolePrint(sourceUID, user, expression, result);		
	}
			
	public GenericCallbackDevice newGenericCallbackDevice() throws RemoteException {
		GenericCallbackDevice result=new GenericCallbackDeviceImpl(_genericCallbackDeviceHashMap);
		DirectJNI.getInstance().getRServices().addRCallback(result);
		DirectJNI.getInstance().getRServices().addRConsoleActionListener(result);
		DirectJNI.getInstance().getRServices().addRCollaborationListener(result);
		return result;
	}
	
	public GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException {
		GenericCallbackDevice[] result=new GenericCallbackDevice[_genericCallbackDeviceHashMap.values().size()];
		int i=0; for (GenericCallbackDevice d:_genericCallbackDeviceHashMap.values()) result[i++]=d; 
		return result;
	}
	
	public String[] listPackages() throws RemoteException {
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

					String[] allobj = e.rniGetStringArray(e.rniEval(e.rniParse(".PrivateEnv$ls(all.names=TRUE)", 1), 0));
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

			Vector<Integer> devices = new Vector<Integer>();
			for (Integer d : _deviceHashMap.keySet())
				devices.add(d);

			System.out.println("devices before reset:" + devices);

			for (Integer d : devices) {
				try {
					_deviceHashMap.get(d).dispose();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			RListener.stopAllClusters();
			DirectJNI.getInstance().removeAllRCallbacks();
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

	public boolean hasConsoleMode() throws RemoteException {
		return true;
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		return DirectJNI.getInstance().getRServices().consoleSubmit(cmd);
	}
	public String consoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
		return DirectJNI.getInstance().getRServices().consoleSubmit(cmd, clientProperties);
	}

	private boolean submitted;

	
	public void asynchronousConsoleSubmit(final String cmd) throws RemoteException {
		asynchronousConsoleSubmit(cmd,null);
	}
	
	public void asynchronousConsoleSubmit(final String cmd, final HashMap<String, Object> clientProperties) throws RemoteException {

		submitted = false;

		new Thread(new Runnable() {
			public void run() {
				String result = "";

				try {
					result = DirectJNI.getInstance().getRServices().consoleSubmit(cmd,clientProperties);
				} catch (Exception e) {
					result = PoolUtils.getStackTraceAsString(e);
				}

				submitted = true;

				RConsoleAction consoleLogAppend = new RConsoleAction("ASYNCHRONOUS_SUBMIT_LOG");
				HashMap<String, Object> attrs = new HashMap<String, Object>();
				attrs.put("command", cmd);
				attrs.put("result", result);
				consoleLogAppend.setAttributes(attrs);
				consoleLogAppend.setClientProperties(clientProperties);
				DirectJNI.getInstance().notifyRActionListeners(consoleLogAppend);
			}
		}).start();

		if (!submitted && !isBusy()) {
			while (!isBusy()) {
				if (submitted)
					break;
				try {
					Thread.sleep(20);
				} catch (Exception e) {
				}
			}
		}

	}

	public boolean hasPushPopMode() throws RemoteException {
		return true;
	}

	public Serializable pop(String symbol) throws RemoteException {
		Serializable result = DirectJNI.getInstance().getRServices().getObject(symbol);
		System.out.println("result for " + symbol + " : " + result);
		return result;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
		DirectJNI.getInstance().getRServices().putAndAssign((RObject) object, symbol);
	}

	public String[] listSymbols() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listSymbols();
	}

	public boolean hasGraphicMode() throws RemoteException {
		return true;
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		return new RGraphicsPanelRemote(w, h, _graphicNotifier);
	}

	public GDDevice newDevice(int w, int h) throws RemoteException {
		return new GDDeviceImpl(w, h, false, _deviceHashMap);
	}

	public GDDevice newBroadcastedDevice(int w, int h) throws RemoteException {
		return new GDDeviceImpl(w, h, true, _deviceHashMap);
	}
		
	public GDDevice[] listDevices() throws RemoteException {
		GDDevice[] result=new GDDevice[_deviceHashMap.values().size()];
		int i=0; for (GDDevice d:_deviceHashMap.values()) result[i++]=d; 
		return result;
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

	public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException {
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

	public String getDemoSource(String demoName) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getDemoSource(demoName);
	}

	public boolean isBusy() throws RemoteException {
		return DirectJNI.getInstance().getRServices().isBusy();
	}

	Server _virtualizationServer = null;

	public void startHttpServer(final int port) throws RemoteException {
		if (_virtualizationServer != null) {
			throw new RemoteException("Server Already Running");
		} else if (ServerManager.isPortInUse("127.0.0.1", port)) {
			throw new RemoteException("Port already in use");
		} else {

			try {
				log.info("!! Request to run virtualization server on port " + port);
				RKit rkit = new RKit() {
					RServices _r = (RServices) UnicastRemoteObject.toStub(RServantImpl.this);
					ReentrantLock _lock = new ExtendedReentrantLock() {
						public void rawLock() {
							super.lock();
						}
						public void rawUnlock() {
							super.unlock();
						}
					};

					public RServices getR() {
						return _r;
					}

					public ReentrantLock getRLock() {
						return _lock;
					}
				};
				
				_virtualizationServer = new Server(port);
				
				_virtualizationServer.setStopAtShutdown(true);
				
				Context root = new Context(_virtualizationServer, "/rvirtual", Context.SESSIONS|Context.NO_SECURITY);
				
				
				final HttpSessionListener sessionListener=new FreeResourcesListener();				
				root.getSessionHandler().setSessionManager(new HashSessionManager(){
					@Override
					protected void addSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session session, boolean arg1) {
						super.addSession(session, arg1);
						sessionListener.sessionCreated(new HttpSessionEvent(session.getSession()));
					}
					
					@Override
					protected void addSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session session) {
						super.addSession(session);
					}
					
					@Override
					public void removeSession(HttpSession session, boolean invalidate) {
						super.removeSession(session, invalidate);
						sessionListener.sessionDestroyed(new HttpSessionEvent(session));
					}
					
					@Override
					public void removeSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session session, boolean arg1) {
						super.removeSession(session, arg1);
						sessionListener.sessionDestroyed(new HttpSessionEvent(session));
					}
					
					@Override
					protected void removeSession(String clusterId) {
						super.removeSession(clusterId);
					}
				});
				
				root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.GraphicsServlet(rkit)), "/graphics/*");
				root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.RESTServlet(rkit)), "/rest/*");
				root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.CommandServlet(rkit,false)), "/cmd/*");
				root.addServlet(new ServletHolder(new org.kchine.r.server.http.local.LocalHelpServlet(rkit)), "/helpme/*");
				root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.WWWDirectoryServlet(new DiretoryProvider(){
					public String getDirectory() throws Exception {
						return DirectJNI.getInstance().getRServices().getWorkingDirectory();
					}
				},"/wd"))	, "/wd/*");
				root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.WWWDirectoryServlet(ServerManager.WWW_DIR,"/www")), "/www/*");
				root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.WWWDirectoryServlet(ServerManager.WWW_DIR,"/appletlibs")), "/appletlibs/*");
				
				System.out.println("+ going to start virtualization http server port : " + port);
				
				_virtualizationServer.start();
				
				log.info("HTTP R URL :"+"http://"+PoolUtils.getHostIp()+":"+port + "/rvirtual/cmd");

			} catch (Exception e) {
				log.info(PoolUtils.getStackTraceAsString(e));
				e.printStackTrace();
			}
		}

	}

	public void stopHttpServer() throws RemoteException {
		if (_virtualizationServer != null) {
			try {
				_virtualizationServer.stop();
			} catch (Exception e) {
				_virtualizationServer = null;
				throw new RemoteException("", e);
			}
			_virtualizationServer = null;
		}
	}

	public boolean isHttpServerStarted(int port) throws RemoteException {
		return _virtualizationServer != null;
	}

	Server server = null;

	synchronized public RServices cloneServer() throws RemoteException {
		System.out.println("cloneServer");
		try {
			RServices w = ServerManager.createR(null, false, false, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), ServerManager.getRegistryNamingInfo(PoolUtils.getHostIp(), LocalRmiRegistry
					.getLocalRmiRegistryPort()), 256, 256, "", false,null,null,System.getProperty("application_type"),null);
			return w;
		} catch (Exception e) {
			throw new RemoteException("", e);
		}

	}

	static int countCreated(Future<RServices>[] futures) {
		int result = 0;
		for (int i = 0; i < futures.length; ++i)
			if (futures[i].isDone())
				++result;
		return result;
	}

	public byte[] getSvg(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getSvg(script, width, height);
	}
	public byte[] getSvg(String script, Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getSvg(script, width, height, onefile, bg, pointsize);
	}
	
	public byte[] getPostscript(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPostscript(script, width, height);
	}
	
	public byte[] getPostscript(String script, Boolean onefile, String family, String title, String[] fonts, String encoding, String bg, String fg, Integer width, Integer height, Boolean horizontal, Integer pointsize, String paper , Boolean pagecentre, String colormodel) throws RemoteException{
		return DirectJNI.getInstance().getRServices().getPostscript(script, onefile, family, title, fonts, encoding, bg, fg, width, height, horizontal, pointsize, paper, pagecentre, colormodel);
	}
	
	public byte[] getPdf(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPdf(script, width, height);
	}
	
	public byte[] getPdf(String script, Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version, String paper, 
			String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPdf(script, width, height, onefile, family, title, fonts, version, paper, encoding, bg, fg, pointsize, pagecentre, colormodel, useDingbats);
	}
	
	public byte[] getPictex(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPictex(script, width, height);
	}
	
	public byte[] getPictex(String script, Integer width, Integer height, Boolean debug, String bg, String fg) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPictex(script, width, height, debug, bg, fg);
	}
	
	public byte[] getBmp(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getBmp(script, width, height);
	}
	
	public byte[] getBmp(String script, Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getBmp(script, width, height, units, pointsize, bg, res);
	}
	
	public byte[] getJpeg(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getJpeg(script, width, height);
	}
	
	public byte[] getJpeg(String script, Integer width, Integer height, String units, Integer pointsize, Integer quality, String bg,Integer res) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getJpeg(script, width, height, units, pointsize, quality, bg, res);	
	}
	
	public byte[] getPng(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPng(script, width, height);
	}
	
	public byte[] getPng(String script, Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPng(script, width, height, units, pointsize, bg, res);
	}
	
	public byte[] getTiff(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getTiff(script, width, height);
	}
	
	public byte[] getTiff(String script, Integer width, Integer height, String units, Integer pointsize, String compression, String bg,Integer res) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getTiff(script, width, height, units, pointsize, compression, bg, res);	
	}
	
	public byte[] getXfig(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getXfig(script, width, height);
	}
	
	public byte[] getXfig(String script, Boolean onefile, String encoding , String paper, Boolean horizontal, 
			Integer width, Integer height, String family , Integer pointsize, String bg, String fg, Boolean pagecentre) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getXfig(script, onefile, encoding, paper, horizontal, width, height, family, pointsize, bg, fg, pagecentre);
	}
	
	public byte[] getWmf(String script, int width, int height, boolean useserver) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getWmf(script, width, height, useserver);
	}
	
	public byte[] getEmf(String script, int width, int height, boolean useserver) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getEmf(script, width, height, useserver);	
	}
	
	public byte[] getOdg(String script, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getOdg(script, width, height);
	}
	
	public byte[] getFromImageIOWriter(String script, int width, int height,String format) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getFromImageIOWriter(script, width, height, format);
	}
	
	public String getPythonStatus() throws RemoteException {
		return DirectJNI.getInstance().getRServices().getPythonStatus();
	}

	public String pythonExceFromResource(String resource) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonExceFromResource(resource);
	}

	public String pythonExec(String pythonCommand) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonExec(pythonCommand);
	}

	public String pythonExecFromBuffer(String buffer) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonExecFromBuffer(buffer);
	}

	public String pythonExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonExecFromWorkingDirectoryFile(fileName);
	}

	public RObject pythonEval(String pythonCommand) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonEval(pythonCommand);
	}

	public Object pythonEvalAndConvert(String pythonCommand) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonEvalAndConvert(pythonCommand);
	}	
	
	public RObject pythonGet(String name) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonGet(name);
	}
	
	public Object pythonGetAndConvert(String name) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonGetAndConvert(name);
	}
	
	public void pythonSet(String name, Object Value) throws RemoteException {
		DirectJNI.getInstance().getRServices().pythonSet(name, Value);
	}

	public String groovyExecFromResource(String resource) throws RemoteException {
		return DirectJNI.getInstance().getRServices().groovyExecFromResource(resource);
	}

	public String groovyExec(String groovyCommand) throws RemoteException {
		return DirectJNI.getInstance().getRServices().groovyExec(groovyCommand);
	}

	public String groovyExecFromBuffer(String buffer) throws RemoteException {
		return DirectJNI.getInstance().getRServices().groovyExecFromBuffer(buffer);
	}

	public String groovyExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
		return DirectJNI.getInstance().getRServices().groovyExecFromWorkingDirectoryFile(fileName);
	}

	public Object groovyEval(String expression) throws RemoteException {
		return DirectJNI.getInstance().getRServices().groovyEval(expression);
	}

	public Object groovyGet(String name) throws RemoteException {
		return DirectJNI.getInstance().getRServices().groovyGet(name);
	}

	public void groovySet(String name, Object Value) throws RemoteException {
		DirectJNI.getInstance().getRServices().groovySet(name, Value);		
	}
	
	public String getGroovyStatus() throws RemoteException {
		return DirectJNI.getInstance().getRServices().getGroovyStatus();
	}

	public void reinitializeGroovyInterpreter() throws RemoteException {
		DirectJNI.getInstance().getRServices().reinitializeGroovyInterpreter();
	}
	
	public boolean isExtensionAvailable(String extensionName) throws RemoteException {
		return DirectJNI.getInstance().getRServices().isExtensionAvailable(extensionName);
	}
	
	public String[] listExtensions() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listExtensions();
	}
	
	public void installExtension(String extensionName, String extensionURL) throws RemoteException {
		DirectJNI.getInstance().getRServices().installExtension(extensionName, extensionURL);
	}
	
	public void installExtension(String extensionName, byte[] extension) throws RemoteException{
		DirectJNI.getInstance().getRServices().installExtension(extensionName, extension);		
	}
	
	public void removeExtension(String extensionName) throws RemoteException {
		DirectJNI.getInstance().getRServices().removeExtension(extensionName);
	}
	
	public void convertFile(String inputFile, String outputFile, String conversionFilter, boolean useServer) throws RemoteException {
		DirectJNI.getInstance().getOpenOfficeServices().convertFile(inputFile, outputFile, conversionFilter, useServer);		
	}
	
	public SpreadsheetModelRemote newSpreadsheetTableModelRemote(int rowCount, int colCount) throws RemoteException {
		return DirectJNI.getInstance().getRServices().newSpreadsheetTableModelRemote(rowCount, colCount);
	}	
	
	public SpreadsheetModelRemote getSpreadsheetTableModelRemote(String Id) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getSpreadsheetTableModelRemote(Id);
	}

	public SpreadsheetModelRemote[] listSpreadsheetTableModelRemote() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listSpreadsheetTableModelRemote();
	}

	public String[] listSpreadsheetTableModelRemoteId() throws RemoteException {
		return DirectJNI.getInstance().getRServices().listSpreadsheetTableModelRemoteId();
	}
			
	public int countSets() throws RemoteException {
		return Framework.getInstance().countSets();
	}
	
	public SVarSetInterfaceRemote getSet(int i) throws RemoteException {
		return Framework.getInstance().getSet(i).getRemote();
	}
	
	public SVarSetInterfaceRemote getCurrentSet() throws RemoteException {
		return Framework.getInstance().getCurrentSet().getRemote();
	}
	
	public int curSetId() throws RemoteException {
		return Framework.getInstance().curSetId();
	}
	
	public SVarInterfaceRemote getVar(int setId, int i) throws RemoteException {
		return Framework.getInstance().getSet(setId).at(i).getRemote();
	}
	
	public SVarInterfaceRemote getVar(int setId, String name) throws RemoteException {
		return Framework.getInstance().getSet(setId).byName(name).getRemote();
	}
	
	public void setJobId(String jobId) throws RemoteException {
		_jobId=jobId;
		if (_registry instanceof DBLayerInterface) {
			((DBLayerInterface)_registry).setJobID(_servantName, _jobId);
		}		
	}
	

	public String getStub() throws RemoteException {
		return super.getStub();
	}
	
	
    public void addProbeOnVariables(String[] variables) throws RemoteException {
    	DirectJNI.getInstance().getRServices().addProbeOnVariables(variables);
    }
    
    public void removeProbeOnVariables(String[] variables) throws RemoteException{
    	DirectJNI.getInstance().getRServices().removeProbeOnVariables(variables);
    }
    
    public String[] getProbedVariables() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getProbedVariables();
    }
    public void setProbedVariables(String[] variables) throws RemoteException {
    	DirectJNI.getInstance().getRServices().setProbedVariables(variables);    	
    }
    
    public String[] getMissingLibraries(String[] requiredLibraries) throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getMissingLibraries(requiredLibraries);
    }
    
    public RObject cellsGet(String range , String type, String spreadsheetName ) throws RemoteException {
    	return DirectJNI.getInstance().getRServices().cellsGet( range ,  type,  spreadsheetName );
    }
    
    public Object cellsGetConverted(String range , String type, String spreadsheetName ) throws RemoteException {
    	return DirectJNI.getInstance().getRServices().cellsGetConverted( range ,  type,  spreadsheetName );
    }
    
    public void  cellsPut(Object value , String location, String spreadsheetName ) throws RemoteException {
    	DirectJNI.getInstance().getRServices().cellsPut( value ,  location,  spreadsheetName );
    }

    public void addProbeOnCells(String spreadsheetName) throws RemoteException{
    	DirectJNI.getInstance().getRServices().addProbeOnCells( spreadsheetName );
    }
    
    public boolean isProbeOnCell(String spreadsheetName) throws RemoteException{
    	return DirectJNI.getInstance().getRServices().isProbeOnCell(  spreadsheetName );
    }
    
    public void removeProbeOnCells(String spreadsheetName) throws RemoteException{
    	DirectJNI.getInstance().getRServices().removeProbeOnCells( spreadsheetName );
    }
    
    public String getWorkingDirectory() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getWorkingDirectory();
    }
    
    public String getInstallDirectory() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getInstallDirectory();
    }
    
    public String getExtensionsDirectory() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getExtensionsDirectory();
    }
    
    public String scilabConsoleSubmit(String cmd) throws RemoteException {
    	return DirectJNI.getInstance().getScilabServices().scilabConsoleSubmit(cmd);
    }
    
    public Object scilabGetObject(String expression) throws RemoteException {
    	return DirectJNI.getInstance().getScilabServices().scilabGetObject(expression);
    }
    
    public void scilabPutAndAssign(Object obj, String name) throws RemoteException {
    	DirectJNI.getInstance().getScilabServices().scilabPutAndAssign(obj, name);    	
    }

    public boolean scilabExec(String cmd) throws RemoteException {
    	return DirectJNI.getInstance().getScilabServices().scilabExec(cmd);
    }
    
    public void installPackage(String label, byte[] packageBuffer) throws RemoteException {
    	DirectJNI.getInstance().getRServices().installPackage(label, packageBuffer);    	
    }
    
    public void installPackages(String[] label, byte[][] packageBuffer) throws RemoteException {
    	DirectJNI.getInstance().getRServices().installPackages(label, packageBuffer);    	
    }
        
    public String getRHome() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getRHome();
    }
    
    public String getRVersion() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getRVersion();
    }
    
    public String getRJavaHome() throws RemoteException {
    	return DirectJNI.getInstance().getRServices().getRJavaHome();
    }
    
}