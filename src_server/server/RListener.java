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


import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.bioconductor.packages.rservices.RArray;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RComplex;
import org.bioconductor.packages.rservices.RInteger;
import org.bioconductor.packages.rservices.RList;
import org.bioconductor.packages.rservices.RLogical;
import org.bioconductor.packages.rservices.RMatrix;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.rservices.RObject;
import org.bioconductor.packages.rservices.RVector;

import remoting.RAction;
import remoting.RCallback;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.NodeManager;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public abstract class RListener {

	private static RCallback _callbackInterface = null;
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(RListener.class);

	public static void setCallbackInterface(RCallback callbackInterface) {
		_callbackInterface = callbackInterface;
	}

	public static void progress(float percentageDone, String phaseDescription, float phasePercentageDone) {
		if (_callbackInterface != null) {
			try {
				_callbackInterface.progress(percentageDone, phaseDescription, phasePercentageDone);
			} catch (RemoteException e) {
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
		RAction action = new RAction("help");
		action.setAttributes(attributes);
		DirectJNI._rActions.add(action);
		return null;
	}

	private static Vector<String> list = null;

	public static String[] listLightPacks(String v) {

		if (list == null) {

			list = new Vector<String>();
			URL jarURL = null;
			StringTokenizer st = new StringTokenizer(System.getProperty("java.class.path"), System
					.getProperty("path.separator"));
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
		Vector<RServices> workers = new Vector<RServices>();

		try {
			
			ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

			if (spFactory == null) {
				throw new Exception("no registry");
			}
							
			Registry registry = spFactory.getServantProvider().getRegistry();
			NodeManager nm = null;
			try {
				nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name") + "_"
						+ nodeName);
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

			String clusterName = "CL_" + (CLUSTER_COUNTER++);
			_clustersHash.put(clusterName, new Cluster(clusterName, workers, nodeName));

			return new String[] { "OK", clusterName };

		} catch (Exception e) {

			e.printStackTrace();
			if (workers.size() > 0) {
				for (int i = 0; i < workers.size(); ++i) {
					try {
						ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(
								workers.elementAt(i));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
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
		Vector<String> v=new Vector<String>(_clustersHash.keySet());
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
			Registry registry = ServantProviderFactory.getFactory().getServantProvider().getRegistry();
			NodeManager nm = null;
			try {
				nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name") + "_"
						+ cluster.getNodeName());
			} catch (NotBoundException nbe) {
				nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
			} catch (Exception e) {
				throw new Exception("no node manager");
			}
			
			for (int i = 0; i < cluster.getWorkers().size(); ++i) {			
				try {
					nm.kill(cluster.getWorkers().elementAt(i));
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
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
						return new RComplex(new double[] { ((RComplex) var).getReal()[i] },
								new double[] { ((RComplex) var).getImaginary()[i] },
								((RComplex) var).getIndexNA() != null ? new int[] { ((RComplex) var).getIndexNA()[i] }
										: null, ((RComplex) var).getNames() != null ? new String[] { ((RComplex) var)
										.getNames()[i] } : null);
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
				String s = r.consoleSubmit(expression);
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

	public static String[] clusterExport(String cl, String varName) {
		try {
			StringBuffer feedback = new StringBuffer();
			Cluster cluster = _clustersHash.get(cl);
			if (cluster == null)
				return new String[] { "NOK", "Invalid cluster" };
			RObject v = DirectJNI.getInstance().getObjectFrom(varName);

			for (int i = 0; i < cluster.getWorkers().size(); ++i) {
				RServices r = cluster.getWorkers().elementAt(i);
				r.putObjectAndAssignName(v, varName);
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

	private static long CLUSTER_COUNTER = 0;
	private static HashMap<String, Cluster> _clustersHash = new HashMap<String, Cluster>();

	public static String convertToPrintCommand(String s) {
		if (s.length() == 0)
			return "";
		StringBuffer result = new StringBuffer();
		result.append("print(\"");
		for (int i = 0; i < s.length(); ++i) {
			char si = s.charAt(i);
			if (si == '\n') {
				result.append("\",quote=FALSE);print(\"");
			} else if (si == '\"') {
				result.append("\\'");
			} else {
				result.append(si);
			}
		}
		result.append("\",quote=FALSE);");
		return result.toString();
	}

}
