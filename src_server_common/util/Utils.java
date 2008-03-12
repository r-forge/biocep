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
package util;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.PropertyConfigurator;
//import org.apache.commons.logging.impl.Log4JLogger;
//import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class Utils {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(Utils.class);

	public static String flatArray(Object[] array) {
		String result = "{";
		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				result += array[i].toString() + (i == (array.length - 1) ? "" : ",");
			}
		} else {
			result += "null";
		}
		result += "}";
		return result;
	}

	public static String flatArray(boolean[] array) {
		String result = "{";
		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				result += new Boolean(array[i]).toString() + (i == (array.length - 1) ? "" : ",");
			}
		} else {
			result += "null";
		}
		result += "}";
		return result;
	}

	public static String flatArray(int[] array) {
		String result = "{";
		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				result += new Integer(array[i]).toString() + (i == (array.length - 1) ? "" : ",");
			}
		} else {
			result += "null";
		}
		result += "}";
		return result;
	}

	public static String flatArray(double[] array) {
		String result = "{";
		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				result += new Double(array[i]).toString() + (i == (array.length - 1) ? "" : ",");
			}
		} else {
			result += "null";
		}
		result += "}";
		return result;
	}

	public static Vector<String> getVectorFromSet(Set<String> s) {
		Vector<String> result = new Vector<String>();
		for (Iterator<String> iter = s.iterator(); iter.hasNext();) {
			result.add((String) iter.next());
		}
		return result;
	}

	public static String captalizeFirstChar(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1, s.length());
	}

	public static String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	public static String retro(Registry registry, Remote servant) throws Exception {
		String[] names = registry.list();
		for (int i = 0; i < names.length; ++i)
			if (registry.lookup(names[i]).equals(servant))
				return names[i];
		return null;
	}

	public static String retro(String registryHost, int registryPort, Remote servant) throws Exception {
		return retro(LocateRegistry.getRegistry(registryHost, registryPort), servant);
	}

	public static StringBuffer getFileAsStringBuffer(String fileName) throws Exception {
		StringBuffer result = new StringBuffer();
		BufferedReader breader = new BufferedReader(new FileReader(fileName));
		String line;
		do {
			line = breader.readLine();
			if (line != null) {
				result.append(line);
				result.append('\n');
			}
		} while (line != null);
		breader.close();
		return result;
	}

	public static void catchNodes(Node node, String nameToCatch, Vector<Node> result) throws DOMException {
		if (node == null)
			return;
		if (node.getNodeName().equals(nameToCatch)) {
			result.add(node);
		}
		if (node.hasChildNodes())
			for (int i = 0; i < node.getChildNodes().getLength(); i++)
				catchNodes(node.getChildNodes().item(i), nameToCatch, result);
	}

	public static Node catchNode(Node node, String nameToCatch) throws DOMException {
		if (node.getNodeName().equals(nameToCatch)) {
			return node;
		}
		if (node.hasChildNodes())
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				Node result = catchNode(node.getChildNodes().item(i), nameToCatch);
				if (result != null)
					return result;
			}
		return null;
	}

	public static String getGetterName(Field field) {

		return (field.getClass().equals(Boolean.class) ? "is" + Utils.captalizeFirstChar(field.getName()) : "get")
				+ Utils.captalizeFirstChar(field.getName());

	}

	public static String indentS4Print(String p) {

		BufferedReader br = new BufferedReader(new StringReader(p));

		StringBuffer result = new StringBuffer();
		int indent = -1;
		String line = null;
		String linePrec = null;

		do {
			try {
				line = br.readLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (line != null) {
				if (line.startsWith("An object of class"))
					++indent;
				if (line.equals("") && linePrec != null && linePrec.equals(""))
					--indent;
				for (int i = 0; i < indent; ++i)
					result.append("\t");
				result.append(line);
				result.append("\n");
				linePrec = line;
			}

		} while (line != null);
		return result.toString();

	}

	public static String indent(String p, int indent) {
		if (p == null)
			return p;
		BufferedReader br = new BufferedReader(new StringReader(p));
		StringBuffer result = new StringBuffer();
		String line = null;
		do {
			try {
				line = br.readLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (line != null) {
				for (int i = 0; i < indent; ++i)
					result.append("\t");
				result.append(line);
				result.append("\n");
			}

		} while (line != null);
		return result.toString();
	}

	static class StreamGobbler extends Thread {
		InputStream is;

		String type;

		OutputStream os;

		StreamGobbler(InputStream is, String type) {
			this(is, type, null);
		}

		StreamGobbler(InputStream is, String type, OutputStream redirect) {
			this.is = is;
			this.type = type;
			this.os = redirect;
		}

		public void run() {
			try {
				PrintWriter pw = null;
				if (os != null)
					pw = new PrintWriter(os);

				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null) {
					if (pw != null)
						pw.println(line);
					System.out.println(type + ">" + line);
				}
				if (pw != null)
					pw.flush();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public static void exec(String command[], String fileNameOut, String fileNameErr) {
		try {
			FileOutputStream errf = fileNameErr == null ? null : new FileOutputStream(fileNameErr);
			FileOutputStream outf = fileNameOut == null ? null : new FileOutputStream(fileNameOut);
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);

			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", errf);

			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", outf);

			errorGobbler.start();
			outputGobbler.start();

			int exitVal = proc.waitFor();
			System.out.println("ExitValue: " + exitVal);

			if (errf != null) {
				errf.flush();
				errf.close();
			}
			if (outf != null) {
				outf.flush();
				outf.close();
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public static void initLog() {
		
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

	public static boolean isWebBrowserSupported() throws Exception {
		Class<?> ServiceManagerClass = null;
		try {
			ServiceManagerClass = Utils.class.getClassLoader().loadClass("javax.jnlp.ServiceManager");
		} catch (Exception e) {
			return false;
		}
		if (ServiceManagerClass == null)
			return false;
		Object basicServiceInstance = ServiceManagerClass.getMethod("lookup", String.class).invoke(null,
				"javax.jnlp.BasicService");
		Class<?> BasicServiceClass = Utils.class.getClassLoader().loadClass("javax.jnlp.BasicService");
		return (Boolean) BasicServiceClass.getMethod("isWebBrowserSupported").invoke(basicServiceInstance);
	}

	public static void showDocument(URL url) throws Exception {
		Class<?> ServiceManagerClass = null;
		try {
			ServiceManagerClass = Utils.class.getClassLoader().loadClass("javax.jnlp.ServiceManager");
		} catch (Exception e) {
			return;
		}
		if (ServiceManagerClass == null)
			return;
		Object basicServiceInstance = ServiceManagerClass.getMethod("lookup", String.class).invoke(null,
				"javax.jnlp.BasicService");
		Class<?> BasicServiceClass = Utils.class.getClassLoader().loadClass("javax.jnlp.BasicService");
		BasicServiceClass.getMethod("showDocument", URL.class).invoke(basicServiceInstance, url);
	}

}
