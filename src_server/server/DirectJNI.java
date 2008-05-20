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

import graphics.pop.GDContainerBag;
import graphics.pop.GDDevice;
import graphics.rmi.DoublePoint;
import graphics.rmi.GraphicNotifier;
import graphics.rmi.JGDPanel;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
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
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import mapping.RPackage;
import mapping.ReferenceInterface;
import mapping.StandardReference;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.commons.logging.Log;
import org.bioconductor.packages.rservices.ObjectNameInterface;
import org.bioconductor.packages.rservices.RArray;
import org.bioconductor.packages.rservices.RArrayRef;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RCharRef;
import org.bioconductor.packages.rservices.RComplex;
import org.bioconductor.packages.rservices.RComplexRef;
import org.bioconductor.packages.rservices.RDataFrame;
import org.bioconductor.packages.rservices.RDataFrameRef;
import org.bioconductor.packages.rservices.REnvironment;
import org.bioconductor.packages.rservices.REnvironmentRef;
import org.bioconductor.packages.rservices.RFactor;
import org.bioconductor.packages.rservices.RFactorRef;
import org.bioconductor.packages.rservices.RInteger;
import org.bioconductor.packages.rservices.RIntegerRef;
import org.bioconductor.packages.rservices.RList;
import org.bioconductor.packages.rservices.RListRef;
import org.bioconductor.packages.rservices.RLogical;
import org.bioconductor.packages.rservices.RLogicalRef;
import org.bioconductor.packages.rservices.RMatrix;
import org.bioconductor.packages.rservices.RMatrixRef;
import org.bioconductor.packages.rservices.RNamedArgument;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.rservices.RNumericRef;
import org.bioconductor.packages.rservices.RObject;
import org.bioconductor.packages.rservices.RUnknown;
import org.bioconductor.packages.rservices.RVector;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.rosuda.JRI.RMainLoopCallbacks;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.RengineWrapper;
import org.rosuda.javaGD.GDInterface;
import org.rosuda.javaGD.GDObject;
import org.rosuda.javaGD.JavaGD;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import python.PythonInterpreterSingleton;
import remoting.AssignInterface;
import remoting.FileDescription;
import remoting.RAction;
import remoting.RCallback;
import remoting.RNI;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemoteLogListener;
import uk.ac.ebi.microarray.pools.RemotePanel;
import util.Utils;
import static server.RConst.*;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
	private static final String TAIL_PATTERN = "###> End of R Log";
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
	private String[] _packNames = null;
	private HashMap<String, Vector<String>> _nameSpacesHash = new HashMap<String, Vector<String>>();
	private HashMap<String, RPackage> _packs = new HashMap<String, RPackage>();
	private Vector<Long> _protectedExpReference = new Vector<Long>();
	private static Vector<String> demosList = null;
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(DirectJNI.class);
	private boolean _progrssiveConsoleFeedbackEnabled = false;
	private HashMap<String, byte[]> _resourceCache = new HashMap<String, byte[]>();

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
		if (Thread.currentThread() == _rEngine) {
			throw new RuntimeException("runR called from within the R MainLoop Thread");
		} else {
			_mainLock.lock();
			try {
				boolean hasConsoleInput = (eu.getConsoleInput() != null && !eu.getConsoleInput().equals(""));
				PrintStream _o = System.out;
				Boolean[] scanResultHolder = null;

				if (hasConsoleInput) {
					scanResultHolder = new Boolean[1];
					scanResultHolder[0] = false;
					System.setOut(new PrintStream(new ScanStream(_o, _continueStr.getBytes(), _promptStr.getBytes(), scanResultHolder)));
				}

				try {

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

					if (hasConsoleInput) {

						_runRlock.lock();
						try {
							_sharedExecutionUnit = new ExecutionUnit() {
								public void run(Rengine e) {
								}

								public boolean emptyConsoleBufferBefore() {
									return false;
								}

								public String getConsoleInput() {
									return "print('" + TAIL_PATTERN + "')";
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

						if (scanResultHolder[0] == true) {

							_runRlock.lock();
							try {
								_sharedExecutionUnit = new ExecutionUnit() {
									public void run(Rengine e) {
										_rEngine.rniStop(1);
									}

									public boolean emptyConsoleBufferBefore() {
										return false;
									}

									public String getConsoleInput() {
										return null;
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

							return "incomplete function call\n";
						}

						int p;
						while ((p = _sharedBuffer.indexOf(TAIL_PATTERN)) == -1) {

							try {
								Thread.sleep(10);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						return _sharedBuffer.substring(0, p - 5);

					} else {
						return _sharedBuffer.toString();
					}
				} finally {
					if (hasConsoleInput)
						System.setOut(_o);
				}
			} finally {
				_mainLock.unlock();
			}

		}
	}

	InputStream getResourceAsStream(String resource) {
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

		public void rBusy(Rengine re, int which) {
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
			if (_sharedExecutionUnit != null) {
				_runRlock.lock();
				try {
					if (_sharedExecutionUnit != null) {
						if (_sharedExecutionUnit.emptyConsoleBufferBefore())
							_sharedBuffer.setLength(0);
						if (_progrssiveConsoleFeedbackEnabled) {
							_rActions.add(new RAction("RESET_CONSOLE_LOG"));
						}

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
		}

		private void appendText(String t) {
			_sharedBuffer.append(t);
			if (_progrssiveConsoleFeedbackEnabled) {
				RAction consoleLogAppend = new RAction("APPEND_CONSOLE_LOG");
				HashMap<String, Object> attrs = new HashMap<String, Object>();
				attrs.put("log", t);
				consoleLogAppend.setAttributes(attrs);
				_rActions.add(consoleLogAppend);
			}
		}

		public void rWriteConsole(Rengine re, String text) {
			appendText(text);
		}

		public void rWriteConsole(Rengine re, String s, int i) {
			appendText(s);
		}

	}

	private DirectJNI() {
		_rEngine = new RengineWrapper(new String[] { "--no-save" }, true, new RMainLoopCallbacksImpl());

		if (!_rEngine.waitForR()) {
			log.info("Cannot load R");
			return;
		}

		try {
			String initRSourcingLog = getRServices().sourceFromResource("/rscripts/init.R");
			System.out.println("init.R sourcing log : " + initRSourcingLog);
			initPrivateEnv();
			_continueStr = ((RChar) ((RList) getRServices().getObject("options('continue')")).getValue()[0]).getValue()[0];
			_promptStr = ((RChar) ((RList) getRServices().getObject("options('prompt')")).getValue()[0]).getValue()[0];

			getRServices().consoleSubmit("1");
			_packNames = ((RChar) getRServices().getObject(".packages(all=T)")).getValue();

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

	public void addAction(RAction action) {
		_rActions.add(action);
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

	public void preprocessHelp() {
		for (int i = 0; i < _packNames.length; ++i) {
			try {

				String uriPrefix = "/library/" + _packNames[i] + "/html/";
				String indexFile = null;

				if (System.getenv().get("R_LIBS") != null && !System.getenv().get("R_LIBS").equals("")) {
					indexFile = System.getenv().get("R_LIBS") + "/" + _packNames[i] + "/html/" + "00Index.html";
					if (!new File(indexFile).exists()) {
						indexFile = null;
					} else {
						//System.out.println("index file:" + indexFile);
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
				processNode(_packNames[i], uriPrefix, p.extractAllNodesThatMatch(new TagNameFilter("BODY")).elementAt(0));

			} catch (Exception e) {
				// e.printStackTrace();
			}
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

	long putObject(Object obj) throws Exception {

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
					
					if (((ArrayList<?>)obj).size()>0) {
						Class<?> componentType=((ArrayList<?>)obj).get(0).getClass();
						if (componentType==Integer.class)
							obj = getRArrayFromJavaArray( (Integer[])((ArrayList<?>)obj).toArray(new Integer[0]) );
						else if (obj instanceof Long)
							obj = getRArrayFromJavaArray( (Long[])((ArrayList<?>)obj).toArray(new Long[0]) );
						else if (obj instanceof String)
							obj = getRArrayFromJavaArray( (String[])((ArrayList<?>)obj).toArray(new String[0]) );
						else if (obj instanceof Double)
							obj = getRArrayFromJavaArray( (Double[])((ArrayList<?>)obj).toArray(new Double[0]) );
						else if (obj instanceof Float)
							obj = getRArrayFromJavaArray( (Float[])((ArrayList<?>)obj).toArray(new Float[0]) );
						else if (obj instanceof Boolean)
							obj = getRArrayFromJavaArray( (Boolean[])((ArrayList<?>)obj).toArray(new Boolean[0]) );
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
					obj=getRArrayFromJavaArray(obj);
					
					//throw new Exception("argument classe must be a subclass of RObject or Standard Java Types");
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
		//System.out.println("putObjectAndAssignName called, obj:" + obj);
		long resultId = putObject(obj);
		//System.out.println("Result id=" + resultId);
		_rEngine.rniAssign(name, resultId, (privateEnv ? _privateEnvExp : 0));
	}

	private RObject getObjectFrom(String expression, String rclass) throws NoMappingAvailable, Exception {
		// log.info(".... quering for =" + expression + " rclass="+rclass);
		Rengine e = _rEngine;
		long expressionId = e.rniEval(e.rniParse(expression, 1), 0);
		RObject result = null;
		String typeStr = null;
		int rmode = e.rniExpType(expressionId);
		boolean isVirtual = e.rniGetBoolArrayI(e.rniEval(e.rniParse("isVirtualClass(\"" + rclass + "\")", 1), 0))[0] == 1;
		if (isVirtual && !rclass.equals("vector")) {

			String unionrclass = e.rniGetString(e.rniEval(e.rniParse("class(" + expression + ")", 1), 0));
			// log.info(">>> union r class=" + unionrclass );
			RObject o = getObjectFrom(expression, unionrclass);

			if (e.rniExpType(expressionId) != S4SXP) {
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
			throw new RuntimeException(util.Utils.getStackTraceAsString(e));
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

				if (arg instanceof mapping.ReferenceInterface) {

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

	RObject getObjectFromReference(final ReferenceInterface refObj) throws Exception {
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

	public String guessJavaClassRef(String rclass, int rtype) {
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
				else {
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
				String javaClassName = guessJavaClassRef(rclass, resultType);
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
		return guessJavaClassRef(rclass, symbolType) != null;
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

	public String sourceFromBuffer(StringBuffer buffer) {
		try {
			File tempFile = PoolUtils.createFileFromBuffer(null,buffer);
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
		for (int i = 0; i < devices.getValue().length; ++i) {
			if (devices.getNames()[i].equals(deviceType)) {
				getRServices().getObject(".PrivateEnv$dev.off(" + devices.getValue()[i] + ")");
			}
		}
	}

	Integer getDevice(String deviceType) throws RemoteException {
		RInteger devices = (RInteger) getRServices().getObject(".PrivateEnv$dev.list()");
		for (int i = 0; i < devices.getValue().length; ++i) {
			if (devices.getNames()[i].equals(deviceType)) {
				return devices.getValue()[i];
			}
		}
		return null;
	}

	String newTemporaryVariableName() {
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

	public static void generateMaps(URL jarUrl) {
		DirectJNI.generateMaps(jarUrl, false);
	}

	
	public static Object getJavaArrayFromRArray__(RArray array) {
		int[] dim=array.getDim();
		
		RVector vector = array.getValue();
		Class<?> componentType=null;
		if (vector instanceof RInteger) componentType=int.class;
		else if (vector instanceof RNumeric) componentType=double.class;
		else if (vector instanceof RChar) componentType=String.class;
		else if (vector instanceof RLogical) componentType=boolean.class;
				
	    Object result=null;
	    try {result=Array.newInstance( componentType , dim);} catch (Exception e) {e.printStackTrace();}				
		Vector<Integer> v=new Vector<Integer>();
		int p=1;for (int i=dim.length-1; i>0;--i) {	p=p*dim[i];	v.add(0,p);	}
		
		for ( int bi=0; bi<p*dim[0]; ++bi) {
			int bindex=bi;
			int[] indexes=new int[dim.length];
			for (int i=0; i<indexes.length-1;++i) {
				indexes[i]=bindex / v.elementAt(i); 
				bindex=bindex % v.elementAt(i); 
			}
			indexes[indexes.length-1]=bindex;
		
			Object arrayTail=null;		
			if (dim.length==1) {
				arrayTail=result;
			} else {
				arrayTail=Array.get(result, indexes[0]);
				for (int i=1; i<indexes.length-1;++i) arrayTail=Array.get(arrayTail, indexes[i] );
			}				
			if (vector instanceof RInteger) Array.setInt(arrayTail, indexes[indexes.length-1], ((RInteger)vector).getValue()[bi] );
			else if (vector instanceof RNumeric) Array.setDouble(arrayTail, indexes[indexes.length-1], ((RNumeric)vector).getValue()[bi] );
			else if (vector instanceof RChar) Array.set(arrayTail, indexes[indexes.length-1], ((RChar)vector).getValue()[bi] );
			else if (vector instanceof RLogical) Array.setBoolean(arrayTail, indexes[indexes.length-1], ((RLogical)vector).getValue()[bi] );
		}
		
		return result;
	}

	
	public static Object getJavaArrayFromRArray(RArray array) {
		int[] dim=array.getDim();
		
		RVector vector = array.getValue();
		Class<?> componentType=null;
		if (vector instanceof RInteger) componentType=int.class;
		else if (vector instanceof RNumeric) componentType=double.class;
		else if (vector instanceof RChar) componentType=String.class;
		else if (vector instanceof RLogical) componentType=boolean.class;
				
	    Object result=null;
	    try {result=Array.newInstance( componentType , dim);} catch (Exception e) {e.printStackTrace();}				
		
	    Vector<Integer> v1=new Vector<Integer>();
		int p1=1;for (int i=dim.length-1; i>0;--i) {p1=p1*dim[i];v1.add(0,p1);}
	    Vector<Integer> v2=new Vector<Integer>();
		int p2=1;for (int i=0 ; i<dim.length-1; ++i) {p2=p2*dim[i];v2.add(0,p2);}
		
		for ( int bi=0; bi<p1*dim[0]; ++bi) {
			int bindex=bi;
			int[] indexes=new int[dim.length];
			for (int i=0; i<indexes.length-1;++i) {
				indexes[i]=bindex / v1.elementAt(i); 
				bindex=bindex % v1.elementAt(i); 
			}
			indexes[indexes.length-1]=bindex;
		
			Object arrayTail=null;		
			if (dim.length==1) {
				arrayTail=result;
			} else {
				arrayTail=Array.get(result, indexes[0]);
				for (int i=1; i<indexes.length-1;++i) arrayTail=Array.get(arrayTail, indexes[i] );
			}				
			
			int linearVectorIndex=0;
			for (int i=(indexes.length-1); i>0; --i) linearVectorIndex+=indexes[i]*v2.elementAt((indexes.length-1)-i);
			linearVectorIndex+=indexes[0];
			//System.out.println("linearVectorIndex:"+linearVectorIndex);
			
			if (vector instanceof RInteger) Array.setInt(arrayTail, indexes[indexes.length-1], ((RInteger)vector).getValue()[linearVectorIndex] );
			else if (vector instanceof RNumeric) Array.setDouble(arrayTail, indexes[indexes.length-1], ((RNumeric)vector).getValue()[linearVectorIndex] );
			else if (vector instanceof RChar) Array.set(arrayTail, indexes[indexes.length-1], ((RChar)vector).getValue()[linearVectorIndex] );
			else if (vector instanceof RLogical) Array.setBoolean(arrayTail, indexes[indexes.length-1], ((RLogical)vector).getValue()[linearVectorIndex] );
		}
		
		return result;
	}
	
	
	public static int[] getJavaArrayDimensions(Object table, Class<?>[] classHolder, int[] lengthHolder) {
		Vector<Integer> dimV=new Vector<Integer>();
		Object obj=table;
		while(Array.get(obj, 0).getClass().isArray()) {
			dimV.add(Array.getLength(obj));	
			obj=Array.get(obj, 0);
		}
		dimV.add(Array.getLength(obj));
		classHolder[0]=Array.get(obj, 0).getClass();
		
		int[] result=new int[dimV.size()];
		lengthHolder[0]=1;
		for (int i=0; i<dimV.size(); ++i) {
			result[i]=dimV.elementAt(i);
			lengthHolder[0]=lengthHolder[0]*result[i];
		}
		return result;
	}
	
	public static RArray getRArrayFromJavaArray(Object javaArray) {
		Class<?>[] classHolder=new Class<?>[1];
		int[] lengthHolder=new int[1];
		
		int[] dim=getJavaArrayDimensions(javaArray, classHolder, lengthHolder);
		RVector vector=null;
		
		Class<?> componentType=classHolder[0];		
		if (componentType==Integer.class || componentType==int.class) vector=new RInteger(new int[lengthHolder[0]]);
		else if (componentType==Double.class || componentType==double.class) vector=new RNumeric(new double[lengthHolder[0]]);
		else if (componentType==Boolean.class || componentType==boolean.class) vector=new RLogical(new boolean[lengthHolder[0]]);
		else if (componentType==String.class) vector=new RChar(new String[lengthHolder[0]]);
		else throw new RuntimeException("unsupported elements class type :"+componentType);
				
	    Vector<Integer> v1=new Vector<Integer>();
		int p1=1;for (int i=dim.length-1; i>0;--i) {p1=p1*dim[i];v1.add(0,p1);}
	    Vector<Integer> v2=new Vector<Integer>();
		int p2=1;for (int i=0 ; i<dim.length-1; ++i) {p2=p2*dim[i];v2.add(0,p2);}
		
		for ( int bi=0; bi<p1*dim[0]; ++bi) {
			int bindex=bi;
			int[] indexes=new int[dim.length];
			for (int i=0; i<indexes.length-1;++i) {
				indexes[i]=bindex / v1.elementAt(i); 
				bindex=bindex % v1.elementAt(i); 
			}
			indexes[indexes.length-1]=bindex;
		
			Object arrayTail=null;		
			if (dim.length==1) {
				arrayTail=javaArray;
			} else {
				arrayTail=Array.get(javaArray, indexes[0]);
				for (int i=1; i<indexes.length-1;++i) arrayTail=Array.get(arrayTail, indexes[i] );
			}				
			
			int linearVectorIndex=0;
			for (int i=(indexes.length-1); i>0; --i) linearVectorIndex+=indexes[i]*v2.elementAt((indexes.length-1)-i);
			linearVectorIndex+=indexes[0];
			//System.out.println("linearVectorIndex:"+linearVectorIndex);
			
			if (vector instanceof RInteger) ((RInteger)vector).getValue()[linearVectorIndex]=(Integer)Array.get(arrayTail, indexes[indexes.length-1]) ;
			else if (vector instanceof RNumeric) ((RNumeric)vector).getValue()[linearVectorIndex]=(Double)Array.get(arrayTail, indexes[indexes.length-1]) ;
			else if (vector instanceof RChar) ((RChar)vector).getValue()[linearVectorIndex] = (String)Array.get(arrayTail, indexes[indexes.length-1]);
			else if (vector instanceof RLogical) ((RLogical)vector).getValue()[linearVectorIndex]=(Boolean)Array.get(arrayTail, indexes[indexes.length-1]);
		}
		
		return new RArray(vector,dim,null);
	}

	

	private Object convert(RObject obj) {
		//System.out.println("obj:" + obj);
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
		} else if (result instanceof RArray){
			result=getJavaArrayFromRArray((RArray)result);			
		}
		return result;
	}

	private RServices _rServices = new RServices() {

		private String _lastStatus = null;

		public String getStatus() {
			return clean(_lastStatus);
		}

		public String evaluate(final String expression, final int n) throws RemoteException {
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						e.rniEval(e.rniParse(expression, n), 0);
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
			return clean(_lastStatus);
		}

		public String sourceFromResource(final String resource) throws RemoteException {
			final Exception[] exceptionHolder = new Exception[1];
			runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						_lastStatus = DirectJNI.this.sourceFromResource(resource);
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
			return clean(_lastStatus);
		}

		public String sourceFromBuffer(final StringBuffer buffer) throws RemoteException {
			final Exception[] exceptionHolder = new Exception[1];
			runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						_lastStatus = DirectJNI.this.sourceFromBuffer(buffer);
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
			return clean(_lastStatus);
		}

		public String print(String expression) throws RemoteException {
			return printExpressions(new String[] { expression });
		}

		public String printExpressions(final String[] expressions) throws RemoteException {

			final String[] stringHolder = new String[1];
			stringHolder[0] = "";
			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new server.ExecutionUnit() {
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
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(false, null, methodName, args);
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

		public void callAndAssign(final String varName, final String methodName, final Object... args) throws RemoteException {
			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						DirectJNI.this.call(false, varName, methodName, args);

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

		public RObject callAndGetReference(final String methodName, final Object... args) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(true, null, methodName, args);
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
		
		public RObject callAndGetObjectName(final String methodName,final Object... args) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(true, null, methodName, args);
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
				String refClassName=objHolder[0].getClass().getName();
				ObjectNameInterface objectName=(ObjectNameInterface)_mappingClassLoader.loadClass(refClassName.substring(0, refClassName.length()-"Ref".length())+"ObjectName").newInstance();
				objectName.setRObjectName(PROTECT_VAR_PREFIXE +((ReferenceInterface)objHolder[0]).getRObjectId());
				objectName.setRObjectEnvironment(PENV);
				return (RObject)objectName;
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}
		}

		public Object callAndConvert(final String methodName, final Object... args) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.call(false, null, methodName, args);
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

		public void freeReference(final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an object reference");
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
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
			_lastStatus = runR(new server.ExecutionUnit() {
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
				String refClassName=objHolder[0].getClass().getName();
				ObjectNameInterface objectName=(ObjectNameInterface)_mappingClassLoader.loadClass(refClassName.substring(0, refClassName.length()-"Ref".length())+"ObjectName").newInstance();
				objectName.setRObjectName(PROTECT_VAR_PREFIXE +((ReferenceInterface)objHolder[0]).getRObjectId());
				objectName.setRObjectEnvironment(PENV);
				return (RObject)objectName;
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
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						robjHolder[0] = DirectJNI.this.getObjectFrom(((ObjectNameInterface)objectName).getRObjectEnvironment()+"$"+((ObjectNameInterface)objectName).getRObjectName());
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
			return (RObject)realizeObjectName(objectName,false);
		}
		
		public Object realizeObjectNameConverted(RObject objectName) throws RemoteException {
			return realizeObjectName(objectName,true);
		}
		
		public RObject referenceToObject(final RObject refObj) throws RemoteException {
			if (!(refObj instanceof ReferenceInterface))
				throw new RemoteException("not an object reference");
			final RObject[] robjHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
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
			final RObject[] refHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						refHolder[0] = (RObject) DirectJNI.this.putObjectAndGetReference(obj);
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
			return refHolder[0];
		}

		public void putAndAssign(final Object obj, final String name) throws RemoteException {
			final Exception[] exceptionHolder = new Exception[1];

			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						long resultId = putObject(obj);
						e.rniAssign(name, resultId, 0);
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

		public RObject getObject(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, false);
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
			_lastStatus = runR(new server.ExecutionUnit() {
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
			return objHolder[0];
		}

		public Object getObjectConverted(final String expression) throws RemoteException {
			final RObject[] objHolder = new RObject[1];
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
					try {
						objHolder[0] = DirectJNI.this.evalAndGetObject(expression, false);
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
			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
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

		public void setCallBack(RCallback callback) throws RemoteException {
			RListener.setCallbackInterface(callback);
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

		public void stop() throws RemoteException {
			_rEngine.rniStop(1);
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

			log.info("submit : " + cmd);

			final Exception[] exceptionHolder = new Exception[1];
			_lastStatus = runR(new server.ExecutionUnit() {
				public void run(Rengine e) {
				}

				public String getConsoleInput() {
					if (cmd.startsWith("?")) {
						return "help(" + cmd.substring(1) + ")";
					} else {
						return cmd;
					}
				}
			});
			if (exceptionHolder[0] != null) {
				if (exceptionHolder[0] instanceof RemoteException) {
					throw (RemoteException) exceptionHolder[0];
				} else {
					throw new RemoteException("Exception Holder", (Throwable) exceptionHolder[0]);
				}
			}

			return clean(_lastStatus);

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
			_lastStatus = runR(new server.ExecutionUnit() {
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
			return new JGDPanel(w, h, gn);
		}

		public GDDevice newDevice(int w, int h) throws RemoteException {
			return new GDDeviceLocal(w, h);
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
					props.loadFromXML(getResourceAsStream("/rdemos/list.properties"));
					for (Object key : PoolUtils.orderO(props.keySet())) {
						demosList.add(props.getProperty((String) key));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			return demosList.toArray(new String[0]);

		}

		public StringBuffer getDemoSource(String demoName) throws RemoteException {
			if (!resourceExists("/rdemos/" + demoName + ".r")) {
				throw new RemoteException("no demo with name <" + demoName + ">");
			} else {
				try {
					StringBuffer result = new StringBuffer();
					BufferedReader br = new BufferedReader(new InputStreamReader(getResourceAsStream("/rdemos/" + demoName + ".r")));
					String line = null;
					while ((line = br.readLine()) != null) {
						result.append(line + "\n");
					}
					return result;
				} catch (Exception e) {
					throw new RemoteException("", e);
				}
			}
		}

		public void setProgressiveConsoleLogEnabled(boolean progressiveLog) throws RemoteException {
			_progrssiveConsoleFeedbackEnabled = progressiveLog;
		}

		public boolean isProgressiveConsoleLogEnabled() throws RemoteException {
			return _progrssiveConsoleFeedbackEnabled;
		}

		public Vector<RAction> popRActions() throws RemoteException {
			if (_rActions.size() == 0)
				return null;
			Vector<RAction> result = (Vector<RAction>) _rActions.clone();
			for (int i = 0; i < result.size(); ++i)
				_rActions.remove(0);
			return result;
		}

		public String getProcessId() throws RemoteException {
			return PoolUtils.getProcessId();
		}

		public String getHostIp() throws RemoteException {
			return PoolUtils.getHostIp();
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

		public Vector<String> getSvg(String expression, int width, int height) throws RemoteException {

			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + ".svg").getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}

			DirectJNI.getInstance().getRServices().evaluate("library(Cairo)");
			String loadStatus = DirectJNI.getInstance().getRServices().getStatus();

			if (!loadStatus.equals("")) {

				DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
				String svgNS = "http://www.w3.org/2000/svg";
				Document document = domImpl.createDocument(svgNS, "svg", null);
				SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

				Dimension dSize = new Dimension(width, height);
				GDDevice device = null;
				try {
					device = DirectJNI.getInstance().getRServices().newDevice(dSize.width, dSize.height);
					DirectJNI.getInstance().getRServices().sourceFromBuffer(new StringBuffer(expression));
					if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
						log.info(DirectJNI.getInstance().getRServices().getStatus());
					}
					Vector<GDObject> g2dObjects = device.popAllGraphicObjects();
					if (g2dObjects.size() == 0)
						throw new RemoteException(DirectJNI.getInstance().getRServices().getStatus());
					Java2DUtils.paintAll(svgGenerator, new Point(0, 0), dSize, g2dObjects);
				} catch (Exception e) {
					throw new RemoteException("", e);
				} finally {
					device.dispose();
				}

				try {
					svgGenerator.setSVGCanvasSize(dSize);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					boolean useCSS = true;
					Writer out = new OutputStreamWriter(baos, "UTF-8");
					svgGenerator.stream(out, useCSS);
					out.close();

					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document doc = builder.parse(new ByteArrayInputStream(baos.toByteArray()));
					((Element) doc.getElementsByTagName("svg").item(0)).setAttribute("viewBox", "0,0," + dSize.width + "," + dSize.height);

					Writer stringW = new StringWriter();
					DOMSource source = new DOMSource(doc);
					TransformerFactory transFactory = TransformerFactory.newInstance();
					Transformer transformer = transFactory.newTransformer();
					transformer.transform(source, new StreamResult(stringW));

					Vector<String> result = new Vector<String>();
					BufferedReader br = new BufferedReader(new StringReader(stringW.toString()));
					String line = null;
					while ((line = br.readLine()) != null)
						result.add(line);

					return result;
				} catch (Exception e) {
					throw new RemoteException("", e);
				}

			} else {
				final StringBuffer command = new StringBuffer("CairoSVG(file = \"" + tempFile.getAbsolutePath().replace('\\', '/') + "\", width = "
						+ new Double(10 * (width / height)) + ", height = " + 10 + " , onefile = TRUE, bg = \"transparent\" ,pointsize = 12);" + expression);
				DirectJNI.getInstance().getRServices().sourceFromBuffer(command);

				if (!_lastStatus.equals("")) {
					log.info(_lastStatus);
				}

				if (tempFile.exists()) {
					Vector<String> result = null;
					try {

						DirectJNI.getInstance().shutdownDevices("Cairo");

						result = new Vector<String>();
						BufferedReader br = new BufferedReader(new FileReader(tempFile));
						String line = null;
						while ((line = br.readLine()) != null) {
							result.add(line);
						}
						br.close();
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
		}

		public String getPythonStatus() throws RemoteException {
			return PythonInterpreterSingleton.getPythonStatus();

		}

		public String pythonExceFromResource(String resource) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
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

		public String pythonExecFromBuffer(StringBuffer buffer) throws RemoteException {
			File f = null;
			try {
				f = PoolUtils.createFileFromBuffer(null,buffer);
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
			// TODO Auto-generated method stub
			return null;
		}

		public Object pythonEvalAndConvert(String pythonCommand) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public RObject pythonGet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public Object pythonGetAndConvert(String name) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void pythonSet(String name, Object Value) throws RemoteException {
			// TODO Auto-generated method stub

		}

		public Object groovyEval(String expression) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public String groovyExecFromResource(String resource) throws RemoteException {
			return null;
		}

		public String groovyExec(String groovyCommand) throws RemoteException {
			try {
				return groovy.GroovyInterpreterSingleton.getInstance().exec(groovyCommand);
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public String groovyExecFromBuffer(StringBuffer buffer) throws RemoteException {
			File f = null;
			try {
				return groovy.GroovyInterpreterSingleton.getInstance().execFromBuffer(buffer);
			} catch (Exception e) {
				throw new RemoteException("", e);
			} finally {
				if (f != null)
					f.delete();
			}
		}

		public String groovyExecFromWorkingDirectoryFile(String fileName) throws RemoteException {
			try {
				return groovy.GroovyInterpreterSingleton.getInstance().execFromFile(new File(WDIR + "/" + fileName));
			} catch (Exception e) {
				throw new RemoteException("", e);
			}
		}

		public Object groovyGet(String name) throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}

		public void groovySet(String name, Object Value) throws RemoteException {
			// TODO Auto-generated method stub

		}

		public boolean isGroovyEnabled() throws RemoteException {
			return groovy.GroovyInterpreterSingleton.getInstance() != null;
		}
		
		public String getGroovyStatus() throws RemoteException {
			return null;
		}

	};

	public static Vector<RAction> _rActions = new Vector<RAction>();

	private static HashMap<Integer, GDDevice> _localDeviceHashMap = new HashMap<Integer, GDDevice>();

	public static class GDDeviceLocal implements GDDevice {
		GDContainerBag gdBag = null;

		public GDDeviceLocal(int w, int h) throws RemoteException {
			gdBag = new GDContainerBag(w, h);
			JavaGD.setGDContainer(gdBag);
			Dimension dim = gdBag.getSize();

			RInteger devicesBefore = (RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.list()");
			Vector<Integer> devicesVector = new Vector<Integer>();
			if (devicesBefore != null) {
				for (int i = 0; i < devicesBefore.getValue().length; ++i)
					devicesVector.add(devicesBefore.getValue()[i]);
			}
			System.out.println("devices before :" + devicesBefore);

			System.out.println(DirectJNI.getInstance().getRServices().evaluate(
					"JavaGD(name='JavaGD', width=" + dim.getWidth() + ", height=" + dim.getHeight() + ", ps=12)"));

			RInteger devicesAfter = (RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.list()");
			for (int i = 0; i < devicesAfter.getValue().length; ++i)
				if (!devicesVector.contains(devicesAfter.getValue()[i])) {
					System.out.println("caught:" + devicesAfter.getValue()[i]);
					gdBag.setDeviceNumber(devicesAfter.getValue()[i]);
					break;
				}

			System.out.println(DirectJNI.getInstance().getRServices().consoleSubmit(".PrivateEnv$dev.list()"));

			_localDeviceHashMap.put(gdBag.getDeviceNumber(), this);

		}

		public Vector<org.rosuda.javaGD.GDObject> popAllGraphicObjects() throws RemoteException {
			return gdBag.popAllGraphicObjects();
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
			DirectJNI.getInstance().getRServices().consoleSubmit("1");
		};

		public void dispose() throws RemoteException {
			_localDeviceHashMap.remove(gdBag.getDeviceNumber());
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
			return gdBag.getSize();
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

		synchronized public Vector<String> getSVG() throws RemoteException {

			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + ".svg").getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}

			DirectJNI.getInstance().getRServices().evaluate("library(Cairo)");
			String loadStatus = DirectJNI.getInstance().getRServices().getStatus();

			if (!loadStatus.equals("")) {

				DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
				String svgNS = "http://www.w3.org/2000/svg";
				Document document = domImpl.createDocument(svgNS, "svg", null);
				SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

				Dimension dSize = getSize();

				GDDevice device = null;
				try {
					int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];
					device = DirectJNI.getInstance().getRServices().newDevice(dSize.width, dSize.height);
					DirectJNI.getInstance().getRServices().evaluate(
							".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + device.getDeviceNumber() + ");"
									+ ".PrivateEnv$dev.set(" + currentDevice + ");", 3);

					Java2DUtils.paintAll(svgGenerator, new Point(0, 0), dSize, device.popAllGraphicObjects());
				} catch (Exception e) {
					throw new RemoteException("", e);
				} finally {
					device.dispose();
				}

				try {
					svgGenerator.setSVGCanvasSize(dSize);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					boolean useCSS = true;
					Writer out = new OutputStreamWriter(baos, "UTF-8");
					svgGenerator.stream(out, useCSS);
					out.close();

					DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document doc = builder.parse(new ByteArrayInputStream(baos.toByteArray()));
					((Element) doc.getElementsByTagName("svg").item(0)).setAttribute("viewBox", "0,0," + dSize.width + "," + dSize.height);

					Writer stringW = new StringWriter();
					DOMSource source = new DOMSource(doc);
					TransformerFactory transFactory = TransformerFactory.newInstance();
					Transformer transformer = transFactory.newTransformer();
					transformer.transform(source, new StreamResult(stringW));

					Vector<String> result = new Vector<String>();
					BufferedReader br = new BufferedReader(new StringReader(stringW.toString()));
					String line = null;
					while ((line = br.readLine()) != null)
						result.add(line);

					return result;
				} catch (Exception e) {
					throw new RemoteException("", e);
				}
			} else {

				int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];

				DirectJNI.getInstance().shutdownDevices("Cairo");

				final String createDeviceCommand = "CairoSVG(file = \"" + tempFile.getAbsolutePath().replace('\\', '/') + "\", width = "
						+ new Double(10 * (getSize().width / getSize().height)) + ", height = " + 10
						+ " , onefile = TRUE, bg = \"transparent\" ,pointsize = 12)";
				DirectJNI.getInstance().getRServices().evaluate(createDeviceCommand);
				if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
					log.info(DirectJNI.getInstance().getRServices().getStatus());
				}

				int cairoDevice = DirectJNI.getInstance().getDevice("Cairo");
				DirectJNI.getInstance().getRServices().evaluate(
						".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + cairoDevice + ");" + ".PrivateEnv$dev.set("
								+ currentDevice + ");", 3);

				if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
					log.info(DirectJNI.getInstance().getRServices().getStatus());
				}

				if (tempFile.exists()) {

					Vector<String> result = null;
					try {

						DirectJNI.getInstance().shutdownDevices("Cairo");

						result = new Vector<String>();
						BufferedReader br = new BufferedReader(new FileReader(tempFile));
						String line = null;
						while ((line = br.readLine()) != null) {
							result.add(line);
						}
						br.close();
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
		}

		public byte[] getBmp() throws RemoteException {
			return null;
		}

		public byte[] getJpg() throws RemoteException {
			return null;
		}

		public byte[] getPdf() throws RemoteException {
			File tempFile = null;
			try {
				tempFile = new File(TEMP_DIR + "/temp" + System.currentTimeMillis() + ".pdf").getCanonicalFile();
				if (tempFile.exists())
					tempFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RemoteException("", e);
			}

			int currentDevice = ((RInteger) DirectJNI.getInstance().getRServices().getObject(".PrivateEnv$dev.cur()")).getValue()[0];

			DirectJNI.getInstance().shutdownDevices("pdf");

			final String createDeviceCommand = "pdf(file = \"" + tempFile.getAbsolutePath().replace('\\', '/') + "\", width = "
					+ new Double(6 * (getSize().width / getSize().height)) + ", height = " + 6
					+ " , onefile = TRUE, title = '', fonts = NULL, version = '1.1' )";
			DirectJNI.getInstance().getRServices().evaluate(createDeviceCommand);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}

			int pdfDevice = DirectJNI.getInstance().getDevice("pdf");
			DirectJNI.getInstance().getRServices().evaluate(
					".PrivateEnv$dev.set(" + gdBag.getDeviceNumber() + ");" + ".PrivateEnv$dev.copy(which=" + pdfDevice + ");" + ".PrivateEnv$dev.set("
							+ currentDevice + ");", 3);
			if (!DirectJNI.getInstance().getRServices().getStatus().equals("")) {
				log.info(DirectJNI.getInstance().getRServices().getStatus());
			}

			if (tempFile.exists()) {

				byte[] result = null;
				try {

					DirectJNI.getInstance().shutdownDevices("pdf");

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
			return null;
		}

		public byte[] getPng() throws RemoteException {
			return null;
		}

		public byte[] getPostScript() throws RemoteException {
			return null;
		}

		public byte[] getXfig() throws RemoteException {
			return null;
		}

	}

	private GraphicNotifier gn = new LocalGraphicNotifier();

	public GraphicNotifier getGraphicNotifier() {
		return gn;
	}

	private static boolean _initHasBeenCalled = false;

	public void initPrivateEnv() {
		runR(new server.ExecutionUnit() {
			public void run(Rengine e) {
				try {
					_privateEnvExp = e.rniEval(e.rniParse(PENV, 1), 0);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
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
				if (className.startsWith("org.bioconductor.packages.") && !className.startsWith("org.bioconductor.packages.rservices")) {
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
							// (DirectJNI._factoriesMapping.get(nameWithoutPackage)
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

}