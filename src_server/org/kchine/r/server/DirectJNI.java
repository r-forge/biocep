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
package org.kchine.r.server;

import org.kchine.openoffice.server.OpenOfficeServices;
import org.kchine.r.server.graphics.utils.Dimension;
import java.awt.Point;
import org.kchine.r.server.graphics.utils.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.imageio.ImageIO;
import org.apache.commons.logging.Log;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.kchine.r.ObjectNameInterface;
import org.kchine.r.RArray;
import org.kchine.r.RArrayRef;
import org.kchine.r.RChar;
import org.kchine.r.RCharRef;
import org.kchine.r.RComplex;
import org.kchine.r.RComplexRef;
import org.kchine.r.RDataFrame;
import org.kchine.r.RDataFrameRef;
import org.kchine.r.REnvironment;
import org.kchine.r.REnvironmentRef;
import org.kchine.r.RFactor;
import org.kchine.r.RFactorRef;
import org.kchine.r.RInteger;
import org.kchine.r.RIntegerRef;
import org.kchine.r.RList;
import org.kchine.r.RListRef;
import org.kchine.r.RLogical;
import org.kchine.r.RLogicalRef;
import org.kchine.r.RMatrix;
import org.kchine.r.RMatrixRef;
import org.kchine.r.RNamedArgument;
import org.kchine.r.RNumeric;
import org.kchine.r.RNumericRef;
import org.kchine.r.RObject;
import org.kchine.r.RS3;
import org.kchine.r.RS3Ref;
import org.kchine.r.RUnknown;
import org.kchine.r.RVector;
import org.kchine.r.server.AssignInterface;
import org.kchine.r.server.FileDescription;
import org.kchine.r.server.GenericCallbackDevice;
import org.kchine.r.server.RCallBack;
import org.kchine.r.server.RCollaborationListener;
import org.kchine.r.server.RConsoleAction;
import org.kchine.r.server.RConsoleActionListener;
import org.kchine.r.server.RNI;
import org.kchine.r.server.RPackage;
import org.kchine.r.server.RServices;
import org.kchine.r.server.ReferenceInterface;
import org.kchine.r.server.StandardReference;
import org.kchine.r.server.UserStatus;
import org.kchine.r.server.Utils;
import org.kchine.r.server.graphics.DoublePoint;
import org.kchine.r.server.graphics.GDContainerBag;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.graphics.GraphicNotifier;
import org.kchine.r.server.http.Java2DUtils;
import org.kchine.r.server.impl.DefaultAssignInterfaceImpl;
import org.kchine.r.server.impl.RGraphicsPanelRemote;
import org.kchine.r.server.iplots.SVarInterfaceRemote;
import org.kchine.r.server.iplots.SVarSetInterfaceRemote;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.r.server.scripting.GroovyInterpreter;
import org.kchine.r.server.scripting.GroovyInterpreterSingleton;
import org.kchine.r.server.scripting.PythonInterpreterSingleton;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemote;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemoteImpl;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RemoteLogListener;
import org.kchine.rpf.RemotePanel;
import org.kchine.scilab.server.ScilabServices;
import org.kchine.scilab.server.ScilabServicesSingleton;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.RengineWrapper;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.JavaGD;

