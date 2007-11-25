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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import mapping.RPackage;

import org.apache.commons.logging.Log;
import org.bioconductor.packages.rservices.RArray;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RComplex;
import org.bioconductor.packages.rservices.RDataFrame;
import org.bioconductor.packages.rservices.REnvironment;
import org.bioconductor.packages.rservices.RFactor;
import org.bioconductor.packages.rservices.RInteger;
import org.bioconductor.packages.rservices.RList;
import org.bioconductor.packages.rservices.RLogical;
import org.bioconductor.packages.rservices.RMatrix;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.rservices.RObject;
import org.bioconductor.packages.rservices.RUnknown;
import org.bioconductor.packages.rservices.RVector;

import util.Utils;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class Globals {

	public static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);

	public static String GEN_ROOT = null;
	public static String MAPPING_JAR_NAME = null;
	public static String GEN_ROOT_SRC = null;
	public static String GEN_ROOT_LIB = null;
	public static final String TEMP_JARS_PREFIX = "_temp";
	private static final String LOC_STR_LEFT = "It represents the S4 Class";
	private static final String LOC_STR_RIGHT = "in R package";
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(Globals.class);
	public static void scanJavaFiles(File node, Vector<String> result) {
		if (!node.isDirectory() && node.getName().endsWith(".java")) {
			result.add(node.getAbsolutePath());
			return;
		}
		File[] list = node.listFiles();
		if (list != null) {
			for (int i = 0; i < list.length; ++i) {
				scanJavaFiles(list[i], result);
			}
		}
	}

	private static String getRClassForBean(JarFile jarFile, String beanClassName) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarFile
				.getEntry(beanClassName.replace('.', '/') + ".java"))));
		do {
			String line = br.readLine();
			if (line != null) {
				int p = line.indexOf(Globals.LOC_STR_LEFT);
				if (p != -1) {
					return line.substring(p + Globals.LOC_STR_LEFT.length(), line.indexOf(Globals.LOC_STR_RIGHT))
							.trim();
				}
			} else
				break;
		} while (true);
		return null;
	}

	public static void generateMaps(URL jarUrl, boolean rawClasses) {

		try {

			DirectJNI._mappingClassLoader = new URLClassLoader(new URL[] { jarUrl }, Globals.class.getClassLoader());
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
				if (className.startsWith("org.bioconductor.packages.")
						&& !className.startsWith("org.bioconductor.packages.rservices")) {
					Class<?> c_ = DirectJNI._mappingClassLoader.loadClass(className);

					if (c_.getSuperclass() != null && c_.getSuperclass().equals(RObject.class)
							&& !Modifier.isAbstract(c_.getModifiers())) {

						if (c_.equals(RLogical.class) || c_.equals(RInteger.class) || c_.equals(RNumeric.class)
								|| c_.equals(RComplex.class) || c_.equals(RChar.class) || c_.equals(RMatrix.class)
								|| c_.equals(RArray.class) || c_.equals(RList.class) || c_.equals(RDataFrame.class)
								|| c_.equals(RFactor.class) || c_.equals(REnvironment.class)
								|| c_.equals(RVector.class) || c_.equals(RUnknown.class)) {
						} else {
							String rclass = getRClassForBean(jarfile, className);
							DirectJNI._s4BeansHash.put(className, c_);
							DirectJNI._s4BeansMapping.put(rclass, className);
							DirectJNI._s4BeansMappingRevert.put(className, rclass);
						}

					} else if ((rawClasses && c_.getSuperclass() != null && c_.getSuperclass().equals(Object.class))
							|| (!rawClasses && RPackage.class.isAssignableFrom(c_) && (c_.isInterface()))) {

						String shortClassName = className.substring(className.lastIndexOf('.') + 1);
						DirectJNI._packageNames.add(shortClassName);

						Vector<Class<?>> v = DirectJNI._rPackageInterfacesHash.get(className);
						if (v == null) {
							v = new Vector<Class<?>>();
							DirectJNI._rPackageInterfacesHash.put(className, v);
						}
						v.add(c_);

					} else {
						String nameWithoutPackage = className.substring(className.lastIndexOf('.') + 1);
						if (nameWithoutPackage.indexOf("Factory") != -1
								&& c_.getMethod("setData", new Class[] { RObject.class }) != null) {
							// if
							// (DirectJNI._factoriesMapping.get(nameWithoutPackage)
							// != null) throw new Exception("Factories Names
							// Conflict : two " + nameWithoutPackage);
							DirectJNI._factoriesMapping.put(nameWithoutPackage, className);
							if (Modifier.isAbstract(c_.getModifiers()))
								DirectJNI._abstractFactories.add(className);
						}
					}
				}
			}

			// log.info("s4Beans:" +s4Beans);
			log.info("rPackageInterfaces:" + DirectJNI._packageNames);
			log.info("s4Beans MAP :" + DirectJNI._s4BeansMapping);
			log.info("s4Beans Revert MAP :" + DirectJNI._s4BeansMappingRevert);
			log.info("factories :" + DirectJNI._factoriesMapping);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public static void regenerateRPackageClass(boolean embedRScript) throws Exception {

		for (Iterator iter = DirectJNI._rPackageInterfacesHash.keySet().iterator(); iter.hasNext();) {

			String className = (String) iter.next();
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);

			String outputFileName = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR
					+ className.replace('.', Globals.FILE_SEPARATOR) + "Impl.java";
			new File(outputFileName.substring(0, outputFileName.lastIndexOf(FILE_SEPARATOR))).mkdirs();
			log.info("output file:" + outputFileName);
			PrintWriter outputWriter = new PrintWriter(outputFileName);
			outputWriter.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriter.println("public class " + shortClassName + "Impl" + " implements " + shortClassName + " {");
			outputWriter.println("private " + className.substring(className.lastIndexOf('.') + 1) + "Impl"
					+ "(){ init(); }");
			outputWriter.println("public void init() { "
					+ (embedRScript ? "try {server.DirectJNI.getInstance().getRServices().sourceFromResource(\"/"
							+ className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/"
							+ className.substring(className.lastIndexOf('.') + 1)
							+ ".R\");} catch (Exception e) {e.printStackTrace();}\n" : "") + " }");

			String outputFileNameRemote = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR
					+ className.replace('.', Globals.FILE_SEPARATOR) + ".java";
			new File(outputFileNameRemote.substring(0, outputFileNameRemote.lastIndexOf(FILE_SEPARATOR))).mkdirs();
			log.info("output remote file:" + outputFileNameRemote);
			PrintWriter outputWriterRemote = new PrintWriter(outputFileNameRemote);
			outputWriterRemote.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriterRemote.println("public interface " + shortClassName + " extends mapping.RPackage  {");
			outputWriter.println("private static " + shortClassName
					+ " _packageInstance = null;private static Integer _lock = new Integer(0);");
			outputWriter
					.println("public static "
							+ shortClassName
							+ " getInstance() {	if (_packageInstance != null) return _packageInstance; synchronized (_lock) { if (_packageInstance == null) { _packageInstance = new "
							+ shortClassName + "Impl" + "(); }	return _packageInstance;}}");

			String outputFileNameRemoteImpl = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR
					+ className.replace('.', Globals.FILE_SEPARATOR) + "ImplRemote" + ".java";
			new File(outputFileNameRemoteImpl.substring(0, outputFileNameRemoteImpl.lastIndexOf(FILE_SEPARATOR)))
					.mkdirs();
			log.info("output remote impl file:" + outputFileNameRemoteImpl);
			PrintWriter outputWriterRemoteImpl = new PrintWriter(outputFileNameRemoteImpl);
			outputWriterRemoteImpl.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriterRemoteImpl.println("public class " + shortClassName
					+ "ImplRemote extends java.rmi.server.UnicastRemoteObject implements " + shortClassName + " {");
			outputWriterRemoteImpl.println("public " + className.substring(className.lastIndexOf('.') + 1)
					+ "ImplRemote" + "() throws java.rmi.RemoteException { super(); };");

			PrintWriter outputWriterWebservice = null;
			if (_webPublishingEnabled) {
				String outputFileNameWebservice = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR
						+ className.replace('.', Globals.FILE_SEPARATOR) + "Web" + ".java";
				new File(outputFileNameWebservice.substring(0, outputFileNameWebservice.lastIndexOf(FILE_SEPARATOR)))
						.mkdirs();
				log.info("output web service file:" + outputFileNameWebservice);
				outputWriterWebservice = new PrintWriter(outputFileNameWebservice);
				outputWriterWebservice.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
				outputWriterWebservice
						.println("import javax.jws.WebService;\nimport org.bioconductor.packages.rservices.*;\n import static  uk.ac.ebi.microarray.pools.PoolUtils.*;\n @WebService\n");
				outputWriterWebservice.println("public class " + shortClassName + "Web {");
				outputWriterWebservice.println("public " + className.substring(className.lastIndexOf('.') + 1) + "Web"
						+ "(){};");
			}

			Vector<Method> methodsVector = new Vector<Method>();
			Vector classes = DirectJNI._rPackageInterfacesHash.get(className);
			for (int i = 0; i < classes.size(); ++i) {
				Class<?> c = (Class<?>) classes.elementAt(i);
				for (int j = 0; j < c.getDeclaredMethods().length; ++j) {
					Method m = c.getDeclaredMethods()[j];
					if (!m.getName().startsWith(Globals.PUBLISH_S4_HEADER))
						methodsVector.add(m);
				}
			}

			for (int i = 0; i < methodsVector.size(); ++i) {
				Method m = methodsVector.elementAt(i);

				FAttributes fattrs = getAttributes(m);
				if (fattrs == null)
					continue;

				if (!_webPublishingEnabled)
					fattrs.setPublishToWeb(false);

				log.info("##" + m.getName() + ">>" + fattrs);

				String m_name = fattrs.getRenameTo() == null ? m.getName() : fattrs.getRenameTo();

				String mHeader = " public " + (m.getReturnType() == null ? "void" : m.getReturnType().getName()) + " "
						+ m_name + "(";
				String mHeaderAsRef = " public " + (m.getReturnType() == null ? "void" : m.getReturnType().getName())
						+ " " + m_name + "AsReference" + "(";

				String[] formalArgs = ((RChar) DirectJNI.getInstance().getRServices().evalAndGetObject(
						"names(formals('" + m.getName() + "'))")).getValue();
				boolean hasDotDotDot = formalArgs.length > 0 && formalArgs[formalArgs.length - 1].equals("...");

				String paramsStr = "";
				String varargsStr = "org.bioconductor.packages.rservices.RObject[] params=new org.bioconductor.packages.rservices.RObject[args.length+"
						+ (m.getParameterTypes().length - 1) + "];";

				for (int j = 0; j < (m.getParameterTypes().length - 1); ++j) {
					Class<?> pclass = m.getParameterTypes()[j];
					String pclassName = null;
					if (pclass.isArray()) {
						pclassName = pclass.getComponentType().getName() + "[]";
					} else {
						pclassName = pclass.getName();
					}

					mHeader += (" " + pclassName + " " + "p" + j + (j == m.getParameterTypes().length - 2 ? "" : ","));
					mHeaderAsRef += (" " + pclassName + " " + "p" + j + (j == m.getParameterTypes().length - 2 ? ""
							: ","));

					paramsStr += "p" + j + (j == m.getParameterTypes().length - 2 ? "" : ",");

					if (j == (m.getParameterTypes().length - 2)) {
						if (hasDotDotDot) {
							mHeader += ", org.bioconductor.packages.rservices.RObject... args ";
							mHeaderAsRef += ", org.bioconductor.packages.rservices.RObject... args ";
						}
					}

					varargsStr += "params[" + j + "]=p" + j + ";";

				}

				outputWriter
						.print(mHeader
								+ ") throws java.rmi.RemoteException {remoting.RServices r=server.DirectJNI.getInstance().getRServices();");
				outputWriterRemote.print(mHeader + ") throws java.rmi.RemoteException ;\n");
				outputWriterRemoteImpl
						.print(mHeader
								+ ") throws java.rmi.RemoteException {remoting.RServices r=server.DirectJNI.getInstance().getRServices();");

				if (fattrs.isPublishToWeb()) {
					outputWriterWebservice
							.print(mHeader
									+ ") throws Exception { remoting.RServices r=null;"
									+ (fattrs.isSingleThreadedWeb() ? "server.DirectJNI.init();r=server.DirectJNI.getInstance().getRServices();"
											: "r=(remoting.RServices)uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();"));
				}

				String callStrImpl = null;
				if (hasDotDotDot) {
					varargsStr += "for (int k=0; k<args.length;++k) {params[" + (m.getParameterTypes().length - 1)
							+ "+k]=args[k];}";
					callStrImpl = varargsStr + m.getReturnType().getName() + " result= (" + m.getReturnType().getName()
							+ ")r.call(\"" + m.getName() + "\", params);";

				} else {
					callStrImpl = m.getReturnType().getName() + " result= (" + m.getReturnType().getName()
							+ ")r.call(\"" + m.getName() + "\"," + paramsStr + ");";
				}

				String callStrRemoteImpl = " try{ " + m.getReturnType().getName() + " result= ((" + className
						+ ")r.getPackage(\"" + shortClassName + "\"))." + m_name + "(" + paramsStr
						+ (hasDotDotDot ? ",args" : "") + ");";
				outputWriter.print(callStrImpl);
				outputWriterRemoteImpl.print(callStrRemoteImpl);
				if (fattrs.isPublishToWeb())
					outputWriterWebservice.print(callStrRemoteImpl);

				if (m.getReturnType() != null) {

					outputWriter.println("return result;");
					outputWriterRemoteImpl.println("return result;");
					if (fattrs.isPublishToWeb())
						outputWriterWebservice.println("return result;");

				}

				outputWriter.println("}");
				outputWriterRemoteImpl
						.println("} catch (Exception ex) {throw new java.rmi.RemoteException( util.Utils.getStackTraceAsString(ex) );}}");
				if (fattrs.isPublishToWeb()) {
					outputWriterWebservice
							.println("} catch (Exception ex) {throw new Exception( util.Utils.getStackTraceAsString(ex) );} finally {"
									+ (fattrs.isSingleThreadedWeb() ? ""
											: "uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);")
									+ "} }");
				}

				outputWriter
						.print(mHeaderAsRef
								+ ") throws java.rmi.RemoteException { remoting.RServices r=server.DirectJNI.getInstance().getRServices(); ");
				outputWriterRemote.print(mHeaderAsRef + ") throws java.rmi.RemoteException ;\n");
				outputWriterRemoteImpl
						.print(mHeaderAsRef
								+ ") throws java.rmi.RemoteException { remoting.RServices r=server.DirectJNI.getInstance().getRServices(); ");

				String callStrImplAsRef = null;
				if (hasDotDotDot) {
					callStrImplAsRef = varargsStr + m.getReturnType().getName() + " result= ("
							+ m.getReturnType().getName() + ")r.callAsReference(\"" + m.getName() + "\", params);";

				} else {
					callStrImplAsRef = m.getReturnType().getName() + " result= (" + m.getReturnType().getName()
							+ ")r.callAsReference(\"" + m.getName() + "\", " + paramsStr + ");";
				}

				String callStrRemoteImplAsRef = " try{ " + m.getReturnType().getName() + " result= ((" + className
						+ ")r.getPackage(\"" + shortClassName + "\"))." + m_name + "AsReference" + "(" + paramsStr
						+ (hasDotDotDot ? ",args" : "") + ");";

				outputWriter.print(callStrImplAsRef);
				outputWriterRemoteImpl.print(callStrRemoteImplAsRef);

				if (m.getReturnType() != null) {

					outputWriter.println("return result;");
					outputWriterRemoteImpl.println("return result;");

				}

				outputWriter.println("}");
				outputWriterRemoteImpl
						.println("} catch (Exception ex) {throw new java.rmi.RemoteException( util.Utils.getStackTraceAsString(ex) );}}");

			}

			outputWriter.println("}");
			outputWriter.close();
			outputWriterRemote.println("}");
			outputWriterRemote.close();
			outputWriterRemoteImpl.println("}");
			outputWriterRemoteImpl.close();

			if (_webPublishingEnabled) {

				if (System.getProperty("ws.r.api") != null && System.getProperty("ws.r.api").equalsIgnoreCase("true")) {
					outputWriterWebservice
							.println("\npublic String evaluateExpressions(String expression, int n) throws Exception { "
									+ "remoting.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null
											&& System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "server.DirectJNI.init();r=server.DirectJNI.getInstance().getRServices();"
											: "r=(remoting.RServices)uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { String result =  r.evaluateExpressions(expression,n); return result;} catch (Exception ex) {throw new Exception(util.Utils.getStackTraceAsString(ex));}"
									+ "finally {uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject call(String methodName, RObject... args) throws Exception { "
									+ "remoting.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null
											&& System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "server.DirectJNI.init();r=server.DirectJNI.getInstance().getRServices();"
											: "r=(remoting.RServices)uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { RObject result =  r.call(methodName, args); return result;} catch (Exception ex) {throw new Exception(util.Utils.getStackTraceAsString(ex));}"
									+ "finally {uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void callAndAssignName(String varName, String methodName, RObject... args) throws Exception { "
									+ "remoting.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null
											&& System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "server.DirectJNI.init();r=server.DirectJNI.getInstance().getRServices();"
											: "r=(remoting.RServices)uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { r.callAndAssignName(varName, methodName, args); } catch (Exception ex) {throw new Exception(util.Utils.getStackTraceAsString(ex));}"
									+ "finally {uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject evalAndGetObject(String expression) throws Exception { "
									+ "remoting.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null
											&& System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "server.DirectJNI.init();r=server.DirectJNI.getInstance().getRServices();"
											: "r=(remoting.RServices)uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { RObject result =  r.evalAndGetObject(expression); return result;} catch (Exception ex) {throw new Exception(util.Utils.getStackTraceAsString(ex));}"
									+ "finally {uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String consoleSubmit(String expression) throws Exception { "
									+ "remoting.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null
											&& System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "server.DirectJNI.init();r=server.DirectJNI.getInstance().getRServices();"
											: "r=(remoting.RServices)uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { String result =  r.consoleSubmit(expression); return result;} catch (Exception ex) {throw new Exception(util.Utils.getStackTraceAsString(ex));}"
									+ "finally {uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String logOn(String session, String login, String pwd, java.util.HashMap<String, Object> options) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return http.RHttpProxy.logOn(System.getProperty(\"http.frontend.url\"), session, login, pwd, options);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void logOff(String session) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {http.RHttpProxy.logOff(System.getProperty(\"http.frontend.url\"), session);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void interrupt(String session) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {http.RHttpProxy.interrupt(System.getProperty(\"http.frontend.url\"), session);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulEvaluate(String session, String expression) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"evaluate\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulEvaluateExpressions(String session, String expression, int n) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"evaluate\", new Class[]{String.class, int.class}, new Object[]{expression,n});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject statefulCall(String session, String methodName, RObject... args) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (RObject)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"call\", new Class[]{String.class, RObject[].class}, new Object[]{methodName,args});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void statefulCallAndAssignName(String session, String varName, String methodName, RObject... args) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"callAndAssignName\", new Class[]{String.class, String.class, RObject[].class}, new Object[]{varName,methodName,args});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void statefulPutObjectAndAssignName(String session, RObject obj, String name) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"putObjectAndAssignName\", new Class[]{RObject.class, String.class}, new Object[]{obj,name});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject statefulEvalAndGetObject(String session, String expression) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (RObject)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"evalAndGetObject\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulConsoleSubmit(String session, String expression) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"consoleSubmit\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulPrint(String session, String expression) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"print\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulPrintExpressions(String session, String[] expressions) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"printExpressions\", new Class[]{String[].class}, new Object[]{expressions});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulSourceFromResource(String session, String resource) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"sourceFromResource\", new Class[]{String.class}, new Object[]{resource});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulSourceFromBuffer(String session, StringBuffer buffer) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"sourceFromBuffer\", new Class[]{StringBuffer.class}, new Object[]{buffer});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statefulGetStatus(String session) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getStatus\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void statefulStop(String session) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try { http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"stop\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String[] statefulGetWorkingDirectoryFileNames(String session) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (String[])http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getWorkingDirectoryFileNames\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic remoting.FileDescription[] statefulGetWorkingDirectoryFileDescriptions(String session) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (remoting.FileDescription[])http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getWorkingDirectoryFileDescriptions\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic remoting.FileDescription statefulGetWorkingDirectoryFileDescription(String session,String fileName) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (remoting.FileDescription)http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getWorkingDirectoryFileDescription\", new Class[]{String.class}, new Object[]{fileName} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void statefulCreateWorkingDirectoryFile(String session,String fileName) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"createWorkingDirectoryFile\", new Class[]{String.class}, new Object[]{fileName} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void statefulRemoveWorkingDirectoryFile(String session,String fileName) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"removeWorkingDirectoryFile\", new Class[]{String.class}, new Object[]{fileName} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic byte[] statefulReadWorkingDirectoryFileBlock(String session, String fileName,long offset, int blocksize) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {return (byte[])http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"readWorkingDirectoryFileBlock\", new Class[]{String.class, long.class, int.class}, new Object[]{fileName,offset,blocksize} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void statefulAppendBlockToWorkingDirectoryFile(String session,String fileName, byte[] block) throws Exception { "
									+ "injectSystemProperties(true);"
									+ "try {http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"appendBlockToWorkingDirectoryFile\", new Class[]{String.class, byte[].class}, new Object[]{fileName,block} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

				}

				outputWriterWebservice
						.println("\npublic void _exportStandardTypesToWSDL(RObject robject, RDataFrame rdataframe, RList rlist, REnvironment renvironment,"
								+ " RFactor rfactor, RUnknown runknown, RArray rarray, RMatrix rmatrix, RVector rvector, RNumeric rnumeric, RInteger rinteger,"
								+ " RChar rchar, RComplex rcomplex, RLogical rlogical, RRaw rraw, RNamedArgument rnamedargument, RObjectName robjectname){}");

				outputWriterWebservice.println("\npublic void _exportMappedTypesToWSDL(");

				int counter = 0;
				for (String k : DirectJNI._s4BeansMappingRevert.keySet()) {
					if (counter > 0) {
						outputWriterWebservice.println(",");
					}
					outputWriterWebservice.println(k + " p" + counter);
					++counter;
				}

				for (String v : DirectJNI._factoriesMapping.values()) {
					if (counter > 0)
						outputWriterWebservice.println(",");
					outputWriterWebservice.println(v + " p" + counter);
					++counter;
				}

				outputWriterWebservice.println("){}");

				outputWriterWebservice.println("}");

				outputWriterWebservice.close();

			}

		}

	}

	public static void generateS4BeanRef() throws Exception {

		for (Iterator iter = DirectJNI._s4BeansHash.keySet().iterator(); iter.hasNext();) {

			String className = (String) iter.next();

			Field[] fields = DirectJNI._s4BeansHash.get(className).getDeclaredFields();
			final String rclass = DirectJNI._s4BeansMappingRevert.get(className);

			final String[][] slotsContainer = new String[1][];
			server.DirectJNI.getInstance().runR(new server.ExecutionUnit() {
				public void run(org.rosuda.JRI.Rengine e) {
					long slotsId = e.rniEval(e.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
					slotsContainer[0] = e.rniGetStringArray(e.rniGetAttr(slotsId, "names"));
				}
			});

			String classShortName = className.substring(className.lastIndexOf('.') + 1);

			String outputFileName = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR
					+ className.replace('.', Globals.FILE_SEPARATOR) + "Ref" + ".java";
			new File(outputFileName.substring(0, outputFileName.lastIndexOf(FILE_SEPARATOR))).mkdirs();

			log.info("output file:" + outputFileName);
			PrintWriter outputWriter = new PrintWriter(outputFileName);

			outputWriter.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriter.println("public class " + classShortName + "Ref" + " extends " + classShortName
					+ " implements mapping.ReferenceInterface, java.io.Externalizable {");

			outputWriter.println("private long[] _rObjectIdHolder;\n");
			outputWriter.println("private String _slotsPath;\n");
			outputWriter.println("private remoting.AssignInterface _assignInterface;\n");
			outputWriter.println("public long getRObjectId() {");
			outputWriter.println("	return _rObjectIdHolder[0];");
			outputWriter.println("}\n");
			outputWriter.println("public String getSlotsPath() {");
			outputWriter.println("  	return _slotsPath;");
			outputWriter.println("}\n");

			outputWriter.println("public void setAssignInterface(remoting.AssignInterface assignInterface) {");
			outputWriter.println("  	_assignInterface=assignInterface;");
			outputWriter.println("}\n");
			outputWriter.println("public remoting.AssignInterface getAssignInterface() {");
			outputWriter.println("	return _assignInterface;");
			outputWriter.println("}\n");

			outputWriter
					.println("\npublic org.bioconductor.packages.rservices.RObject extractRObject() {try {return _assignInterface.getObjectFromReference(this);} catch (java.rmi.RemoteException re) {throw new RuntimeException(util.Utils.getStackTraceAsString(re));}}\n");

			String nullifyFields = "";
			for (int i = 0; i < fields.length; ++i)
				nullifyFields += "super.set" + Utils.captalizeFirstChar(fields[i].getName()) + "(null);";
			outputWriter.println("public " + classShortName + "Ref " + "(){ super(); _rObjectIdHolder=new long[1];"
					+ nullifyFields + " };");
			outputWriter
					.println("public "
							+ classShortName
							+ "Ref "
							+ "(long rObjectId, String slotsPath){ super(); _rObjectIdHolder=new long[1]; _rObjectIdHolder[0]=rObjectId; _slotsPath=slotsPath; "
							+ nullifyFields + "};");
			outputWriter
					.println("public "
							+ classShortName
							+ "Ref "
							+ "(long[] rObjectIdHolder, String slotsPath){ super(); _rObjectIdHolder=rObjectIdHolder; _slotsPath=slotsPath; "
							+ nullifyFields + "};");

			for (int i = 0; i < fields.length; ++i) {
				Field f = fields[i];
				String getterName = (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class) ? "is"
						: "get")
						+ Utils.captalizeFirstChar(f.getName());
				String setterName = "set" + Utils.captalizeFirstChar(f.getName());

				if (f.getDeclaringClass().getName().equals(className)) {
					outputWriter.print("\n public " + "void " + setterName + "(" + f.getType().getName() + " p0" + ")");
					outputWriter
							.print("{ "
									+ "if ( p0 instanceof mapping.ReferenceInterface ) {"
									+ "super."
									+ setterName
									+ "(p0);"
									+ "} else "
									+ "   {try {_rObjectIdHolder[0]=_assignInterface.assign(_rObjectIdHolder[0],_slotsPath+\"@\"+\""
									+ slotsContainer[0][i]
									+ "\",p0);} \n catch (Exception ex) {ex.printStackTrace();}}" + " }\n");
					outputWriter.print("\n public " + f.getType().getName() + " " + getterName + "(){");

					if (!DirectJNI._abstractFactories.contains(f.getType().getName())) {

						outputWriter.print("if (super." + getterName + "()==null){ ");
						outputWriter.print(f.getType().getName() + "Ref" + " result=new " + f.getType().getName()
								+ "Ref(_rObjectIdHolder,_slotsPath+\"@\"+\"" + slotsContainer[0][i]
								+ "\"); result.setAssignInterface(_assignInterface); " + "super." + setterName
								+ "(result);" + "} return " + "super." + getterName + "();");
					} else {
						outputWriter.print(" return null;/* !!!!!! to be changed */ ");
					}
					outputWriter.print("}");

				}
			}

			outputWriter.println(

			"public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {"
					+ "	out.writeLong(_rObjectIdHolder[0]);" + "	out.writeUTF(_slotsPath);"
					+ "	out.writeObject(_assignInterface);");

			if (true) {

				outputWriter
						.print("java.lang.reflect.Field[] fields= "
								+ classShortName
								+ ".class.getDeclaredFields();"
								+ "java.util.Vector<java.lang.reflect.Field> nonNullFields=new java.util.Vector<java.lang.reflect.Field>();"
								+ "for (int i=0;i<fields.length;++i) fields[i].setAccessible(true);"
								+ "try {"

								+ "	for (int i=0;i<fields.length;++i) {"
								+ "mapping.ReferenceInterface fValue=(mapping.ReferenceInterface)fields[i].get(this);"
								+ "if ( fValue!= null && (!fValue.getAssignInterface().equals(_assignInterface) || fValue.getRObjectId()!=_rObjectIdHolder[0] ||  server.DirectJNI.hasDistributedReferences(fValue)) ) {"
								+ "   nonNullFields.add(fields[i]);" + "}}" + "	out.writeInt(nonNullFields.size());"
								+ "	for (java.lang.reflect.Field f:nonNullFields) {" + "		out.writeUTF(f.getName());"
								+ "       out.writeObject(f.get(this));" + "	}" + "}" + "catch (Exception e) {"
								+ "	e.printStackTrace();"
								+ "} finally {for (int i=0;i<fields.length;++i) fields[i].setAccessible(false);}");

			} else {
				outputWriter.print("int counter=0;");
				for (int i = 0; i < fields.length; ++i) {
					outputWriter.print(" if (super.get" + Utils.captalizeFirstChar(fields[i].getName())
							+ "()!=null) ++counter;");
				}
				outputWriter.println("out.writeInt(counter);");
				for (int i = 0; i < fields.length; ++i) {
					Field f = fields[i];
					outputWriter.print(" if (super.get" + Utils.captalizeFirstChar(f.getName()) + "()!=null) {");
					outputWriter.print(" out.writeUTF(\"" + f.getName() + "\");");
					outputWriter.print(" out.writeObject(super."
							+ (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class) ? "is" : "get")
							+ Utils.captalizeFirstChar(f.getName()) + "());}");

				}
			}

			outputWriter.println("}");
			outputWriter
					.println("public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {"
							+ "	_rObjectIdHolder[0]=in.readLong();"
							+ "	_slotsPath=in.readUTF();"
							+ "	_assignInterface=(remoting.AssignInterface)in.readObject();");

			outputWriter.println("int counter=in.readInt();  if (counter>0) {");
			outputWriter.println("try { for (int i=0; i<counter; ++i) {String fname=in.readUTF();");
			outputWriter.println("java.lang.reflect.Field f=" + classShortName + ".class.getDeclaredField(fname);");
			outputWriter.println("f.setAccessible(true);");
			outputWriter.println("f.set(this, in.readObject());");
			outputWriter.println("f.setAccessible(false);");

			// outputWriter.println("java.lang.reflect.Method
			// setter="+classShortName
			// +".class.getMethod(\"set\"+util.Utils.captalizeFirstChar(fname),
			// new Class[]{f.getType()} );");
			// outputWriter.println("setter.invoke(this, new
			// Object[]{in.readObject()}); "

			outputWriter.println("  }} catch (Exception e) {e.printStackTrace();} ");

			outputWriter.println("}");
			outputWriter.println("}");

			outputWriter.println("public String toString() {" + "StringBuffer result=new StringBuffer();" + "try {"
					+ "result.append(\"A Reference to an object of Class \\\"" + classShortName
					+ "\\\" on the R servant "
					+ "<\"+_assignInterface.getName()+\">  [\"+_rObjectIdHolder[0]+\"/\"+_slotsPath+\"]\\n\");");
			for (int i = 0; i < fields.length; ++i) {
				Field f = fields[i];

				outputWriter.print("result.append(" + "\"Field \\\"" + f.getName() + "\\\":\\n\");");
				outputWriter.print("if (super.get" + Utils.captalizeFirstChar(f.getName())
						+ "()!=null) result.append(util.Utils.indent (super.get"
						+ Utils.captalizeFirstChar(f.getName()) + "().toString(),1)); "
						+ "else result.append(\"null(\"+_rObjectIdHolder[0]+\"@" + slotsContainer[0][i] + ")\\n\");");
			}
			outputWriter.println("} catch (java.rmi.RemoteException e) {e.printStackTrace();}"
					+ "return result.toString();" + "}");

			outputWriter.println("public boolean equals(Object inputObject) {");
			outputWriter.println("if (inputObject==null || !(inputObject instanceof " + classShortName + "Ref"
					+ ")) return false;");
			outputWriter.println("return  ((" + classShortName + "Ref)"
					+ "inputObject)._assignInterface.equals( _assignInterface ) && ((" + classShortName + "Ref"
					+ ")inputObject)._rObjectIdHolder[0]==_rObjectIdHolder[0] && ((" + classShortName + "Ref"
					+ ")inputObject)._slotsPath.equals(_slotsPath);");
			outputWriter.println("}");

			outputWriter.println("}");
			outputWriter.close();
		}

	}

	public final static String PUBLISH_S4_HEADER = "export____";

	public static boolean _webPublishingEnabled = false;

	public static HashMap<String, HashMap<String, FAttributes>> _functionsToPublish = new HashMap<String, HashMap<String, FAttributes>>();

	public static String getRClass(Class<?> javaClass) {

		if (RLogical.class.equals(javaClass))
			return "logical";
		else if (RInteger.class.equals(javaClass))
			return "integer";
		else if (RNumeric.class.equals(javaClass))
			return "numeric";
		else if (RComplex.class.equals(javaClass))
			return "complex";
		else if (RChar.class.equals(javaClass))
			return "character";
		else if (RMatrix.class.equals(javaClass))
			return "matrix";
		else if (RArray.class.equals(javaClass))
			return "array";
		else if (RList.class.equals(javaClass))
			return "list";
		else if (RDataFrame.class.equals(javaClass))
			return "data.frame";
		else if (RFactor.class.equals(javaClass))
			return "factor";
		else if (RFactor.class.equals(javaClass))
			return "factor";
		else if (REnvironment.class.equals(javaClass))
			return "environment";
		else {

			String rclass = DirectJNI._s4BeansMappingRevert.get(javaClass.getName());
			if (rclass != null) {
				return rclass;
			} else {
				try {
					Method getDataMethod = javaClass.getMethod("getData", (Class[]) null);
					if (getDataMethod != null && javaClass.getName().endsWith("Factory")) {
						String shortName = javaClass.getName().substring(javaClass.getName().lastIndexOf('.') + 1);
						return shortName.substring(0, shortName.length() - "Factory".length());
					}

				} catch (NoSuchMethodException ex) {
					ex.printStackTrace();
				}
			}
		}
		return null;
	}

	public static class FAttributes {
		private String renameTo = null;

		private boolean publishToWeb = false;

		private boolean singleThreadedWeb = false;

		public String getRenameTo() {
			return renameTo;
		}

		public boolean isPublishToWeb() {
			return publishToWeb;
		}

		public void setPublishToWeb(boolean publishToWeb) {
			this.publishToWeb = publishToWeb;
		}

		public boolean isSingleThreadedWeb() {
			return singleThreadedWeb;
		}

		public FAttributes(String renameTo, boolean publishToWeb, boolean singleThreadedWeb) {
			this.renameTo = renameTo;
			this.publishToWeb = publishToWeb;
			this.singleThreadedWeb = singleThreadedWeb;
		}

		public String toString() {
			return "{renameTo=" + renameTo + "  publishToWeb=" + publishToWeb + " singleThreadedWeb="
					+ singleThreadedWeb + "}";
		}

	}

	public static FAttributes getAttributes(Method m) {

		HashMap<String, FAttributes> sigMap = Globals._functionsToPublish.get(m.getName());

		String signature = "";

		for (int j = 0; j < (m.getParameterTypes().length - 1); ++j) {
			signature += getRClass(m.getParameterTypes()[j]) + ",";
		}

		for (String s : sigMap.keySet()) {
			if (signature.startsWith(s))
				return sigMap.get(s);
		}

		return null;
	}

}
