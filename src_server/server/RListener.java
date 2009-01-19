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
package server;

import graphics.rmi.RClustserInterface;
import http.RHttpProxy;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.JarURLConnection;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.logging.Log;
import org.kchine.r.RArray;
import org.kchine.r.RChar;
import org.kchine.r.RComplex;
import org.kchine.r.RInteger;
import org.kchine.r.RList;
import org.kchine.r.RLogical;
import org.kchine.r.RMatrix;
import org.kchine.r.RNumeric;
import org.kchine.r.RObject;
import org.kchine.r.RVector;
import org.kchine.r.server.LocalHttpServer;
import org.kchine.r.server.RConsoleAction;
import org.kchine.r.server.RServices;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.r.server.scripting.PythonInterpreterSingleton;
import org.kchine.r.server.spreadsheet.CellRange;
import org.kchine.r.server.spreadsheet.ExportInfo;
import org.kchine.r.server.spreadsheet.ImportInfo;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemote;
import org.kchine.r.server.spreadsheet.SpreadsheetTableModelClipboardInterface;
import org.kchine.rpf.NodeManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServantProviderFactory;
import org.kchine.rpf.ServerDefaults;
import org.python.core.PyComplex;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyObject;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public abstract class RListener {

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(RListener.class);

	private static RClustserInterface _rClusterInterface = new RClustserInterface() {
		public Vector<RServices> createRs(int n, String nodeName) throws Exception {
			Vector<RServices> workers = null;

			try {
				ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

				if (spFactory == null) {
					throw new Exception("no registry");
				}

				Registry registry = spFactory.getServantProvider().getRegistry();
				NodeManager nm = null;
				try {
					nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name") + "_" + nodeName);
				} catch (NotBoundException nbe) {
					nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
				} catch (Exception e) {
					throw new Exception("no node manager");
				}

				for (int i = 0; i < n; ++i) {
					RServices r = null;

					r = (RServices) nm.createPrivateServant(nodeName);

					if (r == null) {
						throw new Exception("not enough number of servants available");
					}

					workers.add(r);
				}

				for (int i = 0; i < n; ++i) {
					RServices r = null;

					r = (RServices) nm.createPrivateServant(nodeName);

					if (r == null) {
						throw new Exception("not enough number of servants available");
					}

					workers.add(r);
				}
				return workers;
			} catch (Exception e) {
				if (workers.size() > 0) {
					for (int i = 0; i < workers.size(); ++i) {
						try {
							ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(workers.elementAt(i));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
				throw e;

			}
		}

		public void releaseRs(Vector<RServices> rs, int n, String nodeName) throws Exception {
			try {
				Registry registry = ServantProviderFactory.getFactory().getServantProvider().getRegistry();
				NodeManager nm = null;
				try {
					nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name") + "_" + nodeName);
				} catch (NotBoundException nbe) {
					nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
				} catch (Exception e) {
					throw new Exception("no node manager");
				}

				for (int i = 0; i < n; ++i) {
					try {
						nm.kill(rs.elementAt(i));
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	};

	public static void setRClusterInterface(RClustserInterface rClusterInterface) {
		_rClusterInterface = rClusterInterface;
	}

	public static void notifyJavaListeners(String parametersStr) {
		HashMap<String, String> parameters = PoolUtils.getParameters(parametersStr);
		for (int i = 0; i < DirectJNI.getInstance().getRCallBacks().size(); ++i) {
			try {
				DirectJNI.getInstance().getRCallBacks().elementAt(i).notify(parameters);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String[] loadLightPack(String packName) {
		if (RListener.class.getResource("/monoscriptpackage/" + packName + ".r") == null) {
			return new String[] { "NOK", convertToPrintCommand("no light package with name :" + packName) };
		} else {
			try {
				DirectJNI.getInstance().sourceFromResource("/monoscriptpackage/" + packName + ".r");
				return new String[] { "OK", convertToPrintCommand(packName + " loaded successfully") };
			} catch (Exception e) {
				return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
			}
		}

	}

	public static String[] reinitWorkingDirectory(String dir) {
		try {
			DirectJNI.getInstance().reinitWorkingDirectory(dir);
			return new String[] { "OK", convertToPrintCommand("working directory was set successfully to " + dir) };
		} catch (Exception e) {
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] _forbiddenSymbols = null;;

	public static String[] forbiddenSymbols(String voidStr) {
		if (_forbiddenSymbols == null) {
			Vector<String> v = new Vector<String>();
			v.addAll(DirectJNI.getInstance().getBootStrapRObjects());
			v.add("ls");
			v.add("objects");
			v.add("q");
			v.add("win.graph");
			v.add("x11");
			v.add("X11");
			v.add("dev.off");
			v.add("graphics.off");
			v.add("dev.set");
			v.add("help");
			v.add("setwd");
			_forbiddenSymbols = (String[]) v.toArray(new String[0]);
		}
		return _forbiddenSymbols;
	}

	public static String[] help(String topic, String pack, String[] libLoc) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		if (pack != null && pack.equals("NA"))
			pack = null;
		attributes.put("topic", topic);
		attributes.put("package", pack);
		DirectJNI.getInstance().notifyRActionListeners(new RConsoleAction("help", attributes));
		return null;
	}

	public static String[] q(String save, String status, String runLast) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();
		attributes.put("save", save);
		attributes.put("status", status);
		attributes.put("runLast", runLast);
		DirectJNI.getInstance().notifyRActionListeners(new RConsoleAction("q", attributes));
		return null;
	}

	private static Vector<String> list = null;

	public static String[] listLightPacks(String v) {

		if (list == null) {

			list = new Vector<String>();
			URL jarURL = null;
			StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), System.getProperty("path.separator"));
			while (st.hasMoreTokens()) {
				String pathElement = st.nextToken();
				if (pathElement.endsWith("RJB.jar")) {
					try {
						jarURL = (new URL("jar:file:" + pathElement.replace('\\', '/') + "!/"));
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}

			if (jarURL != null) {
				try {
					JarURLConnection jarConnection = (JarURLConnection) jarURL.openConnection();
					JarFile jarfile = jarConnection.getJarFile();
					Enumeration<JarEntry> enu = jarfile.entries();
					while (enu.hasMoreElements()) {
						String entry = enu.nextElement().toString();
						if (entry.startsWith("monoscriptpackage") && entry.endsWith(".r"))
							list.add(entry.substring("monoscriptpackage".length() + 1, entry.length() - 2));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
		return new String[] { "OK", convertToPrintCommand(list.toString()) };

	}

	public static String[] setClusterProperties(String gprops) {
		if (!new File(gprops).exists()) {
			return new String[] { "NOK", "The file '" + gprops + "' doesn't exist" };
		} else {
			try {
				System.setProperty("properties.extension", gprops);
				ServantProviderFactory.init();
				return new String[] { "OK" };
			} catch (Exception e) {
				return new String[] { "NOK", PoolUtils.getStackTraceAsString(e) };
			}
		}
	}

	public static String[] makeCluster(long n, String nodeName) {
		Vector<RServices> workers = null;
		try {
			workers = _rClusterInterface.createRs((int) n, nodeName);
			String clusterName = "CL_" + (CLUSTER_COUNTER++);
			_clustersHash.put(clusterName, new Cluster(clusterName, workers, nodeName));
			return new String[] { "OK", clusterName };

		} catch (Exception e) {

			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand("couldn't create cluster") };
		}

	}

	public interface VWrapper {
		public int getSize();

		public RObject getElementAt(int i);

		public Object gatherResults(RObject[] f);
	}

	public static RInteger nullObject = new RInteger(-11);

	public static void stopAllClusters() {

		System.out.println("Stop All Clusters");
		Vector<String> v = new Vector<String>(_clustersHash.keySet());
		for (String cl : v) {
			Cluster cluster = _clustersHash.get(cl);
			stopCluster(cluster.getName());
		}
	}

	public static String[] stopCluster(String cl) {
		Cluster cluster = _clustersHash.get(cl);
		if (cluster == null)
			return new String[] { "NOK", "Invalid cluster" };
		try {

			// _rClusterInterface.releaseRs(cluster.getWorkers(),
			// cluster.getWorkers().size(), cluster.getNodeName());

		} catch (Exception e) {
			e.printStackTrace();
		}
		_clustersHash.remove(cl);
		return new String[] { "OK" };
	}

	public static String[] clusterApply(String cl, String varName, final String functionName) {
		try {

			Cluster cluster = _clustersHash.get(cl);
			if (cluster == null)
				return new String[] { "NOK", "Invalid cluster" };
			RObject v = DirectJNI.getInstance().getObjectFrom(varName);
			RObject vtemp = null;
			if (v.getClass() == RMatrix.class) {
				vtemp = ((RMatrix) v).getValue();
			} else if (v.getClass() == RArray.class) {
				vtemp = ((RArray) v).getValue();
			} else {
				vtemp = v;
			}

			final RObject var = vtemp;

			final VWrapper vwrapper = new VWrapper() {
				public int getSize() {
					if (var.getClass() == RNumeric.class) {
						return ((RNumeric) var).getValue().length;
					} else if (var.getClass() == RInteger.class) {
						return ((RInteger) var).getValue().length;
					} else if (var.getClass() == RChar.class) {
						return ((RChar) var).getValue().length;
					} else if (var.getClass() == RLogical.class) {
						return ((RLogical) var).getValue().length;
					} else if (var.getClass() == RComplex.class) {
						return ((RComplex) var).getReal().length;
					} else if (var.getClass() == RList.class) {
						return ((RList) var).getValue().length;
					}
					return 0;
				}

				public RObject getElementAt(int i) {
					if (var.getClass() == RNumeric.class) {
						return new RNumeric(((RNumeric) var).getValue()[i]);
					} else if (var.getClass() == RInteger.class) {
						return new RInteger(((RInteger) var).getValue()[i]);
					} else if (var.getClass() == RChar.class) {
						return new RChar(((RChar) var).getValue()[i]);
					} else if (var.getClass() == RLogical.class) {
						return new RLogical(((RLogical) var).getValue()[i]);
					} else if (var.getClass() == RComplex.class) {
						return new RComplex(new double[] { ((RComplex) var).getReal()[i] }, new double[] { ((RComplex) var).getImaginary()[i] },
								((RComplex) var).getIndexNA() != null ? new int[] { ((RComplex) var).getIndexNA()[i] } : null,
								((RComplex) var).getNames() != null ? new String[] { ((RComplex) var).getNames()[i] } : null);
					}

					else if (var.getClass() == RList.class) {
						return (RObject) ((RList) var).getValue()[i];
					}
					return null;
				}

				public Object gatherResults(RObject[] f) {

					if (var.getClass() == RList.class) {
						return f;
					} else {
						Class<?> resultClass = f[0].getClass();
						RObject result = null;
						if (resultClass == RNumeric.class) {
							double[] t = new double[f.length];
							for (int i = 0; i < f.length; ++i)
								t[i] = ((RNumeric) f[i]).getValue()[0];
							result = new RNumeric(t);
						} else if (resultClass == RInteger.class) {
							int[] t = new int[f.length];
							for (int i = 0; i < f.length; ++i)
								t[i] = ((RInteger) f[i]).getValue()[0];
							result = new RInteger(t);
						} else if (resultClass == RChar.class) {
							String[] t = new String[f.length];
							for (int i = 0; i < f.length; ++i)
								t[i] = ((RChar) f[i]).getValue()[0];
							result = new RChar(t);
						} else if (resultClass == RLogical.class) {
							boolean[] t = new boolean[f.length];
							for (int i = 0; i < f.length; ++i)
								t[i] = ((RLogical) f[i]).getValue()[0];
							result = new RLogical(t);
						} else if (resultClass == RComplex.class) {
							double[] real = new double[f.length];
							double[] im = new double[f.length];

							for (int i = 0; i < f.length; ++i) {
								real[i] = ((RComplex) f[i]).getReal()[0];
								im[i] = ((RComplex) f[i]).getImaginary()[0];
							}

							result = new RComplex(real, im, null, null);
						} else {
							throw new RuntimeException("Can't Handle this result type :" + resultClass.getName());
						}
						return result;
					}

				}
			};

			if (vwrapper.getSize() == 0)
				return new String[] { "NOK", "0 elements in data" };

			Vector<RServices> workers = cluster.getWorkers();

			final ArrayBlockingQueue<Integer> indexesQueue = new ArrayBlockingQueue<Integer>(vwrapper.getSize());
			for (int i = 0; i < vwrapper.getSize(); ++i)
				indexesQueue.add(i);

			final ArrayBlockingQueue<RServices> workersQueue = new ArrayBlockingQueue<RServices>(workers.size());
			for (int i = 0; i < workers.size(); ++i)
				workersQueue.add(workers.elementAt(i));

			final RObject[] result = new RObject[vwrapper.getSize()];

			for (int i = 0; i < workers.size(); ++i) {
				new Thread(new Runnable() {
					public void run() {
						RServices r = workersQueue.poll();
						while (indexesQueue.size() > 0) {
							Integer idx = indexesQueue.poll();
							if (idx != null) {
								try {
									result[idx] = r.call(functionName, vwrapper.getElementAt(idx));
								} catch (Exception e) {
									e.printStackTrace();
									result[idx] = nullObject;
								}
							}
						}
					}
				}).start();
			}

			while (true) {
				int count = 0;
				for (int i = 0; i < result.length; ++i)
					if (result[i] != null)
						++count;
				if (count == result.length)
					break;
				Thread.sleep(100);
			}

			Object reconstituedObject = vwrapper.gatherResults(result);
			if (v.getClass() == RMatrix.class) {
				((RArray) v).setValue((RVector) reconstituedObject);
			} else if (v.getClass() == RArray.class) {
				((RArray) v).setValue((RVector) reconstituedObject);
			} else if (v.getClass() == RList.class) {
				((RList) v).setValue((RObject[]) reconstituedObject);
			} else {
				v = (RObject) reconstituedObject;
			}

			DirectJNI.getInstance().putObjectAndAssignName(v, "clusterApplyResult", true);
			// DirectJNI.getInstance().putObjectAndAssignName(new RNumeric(12)
			// ,"clusterApplyResult" , true);

			return new String[] { "OK" };
		} catch (Exception e) {
			return new String[] { "NOK", PoolUtils.getStackTraceAsString(e) };
		}
	}

	public static String[] clusterEvalQ(String cl, String expression) {
		try {
			StringBuffer feedback = new StringBuffer();
			Cluster cluster = _clustersHash.get(cl);
			if (cluster == null)
				return new String[] { "NOK", "Invalid cluster" };
			for (int i = 0; i < cluster.getWorkers().size(); ++i) {
				RServices r = cluster.getWorkers().elementAt(i);
				r.consoleSubmit(expression);
				String s = r.getStatus();
				System.out.println("** submitted :" + expression + " to " + r.getServantName());
				if (s != null && !s.trim().equals(""))
					feedback.append("worker<" + r.getServantName() + ">:\n" + s + "\n");
			}
			System.out.println("##<" + feedback + ">##");
			return new String[] { "OK", convertToPrintCommand(feedback.toString()) };
		} catch (Exception e) {
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] clusterExport(String cl, String exp, String ato) {
		try {
			StringBuffer feedback = new StringBuffer();
			Cluster cluster = _clustersHash.get(cl);
			if (cluster == null)
				return new String[] { "NOK", "Invalid cluster" };
			RObject v = DirectJNI.getInstance().getObjectFrom(exp);
			if (ato.equals("")) ato=exp;			
			for (int i = 0; i < cluster.getWorkers().size(); ++i) {
				RServices r = cluster.getWorkers().elementAt(i);
				r.putAndAssign(v, ato);
				String s = r.getStatus();
				if (s != null && !s.trim().equals(""))
					feedback.append("worker<" + r.getServantName() + ">:" + s + "\n");
			}
			return new String[] { "OK", convertToPrintCommand(feedback.toString()) };
		} catch (Exception e) {
			return new String[] { "NOK", PoolUtils.getStackTraceAsString(e) };
		}
	}

	public static class Cluster {
		private String _name;
		private Vector<RServices> _workers;
		private String _nodeName;

		public Cluster(String name, Vector<RServices> workers, String nodeName) {
			_name = name;
			_workers = workers;
			_nodeName = nodeName;
		}

		public String getName() {
			return _name;
		}

		public Vector<RServices> getWorkers() {
			return _workers;
		}

		public String getNodeName() {
			return _nodeName;
		}
	}

	public static class RLink {
		private String _name;
		private RServices r;
		private Exception creationException;

		public RLink(String name, RServices r) {
			_name = name;
			this.r = r;
		}

		public String getName() {
			return _name;
		}

		public RServices getR() {
			return r;
		}

		public void setR(RServices r) {
			this.r = r;
		}

		public Exception getCreationException() {
			return creationException;
		}

		public void setCreationException(Exception creationException) {
			this.creationException = creationException;
		}
	}

	private static long CLUSTER_COUNTER = 0;
	private static HashMap<String, Cluster> _clustersHash = new HashMap<String, Cluster>();

	private static long RLINK_COUNTER = 0;
	private static HashMap<String, RLink> _rlinkHash = new HashMap<String, RLink>();

	public static String convertToPrintCommand(String s) {
		if (s.length() == 0)
			return "";
		StringBuffer result = new StringBuffer();
		result.append("print(\"");
		for (int i = 0; i < s.length(); ++i) {
			char si = s.charAt(i);
			if (si == '\n') {
				if (i == s.length() - 1) {

				} else {
					result.append("\",quote=FALSE);print(\"");
				}
			} else if (si == '\"') {
				result.append("\\'");
			} else if (si == '\t') {
				result.append("    ");
			} else if (si == '\r') {
				result.append("");
			} else if (si == '\\') {
				result.append("/");
			} else {
				result.append(si);
			}
		}
		result.append("\",quote=FALSE);");
		return result.toString();
	}

	public static String[] pythonExec(String command) {

		try {

			PythonInterpreterSingleton.startLogCapture();
			PythonInterpreterSingleton.getInstance().exec(command);
			System.out.println("#>>>:" + PythonInterpreterSingleton.getPythonStatus());
			return new String[] { "OK", convertToPrintCommand(PythonInterpreterSingleton.getPythonStatus()) };

		} catch (Exception e) {
			return new String[] { "NOK", convertToPrintCommand(PythonInterpreterSingleton.getPythonStatus()) };
		}
	}

	public static String[] pythonEval(String expression) {

		try {

			PythonInterpreterSingleton.startLogCapture();
			PyObject pyObject = PythonInterpreterSingleton.getInstance().eval(expression);
			if (pyObject != null) {
				System.out.println("Python Object Class ::" + pyObject.getClass().getName());
				RObject v = null;
				if (pyObject instanceof PyInteger) {
					v = new RInteger(((PyInteger) pyObject).getValue());
				} else if (pyObject instanceof PyLong) {
					v = new RInteger(((PyLong) pyObject).getValue().intValue());
				} else if (pyObject instanceof PyFloat) {
					v = new RNumeric(((PyFloat) pyObject).getValue());
				} else if (pyObject instanceof PyFloat) {
					v = new RNumeric(((PyFloat) pyObject).getValue());
				} else if (pyObject instanceof PyComplex) {
					v = new RComplex(new double[] { ((PyComplex) pyObject).getReal().getValue() },
							new double[] { ((PyComplex) pyObject).getImag().getValue() }, null, null);
				}

				if (v != null) {
					DirectJNI.getInstance().putObjectAndAssignName(v, "pythonEvalResult", true);
					return new String[] { "OK", convertToPrintCommand(PythonInterpreterSingleton.getPythonStatus()) };
				} else {
					PythonInterpreterSingleton.insertLog("The python type <" + pyObject.getClass().getName() + "> cannot be imported to R");
					throw new Exception();
				}
			} else {
				DirectJNI.getInstance().getRServices().evaluate("pythonEvalResult<-NULL");
				return new String[] { "OK", convertToPrintCommand(PythonInterpreterSingleton.getPythonStatus()) };
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PythonInterpreterSingleton.getPythonStatus()) };
		}
	}

	public static void pager(String fileName, String header, String title, String deleteFile) {
		HashMap<String, Object> attributes = new HashMap<String, Object>();

		byte[] buffer = null;
		try {
			RandomAccessFile raf = new RandomAccessFile(fileName, "r");
			buffer = new byte[(int) raf.length()];
			raf.readFully(buffer);
			raf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		attributes.put("fileName", new File(fileName).getName());
		attributes.put("content", buffer);
		attributes.put("header", header);
		attributes.put("title", title);
		attributes.put("deleteFile", new Boolean(deleteFile));

		DirectJNI.getInstance().notifyRActionListeners(new RConsoleAction("PAGER", attributes));

	}

	
	  public static String[] makeRLink(String mode, String params, String[] name) { return
		  makeRLink(mode, new String[]{params},name); 
	  }
	  
	  public static String[] makeRLink(String mode, String params, String name) { return
		  makeRLink(mode, new String[]{params},new String[]{name}); 
	  }
	  
	  public static String[] makeRLink(String mode, String[] params, String name) { return
		  makeRLink(mode, params,new String[]{name}); 
	  }
	  
	public static String[] makeRLink(final String mode, final String[] params, final String[] name) {

		if (name.length > 1) {
			String[] rlinkNames=new String[name.length];
			for (int i=0; i<name.length;++i) {
				String result[]=makeRLink(mode, params, name[i]);
				rlinkNames[i]=result[0];
			}
			return rlinkNames;
			
		} else {

			try {
				final String rlinkName = "RLINK_" + (RLINK_COUNTER++);
				_rlinkHash.put(rlinkName, new RLink(rlinkName, null));

				new Thread(new Runnable() {
					public void run() {
						try {

							// _rlinkHash.get(rlinkName).setR(ServerManager.createR(rlinkName));

							Properties props = new Properties();
							if (params != null && params.length > 0) {
								for (int i = 0; i < params.length; i++) {
									String element = params[i];
									int p = element.indexOf('=');
									if (p == -1) {
										props.put(element.toLowerCase(), "");
									} else {
										props.put(element.substring(0, p).trim().toLowerCase(), element.substring(p + 1, element.length()).trim());
									}
								}
							}
							
							System.out.println(props);

							RServices r = null;
							if (mode.equalsIgnoreCase("self")) {
								r = DirectJNI.getInstance().getRServices();
							} else if (mode.equalsIgnoreCase("new")) {
								if (props.get("naming.mode")==null) {
									props.put("naming.mode", "self");
								}
								r = ServerManager.createR(props.getProperty("r.binary"), false, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), 
										props, props.get("memorymin")==null ? ServerDefaults._memoryMin : Integer.decode(props.getProperty("memorymin")), 
											   props.get("memorymax")==null ? ServerDefaults._memoryMax: Integer.decode(props.getProperty("memorymax")), name[0], false, null, null);
							} else if (mode.equalsIgnoreCase("rmi")) {

								try {
									if (props.getProperty("stub") != null) {
										r = (RServices) PoolUtils.hexToStub(props.getProperty("stub"), RListener.class.getClassLoader());
									} else {
										r = (RServices) ServerDefaults.getRegistry(props).lookup(name[0]);
									}

								} catch (Exception e) {
									DirectJNI.getInstance().getRServices().consoleSubmit(
											convertToPrintCommand("RLink Creation Failed\nuse show.rlink to see creation error"));
									_rlinkHash.get(rlinkName).setCreationException(e);
								}

							} else if (mode.equalsIgnoreCase("http")) {

								try {
									HashMap<String, Object> options = new HashMap<String, Object>();
									if (props.getProperty("privatename") != null) {
										options.put("privatename", props.getProperty("privatename"));
									}
									if (props.getProperty("memorymin") != null) {
										options.put("memorymin", props.getProperty("memorymin"));
									}
									if (props.getProperty("memorymax") != null) {
										options.put("memorymax", props.getProperty("memorymax"));
									}

									final String sessionId = RHttpProxy.logOn(props.getProperty("url"), "", props.getProperty("login") == null ? "guest"
											: props.getProperty("login"), props.getProperty("password") == null ? "guest" : props.getProperty("password"),
											options);
									r = RHttpProxy.getR(props.getProperty("url"), sessionId, true, 30);

									r = (RServices) ServerDefaults.getRmiRegistry().lookup(name[0]);
								} catch (Exception e) {
									DirectJNI.getInstance().getRServices().consoleSubmit(
											convertToPrintCommand("RLink Creation Failed\nuse show.rlink to see creation error"));
									_rlinkHash.get(rlinkName).setCreationException(e);
								}

							}

							_rlinkHash.get(rlinkName).setR(r);

							try {
								DirectJNI.getInstance().getRServices().consoleSubmit(
										convertToPrintCommand("RLink Creation Succeeded\nuse show.rlink to get creation details"));
							} catch (Exception e) {
								e.printStackTrace();
							}

						} catch (Exception ex) {

							_rlinkHash.get(rlinkName).setCreationException(ex);
							try {
								DirectJNI.getInstance().getRServices().consoleSubmit(
										convertToPrintCommand("RLink Creation Failed!\n" + PoolUtils.getStackTraceAsString(ex)));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					}
				}).start();

				return new String[] { rlinkName };

			} catch (Exception e) {
				e.printStackTrace();
				return new String[] { "NOK", convertToPrintCommand("couldn't create rlink") };
			}
		}
	}

	public static String[] RLinkConsole(final String rlinkName, final String command) {
		try {

			new Thread(new Runnable() {
				public void run() {

					try {

						if (_rlinkHash.get(rlinkName) == null) {
							DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand("Invalid RLink:" + rlinkName));
							return;
						}

						RServices r = _rlinkHash.get(rlinkName).getR();
						Exception creationException = _rlinkHash.get(rlinkName).getCreationException();

						if (r == null) {
							if (creationException == null) {
								DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand("RLink Not yet Created"));
								return;
							} else {
								DirectJNI.getInstance().getRServices().consoleSubmit(
										convertToPrintCommand("RLink creation has failed :\nCreation Exception:\n"
												+ PoolUtils.getStackTraceAsString(creationException)));
								return;
							}
						}

						r.consoleSubmit(command);

						try {
							DirectJNI.getInstance().getRServices().consoleSubmit(
									convertToPrintCommand("Command submitted to RLink [" + rlinkName + "]\n" + r.getStatus()));
						} catch (Exception e) {
							e.printStackTrace();
						}

					} catch (Exception ex) {

						try {
							DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand(PoolUtils.getStackTraceAsString(ex)));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}).start();

			return new String[] { "OK", convertToPrintCommand("RLink Console Submit in background..") };

		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] RLinkGet(final String rlinkName, final String expression, final String ato) {
		try {

			new Thread(new Runnable() {
				public void run() {

					try {

						if (_rlinkHash.get(rlinkName) == null) {
							DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand("Invalid RLink:" + rlinkName));
							return;
						}

						RServices r = _rlinkHash.get(rlinkName).getR();
						Exception creationException = _rlinkHash.get(rlinkName).getCreationException();

						if (r == null) {
							if (creationException == null) {
								DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand("RLink Not yet Created"));
								return;
							} else {
								DirectJNI.getInstance().getRServices().consoleSubmit(
										convertToPrintCommand("RLink creation has failed :\nCreation Exception:\n"
												+ PoolUtils.getStackTraceAsString(creationException)));
								return;
							}
						}

						RObject robj = r.getObject(expression);

						try {
							DirectJNI.getInstance().getRServices().putAndAssign(robj, ato.equals("") ? expression : ato);
							DirectJNI.getInstance().getRServices().consoleSubmit(
									convertToPrintCommand((ato.equals("") ? expression : ato) + " Has been assigned a value from RLink [" + rlinkName + "]\n" + r.getStatus()));
						} catch (Exception e) {
							e.printStackTrace();
						}

					} catch (Exception ex) {

						try {
							DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand(PoolUtils.getStackTraceAsString(ex)));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}).start();

			return new String[] { "OK", convertToPrintCommand("RLink Get in background..") };

		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] RLinkPut(final String rlinkName, final String expression, final String ato) {
		try {

			new Thread(new Runnable() {
				public void run() {

					try {

						if (_rlinkHash.get(rlinkName) == null) {
							DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand("Invalid RLink:" + rlinkName));
							return;
						}

						RServices r = _rlinkHash.get(rlinkName).getR();
						Exception creationException = _rlinkHash.get(rlinkName).getCreationException();

						if (r == null) {
							if (creationException == null) {
								DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand("RLink Not yet Created"));
								return;
							} else {
								DirectJNI.getInstance().getRServices().consoleSubmit(
										convertToPrintCommand("RLink creation has failed :\nCreation Exception:\n"
												+ PoolUtils.getStackTraceAsString(creationException)));
								return;
							}
						}

						try {
							RObject robj = DirectJNI.getInstance().getRServices().getObject(expression);
							DirectJNI.getInstance().getRServices()
									.consoleSubmit(
											convertToPrintCommand("Retrieve <" + expression + "> from R Session\n"
													+ DirectJNI.getInstance().getRServices().getStatus()));
							r.putAndAssign(robj, ato.equals("") ? expression : ato);
							DirectJNI.getInstance().getRServices().consoleSubmit(
									convertToPrintCommand("Value assigned to " + (ato.equals("") ? expression : ato) + " on RLink [" + rlinkName + "]\n" + " has been assigned a value\n")
											+ r.getStatus());
						} catch (Exception e) {
							e.printStackTrace();
						}

					} catch (Exception ex) {
						try {
							DirectJNI.getInstance().getRServices().consoleSubmit(convertToPrintCommand(PoolUtils.getStackTraceAsString(ex)));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}).start();

			return new String[] { "OK", convertToPrintCommand("RLink Put in background..") };

		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] RLinkShow(String rlinkName) {
		try {
			if (_rlinkHash.get(rlinkName) == null) {
				return new String[] { "NOK", convertToPrintCommand("couldn't find rlink") };
			}
			return new String[] {
					"OK",
					convertToPrintCommand("Name:" + rlinkName + "\nR:" + _rlinkHash.get(rlinkName).getR() + "\nCreation Exception:"
							+ _rlinkHash.get(rlinkName).getCreationException()) };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand("couldn't create rlink") };
		}
	}

	public static String[] RLinkRelease(String rlinkName) {
		try {
			_rlinkHash.remove(rlinkName);
			return new String[] { "OK", "" };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] RLinkList() {
		String result = "";
		for (String k : _rlinkHash.keySet())
			result += k + "\n";
		try {
			return new String[] { "OK", convertToPrintCommand(result) };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] RLinkRegistryList(final String params) {
		return RLinkRegistryList(new String[]{params});
	}
	public static String[] RLinkRegistryList(final String[] params) {
		try {
			String[] names = null;
			if (params.length == 1 && params[0].equals("")) {
				names = ServerDefaults.getRmiRegistry().list();
			} else {
				Properties props = new Properties();
				if (params != null && params.length > 0) {
					for (int i = 0; i < params.length; i++) {
						String element = params[i];
						int p = element.indexOf('=');
						if (p == -1) {
							props.put(element.toLowerCase(), "");
						} else {
							props.put(element.substring(0, p).trim().toLowerCase(), element.substring(p + 1, element.length()).trim());
						}
					}
				}
				names = ServerDefaults.getRegistry(props).list();
			}
			return names;
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] makeRLinkCluster(String[] rlinks) {
		Vector<RServices> workers = new Vector<RServices>();
		for (int i = 0; i < rlinks.length; ++i)
			workers.add(_rlinkHash.get(rlinks[i]).getR());
		try {
			String clusterName = "CL_" + (CLUSTER_COUNTER++);
			_clustersHash.put(clusterName, new Cluster(clusterName, workers, ""));
			return new String[] { "OK", clusterName };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand("couldn't create cluster") };
		}
	}
	
	public static String[] spreadsheetPut(String location, String name) {
		try {
			System.out.println("name=<" + name + ">");

			int ssNbr = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet().size();
			if (ssNbr == 0) {
				return new String[] { "NOK",
						convertToPrintCommand("Tere are no Spreadsheets on Server : \nCreate one first via: Collaboration/New Collaborative Spreadsheet\n") };
			}
			if (name.equals("")) {
				if (ssNbr == 1) {
					name = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet().iterator().next();
				} else {
					String[] ssNames = new String[ssNbr];
					int i = 0;
					for (String s : DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet())
						ssNames[i++] = s;
					return new String[] {
							"NOK",
							convertToPrintCommand("Tere are " + ssNbr + " Spreadsheets on Server : \n" + Arrays.toString(ssNames)
									+ "\nChoose one via name='Spreadsheet Name'\n") };
				}
			}

			SpreadsheetModelRemote model = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().get(name);
			if (model == null)
				return new String[] { "NOK", convertToPrintCommand("Bad Spreadsheet Name") };

			int startRow = 0;
			int startCol = 0;
			try {
				startCol = ImportInfo.getCol(location);
				startRow = ImportInfo.getRow(location);
			} catch (Exception e) {
				e.printStackTrace();
				return new String[] { "NOK", convertToPrintCommand("Bad Cell Location") };
			}

			String trstring = ImportInfo.getImportInfo(DirectJNI.getInstance().getObjectFrom(".PrivateEnv$spreadsheet.put.value")).getTabString();
			model.paste(startRow, startCol, trstring);
			return new String[] { "OK" };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] spreadsheetGet(String range, String type, String name) {
		try {

			System.out.println("name=<" + name + ">");

			int ssNbr = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet().size();
			if (ssNbr == 0) {
				return new String[] { "NOK",
						convertToPrintCommand("Tere are No Spreadsheets on Server : \nCreate one first via: Spreadsheet/New Server-side Spreadsheet\n") };
			}
			if (name.equals("")) {
				if (ssNbr == 1) {
					name = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet().iterator().next();
				} else {
					String[] ssNames = new String[ssNbr];
					int i = 0;
					for (String s : DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet())
						ssNames[i++] = s;
					return new String[] {
							"NOK",
							convertToPrintCommand("Tere are " + ssNbr + " Spreadsheets on Server : \n" + Arrays.toString(ssNames)
									+ "\nChoose one via name='Spreadsheet Name'\n") };
				}
			}

			SpreadsheetModelRemote model = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().get(name);
			if (model == null)
				return new String[] { "NOK", convertToPrintCommand("Bad Spreadsheet Name") };

			CellRange cellrange = null;
			try {
				cellrange = ImportInfo.getRange(range);
			} catch (Exception e) {
				e.printStackTrace();
				return new String[] { "NOK", convertToPrintCommand("Bad Cell Range") };
			}

			int dataType = -1;
			for (int i = 0; i < ImportInfo.R_TYPES_NAMES.length; ++i) {
				if (ImportInfo.R_TYPES_NAMES[i].equals(type)) {
					dataType = i;
					break;
				}
			}
			if (dataType == -1) {
				return new String[] { "NOK", convertToPrintCommand("Bad Data Type, allowed types: " + Arrays.toString(ImportInfo.R_TYPES_NAMES)) };
			}

			ExportInfo info = ExportInfo.getExportInfo(cellrange, dataType, (SpreadsheetTableModelClipboardInterface) model);
			DirectJNI.getInstance().putObjectAndAssignName(info.getRObject(), "spreadsheet.get.result", true);
			System.out.println("--->" + PoolUtils.replaceAll(info.getConversionCommand(), "${VAR}", ".PrivateEnv$spreadsheet.get.result"));
			DirectJNI.getInstance().evaluate(PoolUtils.replaceAll(info.getConversionCommand(), "${VAR}", ".PrivateEnv$spreadsheet.get.result"),
					info.getCommandsNbr());

			return new String[] { "OK" };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

	public static String[] spreadsheetSelect(String range, String name) {
		try {

			System.out.println("name=<" + name + ">");

			int ssNbr = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet().size();
			if (ssNbr == 0) {
				return new String[] { "NOK",
						convertToPrintCommand("Tere are No Spreadsheets on Server : \nCreate one first via: Spreadsheet/New Server-side Spreadsheet\n") };
			}
			if (name.equals("")) {
				if (ssNbr == 1) {
					name = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet().iterator().next();
				} else {
					String[] ssNames = new String[ssNbr];
					int i = 0;
					for (String s : DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().keySet())
						ssNames[i++] = s;
					return new String[] {
							"NOK",
							convertToPrintCommand("Tere are " + ssNbr + " Spreadsheets on Server : \n" + Arrays.toString(ssNames)
									+ "\nChoose one via name='Spreadsheet Name'\n") };
				}
			}

			SpreadsheetModelRemote model = DirectJNI.getInstance().getSpreadsheetTableModelRemoteHashMap().get(name);
			if (model == null)
				return new String[] { "NOK", convertToPrintCommand("Bad Spreadsheet Name") };

			CellRange cellrange = null;
			try {
				cellrange = ImportInfo.getRange(range);
			} catch (Exception e) {
				e.printStackTrace();
				return new String[] { "NOK", convertToPrintCommand("Bad Cell Range") };
			}

			model.setSpreadsheetSelection(DirectJNI.getInstance().getOriginatorUID(), cellrange);
			return new String[] { "OK" };
		} catch (Exception e) {
			e.printStackTrace();
			return new String[] { "NOK", convertToPrintCommand(PoolUtils.getStackTraceAsString(e)) };
		}
	}

}