import static org.kchine.r.server.RConst.*;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DirectJNI {

	public static ClassLoader _mappingClassLoader = DirectJNI.class.getClassLoader();
	public static ClassLoader _resourcesClassLoader = DirectJNI.class.getClassLoader();

	public static Vector<String> _abstractFactories = new Vector<String>();
	public static HashMap<String, String> _factoriesMapping = new HashMap<String, String>();
	public static HashMap<String, String> _s4BeansMappingRevert = new HashMap<String, String>();
	public static HashMap<String, String> _s4BeansMapping = new HashMap<String, String>();
	public static Vector<String> _packageNames = new Vector<String>();
	public static HashMap<String, Class<?>> _s4BeansHash = new HashMap<String, Class<?>>();
	public static HashMap<String, Vector<Class<?>>> _rPackageInterfacesHash = new HashMap<String, Vector<Class<?>>>();

	private static final String V_NAME_PREFIXE = "V__";
	private static final String V_TEMP_PREFIXE = V_NAME_PREFIXE + "TEMP__";
	private static final String PENV = ".PrivateEnv";
	private static final String PROTECT_VAR_PREFIXE = "PROTECT_";
	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private static final String ECHO_VAR_NAME = ".echo___";
	private static final Integer singletonLock = new Integer(0);
	private static DirectJNI _djni = null;
	private static String INSTANCE_NAME = "LOCAL_R";
	private static String DEFAULT_WDIR_ROOT = System.getProperty("user.dir");
	private static String WDIR = null;
	private static HashMap<String, String> _symbolUriMap = new HashMap<String, String>();
	public Rengine _rEngine = null;
	private StringBuffer _sharedBuffer = new StringBuffer();
	private int _markerA = -1;
	private ExecutionUnit _sharedExecutionUnit = null;
	private final ReentrantLock _runRlock = new ReentrantLock();
	private final ReentrantLock _mainLock = new ReentrantLock();
	private final Condition _availableCondition = _runRlock.newCondition();
	private long _varCounter = 0;
	private long _tempCounter = 0;
	private String _continueStr = null;
	private String _promptStr = null;
	private Vector<String> _bootstrapRObjects = new Vector<String>();
	private long _privateEnvExp;

	private HashMap<String, Vector<String>> _nameSpacesHash = new HashMap<String, Vector<String>>();
	private HashMap<String, RPackage> _packs = new HashMap<String, RPackage>();
	private Vector<Long> _protectedExpReference = new Vector<Long>();
	private static Vector<String> demosList = null;
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(DirectJNI.class);
	private HashMap<String, byte[]> _resourceCache = new HashMap<String, byte[]>();
	private String _userInput = null;
	private boolean _stopRequired = false;
	private PrintStream _o = System.out;
	private HashMap<String, UserStatus> _usersHash = new HashMap<String, UserStatus>();
	private HashMap<String, SpreadsheetModelRemoteImpl> _spreadsheetTableModelRemoteHashMap = new HashMap<String, SpreadsheetModelRemoteImpl>();
	private Vector<RConsoleActionListener> _ractionListeners = new Vector<RConsoleActionListener>();
	private String _originatorUID;
	private HashMap<String, Object> _clientProperties;
	private Vector<RCallBack> _callbacks = new Vector<RCallBack>();
	private Vector<RCollaborationListener> _rCollaborationListeners = new Vector<RCollaborationListener>();
	private HashSet<String> _cairoCapabilities = new HashSet<String>();

	public static DirectJNI getInstance() {

		if (_djni != null)
			return _djni;
		synchronized (singletonLock) {
			if (_djni == null) {
				_djni = new DirectJNI();
			}
			return _djni;
		}
	}

	public String runR(ExecutionUnit eu) {
		return runR(eu, null);
	}

	public String runR(ExecutionUnit eu, HashMap<String, Object> clientProperties) {

		if (Thread.currentThread() == _rEngine) {
			throw new RuntimeException("runR called from within the R MainLoop Thread");
		} else {
			_mainLock.lock();
			_clientProperties = clientProperties;
			try {
				boolean hasConsoleInput = (eu.getConsoleInput() != null && !eu.getConsoleInput().equals(""));
				String consoleLog = null;

				_runRlock.lock();
				try {
					_sharedExecutionUnit = eu;

					try {
						_availableCondition.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				} finally {
					_runRlock.unlock();
				}

				consoleLog = _sharedBuffer.toString();

				if (hasConsoleInput && consoleLog.trim().equals("")) {

					System.setOut(new PrintStream(new OutputStream() {
						public void write(final byte[] b) throws IOException {
							_o.write(b);
							if (new String(b).startsWith(_continueStr)) {
								HashMap<String, Object> attrs = new HashMap<String, Object>();
								attrs.put("log", _continueStr);
								notifyRActionListeners(new RConsoleAction("APPEND_CONSOLE_CONTINUE", attrs));
							}
						}

						public void write(final byte[] b, final int off, final int len) throws IOException {
							_o.write(b, off, len);
							if (new String(b, off, len).startsWith(_continueStr)) {
								HashMap<String, Object> attrs = new HashMap<String, Object>();
								attrs.put("log", _continueStr);
								notifyRActionListeners(new RConsoleAction("APPEND_CONSOLE_CONTINUE", attrs));
							}
						}

						public void write(final int b) throws IOException {
							_o.write(b);
						}
					}));

					_runRlock.lock();
					try {
						_sharedExecutionUnit = new ExecutionUnit() {
							public void run(Rengine e) {
							}

							public boolean emptyConsoleBufferBefore() {
								return false;
							}

							public String getConsoleInput() {
								return " ";
							}
						};

						try {
							_availableCondition.await();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					} finally {
						_runRlock.unlock();
					}

					System.setOut(_o);

				}

				return _sharedBuffer.toString();

			} finally {
				_clientProperties = null;
				_mainLock.unlock();
			}

		}
	}

	public InputStream getResourceAsStream(String resource) {
		if (resource.startsWith("/"))
			resource = resource.substring(1);
		byte[] buffer = _resourceCache.get(resource);
		if (buffer != null) {
			return new ByteArrayInputStream(buffer);
		} else {
			InputStream is = _resourcesClassLoader.getResourceAsStream(resource);
			if (is == null)
				return null;
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int b;
				while ((b = is.read()) != -1) {
					baos.write(b);
				}
				buffer = baos.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
			_resourceCache.put(resource, buffer);
			return new ByteArrayInputStream(buffer);
		}
	}

	boolean resourceExists(String resource) {
		return getResourceAsStream(resource) != null;
	}

	public boolean runRInProgress() {
		return _runRlock.isLocked();
	}

	private class RMainLoopCallbacksImpl implements RMainLoopCallbacks {

		boolean busy = false;

		public void rBusy(Rengine re, int which) {
			busy = (which == 1);
		}

		public String rChooseFile(Rengine re, int newFile) {
			return null;
		}

		public void rFlushConsole(Rengine re) {
			System.out.println("rFlushConsole");
		}

		public void rLoadHistory(Rengine re, String filename) {
		}

		public String rReadConsole(Rengine re, String prompt, int addToHistory) {
			String consoleInput = "\n";
			if (busy) {

				_userInput = null;
				notifyRActionListeners(new RConsoleAction("GET_USER_INPUT", new HashMap<String, Object>()));
				_stopRequired = false;
				while (_userInput == null) {
					try {
						if (_stopRequired)
							break;
						Thread.sleep(100);
					} catch (Exception e) {
					}
					;
				}
				consoleInput = _userInput + "\n";

			} else if (_sharedExecutionUnit != null) {

				_runRlock.lock();
				try {
					if (_sharedExecutionUnit != null) {
						if (_sharedExecutionUnit.emptyConsoleBufferBefore())
							_sharedBuffer.setLength(0);

						_markerA = -1;
						_sharedExecutionUnit.run(re);

						if (_sharedExecutionUnit.getConsoleInput() != null && !_sharedExecutionUnit.getConsoleInput().equals("")) {
							System.out.print(prompt);
							consoleInput = _sharedExecutionUnit.getConsoleInput() + "\n";
						}

						_sharedExecutionUnit = null;
					}

					_availableCondition.signal();

				} finally {
					_runRlock.unlock();
				}
			} else {
				try {
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return consoleInput;
		}

		public void rSaveHistory(Rengine re, String filename) {
		}

		public void rShowMessage(Rengine re, String message) {
			appendText("Message From R :" + message);
		}

		private void appendText(String t) {
			_sharedBuffer.append(t);
			HashMap<String, Object> attrs = new HashMap<String, Object>();
			attrs.put("log", t);
			notifyRActionListeners(new RConsoleAction("APPEND_CONSOLE_LOG", attrs));
		}

		public void rWriteConsole(Rengine re, String text) {
			appendText(text);
		}

		public void rWriteConsole(Rengine re, String s, int i) {
			appendText(s);
		}

	}

	public static String[] getROptions() {
		String[] roptions = new String[] { "--no-save" };

		if (System.getProperty("proxy_host") != null && !System.getProperty("proxy_host").equals("")) {
			roptions = new String[] { "--no-save", "--internet2" };
		}

		if (System.getProperty("r.options") != null && !System.getProperty("r.options").equals("")) {
			Vector<String> roptionsVector = new Vector<String>();
			StringTokenizer st = new StringTokenizer(System.getProperty("r.options"), " ");
			while (st.hasMoreElements())
				roptionsVector.add((String) st.nextElement());
			roptions = roptionsVector.toArray(new String[0]);
		}
		System.out.println("r options:" + Arrays.toString(roptions));
		return roptions;
	}

	private DirectJNI() {

		_rEngine = new RengineWrapper(getROptions(), true, new RMainLoopCallbacksImpl());

		if (!_rEngine.waitForR()) {
			log.info("Cannot load R");
			return;
		}

		try {
			String initRSourcingLog = getRServices().sourceFromResource("/org/kchine/r/server/rscripts/init.R");
			System.out.println("init.R sourcing log : " + initRSourcingLog);
			initPrivateEnv();
			_continueStr = ((RChar) ((RList) getRServices().getObject("options('continue')")).getValue()[0]).getValue()[0];
			_promptStr = ((RChar) ((RList) getRServices().getObject("options('prompt')")).getValue()[0]).getValue()[0];

			if (((RLogical) getRServices().getObject("exists('Cairo')")).getValue()[0]) {

				_cairoCapabilities = new HashSet<String>();
				_cairoCapabilities.add("png");
				_cairoCapabilities.add("pdf");
				_cairoCapabilities.add("svg");
				_cairoCapabilities.add("ps");
				_cairoCapabilities.add("x11");

				try {
					getRServices().evaluate("Cairo_capabilities<-Cairo.capabilities()");

					if (((RLogical) getRServices().getObject("exists('Cairo_capabilities')")).getValue()[0]) {

						RLogical c = (RLogical) getRServices().getObject("Cairo_capabilities");
						getRServices().evaluate("rm(Cairo_capabilities)");
						_cairoCapabilities = new HashSet<String>();
						for (int i = 0; i < c.getValue().length; ++i)
							if (c.getValue()[i])
								_cairoCapabilities.add(c.getNames()[i]);
						System.out.println("Cairo capabilities : " + _cairoCapabilities);

					} else {

						System.out.println("Cairo capabilities (Default) : " + _cairoCapabilities);

					}
				} catch (Exception e) {
					// TODO: handle exception
				}
			}

			getRServices().consoleSubmit("1");

			upgdateBootstrapObjects();

			WDIR = System.getProperty("working.dir.root") != null && !System.getProperty("working.dir.root").equals("") ? System
					.getProperty("working.dir.root")
					+ "/" + INSTANCE_NAME : DEFAULT_WDIR_ROOT;
			try {
				WDIR = new File(WDIR).getCanonicalPath().replace('\\', '/');
				regenerateWorkingDirectory(true);
			} catch (Exception e) {
				e.printStackTrace();
			}


		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public Vector<String> getBootStrapRObjects() {
		return _bootstrapRObjects;
	}

	public void applySandbox() {
		try {
			getRServices().sourceFromResource("/rscripts/sandbox.R");
			upgdateBootstrapObjects();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void preprocessHelp(final String[] packNames, boolean inBackground) {
		Runnable run = new Runnable() {
			public void run() {
				for (int i = 0; i < packNames.length; ++i) {
					try {

						String uriPrefix = "/library/" + packNames[i] + "/html/";
						String indexFile = null;

						if (System.getenv().get("R_LIBS") != null && !System.getenv().get("R_LIBS").equals("")) {
							indexFile = System.getenv().get("R_LIBS") + "/" + packNames[i] + "/html/" + "00Index.html";
							if (!new File(indexFile).exists()) {
								indexFile = null;
							} else {
								// System.out.println("index file:" +
								// indexFile);
							}
						}

						if (indexFile == null) {
							indexFile = System.getenv().get("R_HOME") + uriPrefix + "00Index.html";
							if (!new File(indexFile).exists())
								indexFile = null;
						}

						if (indexFile == null)
							continue;

						Parser p = new Parser(indexFile);
						processNode(packNames[i], uriPrefix, p.extractAllNodesThatMatch(new TagNameFilter("BODY")).elementAt(0));

					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}
		};

		if (inBackground) {
			new Thread(run).start();
		} else {
			run.run();
		}
	}

	public static void processNode(String packageName, String uriPrefix, Node n) {
		if (n instanceof LinkTag) {
			LinkTag lt = (LinkTag) n;
			if (lt.getLinkText() != null && !lt.getLinkText().equals("") && !lt.getLinkText().equalsIgnoreCase("overview")
					&& !lt.getLinkText().equalsIgnoreCase("directory") && lt.extractLink().endsWith(".html")) {
				_symbolUriMap.put(packageName + "~" + lt.getLinkText(), uriPrefix + lt.extractLink().substring(lt.extractLink().lastIndexOf('/') + 1));
			}
		}
		NodeList children = n.getChildren();
		if (children != null) {
			for (int i = 0; i < children.size(); ++i) {
				processNode(packageName, uriPrefix, children.elementAt(i));
			}
		}
	}

	public void initPackages() {
		for (int i = 0; i < _packageNames.size(); ++i) {
			try {
				getRServices().getPackage(_packageNames.elementAt(i));
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void toggleMarker() {
		if (_sharedBuffer == null) {
			_markerA = -1;
		} else {
			_markerA = _sharedBuffer.length();
		}
	}

	public String getStatusSinceMarker() {
		return _markerA == -1 ? null : _sharedBuffer.substring(_markerA);
	}

	public String cutStatusSinceMarker() {
		if (_markerA == -1) {
			return null;
		} else {
			String result = _sharedBuffer.substring(_markerA);
			_sharedBuffer.setLength(_markerA);
			return result;
		}
	}

	// public for internal use only
	public long putObject(Object obj) throws Exception {

		Rengine e = _rEngine;

		if (obj == null) {
			return e.rniEval(e.rniParse("NULL", 1), 0);
		}

		if (obj instanceof ReferenceInterface) {
			throw new Exception("putObject is not allowed on proxy objects");
		}

		if (!(obj instanceof RObject)) {
			if (!obj.getClass().isArray()) {
				if (obj instanceof Integer)
					obj = new RInteger((Integer) obj);
				else if (obj instanceof Long)
					obj = new RInteger((int) ((Long) obj).longValue());
				else if (obj instanceof String)
					obj = new RChar((String) obj);
				else if (obj instanceof Double)
					obj = new RNumeric((Double) obj);
				else if (obj instanceof Float)
					obj = new RNumeric((Float) obj);
				else if (obj instanceof Boolean)
					obj = new RLogical((Boolean) obj);
				else if (obj instanceof ArrayList) {

					if (((ArrayList<?>) obj).size() > 0) {
						Class<?> componentType = ((ArrayList<?>) obj).get(0).getClass();
						if (componentType == Integer.class)
							obj = getRArrayFromJavaArray((Integer[]) ((ArrayList<?>) obj).toArray(new Integer[0]));
						else if (obj instanceof Long)
							obj = getRArrayFromJavaArray((Long[]) ((ArrayList<?>) obj).toArray(new Long[0]));
						else if (obj instanceof String)
							obj = getRArrayFromJavaArray((String[]) ((ArrayList<?>) obj).toArray(new String[0]));
						else if (obj instanceof Double)
							obj = getRArrayFromJavaArray((Double[]) ((ArrayList<?>) obj).toArray(new Double[0]));
						else if (obj instanceof Float)
							obj = getRArrayFromJavaArray((Float[]) ((ArrayList<?>) obj).toArray(new Float[0]));
						else if (obj instanceof Boolean)
							obj = getRArrayFromJavaArray((Boolean[]) ((ArrayList<?>) obj).toArray(new Boolean[0]));
						else {
							throw new Exception("cannot convert type in ArrayList");
						}
					} else {
						throw new Exception("empty ArrayList");
					}

				} else
					throw new Exception("argument classe must be a subclass of RObject or Standard Java Types");
			} else {
				Class<?> componentType = obj.getClass().getComponentType();
				if (componentType == int.class)
					obj = new RInteger((int[]) obj);
				else if (componentType == String.class)
					obj = new RChar((String[]) obj);
				else if (componentType == double.class)
					obj = new RNumeric((double[]) obj);
				else if (componentType == boolean.class)
					obj = new RLogical((boolean[]) obj);
				else {
					obj = getRArrayFromJavaArray(obj);

					// throw new Exception("argument classe must be a subclass
					// of RObject or Standard Java Types");
				}
			}
		}

		if (obj instanceof ObjectNameInterface) {
			String env = ((ObjectNameInterface) obj).getRObjectEnvironment();
			if (env == null || env.equals(""))
				env = ".GlobalEnv";
			return e.rniEval(e.rniParse(env + "$" + ((ObjectNameInterface) obj).getRObjectName(), 1), 0);
		}

		long resultId = -1;

		if (obj instanceof RLogical) {

			RLogical vec = (RLogical) obj;

			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('logical')", 1), 0);
			} else {
				resultId = e.rniPutBoolArray(vec.getValue());
			}

			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RInteger) {

			RInteger vec = (RInteger) obj;
			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('integer')", 1), 0);
			} else {
				resultId = e.rniPutIntArray(vec.getValue());
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RNumeric) {

			RNumeric vec = (RNumeric) obj;
			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('numeric')", 1), 0);
			} else {
				resultId = e.rniPutDoubleArray(vec.getValue());
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RComplex) {

			RComplex vec = (RComplex) obj;

			if (vec.getReal() != null && vec.getReal().length == 0 && vec.getImaginary() != null && vec.getImaginary().length == 0) {
				resultId = e.rniEval(e.rniParse("new('complex')", 1), 0);
			} else {
				String v_temp_1 = newTemporaryVariableName();
				String v_temp_2 = newTemporaryVariableName();
				e.rniAssign(v_temp_1, e.rniPutDoubleArray(vec.getReal()), 0);
				e.rniAssign(v_temp_2, e.rniPutDoubleArray(vec.getImaginary()), 0);
				resultId = e.rniEval(e.rniParse(v_temp_1 + "+1i*" + v_temp_2, 1), 0);
				e.rniEval(e.rniParse("rm(" + v_temp_1 + "," + v_temp_2 + ")", 1), 0);
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RChar) {

			RChar vec = (RChar) obj;
			if (vec.getValue() != null && vec.getValue().length == 0) {
				resultId = e.rniEval(e.rniParse("new('character')", 1), 0);
			} else {
				resultId = e.rniPutStringArray(vec.getValue());
			}
			if (vec.getNames() != null)
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(vec.getNames()));

			if (vec.getIndexNA() != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[vec.length()];
				for (int i = 0; i < vec.getIndexNA().length; ++i)
					naBooleans[vec.getIndexNA()[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RMatrix || obj instanceof RArray) {

			RArray rarray = (RArray) obj;
			resultId = putObject(rarray.getValue());
			e.rniSetAttr(resultId, "dim", e.rniPutIntArray(rarray.getDim()));
			if (rarray.getDimnames() != null) {
				e.rniSetAttr(resultId, "dimnames", putObject(rarray.getDimnames()));
			}

			if (rarray.getValue().getNames() != null) {
				e.rniSetAttr(resultId, "names", e.rniPutStringArray(rarray.getValue().getNames()));
			}

			int[] vecIndexNa = null;
			if (rarray.getValue() instanceof RNumeric) {
				vecIndexNa = ((RNumeric) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RInteger) {
				vecIndexNa = ((RInteger) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RComplex) {
				vecIndexNa = ((RComplex) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RChar) {
				vecIndexNa = ((RChar) rarray.getValue()).getIndexNA();
			} else if (rarray.getValue() instanceof RLogical) {
				vecIndexNa = ((RLogical) rarray.getValue()).getIndexNA();
			}

			if (vecIndexNa != null) {
				String temp = newTemporaryVariableName();
				e.rniAssign(temp, resultId, 0);
				boolean[] naBooleans = new boolean[rarray.getValue().length()];
				for (int i = 0; i < vecIndexNa.length; ++i)
					naBooleans[vecIndexNa[i]] = true;
				String naBooleansVar = newTemporaryVariableName();
				e.rniAssign(naBooleansVar, e.rniPutBoolArray(naBooleans), 0);
				e.rniEval(e.rniParse("is.na(" + temp + ")<-" + naBooleansVar, 1), 0);
				resultId = e.rniEval(e.rniParse(temp, 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + "," + naBooleansVar + ")", 1), 0);
			}

		} else if (obj instanceof RList) {

			RList rlist = (RList) obj;
			if (rlist.getValue() != null && rlist.getValue().length > 0) {
				long[] value_ids = new long[rlist.getValue().length];
				for (int i = 0; i < value_ids.length; ++i) {
					RObject v = (RObject) rlist.getValue()[i];
					if (v != null)
						value_ids[i] = putObject(v);
					else {
						value_ids[i] = e.rniEval(e.rniParse("NULL", 1), 0);
					}
				}
				resultId = e.rniPutVector(value_ids);
				if (rlist.getNames() != null) {
					e.rniSetAttr(resultId, "names", e.rniPutStringArray(rlist.getNames()));
				}
			} else {
				resultId = e.rniEval(e.rniParse("new(\"list\")", 1), 0);
				if (rlist.getNames() != null) {
					e.rniSetAttr(resultId, "names", e.rniPutStringArray(rlist.getNames()));
				}
			}

			if (obj instanceof RS3) {
				String[] classAttribute = ((RS3) obj).getClassAttribute();
				if (classAttribute != null) {
					e.rniSetAttr(resultId, "class", e.rniPutStringArray(classAttribute));
				}
			}

		} else if (obj instanceof RDataFrame) {

			RDataFrame dataframe = (RDataFrame) obj;
			resultId = putObject(dataframe.getData());
			e.rniSetAttr(resultId, "row.names", e.rniPutStringArray(dataframe.getRowNames()));
			e.rniSetAttr(resultId, "class", e.rniPutString("data.frame"));

		} else if (obj instanceof RFactor) {
			RFactor factor = (RFactor) obj;

			if (factor.getCode() == null || factor.getCode().length == 0) {
				resultId = e.rniEval(e.rniParse("new('integer')", 1), 0);
			} else {
				resultId = e.rniPutIntArray(factor.getCode());
			}

			e.rniSetAttr(resultId, "levels", e.rniPutStringArray(factor.getLevels()));
			e.rniSetAttr(resultId, "class", e.rniPutString("factor"));

		} else if (obj instanceof REnvironment) {

			REnvironment environment = (REnvironment) obj;

			resultId = e.rniEval(e.rniParse("new.env(parent=baseenv())", 1), 0);

			String resultTemp = newTemporaryVariableName();
			e.rniAssign(resultTemp, resultId, 0);
			for (Iterator<?> iter = environment.getData().keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				String temp = newTemporaryVariableName();
				RObject value = (RObject) environment.getData().get(key);
				e.rniAssign(temp, putObject(value), 0);
				e.rniEval(e.rniParse("assign(\"" + key + "\", " + temp + ", env=" + resultTemp + ")", 1), 0);
				e.rniEval(e.rniParse("rm(" + temp + ")", 1), 0);
			}
			e.rniEval(e.rniParse("rm(" + resultTemp + ")", 1), 0);

		} else {

			String rclass = DirectJNI._s4BeansMappingRevert.get(obj.getClass().getName());
			// log.info("**rclass:" + rclass);
			if (rclass == null)
				log.info(DirectJNI._s4BeansMappingRevert);

			if (rclass != null) {

				long slotsId = e.rniEval(e.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
				String[] slots = e.rniGetStringArray(e.rniGetAttr(slotsId, "names"));

				Field[] fields = obj.getClass().getDeclaredFields();
				String[] temps = new String[fields.length];

				String constructorArgs = "";
				for (int i = 0; i < fields.length; ++i) {

					String getterName = fields[i].getClass().equals(Boolean.class) ? "is" + Utils.captalizeFirstChar(fields[i].getName()) : "get"
							+ Utils.captalizeFirstChar(fields[i].getName());
					Object fieldValue = obj.getClass().getMethod(getterName, (Class[]) null).invoke(obj, (Object[]) null);

					if (fieldValue instanceof RList && (((RList) fieldValue).getValue() == null || ((RList) fieldValue).getValue().length == 0)
							&& (((RList) fieldValue).getNames() == null || ((RList) fieldValue).getNames().length == 0)) {
						fieldValue = null;
					}

					if (fieldValue != null) {
						temps[i] = newTemporaryVariableName();
						e.rniAssign(temps[i], putObject((RObject) fieldValue), 0);
						if (!isNull(temps[i])) {
							if (!constructorArgs.equals("")) {
								constructorArgs += ",";
							}
							constructorArgs += slots[i] + "=" + temps[i];
						}
					} else {
						temps[i] = null;
					}
				}

				String var = newVariableName();
				if (constructorArgs.equals("")) {
					if (slots.length > 0) {
						e.rniEval(e.rniParse(var + "<-NULL", 1), 0);
					} else {
						e.rniEval(e.rniParse(var + "<-new(\"" + rclass + "\")", 1), 0);
					}
				} else {
					e.rniEval(e.rniParse(var + "<-new(\"" + rclass + "\", " + constructorArgs + " )", 1), 0);
				}

				resultId = e.rniEval(e.rniParse(var, 1), 0);
				e.rniEval(e.rniParse("rm(" + var + ")", 1), 0);

				for (int i = 0; i < temps.length; ++i) {
					if (temps[i] != null) {
						e.rniEval(e.rniParse("rm(" + temps[i] + ")", 1), 0);
					}
				}

			} else {
				try {
					Method getDataMethod = obj.getClass().getMethod("getData", (Class[]) null);
					if (getDataMethod != null) {
						resultId = putObject((RObject) getDataMethod.invoke(obj, (Object[]) null));
					}
				} catch (NoSuchMethodException ex) {
					throw new Exception("don't know how to deal with the object of type " + obj.getClass().getName());
				}
			}

		}

		if (((RObject) obj).getOutputMsg() != null)
			e.rniSetAttr(resultId, "comment", e.rniPutString(((RObject) obj).getOutputMsg()));
		return resultId;
	}

	private String expressionClass(String expression) {
		String cls = _rEngine.rniGetString(_rEngine.rniEval(_rEngine.rniParse("class(" + expression + ")", 1), 0));
		if (cls.equals("NULL"))
			throw new RuntimeException("NULL CLASS");
		return cls;
	}

	private boolean isS3Class(String expression) {
		boolean isObject = _rEngine.rniGetBoolArrayI(_rEngine.rniEval(_rEngine.rniParse("is.object(" + expression + ")", 1), 0))[0] == 1;
		if (!isObject)
			return false;
		boolean isClass = _rEngine.rniGetBoolArrayI(_rEngine.rniEval(_rEngine.rniParse("isClass(class(" + expression + "))", 1), 0))[0] == 1;
		return !isClass;
	}

	private boolean isNull(String expression) {
		boolean isNull = _rEngine.rniGetBoolArrayI(_rEngine.rniEval(_rEngine.rniParse("is.null(" + expression + ")", 1), 0))[0] == 1;
		return isNull;
	}

	// public for internal use only (DefaultAssignInterface Use)
	public RObject getObjectFrom(String expression) throws Exception {
		if (isNull(expression))
			return null;
		return getObjectFrom(expression, expressionClass(expression));

	}

	// public for internal use only (RListener)
	public void putObjectAndAssignName(RObject obj, String name, boolean privateEnv) throws Exception {
		// System.out.println("putObjectAndAssignName called, obj:" + obj);
		long resultId = putObject(obj);
		// System.out.println("Result id=" + resultId);
		_rEngine.rniAssign(name, resultId, (privateEnv ? _privateEnvExp : 0));
	}

	// public for internal use only (RListener)
	public void evaluate(String expression, int n) throws Exception {
		_rEngine.rniEval(_rEngine.rniParse(expression, n), 0);
	}

	private RObject getObjectFrom(String expression, String rclass) throws NoMappingAvailable, Exception {
		// log.info(".... quering for =" + expression + " rclass="+rclass);
		Rengine e = _rEngine;
		long expressionId = e.rniEval(e.rniParse(expression, 1), 0);
		RObject result = null;
		String typeStr = null;
		int rmode = e.rniExpType(expressionId);
		boolean isVirtual = e.rniGetBoolArrayI(e.rniEval(e.rniParse("isVirtualClass(\"" + rclass + "\")", 1), 0))[0] == 1;
		boolean isClass = e.rniGetBoolArrayI(e.rniEval(e.rniParse("isClass(\"" + rclass + "\")", 1), 0))[0] == 1;

		if (isClass && isVirtual) {

			String unionrclass = e.rniGetString(e.rniEval(e.rniParse("class(" + expression + ")", 1), 0));
			// log.info(">>> union r class=" + unionrclass );
			RObject o = getObjectFrom(expression, unionrclass);

			if (rmode != S4SXP) {
				if (DirectJNI._s4BeansMapping.get(unionrclass) != null) {

					o = (RObject) DirectJNI._mappingClassLoader.loadClass(DirectJNI._s4BeansMapping.get(unionrclass)).getConstructor(
							new Class[] { o.getClass() }).newInstance(new Object[] { o });
				} else {
				}
			}

			String factoryJavaClassName = DirectJNI._factoriesMapping.get(Utils.captalizeFirstChar(rclass) + "FactoryForR" + unionrclass);
			result = (RObject) DirectJNI._mappingClassLoader.loadClass(factoryJavaClassName).newInstance();
			Method setDataM = result.getClass().getMethod("setData", new Class[] { RObject.class });
			setDataM.invoke(result, o);

		} else {

			RVector vector = null;

			switch (rmode) {

			case NILSXP:
				typeStr = "nil = NULL";
				result = null;
				break;

			case SYMSXP:
				typeStr = "symbols";
				break;

			case LISTSXP:
				typeStr = "lists of dotted pairs";
				break;

			case CLOSXP:
				typeStr = "closures";
				break;

			case ENVSXP:
				typeStr = "environments";
				String[] vars = e.rniGetStringArray(e.rniEval(e.rniParse("ls(" + expression + ")", 1), 0));
				HashMap<String, RObject> data = new HashMap<String, RObject>();

				for (int i = 0; i < vars.length; ++i) {
					String varname = expression + "$" + vars[i];
					String varclass = expressionClass(varname);
					data.put(vars[i], (RObject) getObjectFrom(varname, varclass));
				}

				result = new REnvironment();
				((REnvironment) result).setData(data);

				break;

			case PROMSXP:
				typeStr = "promises: [un]evaluated closure";
				break;

			case LANGSXP:
				typeStr = "language constructs (special lists)";
				break;

			case SPECIALSXP:
				typeStr = "special forms";
				break;

			case BUILTINSXP:
				typeStr = "builtin non-special forms";
				break;

			case CHARSXP:
				typeStr = "'scalar' string type (internal only)";
				break;

			case LGLSXP: {

				typeStr = "logical vectors";

				int[] bAsInt = e.rniGetBoolArrayI(expressionId);
				boolean[] b = new boolean[bAsInt.length];
				for (int i = 0; i < bAsInt.length; ++i)
					b[i] = bAsInt[i] == 1;

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RLogical(b, isNaIdx.length == 0 ? null : isNaIdx, names);
				if (rclass.equals("logical") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					if (rclass.equals("matrix")) {
						result = new RMatrix();
					} else if (rclass.equals("array")) {
						result = new RArray();
					}
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")"));
				} else {
					result = vector;
				}

				long commentId = e.rniGetAttr(expressionId, "comment");
				if (commentId != 0 && e.rniExpType(commentId) == STRSXP) {
					result.setOutputMsg(e.rniGetStringArray(commentId)[0]);
				}

				break;
			}

			case INTSXP: {
				typeStr = "integer vectors";

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RInteger(e.rniGetIntArray(expressionId), isNaIdx.length == 0 ? null : isNaIdx, names);
				if (rclass.equals("integer") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")"));

				} else if (rclass.equals("factor")) {
					String[] levels = e.rniGetStringArray(e.rniGetAttr(expressionId, "levels"));
					result = new RFactor(levels, e.rniGetIntArray(expressionId));
				} else {
					result = vector;
				}

				long commentId = e.rniGetAttr(expressionId, "comment");
				if (commentId != 0 && e.rniExpType(commentId) == STRSXP) {
					result.setOutputMsg(e.rniGetStringArray(commentId)[0]);
				}

				break;
			}

			case REALSXP: {
				typeStr = "real variables";

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}
				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RNumeric(e.rniGetDoubleArray(expressionId), isNaIdx.length == 0 ? null : isNaIdx, names);

				if (rclass.equals("numeric") || rclass.equals("double") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")"));
				} else {
					result = vector;
				}

				long commentId = e.rniGetAttr(expressionId, "comment");
				if (commentId != 0 && e.rniExpType(commentId) == STRSXP) {
					result.setOutputMsg(e.rniGetStringArray(commentId)[0]);
				}

				break;
			}

			case CPLXSXP: {
				typeStr = "complex variables";
				double[] c_real = e.rniGetDoubleArray(e.rniEval(e.rniParse("Re(" + expression + ")", 1), 0));
				double[] c_imaginary = e.rniGetDoubleArray(e.rniEval(e.rniParse("Im(" + expression + ")", 1), 0));

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RComplex(c_real, c_imaginary, isNaIdx.length == 0 ? null : isNaIdx, names);

				if (rclass.equals("complex") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {
					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")"));
				} else {
					result = vector;
				}

				long commentId = e.rniGetAttr(expressionId, "comment");
				if (commentId != 0 && e.rniExpType(commentId) == STRSXP) {
					result.setOutputMsg(e.rniGetStringArray(commentId)[0]);
				}

				break;
			}

			case STRSXP: {
				typeStr = "string vectors";

				String[] names = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				int[] isNaIdx = e.rniGetIntArray(e.rniEval(e.rniParse("(0:(length(" + expression + ")-1))[is.na(" + expression + ")]", 1), 0));
				vector = new RChar(e.rniGetStringArray(expressionId), isNaIdx.length == 0 ? null : isNaIdx, names);
				if (rclass.equals("character") || rclass.equals("vector")) {
					result = vector;
				} else if (rclass.equals("matrix") || rclass.equals("array")) {

					result = rclass.equals("matrix") ? new RMatrix() : new RArray();
					((RArray) result).setDim(e.rniGetIntArray(e.rniGetAttr(expressionId, "dim")));
					((RArray) result).setValue(vector);
					((RArray) result).setDimnames((RList) getObjectFrom("dimnames(" + expression + ")"));

				} else {
					result = vector;
				}

				long commentId = e.rniGetAttr(expressionId, "comment");
				if (commentId != 0 && e.rniExpType(commentId) == STRSXP) {
					result.setOutputMsg(e.rniGetStringArray(commentId)[0]);
				}

				break;
			}

			case DOTSXP:
				typeStr = "dot-dot-dot object";
				break;

			case ANYSXP:
				typeStr = "make 'any' args work";
				break;

			case VECSXP: {
				typeStr = "generic vectors";

				String[] names = null;
				RObject[] objects = null;
				long namesId = e.rniGetAttr(expressionId, "names");
				if (namesId != 0 && e.rniExpType(namesId) == STRSXP) {
					names = e.rniGetStringArray(namesId);
				}

				long[] objectIds = e.rniGetVector(expressionId);
				int[] types = new int[objectIds.length];
				for (int i = 0; i < objectIds.length; ++i) {
					types[i] = e.rniExpType(objectIds[i]);
				}

				if (objectIds.length > 0) {
					objects = new RObject[objectIds.length];
					for (int i = 0; i < objects.length; ++i) {
						String varname = expression + "[[" + (i + 1) + "]]";
						if (!isNull(varname)) {
							objects[i] = getObjectFrom(varname, expressionClass(varname));
						}
					}
				}

				RList rlist = new RList(objects, names);

				if (rclass.equals("list")) {
					result = rlist;
				} else if (rclass.equals("data.frame")) {

					// String[] rowNames =
					// e.rniGetStringArray(e.rniGetAttr(expressionId,
					// "row.names"));
					String[] rowNames = e.rniGetStringArray(e.rniEval(e.rniParse("row.names(" + expression + ")", 1), 0));

					result = new RDataFrame(rlist, rowNames);
				} else {
					boolean isObject = e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.object(" + expression + ")", 1), 0))[0] == 1;
					if (isObject && !isClass)
						result = new RS3(rlist.getValue(), rlist.getNames(), e.rniGetStringArray(e.rniEval(e.rniParse("class(" + expression + ")", 1), 0)));
				}

				long commentId = e.rniGetAttr(expressionId, "comment");
				if (commentId != 0 && e.rniExpType(commentId) == STRSXP) {
					result.setOutputMsg(e.rniGetStringArray(commentId)[0]);
				}

				break;
			}

			case EXPRSXP:
				typeStr = "expressions vectors";
				break;

			case BCODESXP:
				typeStr = "byte code";
				break;

			case EXTPTRSXP:
				typeStr = "external pointer";
				break;

			case WEAKREFSXP:
				typeStr = "weak reference";
				break;

			case RAWSXP:
				typeStr = "raw bytes";
				break;

			case S4SXP:

				Class<?> s4Java_class = null;
				try {
					s4Java_class = DirectJNI._mappingClassLoader.loadClass(DirectJNI._s4BeansMapping.get(rclass));
				} catch (Exception ex) {
					throw new NoMappingAvailable("No mapping available for the object of class : " + rclass);
				}

				long slotsId = e.rniEval(e.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
				String[] slots = e.rniGetStringArray(e.rniGetAttr(slotsId, "names"));
				String[] slotsRClasses = e.rniGetStringArray(slotsId);
				Object[] params = new Object[slots.length];
				for (int i = 0; i < slots.length; ++i) {
					params[i] = getObjectFrom(expression + "@" + slots[i], slotsRClasses[i]);
				}

				Constructor<?> constr = null;
				for (int i = 0; i < s4Java_class.getConstructors().length; ++i) {
					if (s4Java_class.getConstructors()[i].getParameterTypes().length > 0) {
						constr = s4Java_class.getConstructors()[i];
						break;
					}
				}

				result = (RObject) constr.newInstance(params);
				typeStr = "S4 object";

				break;

			case FUNSXP:
				typeStr = "Closure or Builtin";
				break;

			default:
				throw new Exception("type of <" + expression + "> not recognized");
			}
		}

		if (false)
			log.info("TYPE STR FOR<" + expression + ">:" + typeStr + " result:" + result + " type hint was : " + rclass);

		return result;
	}

	public static boolean hasDistributedReferences(ReferenceInterface arg) {
		try {
			for (int i = 0; i < arg.getClass().getSuperclass().getDeclaredFields().length; ++i) {
				Field f = arg.getClass().getSuperclass().getDeclaredFields()[i];
				try {
					f.setAccessible(true);
					ReferenceInterface fobj = (ReferenceInterface) f.get(arg);
					if (fobj == null)
						continue;
					if (fobj.getRObjectId() != arg.getRObjectId() || !fobj.getAssignInterface().equals(arg.getAssignInterface())) {
						return true;
					}
					if (hasDistributedReferences(fobj))
						return true;
				} finally {
					f.setAccessible(false);
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(org.kchine.r.server.Utils.getStackTraceAsString(e));
		}
	}

	private RObject call(boolean resultAsReference, String varName, String methodName, Object... args) throws Exception {

		Rengine e = _rEngine;

		Vector<String> usedVars = new Vector<String>();
		String callStr = methodName + "(";
		for (Object arg : args) {
			if (arg != null && arg instanceof RNamedArgument) {
				callStr += ((RNamedArgument) arg).getName() + "=";
				arg = ((RNamedArgument) arg).getRobject();
			}
			if (arg != null) {
				String argvar = newTemporaryVariableName();
				usedVars.add(argvar);

				if (arg instanceof org.kchine.r.server.ReferenceInterface) {

					ReferenceInterface argRef = (ReferenceInterface) arg;

					if (argRef.getAssignInterface().equals(_ai)) {
						e.rniAssign(argvar, argRef.getRObjectId(), 0);
						callStr += argvar + argRef.getSlotsPath();

						setFields(argvar + argRef.getSlotsPath(), argRef);

					} else {
						e.rniAssign(argvar, putObject(argRef.extractRObject()), 0);
						callStr += argvar;
					}

				} else {
					e.rniAssign(argvar, putObject(arg), 0);
					callStr += argvar;
				}
			} else {
				callStr += "NULL";
			}

			callStr += ",";
		}

		if (callStr.endsWith(","))
			callStr = callStr.substring(0, callStr.length() - 1);
		callStr += ")";

		log.info("call str >>>" + callStr);

		RObject result = null;
		if (varName == null) {
			result = evalAndGetObject(callStr, resultAsReference);
		} else {
			e.rniEval(e.rniParse(varName + "<-" + callStr, 1), 0);
			result = null;
		}

		String rmString = "rm(";
		for (int i = 0; i < usedVars.size(); ++i) {
			rmString += usedVars.elementAt(i) + (i == usedVars.size() - 1 ? "" : ",");
		}
		rmString += ")";
		e.rniEval(e.rniParse(rmString, 1), 0);

		return result;
	}

	public void protectSafe(long expReference) {
		_rEngine.rniAssign(PROTECT_VAR_PREFIXE + expReference, expReference, _privateEnvExp);
		_protectedExpReference.add(expReference);
	}

	public void unprotectSafe(long expReference) {
		_rEngine.rniEval(_rEngine.rniParse("rm(" + PROTECT_VAR_PREFIXE + expReference + ", envir=" + PENV + ")", 1), 0);
		_protectedExpReference.remove(expReference);
	}

	public void unprotectAll() {
		if (_protectedExpReference.size() > 0) {
			Vector<Long> _protectedExpReferenceClone = (Vector<Long>) _protectedExpReference.clone();
			for (int i = 0; i < _protectedExpReferenceClone.size(); ++i)
				unprotectSafe(_protectedExpReferenceClone.elementAt(i));
		}
	}

	private void setFields(String refLabel, ReferenceInterface obj) throws Exception {
		if (obj instanceof StandardReference)
			return;

		String javaBaseObjectName = obj.getClass().getName();
		javaBaseObjectName = javaBaseObjectName.substring(0, javaBaseObjectName.length() - "Ref".length());
		String rclass = DirectJNI._s4BeansMappingRevert.get(javaBaseObjectName);
		long slotsId = _rEngine.rniEval(_rEngine.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
		String[] slots = _rEngine.rniGetStringArray(_rEngine.rniGetAttr(slotsId, "names"));

		Field[] fields = obj.getClass().getSuperclass().getDeclaredFields();
		for (int i = 0; i < fields.length; ++i) {
			try {
				fields[i].setAccessible(true);

				ReferenceInterface fieldValue = (ReferenceInterface) fields[i].get(obj);

				if (fieldValue != null) {
					String temp = newTemporaryVariableName();

					RObject concreteFieldValue = null;

					if (fieldValue.getAssignInterface().equals(_ai)) {

						concreteFieldValue = getObjectFromReference(fieldValue);

					} else {

						concreteFieldValue = fieldValue.extractRObject();

					}

					_rEngine.rniAssign(temp, putObject(concreteFieldValue), 0);
					_rEngine.rniEval(_rEngine.rniParse(refLabel + "@" + slots[i] + "<-" + temp, 1), 0);
					_rEngine.rniEval(_rEngine.rniParse("rm(" + temp + ")", 1), 0);
				}
			} finally {
				fields[i].setAccessible(false);
			}
		}
	}

	// public for internal use only
	public RObject getObjectFromReference(final ReferenceInterface refObj) throws Exception {
		// log.info( "-->getObjectFromReference :" + refObj);
		Rengine e = _rEngine;

		if (refObj.getAssignInterface().equals(_ai)) {
			String rootvar = newTemporaryVariableName();
			e.rniAssign(rootvar, refObj.getRObjectId(), 0);
			String refLabel = rootvar + refObj.getSlotsPath();
			setFields(refLabel, refObj);
			RObject result = getObjectFrom(refLabel);
			e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);
			return result;
		} else {
			return refObj.extractRObject();
		}

	};

	private ReferenceInterface putObjectAndGetReference(final Object obj) throws Exception {
		long resultId = putObject(obj);
		protectSafe(resultId);
		Class<?> javaClass = DirectJNI._mappingClassLoader.loadClass(obj.getClass().getName() + "Ref");
		ReferenceInterface result = (ReferenceInterface) javaClass.getConstructor(new Class[] { long.class, String.class }).newInstance(
				new Object[] { resultId, "" });
		result.setAssignInterface(_ai);
		return result;
	}

	public String guessJavaClassRef(String rclass, int rtype, boolean isS3) {
		String javaClassName = null;

		if (DirectJNI._s4BeansMapping.get(rclass) != null) {
			javaClassName = DirectJNI._s4BeansMapping.get(rclass) + "Ref";
		} else {

			switch (rtype) {
			case ENVSXP:
				javaClassName = REnvironmentRef.class.getName();
				break;

			case LGLSXP:
				if (rclass.equals("logical") || rclass.equals("vector"))
					javaClassName = RLogicalRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RLogicalRef.class.getName();/* TODO */
				}
				break;

			case INTSXP:

				if (rclass.equals("integer") || rclass.equals("vector"))
					javaClassName = RIntegerRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else if (rclass.equals("factor"))
					javaClassName = RFactorRef.class.getName();
				else {
					javaClassName = RIntegerRef.class.getName();/* TODO */
				}
				break;

			case REALSXP:

				if (rclass.equals("numeric") || rclass.equals("double") || rclass.equals("vector"))
					javaClassName = RNumericRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RNumericRef.class.getName();/* TODO */
				}
				break;

			case CPLXSXP:

				if (rclass.equals("complex") || rclass.equals("vector"))
					javaClassName = RComplexRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RComplexRef.class.getName();/* TODO */
				}
				break;

			case STRSXP:

				if (rclass.equals("character") || rclass.equals("vector"))
					javaClassName = RCharRef.class.getName();
				else if (rclass.equals("matrix"))
					javaClassName = RMatrixRef.class.getName();
				else if (rclass.equals("array"))
					javaClassName = RArrayRef.class.getName();
				else {
					javaClassName = RCharRef.class.getName(); /* TODO */
				}
				break;

			case VECSXP:

				if (rclass.equals("list"))
					javaClassName = RListRef.class.getName();
				else if (rclass.equals("data.frame"))
					javaClassName = RDataFrameRef.class.getName();
				else if (isS3) {
					javaClassName = RS3Ref.class.getName();
				} else {

					javaClassName = RListRef.class.getName();/* TODO */
				}

				break;

			}

		}

		return javaClassName;
	}

	private RObject evalAndGetObject(String expression, boolean resultAsReference) throws Exception {

		log.info("+ ------evalAndGetObject from :" + expression);

		try {
			Rengine e = _rEngine;

			long resultId = e.rniEval(e.rniParse(expression, 1), 0);
			int resultType = e.rniExpType(resultId);
			if (resultType == NILSXP)
				return null;

			String resultvar = newTemporaryVariableName();
			e.rniAssign(resultvar, resultId, 0);

			RObject result = null;
			if (!resultAsReference) {
				result = getObjectFrom(resultvar);
			} else {
				String rclass = expressionClass(resultvar);
				String javaClassName = guessJavaClassRef(rclass, resultType, isS3Class(expression));
				Class<?> javaClass = DirectJNI._mappingClassLoader.loadClass(javaClassName);
				protectSafe(resultId);
				result = (RObject) javaClass.getConstructor(new Class[] { long.class, String.class }).newInstance(new Object[] { resultId, "" });
				((ReferenceInterface) result).setAssignInterface(_ai);
			}
			e.rniEval(e.rniParse("rm(" + resultvar + ")", 1), 0);
			return result;

		} catch (NoMappingAvailable re) {
			log.info("****" + Utils.getStackTraceAsString(re));
			throw re;
		} catch (Throwable ex) {
			log.info("#####" + Utils.getStackTraceAsString(ex));
			return null;
		}

	}

	private boolean isExportable(String symbol) {
		if (symbol.equals(ECHO_VAR_NAME) || symbol.equals(".required"))
			return false;
		Rengine e = _rEngine;
		long symbolId = e.rniEval(e.rniParse(symbol, 1), 0);
		if (symbolId <= 0)
			return false;
		int symbolType = e.rniExpType(symbolId);
		if (symbolType == NILSXP)
			return false;
		String rclass = expressionClass(symbol);
		return guessJavaClassRef(rclass, symbolType, isS3Class(symbol)) != null;
	}

	private String[] listExportableSymbols() {
		Rengine e = _rEngine;
		long resultId = e.rniEval(e.rniParse("ls()", 1), 0);
		String[] slist = _rEngine.rniGetStringArray(resultId);
		Vector<String> result = new Vector<String>();
		for (int i = 0; i < slist.length; ++i) {
			if (isExportable(slist[i]) && !slist[i].equals(PENV))
				result.add(slist[i]);
		}
		return (String[]) result.toArray(new String[0]);
	}

	// public for internal use only
	public String sourceFromResource(String resource) {
		try {
			return sourceFromResource_(resource, _resourcesClassLoader);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private String sourceFromResource_(String resource, ClassLoader refClassLoader) {

		if (resource.startsWith("/"))
			resource = resource.substring(1);

		try {
			final File tempFile = new File(TEMP_DIR + "/" + "T" + System.currentTimeMillis() + "_" + resource.substring(resource.lastIndexOf('/') + 1))
					.getCanonicalFile();
			if (tempFile.exists())
				tempFile.delete();

			BufferedReader breader = new BufferedReader(new InputStreamReader(refClassLoader.getResourceAsStream(resource)));
			PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));
			String line;
			do {
				line = breader.readLine();
				if (line != null) {
					pwriter.println(line);
				}
			} while (line != null);
			pwriter.close();

			toggleMarker();
			_rEngine.rniEval(_rEngine.rniParse("source(\"" + tempFile.getAbsolutePath().replace('\\', '/') + "\")", 1), 0);
			String lastStatus = cutStatusSinceMarker();
			log.info(resource + " loading status : " + lastStatus);
			tempFile.delete();
			return lastStatus;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String sourceFromBuffer(String buffer) {
		try {
			File tempFile = PoolUtils.createFileFromBuffer(null, buffer);
			toggleMarker();
			_rEngine.rniEval(_rEngine.rniParse("source(\"" + tempFile.getAbsolutePath().replace('\\', '/') + "\")", 1), 0);

			String lastStatus = cutStatusSinceMarker();

			tempFile.delete();
			return lastStatus;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	void shutdownDevices(String deviceType) throws RemoteException {
		RInteger devices = (RInteger) getRServices().getObject(".PrivateEnv$dev.list()");
		if (devices != null) {
			for (int i = 0; i < devices.getValue().length; ++i) {
				if (devices.getNames()[i].equals(deviceType)) {
					getRServices().getObject(".PrivateEnv$dev.off(" + devices.getValue()[i] + ")");
				}
			}
		}
	}

	public String newTemporaryVariableName() {
		return V_TEMP_PREFIXE + _tempCounter++;
	}

	String newVariableName() {
		return V_NAME_PREFIXE + _varCounter++;
	}

	RNI _rni = new LocalRNI(this);

	private AssignInterface _defaultAssignInterface = new DefaultAssignInterfaceImpl();

	AssignInterface _ai = _defaultAssignInterface;

	public AssignInterface getAssignInterface() {
		return _ai;
	}

	public void setAssignInterface(AssignInterface ai) {
		_ai = ai;
	}

	public AssignInterface getDefaultAssignInterface() {
		return _defaultAssignInterface;
	}

	public RServices getRServices() {
		return _rServices;
	}
	
	public ScilabServices getScilabServices() {
		return (ScilabServices)_rServices;
	}
	
	public OpenOfficeServices getOpenOfficeServices() {
		return (OpenOfficeServices)_rServices;
	}

	public RNI getRNI() {
		return _rni;
	}

	public static void generateMaps(URL jarUrl) {
		DirectJNI.generateMaps(jarUrl, false);
	}

	public static Object getJavaArrayFromRArray__(RArray array) {
		int[] dim = array.getDim();

		RVector vector = array.getValue();
		Class<?> componentType = null;
		if (vector instanceof RInteger)
			componentType = int.class;
		else if (vector instanceof RNumeric)
			componentType = double.class;
		else if (vector instanceof RChar)
			componentType = String.class;
		else if (vector instanceof RLogical)
			componentType = boolean.class;

		Object result = null;
		try {
			result = Array.newInstance(componentType, dim);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Vector<Integer> v = new Vector<Integer>();
		int p = 1;
		for (int i = dim.length - 1; i > 0; --i) {
			p = p * dim[i];
			v.add(0, p);
		}

		for (int bi = 0; bi < p * dim[0]; ++bi) {
			int bindex = bi;
			int[] indexes = new int[dim.length];
			for (int i = 0; i < indexes.length - 1; ++i) {
				indexes[i] = bindex / v.elementAt(i);
				bindex = bindex % v.elementAt(i);
			}
			indexes[indexes.length - 1] = bindex;

			Object arrayTail = null;
			if (dim.length == 1) {
				arrayTail = result;
			} else {
				arrayTail = Array.get(result, indexes[0]);
				for (int i = 1; i < indexes.length - 1; ++i)
					arrayTail = Array.get(arrayTail, indexes[i]);
			}
			if (vector instanceof RInteger)
				Array.setInt(arrayTail, indexes[indexes.length - 1], ((RInteger) vector).getValue()[bi]);
			else if (vector instanceof RNumeric)
				Array.setDouble(arrayTail, indexes[indexes.length - 1], ((RNumeric) vector).getValue()[bi]);
			else if (vector instanceof RChar)
				Array.set(arrayTail, indexes[indexes.length - 1], ((RChar) vector).getValue()[bi]);
			else if (vector instanceof RLogical)
				Array.setBoolean(arrayTail, indexes[indexes.length - 1], ((RLogical) vector).getValue()[bi]);
		}

		return result;
	}

	public static Object getJavaArrayFromRArray(RArray array) {
		int[] dim = array.getDim();

		RVector vector = array.getValue();
		Class<?> componentType = null;
		if (vector instanceof RInteger)
			componentType = int.class;
		else if (vector instanceof RNumeric)
			componentType = double.class;
		else if (vector instanceof RChar)
			componentType = String.class;
		else if (vector instanceof RLogical)
			componentType = boolean.class;

		Object result = null;
		try {
			result = Array.newInstance(componentType, dim);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Vector<Integer> v1 = new Vector<Integer>();
		int p1 = 1;
		for (int i = dim.length - 1; i > 0; --i) {
			p1 = p1 * dim[i];
			v1.add(0, p1);
		}
		Vector<Integer> v2 = new Vector<Integer>();
		int p2 = 1;
		for (int i = 0; i < dim.length - 1; ++i) {
			p2 = p2 * dim[i];
			v2.add(0, p2);
		}

		for (int bi = 0; bi < p1 * dim[0]; ++bi) {
			int bindex = bi;
			int[] indexes = new int[dim.length];
			for (int i = 0; i < indexes.length - 1; ++i) {
				indexes[i] = bindex / v1.elementAt(i);
				bindex = bindex % v1.elementAt(i);
			}
			indexes[indexes.length - 1] = bindex;

			Object arrayTail = null;
			if (dim.length == 1) {
				arrayTail = result;
			} else {
				arrayTail = Array.get(result, indexes[0]);
				for (int i = 1; i < indexes.length - 1; ++i)
					arrayTail = Array.get(arrayTail, indexes[i]);
			}

			int linearVectorIndex = 0;
			for (int i = (indexes.length - 1); i > 0; --i)
				linearVectorIndex += indexes[i] * v2.elementAt((indexes.length - 1) - i);
			linearVectorIndex += indexes[0];
			// System.out.println("linearVectorIndex:"+linearVectorIndex);

			if (vector instanceof RInteger)
				Array.setInt(arrayTail, indexes[indexes.length - 1], ((RInteger) vector).getValue()[linearVectorIndex]);
			else if (vector instanceof RNumeric)
				Array.setDouble(arrayTail, indexes[indexes.length - 1], ((RNumeric) vector).getValue()[linearVectorIndex]);
			else if (vector instanceof RChar)
				Array.set(arrayTail, indexes[indexes.length - 1], ((RChar) vector).getValue()[linearVectorIndex]);
			else if (vector instanceof RLogical)
				Array.setBoolean(arrayTail, indexes[indexes.length - 1], ((RLogical) vector).getValue()[linearVectorIndex]);
		}

		return result;
	}

	public static int[] getJavaArrayDimensions(Object table, Class<?>[] classHolder, int[] lengthHolder) {
		Vector<Integer> dimV = new Vector<Integer>();
		Object obj = table;
		while (Array.get(obj, 0).getClass().isArray()) {
			dimV.add(Array.getLength(obj));
			obj = Array.get(obj, 0);
		}
		dimV.add(Array.getLength(obj));
		classHolder[0] = Array.get(obj, 0).getClass();

		int[] result = new int[dimV.size()];
		lengthHolder[0] = 1;
		for (int i = 0; i < dimV.size(); ++i) {
			result[i] = dimV.elementAt(i);
			lengthHolder[0] = lengthHolder[0] * result[i];
		}
		return result;
	}

	public static RArray getRArrayFromJavaArray(Object javaArray) {
		Class<?>[] classHolder = new Class<?>[1];
		int[] lengthHolder = new int[1];

		int[] dim = getJavaArrayDimensions(javaArray, classHolder, lengthHolder);
		RVector vector = null;

		Class<?> componentType = classHolder[0];
		if (componentType == Integer.class || componentType == int.class)
			vector = new RInteger(new int[lengthHolder[0]]);
		else if (componentType == Double.class || componentType == double.class)
			vector = new RNumeric(new double[lengthHolder[0]]);
		else if (componentType == Boolean.class || componentType == boolean.class)
			vector = new RLogical(new boolean[lengthHolder[0]]);
		else if (componentType == String.class)
			vector = new RChar(new String[lengthHolder[0]]);
		else
			throw new RuntimeException("unsupported elements class type :" + componentType);

		Vector<Integer> v1 = new Vector<Integer>();
		int p1 = 1;
		for (int i = dim.length - 1; i > 0; --i) {
			p1 = p1 * dim[i];
			v1.add(0, p1);
		}
		Vector<Integer> v2 = new Vector<Integer>();
		int p2 = 1;
		for (int i = 0; i < dim.length - 1; ++i) {
			p2 = p2 * dim[i];
			v2.add(0, p2);
		}

		for (int bi = 0; bi < p1 * dim[0]; ++bi) {
			int bindex = bi;
			int[] indexes = new int[dim.length];
			for (int i = 0; i < indexes.length - 1; ++i) {
				indexes[i] = bindex / v1.elementAt(i);
				bindex = bindex % v1.elementAt(i);
			}
			indexes[indexes.length - 1] = bindex;

			Object arrayTail = null;
			if (dim.length == 1) {
				arrayTail = javaArray;
			} else {
				arrayTail = Array.get(javaArray, indexes[0]);
				for (int i = 1; i < indexes.length - 1; ++i)
					arrayTail = Array.get(arrayTail, indexes[i]);
			}

			int linearVectorIndex = 0;
			for (int i = (indexes.length - 1); i > 0; --i)
				linearVectorIndex += indexes[i] * v2.elementAt((indexes.length - 1) - i);
			linearVectorIndex += indexes[0];
			// System.out.println("linearVectorIndex:"+linearVectorIndex);

			if (vector instanceof RInteger)
				((RInteger) vector).getValue()[linearVectorIndex] = (Integer) Array.get(arrayTail, indexes[indexes.length - 1]);
			else if (vector instanceof RNumeric)
				((RNumeric) vector).getValue()[linearVectorIndex] = (Double) Array.get(arrayTail, indexes[indexes.length - 1]);
			else if (vector instanceof RChar)
				((RChar) vector).getValue()[linearVectorIndex] = (String) Array.get(arrayTail, indexes[indexes.length - 1]);
			else if (vector instanceof RLogical)
				((RLogical) vector).getValue()[linearVectorIndex] = (Boolean) Array.get(arrayTail, indexes[indexes.length - 1]);
		}

		return new RArray(vector, dim, null);
	}

	private Object convert(RObject obj) {
		// System.out.println("obj:" + obj);
		Object result = obj;
		if (result instanceof RInteger) {
			if (((RInteger) result).getValue().length == 1) {
				result = ((RInteger) result).getValue()[0];
			} else {
				result = ((RInteger) result).getValue();
			}
		} else if (result instanceof RNumeric) {
			if (((RNumeric) result).getValue().length == 1) {
				result = ((RNumeric) result).getValue()[0];
			} else {
				result = ((RNumeric) result).getValue();
			}
		} else if (result instanceof RChar) {
			if (((RChar) result).getValue().length == 1) {
				result = ((RChar) result).getValue()[0];
			} else {
				result = ((RChar) result).getValue();
			}
		} else if (result instanceof RLogical) {
			if (((RLogical) result).getValue().length == 1) {
				result = ((RLogical) result).getValue()[0];
			} else {
				result = ((RLogical) result).getValue();
			}
		} else if (result instanceof RArray) {
			result = getJavaArrayFromRArray((RArray) result);
		}
		return result;
	}

	private String[] probedVariables = new String[0];

	private long[] getVariablePointersBefore() {
		if (probedVariables.length > 0) {
			try {
				final long[][] finalVariablePointers = new long[1][probedVariables.length];
				runR(new org.kchine.r.server.ExecutionUnit() {
					public void run(Rengine e) {
						for (int i = 0; i < probedVariables.length; ++i) {
							int[] exists = e.rniGetBoolArrayI(e.rniEval(e.rniParse("exists('" + probedVariables[i] + "')", 1), 0));
							if (exists[0] == 1) {
								finalVariablePointers[0][i] = e.rniEval(e.rniParse(probedVariables[i], 1), 0);
							}
						}
					}
				});
				return finalVariablePointers[0];
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	private void fireVariableChangedEvents(long[] variablePointersBefore, HashMap<String, Object> clientProperties) {
		if (probedVariables.length > 0) {
			try {
				final long[][] variablePointersAfter = new long[1][probedVariables.length];
				runR(new org.kchine.r.server.ExecutionUnit() {
					public void run(Rengine e) {
						for (int i = 0; i < probedVariables.length; ++i) {
							int[] exists = e.rniGetBoolArrayI(e.rniEval(e.rniParse("exists('" + probedVariables[i] + "')", 1), 0));
							if (exists[0] == 1) {
								variablePointersAfter[0][i] = e.rniEval(e.rniParse(probedVariables[i], 1), 0);
							}
						}
					}
				});

				HashSet<String> changedVariablesHashSet = new HashSet<String>();
				for (int i = 0; i < probedVariables.length; ++i)
					if (variablePointersBefore[i] != variablePointersAfter[0][i])
						changedVariablesHashSet.add(probedVariables[i]);
				if (changedVariablesHashSet.size() > 0) {
					HashMap<String, Object> attrs = new HashMap<String, Object>();
					attrs.put("variables", changedVariablesHashSet);
					notifyRActionListeners(new RConsoleAction("VARIABLES_CHANGE", attrs, clientProperties));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void fireVariableChangedEvents(long[] variablePointersBefore) {
		fireVariableChangedEvents(variablePointersBefore, null);
	}

	private RServices _rServices = new RServicesImpl(); 
		
	private class RServicesImpl implements RServices, ScilabServices , OpenOfficeServices{

		private String _lastStatus = null;

		public String getStatus() {
			return clean(_lastStatus);
		}

		public String evaluate(final String expression, final int n) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						e.rniEval(e.rniParse(expression, n), 0);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return clean(_lastStatus);
		}

		public String sourceFromResource(final String resource) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();
			final Exception[] exceptionHolder = new Exception[1];
			runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						_lastStatus = DirectJNI.this.sourceFromResource(resource);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return clean(_lastStatus);
		}

		public String sourceFromBuffer(final String buffer) throws RemoteException {
			return sourceFromBuffer(buffer, null);
		}

		public String sourceFromBuffer(final String buffer, HashMap<String, Object> clientProperties) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];
			runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						_lastStatus = DirectJNI.this.sourceFromBuffer(buffer);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			}, clientProperties);

			fireVariableChangedEvents(variablePointersBefore, clientProperties);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return clean(_lastStatus);
		}

		public String print(String expression) throws RemoteException {
			return printExpressions(new String[] { expression });
		}

		public String printExpressions(final String[] expressions) throws RemoteException {

			final String[] stringHolder = new String[1];
			stringHolder[0] = "";
			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {

					try {

						String[] asString = new String[expressions.length];

						for (int i = 0; i < expressions.length; ++i) {
							if ((expressions[i].startsWith("'") && expressions[i].endsWith("'"))
									|| (expressions[i].startsWith("\"") && expressions[i].endsWith("\""))) {
								asString[i] = expressions[i].substring(1, expressions[i].length() - 1);
							} else {

								if (e.rniGetBoolArrayI(e.rniEval(e.rniParse("is.atomic(" + expressions[i] + ")", 1), 0))[0] == 1) {
									asString[i] = e.rniGetString(e.rniEval(e.rniParse("toString(" + expressions[i] + ")", 1), 0));
								} else {
									toggleMarker();
									e.rniEval(e.rniParse("print(" + expressions[i] + ")", 1), 0);
									asString[i] = cutStatusSinceMarker();
								}
							}
						}

						for (int i = 0; i < asString.length; ++i)
							stringHolder[0] += asString[i];

						// broadcast(e);

					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}

				}
			});

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return clean(stringHolder[0]);

		}

		public String evaluate(String expression) throws RemoteException {
			return evaluate(expression, 1);
		};

		public RObject call(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(false, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public void callAndAssign(final String varName, final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						DirectJNI.this.call(false, varName, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

		}

		public RObject callAndGetReference(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(true, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public RObject callAndGetObjectName(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(true, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			try {
				String refClassName = objHolder[0].getClass().getName();
				ObjectNameInterface objectName = (ObjectNameInterface) _mappingClassLoader.loadClass(
						refClassName.substring(0, refClassName.length() - "Ref".length()) + "ObjectName").newInstance();
				objectName.setRObjectName(PROTECT_VAR_PREFIXE + ((ReferenceInterface) objHolder[0]).getRObjectId());
				objectName.setRObjectEnvironment(PENV);
				return (RObject) objectName;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}
		}

		public Object callAndConvert(final String methodName, final Object... args) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(false, null, methodName, args);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			return DirectJNI.this.convert(objHolder[0]);
		}

		public void freeReference(final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an object reference");
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						unprotectSafe(((ReferenceInterface) refObj).getRObjectId());
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
		}

		public void freeAllReferences() throws RemoteException {
			DirectJNI.this.unprotectAll();
		}

		public RObject getObjectName(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, true);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			try {
				String refClassName = objHolder[0].getClass().getName();
				ObjectNameInterface objectName = (ObjectNameInterface) _mappingClassLoader.loadClass(
						refClassName.substring(0, refClassName.length() - "Ref".length()) + "ObjectName").newInstance();
				objectName.setRObjectName(PROTECT_VAR_PREFIXE + ((ReferenceInterface) objHolder[0]).getRObjectId());
				objectName.setRObjectEnvironment(PENV);
				return (RObject) objectName;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}

		}

		public Object realizeObjectName(final RObject objectName, boolean convert) throws RemoteException {
			if (!(objectName instanceof ObjectNameInterface))
				throw new RemoteException("not an object name");
			final RObject[] robjHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						robjHolder[0] = DirectJNI.this.getObjectFrom(((ObjectNameInterface) objectName).getRObjectEnvironment() + "$"
								+ ((ObjectNameInterface) objectName).getRObjectName());
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			if (convert) {
				return DirectJNI.this.convert(robjHolder[0]);
			} else {
				return robjHolder[0];
			}
		}

		public RObject realizeObjectName(final RObject objectName) throws RemoteException {
			return (RObject) realizeObjectName(objectName, false);
		}

		public Object realizeObjectNameConverted(RObject objectName) throws RemoteException {
			return realizeObjectName(objectName, true);
		}

		public RObject referenceToObject(final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an object reference");
			final RObject[] robjHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						robjHolder[0] = DirectJNI.this.getObjectFromReference((ReferenceInterface) refObj);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return robjHolder[0];
		};

		public boolean isReference(RObject obj) {
			return obj instanceof ReferenceInterface;
		}

		public RObject putAndGetReference(final Object obj) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final RObject[] refHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						refHolder[0] = (RObject) DirectJNI.this.putObjectAndGetReference(obj);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return refHolder[0];
		}

		public void putAndAssign(final Object obj, final String name) throws RemoteException {

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						long resultId = putObject(obj);
						e.rniAssign(name, resultId, 0);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

		}

		public RObject getObject(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, false);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);

				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}

			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public RObject getReference(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, true);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public Object getObjectConverted(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, false);
						// broadcast(e);
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);

				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}

			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			return DirectJNI.this.convert(objHolder[0]);
		}

		public Object convert(RObject obj) throws RemoteException {
			return DirectJNI.this.convert(obj);
		}

		public void assignReference(final String name, final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an an object reference");

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						String rootvar = newTemporaryVariableName();
						e.rniAssign(rootvar, ((ReferenceInterface) refObj).getRObjectId(), 0);
						e.rniEval(e.rniParse(name + "<-" + rootvar + ((ReferenceInterface) refObj).getSlotsPath(), 1), 0);
						e.rniEval(e.rniParse("rm(" + rootvar + ")", 1), 0);

					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});

			fireVariableChangedEvents(variablePointersBefore);

			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
		}

		public boolean symbolExists(final String symbol) throws RemoteException {
			final Exception[] exceptionHolder = new Exception[1];
			final Boolean[] objHolder = new Boolean[1];

			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = ((RLogical) DirectJNI.this.evalAndGetObject("exists('" + symbol + "')", false)).getValue()[0];
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}

			return objHolder[0];
		}

		public void addRCallback(RCallBack callback) throws RemoteException {
			DirectJNI.this.addRCallback(callback);
		}

		public void removeRCallback(RCallBack callback) throws RemoteException {
			DirectJNI.this.removeRCallback(callback);
		}

		public void removeAllRCallbacks() throws RemoteException {
			DirectJNI.this.removeAllRCallbacks();
		}

		public void addRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
			System.out.println("addRCollaborationListener");
			_rCollaborationListeners.add(collaborationListener);
		}

		public void removeRCollaborationListener(RCollaborationListener collaborationListener) throws RemoteException {
			_rCollaborationListeners.remove(collaborationListener);
		}

		public void removeAllRCollaborationListeners() throws RemoteException {
			_rCollaborationListeners.removeAllElements();
		}

		public boolean hasRCollaborationListeners() throws RemoteException {
			return _rCollaborationListeners.size() > 0;
		}

		public void addRConsoleActionListener(RConsoleActionListener helpListener) throws RemoteException {
			DirectJNI.this.addRActionListener(helpListener);
		}

		public void removeRConsoleActionListener(RConsoleActionListener helpListener) throws RemoteException {
			DirectJNI.this.removeRActionListener(helpListener);
		}

		public void removeAllRConsoleActionListeners() throws RemoteException {
			DirectJNI.this.removeAllRActionListeners();
		}

		public void registerUser(String sourceUID, String user) throws RemoteException {
			_usersHash.put(sourceUID, new UserStatus(sourceUID, user, false));
			RConsoleAction action = new RConsoleAction("USER_JOINED", new HashMap<String, Object>());
			action.getAttributes().put("user", user);
			action.getAttributes().put("sourceUID", sourceUID);
			notifyRActionListeners(action);
		}

		public void unregisterUser(String sourceUID) throws RemoteException {
			String user = _usersHash.get(sourceUID).getUserName();
			_usersHash.remove(sourceUID);
			RConsoleAction action = new RConsoleAction("USER_LEFT", new HashMap<String, Object>());
			action.getAttributes().put("user", user);
			action.getAttributes().put("sourceUID", sourceUID);
			notifyRActionListeners(action);
		}

		public void updateUserStatus(String sourceUID, UserStatus userStatus) throws RemoteException {
			_usersHash.put(sourceUID, userStatus);
			RConsoleAction action = new RConsoleAction("USER_UPDATED", new HashMap<String, Object>());
			action.getAttributes().put("user", userStatus.getUserName());
			action.getAttributes().put("status", userStatus);
			action.getAttributes().put("sourceUID", sourceUID);
			notifyRActionListeners(action);
		}

		public UserStatus[] getUserStatusTable() throws RemoteException {
			UserStatus[] result = new UserStatus[_usersHash.values().size()];
			int i = 0;
			for (UserStatus us : _usersHash.values())
				result[i++] = us;
			return result;
		}

		public void setUserInput(String userInput) throws RemoteException {
			_userInput = userInput;
		}

		public void setOrginatorUID(String uid) throws RemoteException {
			DirectJNI.this.setOrginatorUID(uid);
		}

		public String getOriginatorUID() throws RemoteException {
			return DirectJNI.this.getOriginatorUID();
		}

		public void chat(String sourceUID, String user, String message) throws RemoteException {
			Vector<RCollaborationListener> removeList = new Vector<RCollaborationListener>();
			for (int i = 0; i < _rCollaborationListeners.size(); ++i) {
				try {
					_rCollaborationListeners.elementAt(i).chat(sourceUID, user, message);
				} catch (Exception e) {
					removeList.add(_rCollaborationListeners.elementAt(i));
				}
			}
			_rCollaborationListeners.removeAll(removeList);
		}

		public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
			Vector<RCollaborationListener> removeList = new Vector<RCollaborationListener>();
			for (int i = 0; i < _rCollaborationListeners.size(); ++i) {
				try {
					_rCollaborationListeners.elementAt(i).consolePrint(sourceUID, user, expression, result);
				} catch (Exception e) {
					removeList.add(_rCollaborationListeners.elementAt(i));
				}
			}
			_rCollaborationListeners.removeAll(removeList);
		}

		public GenericCallbackDevice newGenericCallbackDevice() throws RemoteException {
			return null;
		}

		public GenericCallbackDevice[] listGenericCallbackDevices() throws RemoteException {
			return null;
		}

		public String[] listPackages() throws RemoteException {
			return ((String[]) DirectJNI._packageNames.toArray(new String[0]));
		}

		public RPackage getPackage(String packageName) throws RemoteException {
			if (!DirectJNI._packageNames.contains(packageName))
				throw new RemoteException("Bad Package Name");
			if (_packs.get(packageName) == null) {
				try {
					for (Iterator<?> iter = DirectJNI._rPackageInterfacesHash.keySet().iterator(); iter.hasNext();) {
						String className = (String) iter.next();
						if (className.substring(className.lastIndexOf('.') + 1).equals(packageName)) {
							_packs.put(packageName, (RPackage) DirectJNI._mappingClassLoader.loadClass(className + "Impl").getMethod("getInstance",
									(Class[]) null).invoke((Object) null, (Object[]) null));
							break;
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			return _packs.get(packageName);
		}

		public String unsafeGetObjectAsString(String cmd) throws RemoteException {
			if (cmd.trim().equals(""))
				return "";
			try {
				Object result = DirectJNI.this.evalAndGetObject(cmd, false);
				if (result == null)
					return "";
				else {
					return result.toString();
				}
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public void stop() throws RemoteException {
			if (PoolUtils.isWindowsOs()) {
				_rEngine.rniStop(0);
			} else {
				_rEngine.rniEval(_rEngine.rniParse("stop('stop required')", 1), 0);
			}
			_stopRequired = true;
		}

		public RNI getRNI() throws RemoteException {
			return _rni;
		}

		public void die() throws RemoteException {

		}

		public String getLogs() throws RemoteException {

			return null;
		}

		public String getServantName() throws RemoteException {

			return null;
		}

		public String getJobId() throws RemoteException {
			return null;
		}

		public void setJobId(String jobId) throws RemoteException {
		}

		public String ping() throws RemoteException {

			return null;
		}

		public void reset() throws RemoteException {

		}

		public void addOutListener(RemoteLogListener listener) throws RemoteException {

		}

		public void addErrListener(RemoteLogListener listener) throws RemoteException {

		}

		public void removeOutListener(RemoteLogListener listener) throws RemoteException {

		}

		public void removeErrListener(RemoteLogListener listener) throws RemoteException {

		}

		public void removeAllOutListeners() throws RemoteException {

		}

		public void removeAllErrListeners() throws RemoteException {

		}

		public void logInfo(String message) throws RemoteException {

		}

		public boolean hasConsoleMode() throws RemoteException {
			return true;
		}

		public String consoleSubmit(final String cmd) throws RemoteException {
			return consoleSubmit(cmd, null);
		}

		public String consoleSubmit(final String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
			log.info("submit : " + cmd);

			long[] variablePointersBefore = getVariablePointersBefore();

			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
				}

				public String getConsoleInput() {
					if (cmd.startsWith("?")) {
						return "help(" + cmd.substring(1) + ")";
					} else {
						return cmd;
					}
				}
			}, clientProperties);

			fireVariableChangedEvents(variablePointersBefore, clientProperties);

			if (exceptionHolder[0] != null) {
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			}

			/*
			 * if (_localBroadcastedDevices.size() > 1) { runR(new
			 * server.ExecutionUnit() { public void run(Rengine e) { try {
			 * broadcast(e); } catch (Exception ex) { ex.printStackTrace(); } }
			 * }); }
			 */

			// return clean(_lastStatus);
			return "";

		}

		public boolean isBusy() throws RemoteException {
			return _mainLock.isLocked();
		}

		public boolean isResetEnabled() throws RemoteException {
			return false;
		}

		public void setResetEnabled(boolean enable) throws RemoteException {
		}

		public boolean hasPushPopMode() throws RemoteException {
			return true;
		}

		public void push(String symbol, Serializable object) throws RemoteException {
			DirectJNI.getInstance().getRServices().putAndAssign((RObject) object, symbol);
		}

		public Serializable pop(String symbol) throws RemoteException {
			Serializable result = DirectJNI.getInstance().getRServices().getObject(symbol);
			return result;
		}

		public String[] listSymbols() throws RemoteException {

			final String[][] objHolder = new String[1][];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.listExportableSymbols();
					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(_lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!_lastStatus.equals("")) {
				log.info(_lastStatus);
			}
			return objHolder[0];
		}

		public boolean hasGraphicMode() throws RemoteException {
			return true;
		}

		public RemotePanel getPanel(int w, int h) throws RemoteException {
			return new RGraphicsPanelRemote(w, h, gn);
		}

		public GDDevice newDevice(int w, int h) throws RemoteException {
			return new GDDeviceLocal(w, h);
		}

		public GDDevice newBroadcastedDevice(int w, int h) throws RemoteException {
			return new GDDeviceLocal(w, h, true);
		}

		public GDDevice[] listDevices() throws RemoteException {
			GDDevice[] result = new GDDevice[_localDeviceHashMap.values().size()];
			int i = 0;
			for (GDDevice d : _localDeviceHashMap.values())
				result[i++] = d;
			return result;
		}

		private void getWorkingDirectoryFileNames(File path, Vector<String> result) throws java.rmi.RemoteException {
			File[] files = path.listFiles();
			if (files == null)
				return;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					getWorkingDirectoryFileNames(files[i], result);
				} else {
					String name = path.getAbsolutePath().substring(WDIR.length(), path.getAbsolutePath().length()) + System.getProperty("file.separator")
							+ files[i].getName();
					name = (name.substring(1, name.length())).replace('\\', '/');
				}
			}
		}

		private void getWorkingDirectoryFileDescriptions(File path, Vector<FileDescription> result) throws java.rmi.RemoteException {
			File[] files = path.listFiles();
			if (files == null)
				return;
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					String name = path.getAbsolutePath().substring(WDIR.length(), path.getAbsolutePath().length()) + System.getProperty("file.separator")
							+ files[i].getName();
					name = name.substring(1, name.length()).replace('\\', '/');
					result.add(new FileDescription(name, files[i].length(), true, new Date(files[i].lastModified())));
					/*
					 * String name =
					 * (files[i].getAbsolutePath().substring(WDIR.length() + 1,
					 * files[i].getAbsolutePath().length())).replace('\\', '/');
					 * result.add(new FileDescription(name, 0, true, new
					 * Date(files[i].lastModified())));
					 * getWorkingDirectoryFileDescriptions(files[i], result);
					 */
				} else {
					String name = path.getAbsolutePath().substring(WDIR.length(), path.getAbsolutePath().length()) + System.getProperty("file.separator")
							+ files[i].getName();
					name = name.substring(1, name.length()).replace('\\', '/');
					result.add(new FileDescription(name, files[i].length(), false, new Date(files[i].lastModified())));
				}
			}
		}

		public String[] getWorkingDirectoryFileNames() throws java.rmi.RemoteException {
			Vector<String> result = new Vector<String>();
			getWorkingDirectoryFileNames(new File(WDIR), result);
			return (String[]) result.toArray(new String[0]);
		}

		public FileDescription[] getWorkingDirectoryFileDescriptions() throws java.rmi.RemoteException {
			Vector<FileDescription> result = new Vector<FileDescription>();
			getWorkingDirectoryFileDescriptions(new File(WDIR), result);
			return (FileDescription[]) result.toArray(new FileDescription[0]);
		}

		public FileDescription getWorkingDirectoryFileDescription(String fileName) throws java.rmi.RemoteException {
			File f = new File(WDIR + System.getProperty("file.separator") + fileName);
			return new FileDescription(fileName, f.length(), f.isDirectory(), new Date(f.lastModified()));
		}

		public void createWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
			try {
				File f = new File(WDIR + '/' + fileName);

				String name = f.getAbsolutePath().replace('\\', '/');
				new File(name.substring(0, name.lastIndexOf('/'))).mkdirs();

				RandomAccessFile raf = new RandomAccessFile(f, "rw");
				raf.setLength(0);
				raf.close();
			} catch (Exception e) {
				throw new RemoteException(PoolUtils.getStackTraceAsString(e));
			}
		}

		public void removeWorkingDirectoryFile(String fileName) throws java.rmi.RemoteException {
			try {
				File f = new File(WDIR + '/' + fileName);
				if (f.isDirectory()) {
					PoolUtils.deleteDirectory(f);
				} else {
					f.delete();
				}
			} catch (Exception e) {
				throw new RemoteException(PoolUtils.getStackTraceAsString(e));
			}
		}

		public byte[] readWorkingDirectoryFileBlock(String fileName, long offset, int blocksize) throws java.rmi.RemoteException {
			try {
				RandomAccessFile raf = new RandomAccessFile(new File(WDIR + '/' + fileName), "r");
				raf.seek(offset);
				byte[] result = new byte[blocksize];
				int n = raf.read(result);
				raf.close();

				if (n < blocksize) {
					byte[] temp = new byte[n];
					System.arraycopy(result, 0, temp, 0, n);
					result = temp;
				}
				return result;
			} catch (Exception e) {
				throw new RemoteException("Exception Holder", e);
			}
		}

		public void appendBlockToWorkingDirectoryFile(String fileName, byte[] block) throws java.rmi.RemoteException {
			try {
				RandomAccessFile raf = new RandomAccessFile(new File(WDIR + '/' + fileName), "rw");
				raf.seek(raf.length());
				raf.write(block);
				raf.close();
			} catch (Exception e) {
				throw new RemoteException("Exception Holder", e);
			}
		}

		public byte[] getRHelpFile(String uri) throws RemoteException {
			try {
				if (uri.indexOf('#') != -1) {
					uri = uri.substring(0, uri.indexOf('#'));
				}

				String filePath = null;
				if (System.getenv().get("R_LIBS") != null && !System.getenv().get("R_LIBS").equals("") && uri.startsWith("/library/")) {
					filePath = System.getenv().get("R_LIBS") + uri.substring(8);
					if (!new File(filePath).exists())
						filePath = null;
				}

				if (filePath == null) {
					filePath = System.getenv().get("R_HOME") + uri;
				}

				RandomAccessFile raf = new RandomAccessFile(filePath, "r");
				byte[] result = new byte[(int) raf.length()];
				raf.readFully(result);
				raf.close();

				return result;
			} catch (Exception e) {
				throw new RemoteException("Exception Holder", e);
			}
		}

		public String getRHelpFileUri(String topic, String pack) throws RemoteException {
			if (pack != null && !pack.equals("")) {
				String uri = "/library/" + pack + "/html/" + topic + ".html";
				if (new File(System.getenv().get("R_HOME") + uri).exists()) {
					return uri;
				} else {
					return _symbolUriMap.get(pack + "~" + topic);
				}
			} else {

				String[] nameSpaces = ((RChar) DirectJNI.getInstance().getRServices().getObject("loadedNamespaces()")).getValue();
				for (int i = 0; i < nameSpaces.length; ++i) {
					if (_nameSpacesHash.get(nameSpaces[i]) == null) {
						String[] exportedSymbols = ((RChar) DirectJNI.getInstance().getRServices().getObject(
								"getNamespaceExports(getNamespace('" + nameSpaces[i] + "'))")).getValue();
						Vector<String> v = new Vector<String>();
						for (int j = 0; j < exportedSymbols.length; ++j)
							v.add(exportedSymbols[j]);
						_nameSpacesHash.put(nameSpaces[i], v);
					}
				}

				pack = null;
				for (int i = (nameSpaces.length - 1); i >= 0; --i) {
					if (_nameSpacesHash.get(nameSpaces[i]).contains(topic)) {
						pack = nameSpaces[i];
						break;
					}
					;
				}

				if (pack == null) {
					return null;
				} else {

					String uri = "/library/" + pack + "/html/" + topic + ".html";
					if (new File(System.getenv().get("R_HOME") + uri).exists()) {
						return uri;
					} else {

						if (System.getenv().get("R_LIBS") != null && !System.getenv().get("R_LIBS").equals("")) {
							if (new File(System.getenv().get("R_LIBS") + pack + "/html/" + topic + ".html").exists()) {
								return uri;
							}
						}

						return _symbolUriMap.get(pack + "~" + topic);
					}

				}
			}
		}

		public String[] listDemos() throws RemoteException {
			if (demosList == null) {
				demosList = new Vector<String>();
				try {
					Properties props = new Properties();
					props.loadFromXML(getResourceAsStream("/org/kchine/r/server/rdemos/list.properties"));
					for (Object key : PoolUtils.orderO(props.keySet())) {
						demosList.add(props.getProperty((String) key));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return demosList.toArray(new String[0]);

		}

		public String getDemoSource(String demoName) throws RemoteException {
			if (!resourceExists("/org/kchine/r/server/rdemos/" + demoName + ".r")) {
				throw new RemoteException("no demo with name <" + demoName + ">");
			} else {
				try {
					StringBuffer result = new StringBuffer();
					BufferedReader br = new BufferedReader(new InputStreamReader(getResourceAsStream("/org/kchine/r/server/rdemos/" + demoName + ".r")));
					String line = null;
					while ((line = br.readLine()) != null) {
						result.append(line + "\n");
					}
					return result.toString();
				} catch (Exception e) {
					throw new RemoteException("", e);
				}
			}
		}

		public String getProcessId() throws RemoteException {
			return PoolUtils.getProcessId();
		}

		public String getHostIp() throws RemoteException {
			return PoolUtils.getHostIp();
		}

		public String getHostName() throws RemoteException {
			return PoolUtils.getHostName();
		}

		public Map<String, String> getSystemEnv() throws RemoteException {
			return System.getenv();
		}

		public Properties getSystemProperties() throws RemoteException {
			return System.getProperties();
		}

		public String getWorkingDirectory() throws RemoteException {
			return new File(WDIR).getAbsolutePath();
		}

		public String getInstallDirectory() throws RemoteException {
			return ServerManager.INSTALL_DIR;
		}

		public String getExtensionsDirectory() throws RemoteException {
			return ServerManager.EXTENSIONS_DIR;
		}

		public boolean isPortInUse(int port) throws RemoteException {
			return ServerManager.isPortInUse("127.0.0.1", port);
		}

		public void startHttpServer(int port) throws RemoteException {
		}

		public void stopHttpServer() throws RemoteException {
		}

		public boolean isHttpServerStarted(int port) throws RemoteException {
			return false;
		}

		public RServices cloneServer() throws RemoteException {
			return null;
		}

		public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
		}

		public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
		}

		public byte[] getSvg(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getSvg();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getSvg(String script, Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getSvg(width, height, onefile, bg, pointsize);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPostscript(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPostscript();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPostscript(String script, Boolean onefile, String family, String title, String[] fonts, String encoding, String bg, String fg,
				Integer width, Integer height, Boolean horizontal, Integer pointsize, String paper, Boolean pagecentre, String colormodel)
				throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPostscript(onefile, family, title, fonts, encoding, bg, fg, width, height, horizontal, pointsize, paper, pagecentre,
						colormodel);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPdf(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPdf();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPdf(String script, Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version,
				String paper, String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats)
				throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPdf(width, height, onefile, family, title, fonts, version, paper, encoding, bg, fg, pointsize, pagecentre, colormodel,
						useDingbats);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPictex(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPictex();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPictex(String script, Integer width, Integer height, Boolean debug, String bg, String fg) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPictex(width, height, debug, bg, fg);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getBmp(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getBmp();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getBmp(String script, Integer width, Integer height, String units, Integer pointsize, String bg, Integer res) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getBmp(width, height, units, pointsize, bg, res);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getJpeg(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getJpeg();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getJpeg(String script, Integer width, Integer height, String units, Integer pointsize, Integer quality, String bg, Integer res)
				throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getJpeg(width, height, units, pointsize, quality, bg, res);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPng(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPng();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getPng(String script, Integer width, Integer height, String units, Integer pointsize, String bg, Integer res) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getPng(width, height, units, pointsize, bg, res);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getTiff(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getTiff();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getTiff(String script, Integer width, Integer height, String units, Integer pointsize, String compression, String bg, Integer res)
				throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getTiff(width, height, units, pointsize, compression, bg, res);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getXfig(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getXfig();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getXfig(String script, Boolean onefile, String encoding, String paper, Boolean horizontal, Integer width, Integer height, String family,
				Integer pointsize, String bg, String fg, Boolean pagecentre) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getXfig(onefile, encoding, paper, horizontal, width, height, family, pointsize, bg, fg, pagecentre);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getWmf(String script, int width, int height, boolean useserver) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getWmf(useserver);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getEmf(String script, int width, int height, boolean useserver) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getEmf(useserver);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getOdg(String script, int width, int height) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getOdg();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		public byte[] getFromImageIOWriter(String script, int width, int height, String format) throws RemoteException {
			GDDevice deviceLocal = null;
			String status = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal(width, height);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(script);
				status = getStatus();
				DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1);
				return deviceLocal.getFromImageIOWriter(format);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
				_lastStatus = status;
			}
		}

		/*
		 * public byte[] getSvg(String script, int width, int height) throws
		 * RemoteException {
		 * 
		 * File tempFile = null; String status = "";
		 * 
		 * try { tempFile = new File(TEMP_DIR + "/temp" +
		 * System.currentTimeMillis() + ".svg").getCanonicalFile(); if
		 * (tempFile.exists()) tempFile.delete(); } catch (Exception e) {
		 * e.printStackTrace(); throw new RemoteException("", e); }
		 * 
		 * {
		 * 
		 * int[] devices=snapshotDevices(); String command =
		 * (DirectJNI.getInstance().getCairoCapabilities().contains("svg") ?
		 * "CairoSVG":"svg") +"(file = \"" +
		 * tempFile.getAbsolutePath().replace('\\', '/') + "\", width = " + new
		 * Double(10 (width / height)) + ", height = " + 10 +
		 * " , onefile = TRUE, bg = \"transparent\" ,pointsize = 12);";
		 * 
		 * System.out.println(">>>"+command+"<<");
		 * DirectJNI.getInstance().getRServices().evaluate(command);
		 * 
		 * 
		 * if (!getStatus().equals("")) { log.info(getStatus()); }
		 * 
		 * int deviceNumber=guessNewDevice(devices);
		 * 
		 * sourceFromBuffer(script); status = getStatus(); if
		 * (!status.equals("")) { log.info(status); }
		 * 
		 * if (tempFile.exists()) { byte[] result = null; try {
		 * DirectJNI.getInstance
		 * ().getRServices().evaluate(".PrivateEnv$dev.off("+deviceNumber+")",
		 * 1);
		 * 
		 * RandomAccessFile raf = new RandomAccessFile(tempFile, "r"); result =
		 * new byte[(int) raf.length()]; raf.readFully(result); raf.close();
		 * tempFile.delete();
		 * 
		 * } catch (Exception e) { e.printStackTrace(); throw new
		 * RemoteException("", e); } _lastStatus = status; return result; } else
		 * { return null; }
		 * 
		 * } }
		 * 
		 * 
		 * public byte[] getPdf(String script, int width, int height) throws
		 * RemoteException {
		 * System.out.println("getPDF::"+"width:"+width+"  height:"+height);
		 * String status = ""; File tempFile = null; try { tempFile = new
		 * File(TEMP_DIR + "/temp" + System.currentTimeMillis() +
		 * ".pdf").getCanonicalFile(); if (tempFile.exists()) tempFile.delete();
		 * } catch (Exception e) { e.printStackTrace(); throw new
		 * RemoteException("", e); }
		 * 
		 * int currentDevice = ((RInteger)
		 * getObject(".PrivateEnv$dev.cur()")).getValue()[0];
		 * shutdownDevices("pdf");
		 * 
		 * final String createDeviceCommand = "pdf(file = \"" +
		 * tempFile.getAbsolutePath().replace('\\', '/') + "\", width = " + new
		 * Double(6 ((double)width / (double)height)) + ", height = " + 6 +
		 * " , onefile = TRUE, title = '', fonts = NULL, version = '1.1' )";
		 * evaluate(createDeviceCommand); if (!getStatus().equals("")) {
		 * log.info(getStatus()); }
		 * 
		 * sourceFromBuffer(script); status = getStatus(); if
		 * (!status.equals("")) { log.info(status); }
		 * evaluate(".PrivateEnv$dev.set(" + currentDevice + ");", 1); if
		 * (tempFile.exists()) { byte[] result = null; try {
		 * shutdownDevices("pdf"); RandomAccessFile raf = new
		 * RandomAccessFile(tempFile, "r"); result = new byte[(int)
		 * raf.length()]; raf.readFully(result); raf.close(); tempFile.delete();
		 * } catch (Exception e) { e.printStackTrace(); throw new
		 * RemoteException("", e); } _lastStatus = status; return result; } else
		 * { _lastStatus = status; return null; }
		 * 
		 * }
		 */

		public String getPythonStatus() throws RemoteException {
			return PythonInterpreterSingleton.getPythonStatus();

		}

		public String pythonExceFromResource(String resource) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String pythonExec(String pythonCommand) throws RemoteException {
			try {
				PythonInterpreterSingleton.startLogCapture();
				PythonInterpreterSingleton.getInstance().exec(pythonCommand);
				System.out.println("#>>>:" + PythonInterpreterSingleton.getPythonStatus());
				return PythonInterpreterSingleton.getPythonStatus();
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public String pythonExecFromBuffer(String buffer) throws RemoteException {
			File f = null;
			try {
				f = PoolUtils.createFileFromBuffer(null, buffer);
				PythonInterpreterSingleton.startLogCapture();
				PythonInterpreterSingleton.getInstance().execfile(f.getAbsolutePath());
				System.out.println("#>>>:" + PythonInterpreterSingleton.getPythonStatus());
				return PythonInterpreterSingleton.getPythonStatus();
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public String pythonExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
			try {
				PythonInterpreterSingleton.startLogCapture();
				System.out.println("[[[[[[[" + WDIR + "/" + fileName);
				PythonInterpreterSingleton.getInstance().execfile(WDIR + "/" + fileName);
				System.out.println("#>>>:" + PythonInterpreterSingleton.getPythonStatus());
				return PythonInterpreterSingleton.getPythonStatus();
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public RObject pythonEval(String pythonCommand) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object pythonEvalAndConvert(String pythonCommand) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public RObject pythonGet(String name) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object pythonGetAndConvert(String name) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void pythonSet(String name, Object Value) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public Object groovyEval(String expression) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String groovyExecFromResource(String resource) throws RemoteException {
			return null;
		}

		public String groovyExec(String groovyCommand) throws RemoteException {
			try {
				return org.kchine.r.server.scripting.GroovyInterpreterSingleton.getInstance().exec(groovyCommand);
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public String groovyExecFromBuffer(String buffer) throws RemoteException {
			File f = null;
			try {
				return org.kchine.r.server.scripting.GroovyInterpreterSingleton.getInstance().execFromBuffer(buffer);
			} catch (Exception e) {
				throw new RemoteException("", e);
			} finally {
				if (f != null)
					f.delete();
			}
		}

		public String groovyExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
			try {
				return org.kchine.r.server.scripting.GroovyInterpreterSingleton.getInstance().execFromFile(new File(WDIR + "/" + fileName));
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public Object groovyGet(String name) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void groovySet(String name, Object Value) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public String getGroovyStatus() throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void reinitializeGroovyInterpreter() throws RemoteException {
			GroovyInterpreterSingleton._clientSideGroovy = null;
			new Thread(new Runnable() {
				public void run() {
					try {
						GroovyInterpreterSingleton.getInstance().exec("import org.kchine.r.server.R;");
						GroovyInterpreterSingleton.getInstance().exec("R=org.kchine.r.server.R.getInstance();");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}).start();
		}

		public boolean isExtensionAvailable(String extensionName) throws RemoteException {
			return new File(ServerManager.EXTENSIONS_DIR + "/" + extensionName + ".jar").exists()
					|| new File(ServerManager.EXTENSIONS_DIR + "/" + extensionName).exists();
		}

		public String[] listExtensions() throws RemoteException {
			File[] extensionsJarFiles = new File(ServerManager.EXTENSIONS_DIR).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.endsWith(".jar");
				}
			});
			Arrays.sort(extensionsJarFiles);

			File[] extensionsDirs = new File(ServerManager.EXTENSIONS_DIR).listFiles(new FileFilter() {
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			});
			Arrays.sort(extensionsDirs);

			String[] result = new String[extensionsJarFiles.length + extensionsDirs.length];
			for (int i = 0; i < extensionsJarFiles.length; ++i) {
				String name = extensionsJarFiles[i].getName();
				name = name.substring(0, name.length() - ".jar".length());
				result[i] = name;
			}

			for (int j = 0; j < extensionsDirs.length; ++j) {
				result[j + extensionsJarFiles.length] = extensionsDirs[j].getName();
			}

			return result;

		}

		public void installExtension(String extensionName, String extensionURL) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void installExtension(String extensionName, byte[] extension) {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void removeExtension(String extensionName) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public void convertFile(String inputFile, String outputFile, String conversionFilter, boolean useServer) throws RemoteException {
			inputFile = inputFile.replace('\\', '/');
			outputFile = outputFile.replace('\\', '/');
			if (!inputFile.startsWith("/")) {
				inputFile = new File(WDIR + "/" + inputFile).getAbsolutePath();
			}
			if (!outputFile.startsWith("/")) {
				outputFile = new File(WDIR + "/" + outputFile).getAbsolutePath();
			}

			GroovyInterpreter gr = GroovyInterpreterSingleton.getInstance();
			try {
				System.out.println(gr.exec("import org.kchine.ooc.OOConverter;"));
				System.out.println(gr.exec("org.kchine.ooc.OOConverter.convert" + (useServer ? "" : "NoServer") + "(\""
						+ new File(inputFile).getAbsolutePath().replace('\\', '/') + "\", \"" + new File(outputFile).getAbsolutePath().replace('\\', '/')
						+ "\", \"" + conversionFilter + "\" );"));
			} catch (Exception e) {
				new RemoteException("", e);
			}

			if (!new File(outputFile).exists()) {
				throw new RemoteException(
						"check that you have installed open office 3 and that soffice is in your system path (accessible from your command line)");
			}

		}

		public SpreadsheetModelRemote newSpreadsheetTableModelRemote(int rowCount, int colCount) throws RemoteException {
			return new SpreadsheetModelRemoteImpl(rowCount, colCount, _spreadsheetTableModelRemoteHashMap);
		}

		public SpreadsheetModelRemote getSpreadsheetTableModelRemote(String Id) throws RemoteException {
			return _spreadsheetTableModelRemoteHashMap.get(Id);
		}

		public SpreadsheetModelRemote[] listSpreadsheetTableModelRemote() throws RemoteException {
			SpreadsheetModelRemote[] result = new SpreadsheetModelRemote[_spreadsheetTableModelRemoteHashMap.size()];
			int i = 0;
			for (SpreadsheetModelRemote v : _spreadsheetTableModelRemoteHashMap.values())
				result[i++] = v;
			return result;
		}

		public String[] listSpreadsheetTableModelRemoteId() throws RemoteException {
			String[] result = new String[_spreadsheetTableModelRemoteHashMap.size()];
			int i = 0;
			for (String k : _spreadsheetTableModelRemoteHashMap.keySet())
				result[i++] = k;
			return result;
		}

		public int countSets() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarSetInterfaceRemote getSet(int i) throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarSetInterfaceRemote getCurrentSet() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public int curSetId() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarInterfaceRemote getVar(int setId, int i) throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public SVarInterfaceRemote getVar(int setId, String name) throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		public String getStub() throws RemoteException {
			throw new UnsupportedOperationException("Not supported at this layer.");
		}

		synchronized public void addProbeOnVariables(String[] variables) throws RemoteException {
			HashSet<String> pvHash = new HashSet<String>();
			for (int i = 0; i < probedVariables.length; ++i)
				pvHash.add(probedVariables[i]);
			for (int i = 0; i < variables.length; ++i)
				pvHash.add(variables[i]);

			String[] newProbedVariables = new String[pvHash.size()];
			int i = 0;
			for (String k : pvHash)
				newProbedVariables[i++] = k;
			probedVariables = newProbedVariables;
		}

		synchronized public void removeProbeOnVariables(String[] variables) throws RemoteException {
			HashSet<String> pvHash = new HashSet<String>();
			for (int i = 0; i < probedVariables.length; ++i)
				pvHash.add(probedVariables[i]);
			for (int i = 0; i < variables.length; ++i)
				pvHash.remove(variables[i]);

			String[] newProbedVariables = new String[pvHash.size()];
			int i = 0;
			for (String k : pvHash)
				newProbedVariables[i++] = k;
			probedVariables = newProbedVariables;
			;
		}

		public void setProbedVariables(String[] variables) throws RemoteException {
			HashSet<String> pvHash = new HashSet<String>();
			for (int i = 0; i < variables.length; ++i)
				pvHash.add(variables[i]);
			String[] newProbedVariables = new String[pvHash.size()];
			int i = 0;
			for (String k : pvHash)
				newProbedVariables[i++] = k;
			probedVariables = newProbedVariables;
		}

		public String[] getProbedVariables() throws RemoteException {
			return probedVariables;
		}

		public String[] getMissingLibraries(String[] requiredLibraries) throws RemoteException {
			return null;
		}

		public RObject cellsGet(String range, String type, String spreadsheetName) throws RemoteException {
			return null;
		}

		public Object cellsGetConverted(String range, String type, String spreadsheetName) throws RemoteException {
			return null;
		}

		public void cellsPut(Object value, String location, String spreadsheetName) throws RemoteException {

		}

		public void addProbeOnCells(String spreadsheetName) throws RemoteException {

		}

		public boolean isProbeOnCell(String spreadsheetName) throws RemoteException {
			return false;
		}

		public void removeProbeOnCells(String spreadsheetName) throws RemoteException {

		}

		public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
			return null;
		}

		public boolean scilabExec(String cmd) throws java.rmi.RemoteException {
			return ScilabServicesSingleton.getInstance().scilabExec(cmd);
		}

		public String scilabConsoleSubmit(String cmd) throws RemoteException {
			return ScilabServicesSingleton.getInstance().scilabConsoleSubmit(cmd);
		}

		public Object scilabGetObject(String expression) throws RemoteException {
			return ScilabServicesSingleton.getInstance().scilabGetObject(expression);
		}

		public void scilabPutAndAssign(Object obj, String name) throws RemoteException {
			ScilabServicesSingleton.getInstance().scilabPutAndAssign(obj, name);
		}
		

	};

	static private HashMap<Integer, GDDevice> _localDeviceHashMap = new HashMap<Integer, GDDevice>();
	static private HashSet<Integer> _localBroadcastedDevices = new HashSet<Integer>();

	public boolean broadcastRequired(int currentDevice) {
		return _localBroadcastedDevices.size() > 1 && _localBroadcastedDevices.contains(currentDevice);
	}

	static int[] snapshotDevices() throws RemoteException {
		RInteger list = (RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.list()");
		return (list == null ? new int[0] : list.getValue());
	}

	static Integer guessNewDevice(int[] snapshot) throws RemoteException {
		Vector<Integer> devicesVectorBefore = new Vector<Integer>();
		for (int i = 0; i < snapshot.length; ++i)
			devicesVectorBefore.add(snapshot[i]);
		int[] devicesNow = snapshotDevices();
		for (int i = 0; i < devicesNow.length; ++i)
			if (!devicesVectorBefore.contains(devicesNow[i])) {
				return devicesNow[i];
			}
		return null;
	}

	public static class GDDeviceLocal implements GDDevice {
		GDContainerBag gdBag = null;

		public GDDeviceLocal(int w, int h) throws RemoteException {
			gdBag = new GDContainerBag(w, h);
			JavaGD.setGDContainer(gdBag);
			Dimension dim = gdBag.getContainerSize();

			int[] devicesVector = snapshotDevices();
			System.out.println(DirectJNI.getInstance().getRServices().evaluate(
					"JavaGD(name='JavaGD', width=" + dim.getWidth() + ", height=" + dim.getHeight() + ", ps=12)"));
			gdBag.setDeviceNumber(guessNewDevice(devicesVector));

			// System.out.println(DirectJNI.getInstance().getRServices().
			// consoleSubmit(".PrivateEnv$dev.list()"));

			_localDeviceHashMap.put(gdBag.getDeviceNumber(), this);

		}

		public GDDeviceLocal(int w, int h, boolean broadcasted) throws RemoteException {
			this(w, h);
			if (broadcasted) {
				_localBroadcastedDevices.add(gdBag.getDeviceNumber());
				DirectJNI.getInstance().notifyRActionListeners(new RConsoleAction("OPEN_BROADCASTED_DEVICE", new HashMap<String, Object>()));
			}
		}

		public Vector<org.kchine.r.server.graphics.primitive.GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException {
			return gdBag.popAllGraphicObjects(maxNbrGraphicPrimitives);
		};
		
		public byte[] popAllGraphicObjectsSerialized(int maxNbrGraphicPrimitives) throws RemoteException {
			return gdBag.popAllGraphicObjectsSerialized(maxNbrGraphicPrimitives);
		};

		public boolean hasGraphicObjects() throws RemoteException {
			return gdBag.hasGraphicObjects();
		}

		public void fireSizeChangedEvent(int w, int h) throws RemoteException {
			gdBag.setSize(w, h);
			DirectJNI.getInstance().getRServices().evaluate("try( {.C(\"javaGDresize\",as.integer(" + gdBag.getDeviceNumber() + "))}, silent=TRUE)");
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				System.out.println(DirectJNI.getInstance().getRServices().getStatus());
			}
			// DirectJNI.getInstance().getRServices().consoleSubmit("1");
		};

		public void dispose() throws RemoteException {
			_localDeviceHashMap.remove(gdBag.getDeviceNumber());
			_localBroadcastedDevices.remove(gdBag.getDeviceNumber());
			DirectJNI.getInstance().getRServices().evaluate("try({ .PrivateEnv$dev.off(which=" + gdBag.getDeviceNumber() + ")},silent=TRUE)");

		};

		public int getDeviceNumber() throws RemoteException {
			return gdBag.getDeviceNumber();
		}

		public boolean isCurrentDevice() throws RemoteException {
			int d = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
			return d == gdBag.getDeviceNumber();
		}

		public void setAsCurrentDevice() throws RemoteException {
			DirectJNI.getInstance().getRServices().evaluate(".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ")");
		}

		public Dimension getSize() throws RemoteException {
			return gdBag.getContainerSize();
		}

		public void putLocation(Point2D p) throws RemoteException {
			GDInterface.putLocatorLocation(p);
		}

		public boolean hasLocations() throws RemoteException {
			return GDInterface.hasLocations();
		}

		public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
			GDInterface.saveLocations();
			try {
				for (int i = 0; i < points.length; ++i) {
					GDInterface.putLocatorLocation(points[i]);
				}

				RList l = (RList) DirectJNI.getInstance().getRServices().getObject("locator()");

				Point2D[] result = new Point2D[points.length];
				for (int i = 0; i < points.length; ++i) {
					result[i] = new DoublePoint(((RNumeric) l.getValue()[0]).getValue()[i], ((RNumeric) l.getValue()[1]).getValue()[i]);
				}
				return result;
			} finally {
				GDInterface.restoreLocations();
			}

		}

		synchronized public byte[] getSvg() throws RemoteException {
			return getScalable(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, "svg");
		}

		public byte[] getSvg(Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException {
			throw new UnsupportedOperationException("Not supported yet.");
		}

		public byte[] getPostscript() throws RemoteException {
			return getPostscript(null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		}

		public byte[] getPostscript(Boolean onefile, String family, String title, String[] fonts, String encoding, String bg, String fg, Integer width,
				Integer height, Boolean horizontal, Integer pointsize, String paper, Boolean pagecentre, String colormodel) throws RemoteException {
			return getScalable(width, height, onefile, family, title, fonts, null, paper, encoding, bg, fg, pointsize, pagecentre, colormodel, null,
					horizontal, null, "postscript");
		}

		public byte[] getPdf() throws RemoteException {
			return getPdf(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		}

		public byte[] getPdf(Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version, String paper,
				String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats) throws RemoteException {
			return getScalable(width, height, onefile, family, title, fonts, version, paper, encoding, bg, fg, pointsize, pagecentre, colormodel, useDingbats,
					null, null, "pdf");
		}

		public byte[] getScalable(Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version, String paper,
				String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats, Boolean horizontal,
				Boolean debug, String extension) throws RemoteException {
			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + "." + extension).getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}

			String tempFileName = tempFile.getAbsolutePath().replace('\\', '/');

			int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
			DirectJNI.getInstance().shutdownDevices(extension);

			String createDeviceCommand = DirectJNI.getInstance().getCairoCapabilities().contains(extension) ? "Cairo" + extension.toUpperCase() : extension;
			createDeviceCommand += "(file = '" + tempFileName + "' ";

			if (height == null && width == null) {
				createDeviceCommand += ", height=6 , width=" + new Double(6 * ((double) getSize().width / (double) getSize().height));
			} else if (height != null && width != null) {
				createDeviceCommand += ", height=" + height + " , width=" + width;
			} else {
				throw new RemoteException("width & height should be both null or both set");
			}

			if (onefile != null) {
				createDeviceCommand += ", onefile=" + onefile.toString().toUpperCase();
			}

			if (family != null) {
				createDeviceCommand += ", family='" + family + "'";
			}

			if (title != null) {
				createDeviceCommand += ", title='" + title + "'";
			}

			if (fonts != null && fonts.length > 0) {
				createDeviceCommand += ", c(";
				for (int i = 0; i < fonts.length; ++i)
					createDeviceCommand += "'" + fonts[i] + "'" + (i == fonts.length - 1 ? ")" : ",");
			}

			if (version != null) {
				createDeviceCommand += ", version='" + version + "'";
			}

			if (paper != null) {
				createDeviceCommand += ", paper='" + paper + "'";
			}

			if (paper != null) {
				createDeviceCommand += ", paper='" + paper + "'";
			}

			if (encoding != null) {
				createDeviceCommand += ", encoding='" + encoding + "'";
			}

			if (bg != null) {
				createDeviceCommand += ", bg='" + bg + "'";
			}

			if (fg != null) {
				createDeviceCommand += ", fg='" + fg + "'";
			}

			if (pointsize != null) {
				createDeviceCommand += ", pointsize=" + pointsize;
			}

			if (pagecentre != null) {
				createDeviceCommand += ", pagecentre=" + pagecentre.toString().toUpperCase();
			}

			if (colormodel != null) {
				createDeviceCommand += ", colormodel='" + colormodel + "'";
			}
			if (useDingbats != null) {
				createDeviceCommand += ", useDingbats=" + useDingbats.toString().toUpperCase();
			}
			if (horizontal != null) {
				createDeviceCommand += ", horizontal=" + horizontal.toString().toUpperCase();
			}
			if (debug != null) {
				createDeviceCommand += ", debug=" + debug.toString().toUpperCase();
			}
			createDeviceCommand += " )";

			int[] devicesSnapshot = snapshotDevices();
			System.out.println(">>>" + createDeviceCommand + "<<<<");
			DirectJNI.getInstance().getRServices().evaluate(createDeviceCommand);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}
			int bmpDevice = guessNewDevice(devicesSnapshot);

			DirectJNI.getInstance().getRServices().evaluate(
					".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + bmpDevice + ");" + ".PrivateEnv$dev.set("
							+ currentDevice + ");.PrivateEnv$dev.off(" + bmpDevice + ")", 4);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}

			if (tempFile.exists()) {

				byte[] result = null;
				try {

					RandomAccessFile raf = new RandomAccessFile(tempFile, "r");
					result = new byte[(int) raf.length()];
					raf.readFully(result);
					raf.close();

					tempFile.delete();

				} catch (Exception e) {
					e.printStackTrace();
					throw new RemoteException("", e);
				}
				return result;
			} else {
				return null;
			}
		}

		public byte[] getPictex() throws RemoteException {
			return getPictex(null, null, null, null, null);
		}

		public byte[] getPictex(Integer width, Integer height, Boolean debug, String bg, String fg) throws RemoteException {
			return getScalable(width, height, null, null, null, null, null, null, null, bg, fg, null, null, null, null, null, debug, "pictex");
		}

		public byte[] getBmp() throws RemoteException {
			return getBmp(null, null, null, null, null, null);
		}

		public byte[] getBmp(Integer width, Integer height, String units, Integer pointsize, String bg, Integer res) throws RemoteException {
			return getBitMap(width, height, units, pointsize, null, null, null, bg, res, "bmp");
		}

		public byte[] getBitMap(Integer width, Integer height, String units, Integer pointsize, Integer quality, String compression, Integer tiffindex,
				String bg, Integer res, String extension) throws RemoteException {

			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + "." + extension).getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}

			String tempFileName = tempFile.getAbsolutePath().replace('\\', '/');

			int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];

			String createDeviceCommand = (DirectJNI.getInstance().getCairoCapabilities().contains(extension) ? "Cairo" + extension.toUpperCase() : extension);
			createDeviceCommand += "(filename = '" + tempFileName + "' ";

			if (height == null && width == null) {
				createDeviceCommand += ", height=" + getSize().getHeight() + " , width=" + getSize().getWidth();
			} else if (height != null && width != null) {
				createDeviceCommand += ", height=" + height + " , width=" + width;
			} else {
				throw new RemoteException("width & height should be both null or both set");
			}

			if (units != null) {
				createDeviceCommand += ", units='" + units + "'";
			}

			if (pointsize != null) {
				createDeviceCommand += ", pointsize=" + pointsize;
			}

			if (quality != null) {
				createDeviceCommand += ", quality=" + quality;
			}

			if (compression != null) {
				createDeviceCommand += ", compression='" + compression + "'";
			}

			if (bg != null) {
				createDeviceCommand += ", bg='" + bg + "'";
			}

			if (res != null) {
				createDeviceCommand += ", res=" + res;
			}

			createDeviceCommand += " )";

			int[] devicesSnapshot = snapshotDevices();
			System.out.println(">>>>>" + createDeviceCommand + "<<<");
			DirectJNI.getInstance().getRServices().evaluate(createDeviceCommand);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}
			int bmpDevice = guessNewDevice(devicesSnapshot);

			DirectJNI.getInstance().getRServices().evaluate(
					".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + bmpDevice + ");" + ".PrivateEnv$dev.set("
							+ currentDevice + ");.PrivateEnv$dev.off(" + bmpDevice + ")", 4);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}

			if (tempFile.exists()) {

				byte[] result = null;
				try {

					RandomAccessFile raf = new RandomAccessFile(tempFile, "r");
					result = new byte[(int) raf.length()];
					raf.readFully(result);
					raf.close();

					tempFile.delete();

				} catch (Exception e) {
					e.printStackTrace();
					throw new RemoteException("", e);
				}
				return result;
			} else {
				return null;
			}
		}

		public byte[] getJpeg() throws RemoteException {
			return getJpeg(null, null, null, null, null, null, null);
		}

		public byte[] getJpeg(Integer width, Integer height, String units, Integer pointsize, Integer quality, String bg, Integer res) throws RemoteException {
			return getBitMap(width, height, units, pointsize, (quality == null ? 75 : quality), null, null, bg, res, "jpeg");
		}

		public byte[] getPng() throws RemoteException {
			return getPng(null, null, null, null, null, null);
		}

		public byte[] getPng(Integer width, Integer height, String units, Integer pointsize, String bg, Integer res) throws RemoteException {
			return getBitMap(width, height, units, pointsize, null, null, null, bg, res, "png");
		}

		public byte[] getTiff() throws RemoteException {
			return getTiff(null, null, null, null, null, null, null);
		}

		public byte[] getTiff(Integer width, Integer height, String units, Integer pointsize, String compression, String bg, Integer res)
				throws RemoteException {
			return getBitMap(width, height, units, pointsize, null, compression, 1, bg, res, "tiff");
		}

		public byte[] getXfig() throws RemoteException {
			return getXfig(true, null, null, null, null, null, null, null, null, null, null);
		}

		public byte[] getXfig(Boolean onefile, String encoding, String paper, Boolean horizontal, Integer width, Integer height, String family,
				Integer pointsize, String bg, String fg, Boolean pagecentre) throws RemoteException {
			return getScalable(width, height, onefile, family, null, null, null, paper, encoding, bg, fg, pointsize, pagecentre, null, null, horizontal, null,
					"xfig");
		}

		public byte[] getWmf(boolean useServer) throws RemoteException {
			return getGenericVectorFormat("wmf", useServer);
		}

		public byte[] getEmf(boolean useServer) throws RemoteException {
			return getGenericVectorFormat("emf", useServer);
		}

		public byte[] getOdg() throws RemoteException {
			return getGenericVectorFormat("odg", false);
		}

		public byte[] getFromImageIOWriter(String format) throws RemoteException {
			GDDevice deviceLocal = null;
			try {
				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
				deviceLocal = new GDDeviceLocal((int) getSize().getWidth(), (int) getSize().getHeight());
				DirectJNI.getInstance().getRServices().evaluate(
						".PrivateEnv$dev.set(" + getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + deviceLocal.getDeviceNumber() + ");"
								+ ".PrivateEnv$dev.set(" + currentDevice + ");", 3);
				BufferedImage bufferedImage = Java2DUtils.getBufferedImage(new Point(0, 0), new java.awt.Dimension((int) getSize().getWidth(), (int) getSize()
						.getHeight()), deviceLocal.popAllGraphicObjects(-1));
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, format, bos);
				return bos.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			} finally {
				if (deviceLocal != null)
					deviceLocal.dispose();
			}
		}

		private String capitalizeFirstLetter(String s) {
			return ("" + s.charAt(0)).toUpperCase() + s.substring(1);
		}

		private byte[] getGenericVectorFormat(String format, boolean useserver) throws RemoteException {

			File tempSVGFile = null;
			File tempVectorFile = null;
			try {
				long currentTimeMillis = System.currentTimeMillis();
				tempSVGFile = new File(TEMP_DIR + "/temp" + currentTimeMillis + ".svg").getCanonicalFile();
				tempVectorFile = new File(TEMP_DIR + "/temp" + currentTimeMillis + "." + format).getCanonicalFile();
				if (tempSVGFile.exists())
					tempSVGFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}
			String SvgDeviceName = DirectJNI.getInstance().getCairoCapabilities().contains("svg") ? "CairoSVG" : "svg";
			int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
			DirectJNI.getInstance().shutdownDevices(SvgDeviceName);
			final String createDeviceCommand = SvgDeviceName + "(file = \"" + tempSVGFile.getAbsolutePath().replace('\\', '/') + "\", width = "
					+ new Double(10 * (getSize().width / getSize().height)) + ", height = " + 10 + " , onefile = TRUE, bg = \"transparent\" ,pointsize = 12)";

			int[] devicesSnapshot = snapshotDevices();
			System.out.println("createDeviceCommand:" + createDeviceCommand);
			DirectJNI.getInstance().getRServices().evaluate(createDeviceCommand);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
				System.out.println("Status:" + DirectJNI.getInstance().getRServices().getStatus());
			}
			int cairoDevice = guessNewDevice(devicesSnapshot);

			DirectJNI.getInstance().getRServices().evaluate(
					".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + cairoDevice + ");" + ".PrivateEnv$dev.set("
							+ currentDevice + ");", 3);

			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}

			if (tempSVGFile.exists()) {

				byte[] result = null;
				try {

					DirectJNI.getInstance().shutdownDevices(SvgDeviceName);

					GroovyInterpreter gr = GroovyInterpreterSingleton.getInstance();
					System.out.println(gr.exec("import org.kchine.ooc.OOConverter;"));
					System.out.println(gr.exec("org.kchine.ooc.OOConverter.svgTo" + capitalizeFirstLetter(format) + (useserver ? "" : "NoServer") + "(\""
							+ tempSVGFile.getAbsolutePath().replace('\\', '/') + "\", \"" + tempVectorFile.getAbsolutePath().replace('\\', '/') + "\" );"));
					if (!tempVectorFile.exists()) {
						throw new Exception("Couldn't generate " + format
								+ ", check that you have installed open office 3 and that soffice is in your system path (accessible from your command line)");
					}
					RandomAccessFile raf = new RandomAccessFile(tempVectorFile, "r");
					result = new byte[(int) raf.length()];
					raf.readFully(result);
					raf.close();

					tempVectorFile.delete();
					tempSVGFile.delete();

					return result;

				} catch (Exception e) {
					e.printStackTrace();
					throw new RemoteException("", e);
				}

			} else {
				return null;
			}

		}

		public String getId() throws RemoteException {
			return null;
		}

		public boolean isBroadcasted() throws RemoteException {
			return _localBroadcastedDevices.contains(gdBag.getDeviceNumber());
		}

		public void broadcast() throws RemoteException {
			if (_localBroadcastedDevices.size() <= 1 || !_localBroadcastedDevices.contains(gdBag.getDeviceNumber()))
				return;

			final Exception[] exceptionHolder = new Exception[1];
			String lastStatus = DirectJNI.getInstance().runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(Rengine e) {
					try {

						int currentDevice = e.rniGetIntArray(e.rniEval(e.rniParse(".PrivateEnv$dev.cur()", 1), 0))[0];
						String code = ".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");";
						for (Integer d : _localBroadcastedDevices) {
							if (d != gdBag.getDeviceNumber()) {
								code += ".PrivateEnv$dev.copy(which=" + d + ");";
							}
						}
						code += ".PrivateEnv$dev.set(" + currentDevice + ");";
						e.rniEval(e.rniParse(code, _localBroadcastedDevices.size() + 1), 0);

						System.out.println("code =" + code);

					} catch (Exception ex) {
						exceptionHolder[0] = ex;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				log.error(lastStatus);
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			} else if (!lastStatus.equals("")) {
				log.info(lastStatus);
			}
		}

	}

	private GraphicNotifier gn = new LocalGraphicNotifier();

	public GraphicNotifier getGraphicNotifier() {
		return gn;
	}

	private static boolean _initHasBeenCalled = false;

	public void initPrivateEnv() {
		runR(new org.kchine.r.server.ExecutionUnit() {
			public void run(Rengine e) {
				try {
					_privateEnvExp = e.rniEval(e.rniParse(PENV, 1), 0);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
	}

	// for private use

	public void assignInPrivateEnv(String name, long exp) {
		_rEngine.rniAssign(name, exp, _privateEnvExp);
	}

	private static void init(ClassLoader cl) throws Exception {
		Properties props = new Properties();
		InputStream is = cl.getResourceAsStream("maps/rjbmaps.xml");
		System.out.println("####### is:" + is);
		props.loadFromXML(is);
		DirectJNI._packageNames = (Vector<String>) PoolUtils.hexToObject((String) props.get("PACKAGE_NAMES"), cl);
		DirectJNI._s4BeansMapping = (HashMap<String, String>) PoolUtils.hexToObject((String) props.get("S4BEANS_MAP"), cl);
		DirectJNI._s4BeansMappingRevert = (HashMap<String, String>) PoolUtils.hexToObject((String) props.get("S4BEANS_REVERT_MAP"), cl);
		DirectJNI._factoriesMapping = (HashMap<String, String>) PoolUtils.hexToObject((String) props.get("FACTORIES_MAPPING"), cl);
		DirectJNI._s4BeansHash = (HashMap<String, Class<?>>) PoolUtils.hexToObject((String) props.get("S4BEANS_HASH"), cl);
		DirectJNI._rPackageInterfacesHash = (HashMap<String, Vector<Class<?>>>) PoolUtils.hexToObject((String) props.get("R_PACKAGE_INTERFACES_HASH"), cl);
		DirectJNI._abstractFactories = (Vector<String>) PoolUtils.hexToObject((String) props.get("ABSTRACT_FACTORIES"), cl);
		log.info("<> rPackageInterfaces:" + DirectJNI._packageNames);
		log.info("<> s4Beans MAP :" + DirectJNI._s4BeansMapping);
		log.info("<> s4Beans Revert MAP :" + DirectJNI._s4BeansMappingRevert);
		log.info("<> factories :" + DirectJNI._factoriesMapping);
		_mappingClassLoader = cl;
		_resourcesClassLoader = cl;
		Thread.currentThread().setContextClassLoader(_resourcesClassLoader);
		DirectJNI.getInstance().getRServices().sourceFromResource("/bootstrap.R");
		DirectJNI.getInstance().initPackages();
		DirectJNI.getInstance().upgdateBootstrapObjects();
	}

	private void upgdateBootstrapObjects() throws Exception {
		RChar objs = (RChar) getRServices().getObject(".PrivateEnv$ls(all.names=TRUE)");
		for (int i = 0; i < objs.getValue().length; ++i)
			if (!_bootstrapRObjects.contains(objs.getValue()[i]))
				_bootstrapRObjects.add(objs.getValue()[i]);
	}

	static private void scanMapping() {
		if (!_initHasBeenCalled) {
			_initHasBeenCalled = true;

			if (DirectJNI.class.getClassLoader().getResource("maps/rjbmaps.xml") != null) {
				System.out.println("<1> " + DirectJNI.class.getClassLoader().getResource("maps/rjbmaps.xml"));
				try {
					init(DirectJNI.class.getClassLoader());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return;

			} else if (System.getProperty("java.rmi.server.codebase") != null) {
				ClassLoader codebaseClassLoader = new URLClassLoader(PoolUtils.getURLS(System.getProperty("java.rmi.server.codebase")), DirectJNI.class
						.getClassLoader());
				if (codebaseClassLoader.getResource("maps/rjbmaps.xml") != null) {
					System.out.println("<2> " + codebaseClassLoader.getResource("maps/rjbmaps.xml"));
					try {
						init(codebaseClassLoader);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return;
				}
			} else {
				System.out.println("!! No mapping found");
			}
		}
	}

	public void regenerateWorkingDirectory(boolean callR) throws Exception {
		File wdirFile = new File(WDIR);
		if (wdirFile.exists() && System.getProperty("wks.persitent") != null && System.getProperty("wks.persitent").equals("false")) {
			File[] list = wdirFile.listFiles();
			for (int i = 0; i < list.length; ++i) {
				if (list[i].isDirectory()) {
					PoolUtils.deleteDirectory(list[i]);
				} else {
					list[i].delete();
				}
			}
		} else {
			wdirFile.mkdirs();
		}
		if (callR)
			getRServices().evaluate(".PrivateEnv$setwd(\"" + WDIR + "\")");
	}

	// public for internal use only
	public void reinitWorkingDirectory(String dir) throws Exception {
		File d = new File(dir);
		if (!d.exists() || !d.isDirectory()) {
			throw new Exception("Bad Directory");
		}
		System.setProperty("wks.persitent", "true");
		WDIR = d.getCanonicalPath().replace('\\', '/');
		regenerateWorkingDirectory(false);
	}

	public static final String LOC_STR_LEFT = "It represents the S4 Class";
	public static final String LOC_STR_RIGHT = "in R package";

	public static String getRClassForBean(JarFile jarFile, String beanClassName) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarFile.getEntry(beanClassName.replace('.', '/') + ".java"))));
		do {
			String line = br.readLine();
			if (line != null) {
				int p = line.indexOf(LOC_STR_LEFT);
				if (p != -1) {
					return line.substring(p + LOC_STR_LEFT.length(), line.indexOf(LOC_STR_RIGHT)).trim();
				}
			} else
				break;
		} while (true);
		return null;
	}

	public static void generateMaps(URL jarUrl, boolean rawClasses) {

		try {

			_mappingClassLoader = new URLClassLoader(new URL[] { jarUrl }, DirectJNI.class.getClassLoader());
			Vector<String> list = new Vector<String>();
			JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();
			JarFile jarfile = jarConnection.getJarFile();
			Enumeration<JarEntry> enu = jarfile.entries();
			while (enu.hasMoreElements()) {
				String entry = enu.nextElement().toString();
				if (entry.endsWith(".class"))
					list.add(entry.replace('/', '.').substring(0, entry.length() - ".class".length()));
			}

			log.info(list);

			for (int i = 0; i < list.size(); ++i) {
				String className = list.elementAt(i);
				if (className.startsWith("org.kchine.r.packages.") && !className.startsWith("org.kchine.r.packages.rservices")) {
					Class<?> c_ = _mappingClassLoader.loadClass(className);

					if (c_.getSuperclass() != null && c_.getSuperclass().equals(RObject.class) && !Modifier.isAbstract(c_.getModifiers())) {

						if (c_.equals(RLogical.class) || c_.equals(RInteger.class) || c_.equals(RNumeric.class) || c_.equals(RComplex.class)
								|| c_.equals(RChar.class) || c_.equals(RMatrix.class) || c_.equals(RArray.class) || c_.equals(RList.class)
								|| c_.equals(RDataFrame.class) || c_.equals(RFactor.class) || c_.equals(REnvironment.class) || c_.equals(RVector.class)
								|| c_.equals(RUnknown.class)) {
						} else {
							String rclass = DirectJNI.getRClassForBean(jarfile, className);
							_s4BeansHash.put(className, c_);
							_s4BeansMapping.put(rclass, className);
							_s4BeansMappingRevert.put(className, rclass);
						}

					} else if ((rawClasses && c_.getSuperclass() != null && c_.getSuperclass().equals(Object.class))
							|| (!rawClasses && RPackage.class.isAssignableFrom(c_) && (c_.isInterface()))) {

						String shortClassName = className.substring(className.lastIndexOf('.') + 1);
						_packageNames.add(shortClassName);

						Vector<Class<?>> v = _rPackageInterfacesHash.get(className);
						if (v == null) {
							v = new Vector<Class<?>>();
							_rPackageInterfacesHash.put(className, v);
						}
						v.add(c_);

					} else {
						String nameWithoutPackage = className.substring(className.lastIndexOf('.') + 1);
						if (nameWithoutPackage.indexOf("Factory") != -1 && c_.getMethod("setData", new Class[] { RObject.class }) != null) {
							// if
							// (DirectJNI._factoriesMapping.get(nameWithoutPackage
							// )
							// != null) throw new Exception("Factories Names
							// Conflict : two " + nameWithoutPackage);
							_factoriesMapping.put(nameWithoutPackage, className);
							if (Modifier.isAbstract(c_.getModifiers()))
								_abstractFactories.add(className);
						}
					}
				}
			}

			// log.info("s4Beans:" +s4Beans);
			log.info("rPackageInterfaces:" + _packageNames);
			log.info("s4Beans MAP :" + _s4BeansMapping);
			log.info("s4Beans Revert MAP :" + _s4BeansMappingRevert);
			log.info("factories :" + _factoriesMapping);
			log.info("r package interface hash :" + _rPackageInterfacesHash);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	synchronized public static void init(String instanceName) {
		setInstanceName(instanceName);
		scanMapping();
	}

	public static void init() {
		init((String) null);
	}

	private static void setInstanceName(String instanceName) {
		if (instanceName == null || instanceName.trim().equals("")) {
			instanceName = "LOCAL_R";
		}
		INSTANCE_NAME = instanceName.trim();
	}

	public static String clean(String str) {
		byte[] tab = str.getBytes();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tab.length; ++i) {
			if (tab[i] < 32 && tab[i] != (byte) '\n')
				sb.append(" ");
			else
				sb.append((char) tab[i]);
		}
		return sb.toString();
	}

	public HashMap<String, SpreadsheetModelRemoteImpl> getSpreadsheetTableModelRemoteHashMap() {
		return _spreadsheetTableModelRemoteHashMap;
	}

	public void notifyRActionListeners(final RConsoleAction action) {

		action.getAttributes().put("originatorUID", getOriginatorUID());
		if (_clientProperties != null)
			action.setClientProperties(_clientProperties);

		Vector<RConsoleActionListener> ractionListenersToRemove = new Vector<RConsoleActionListener>();
		for (int i = 0; i < _ractionListeners.size(); ++i) {
			try {
				_ractionListeners.elementAt(i).rConsoleActionPerformed(action);
			} catch (Exception e) {
				e.printStackTrace();
				ractionListenersToRemove.add(_ractionListeners.elementAt(i));
			}
		}
		_ractionListeners.removeAll(ractionListenersToRemove);
	}

	public void removeAllRActionListeners() {
		_ractionListeners.removeAllElements();
	}

	public void removeRActionListener(RConsoleActionListener ractionListener) {
		_ractionListeners.remove(ractionListener);
	}

	public void addRActionListener(RConsoleActionListener ractionListener) {
		_ractionListeners.add(ractionListener);
	}

	public String getOriginatorUID() {
		return _originatorUID;
	}

	public void setOrginatorUID(String uid) {
		_originatorUID = uid;
	}

	public void removeAllRCallbacks() {
		_callbacks.removeAllElements();
	}

	public void removeRCallback(RCallBack callback) {
		_callbacks.remove(callback);
	}

	public void addRCallback(RCallBack callback) {
		_callbacks.add(callback);
	}

	public Vector<RCallBack> getRCallBacks() {
		return _callbacks;
	}

	HashSet<String> getCairoCapabilities() {
		return _cairoCapabilities;
	}
}