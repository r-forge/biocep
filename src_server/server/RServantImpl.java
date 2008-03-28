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
package server;

import graphics.pop.GDDevice;
import graphics.rmi.GraphicNotifier;
import graphics.rmi.JGDPanel;
import graphics.rmi.RClustserInterface;
import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
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
import mapping.RPackage;
import mapping.ReferenceInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.PropertyConfigurator;
import org.bioconductor.packages.rservices.RObject;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.rosuda.JRI.Rengine;
import remoting.AssignInterface;
import remoting.FileDescription;
import remoting.RAction;
import remoting.RCallback;
import remoting.RKit;
import remoting.RNI;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.InitializingException;
import uk.ac.ebi.microarray.pools.ManagedServantAbstract;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemotePanel;
import uk.ac.ebi.microarray.pools.http.LocalClassServlet;
import util.Utils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class RServantImpl extends ManagedServantAbstract implements RServices {

	private StringBuffer _log = new StringBuffer();

	private HashMap<String, RPackage> _rim = null;

	private RNI _remoteRni = null;

	private AssignInterface _assignInterface = null;

	private GraphicNotifier _graphicNotifier = null;

	private HashMap<Integer, GDDeviceImpl> _deviceHashMap = new HashMap<Integer, GDDeviceImpl>();

	private boolean _isReady = false;

	RServices _rCreationPb = new RServicesObject();

	public static RServices _instance=null;
	
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(RServantImpl.class);

	public String runR(ExecutionUnit eu) {
		return DirectJNI.getInstance().runR(eu);
	}

	public RServantImpl(String name, String prefix, Registry registry) throws RemoteException {
		super(name, prefix, registry);
		// --------------


			if (log instanceof Log4JLogger) {
				Properties log4jProperties = new Properties();
				for (Object sprop : System.getProperties().keySet()) {
					if (((String) sprop).startsWith("log4j.")) {
						log4jProperties.put(sprop, System.getProperties().get(sprop));
					}
				}
				PropertyConfigurator.configure(log4jProperties);
			}
			
	
		init();
		log.info("Stub:" + PoolUtils.stubToHex(this));
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

			if (System.getProperty("preprocess.help") != null && System.getProperty("preprocess.help").equalsIgnoreCase("true")) {
				new Thread(new Runnable() {
					public void run() {
						DirectJNI.getInstance().preprocessHelp();
					}
				}).start();
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
			
			_instance=(RServices)java.rmi.server.RemoteObject.toStub(this);
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

	public RObject callAndGetReference(String methodName, RObject... args) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().callAndGetReference(methodName, args);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void callAndAssign(String varName, String methodName, RObject... args) throws RemoteException {
		DirectJNI.getInstance().getRServices().callAndAssign(varName, methodName, args);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public void freeReference(RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().freeReference(refObj);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject referenceToObject(RObject refObj) throws RemoteException {

		ReferenceInterface refObjCast = (ReferenceInterface) refObj;
		if (refObjCast.getAssignInterface().equals(_assignInterface)) {
			RObject result = DirectJNI.getInstance().getRServices().referenceToObject(refObj);
			_log.append(DirectJNI.getInstance().getRServices().getStatus());
			return result;
		} else {
			return refObjCast.extractRObject();
		}
	};

	public RObject putAndGetReference(RObject obj) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().putAndGetReference(obj);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void putAndAssign(RObject obj, String name) throws RemoteException {
		DirectJNI.getInstance().getRServices().putAndAssign(obj, name);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public RObject get(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().get(expression);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public RObject getReference(String expression) throws RemoteException {
		RObject result = DirectJNI.getInstance().getRServices().getReference(expression);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public void assignReference(String varname, RObject refObj) throws RemoteException {
		DirectJNI.getInstance().getRServices().assignReference(varname, refObj);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
	}

	public String evaluate(String expression) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluate(expression);
		_log.append(DirectJNI.getInstance().getRServices().getStatus());
		return result;
	}

	public String evaluate(final String expression, final int n) throws RemoteException {
		String result = DirectJNI.getInstance().getRServices().evaluate(expression, n);
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

	private boolean submitted;

	public void asynchronousConsoleSubmit(final String cmd) throws RemoteException {

		submitted = false;

		new Thread(new Runnable() {
			public void run() {
				String result = "";

				try {
					result = DirectJNI.getInstance().getRServices().consoleSubmit(cmd);
				} catch (Exception e) {
					result = PoolUtils.getStackTraceAsString(e);
				}

				submitted = true;

				RAction consoleLogAppend = new RAction("ASYNCHRONOUS_SUBMIT_LOG");
				HashMap<String, Object> attrs = new HashMap<String, Object>();
				attrs.put("command", cmd);
				attrs.put("result", result);
				consoleLogAppend.setAttributes(attrs);
				DirectJNI.getInstance().addAction(consoleLogAppend);
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
		Serializable result = DirectJNI.getInstance().getRServices().get(symbol);
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

	public boolean isBusy() throws RemoteException {
		return DirectJNI.getInstance().getRServices().isBusy();
	}

	Server _virtualizationServer = null;

	public void startHttpServer(final int port) throws RemoteException {
		log.info(" 1 startHttpServer called");
		System.out.println(" 2 startHttpServer called");
		if (_virtualizationServer != null) {
			throw new RemoteException("Server Already Running");
		} else if (ServerLauncher.isPortInUse("127.0.0.1", port)) {
			throw new RemoteException("Port already in use");
		} else {

			try {
				log.info("!! Request to run virtualization server on port " + port);
				RKit rkit = new RKit() {
					RServices _r = (RServices) UnicastRemoteObject.toStub(RServantImpl.this);
					ReentrantLock _lock = new ReentrantLock();

					public RServices getR() {
						return _r;
					}

					public ReentrantLock getRLock() {
						return _lock;
					}
				};

				_virtualizationServer = new Server(port);
				_virtualizationServer.setStopAtShutdown(true);
				Context root = new Context(_virtualizationServer, "/", Context.SESSIONS);
				root.addServlet(new ServletHolder(new http.local.LocalGraphicsServlet(rkit)), "/graphics/*");
				root.addServlet(new ServletHolder(new http.CommandServlet(rkit)), "/cmd/*");
				root.addServlet(new ServletHolder(new http.local.LocalHelpServlet(rkit)), "/helpme/*");
				System.out.println("+++++++++++++++++++ going to start virtualization http server port : " + port);
				_virtualizationServer.start();

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
			RServices w = ServerLauncher.createR(false, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), PoolUtils.getHostIp(), LocalRmiRegistry
					.getLocalRmiRegistryPort(), 256, 256, false);
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

	public Vector<String> getSvg(String expression, int width, int height) throws RemoteException {
		return DirectJNI.getInstance().getRServices().getSvg(expression, width, height);
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

	public String pythonExecFromBuffer(StringBuffer buffer) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonExecFromBuffer(buffer);
	}

	public String pythonExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonExecFromWorkingDirectoryFile(fileName);
	}

	public RObject pythonEval(String pythonCommand) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonEval(pythonCommand);
	}

	public RObject pythonGet(String name) throws RemoteException {
		return DirectJNI.getInstance().getRServices().pythonGet(name);
	}
	
	public void pythonSet(String name, RObject Value) throws RemoteException {
		DirectJNI.getInstance().getRServices().pythonSet(name, Value);
	}
}