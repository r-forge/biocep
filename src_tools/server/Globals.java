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

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.kchine.r.RArray;
import org.kchine.r.RChar;
import org.kchine.r.RComplex;
import org.kchine.r.RDataFrame;
import org.kchine.r.REnvironment;
import org.kchine.r.RFactor;
import org.kchine.r.RInteger;
import org.kchine.r.RList;
import org.kchine.r.RLogical;
import org.kchine.r.RMatrix;
import org.kchine.r.RNumeric;
import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.Utils;
import org.kchine.rpf.PoolUtils;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class Globals {

	public static final char FILE_SEPARATOR = System.getProperty("file.separator").charAt(0);

	public static String GEN_ROOT = null;
	public static String MAPPING_JAR_NAME = null;
	public static String GEN_ROOT_SRC = null;
	public static String GEN_ROOT_LIB = null;
	public static final String TEMP_JARS_PREFIX = "_temp";
	public static final Log log = org.apache.commons.logging.LogFactory.getLog(Globals.class);

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

	public static void regenerateRPackageClass(boolean embedRScript) throws Exception {

		for (Iterator<?> iter = DirectJNI._rPackageInterfacesHash.keySet().iterator(); iter.hasNext();) {

			String className = (String) iter.next();
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);

			String outputFileName = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR + className.replace('.', Globals.FILE_SEPARATOR) + "Impl.java";
			new File(outputFileName.substring(0, outputFileName.lastIndexOf(FILE_SEPARATOR))).mkdirs();
			log.info("output file:" + outputFileName);
			PrintWriter outputWriter = new PrintWriter(outputFileName);
			outputWriter.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriter.println("public class " + shortClassName + "Impl" + " implements " + shortClassName + " {");
			outputWriter.println("private " + className.substring(className.lastIndexOf('.') + 1) + "Impl" + "(){ init(); }");
			outputWriter.println("public void init() { "
					+ (embedRScript ? "try {org.kchine.r.server.DirectJNI.getInstance().getRServices().sourceFromResource(\"/"
							+ className.substring(0, className.lastIndexOf('.')).replace('.', '/') + "/" + className.substring(className.lastIndexOf('.') + 1)
							+ ".R\");} catch (Exception e) {e.printStackTrace();}\n" : "") + " }");

			String outputFileNameRemote = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR + className.replace('.', Globals.FILE_SEPARATOR) + ".java";
			new File(outputFileNameRemote.substring(0, outputFileNameRemote.lastIndexOf(FILE_SEPARATOR))).mkdirs();
			log.info("output remote file:" + outputFileNameRemote);
			PrintWriter outputWriterRemote = new PrintWriter(outputFileNameRemote);
			outputWriterRemote.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriterRemote.println("public interface " + shortClassName + " extends org.kchine.r.server.RPackage  {");
			outputWriter.println("private static " + shortClassName + " _packageInstance = null;private static Integer _lock = new Integer(0);");
			outputWriter
					.println("public static "
							+ shortClassName
							+ " getInstance() {	if (_packageInstance != null) return _packageInstance; synchronized (_lock) { if (_packageInstance == null) { _packageInstance = new "
							+ shortClassName + "Impl" + "(); }	return _packageInstance;}}");

			String outputFileNameRemoteImpl = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR + className.replace('.', Globals.FILE_SEPARATOR) + "ImplRemote"
					+ ".java";
			new File(outputFileNameRemoteImpl.substring(0, outputFileNameRemoteImpl.lastIndexOf(FILE_SEPARATOR))).mkdirs();
			log.info("output remote impl file:" + outputFileNameRemoteImpl);
			PrintWriter outputWriterRemoteImpl = new PrintWriter(outputFileNameRemoteImpl);
			outputWriterRemoteImpl.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriterRemoteImpl.println("public class " + shortClassName + "ImplRemote extends java.rmi.server.UnicastRemoteObject implements "
					+ shortClassName + " {");
			outputWriterRemoteImpl.println("public " + className.substring(className.lastIndexOf('.') + 1) + "ImplRemote"
					+ "() throws java.rmi.RemoteException { super(); };");

			PrintWriter outputWriterWebservice = null;
			if (_webPublishingEnabled) {
				String outputFileNameWebservice = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR + className.replace('.', Globals.FILE_SEPARATOR) + "Web"
						+ ".java";
				new File(outputFileNameWebservice.substring(0, outputFileNameWebservice.lastIndexOf(FILE_SEPARATOR))).mkdirs();
				log.info("output web service file:" + outputFileNameWebservice);
				outputWriterWebservice = new PrintWriter(outputFileNameWebservice);
				outputWriterWebservice.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
				outputWriterWebservice
						.println("import javax.jws.WebService;\nimport org.kchine.r.*;" +
								"import static  org.kchine.rpf.PoolUtils.*;" 
								+"import org.apache.commons.httpclient.HttpClient;"
								+"import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;"
								+"import org.kchine.r.server.RServices;" 
								+"import org.kchine.r.server.http.RHttpProxy;"
								+"@WebService");
				outputWriterWebservice.println("public class " + shortClassName + "Web {");
				outputWriterWebservice.println("public " + className.substring(className.lastIndexOf('.') + 1) + "Web" + "(){};");
			}

			Vector<Method> methodsVector = new Vector<Method>();
			Vector<?> classes = DirectJNI._rPackageInterfacesHash.get(className);
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

				String mHeader = " public " + (m.getReturnType() == null ? "void" : m.getReturnType().getName()) + " " + m_name + "(";
				String mHeaderAsRef = " public " + (m.getReturnType() == null ? "void" : m.getReturnType().getName()) + " " + m_name + "AsReference" + "(";
				String mHeaderStatefull = " public " + (m.getReturnType() == null ? "void" : m.getReturnType().getName()) + " " + m_name+"Statefull" + "( String sessionId,";
				String mHeaderAsObjectName = " public " + (m.getReturnType() == null ? "void" : m.getReturnType().getName()) + " " + m_name+"GetObjectName" + "( String sessionId,";

				String[] formalArgs = ((RChar) DirectJNI.getInstance().getRServices().getObject("names(formals('" + m.getName() + "'))")).getValue();
				boolean hasDotDotDot = formalArgs.length > 0 && formalArgs[formalArgs.length - 1].equals("...");

				String paramsStr = "";
				String varargsStr = "Object[] params=new Object[args.length+" + (m.getParameterTypes().length - 1) + "];";

				for (int j = 0; j < (m.getParameterTypes().length - 1); ++j) {
					Class<?> pclass = m.getParameterTypes()[j];
					String pclassName = null;
					if (pclass.isArray()) {
						pclassName = pclass.getComponentType().getName() + "[]";
					} else {
						pclassName = pclass.getName();
					}

					mHeader += (" " + pclassName + " " + "p" + j + (j == m.getParameterTypes().length - 2 ? "" : ","));
					mHeaderAsRef += (" " + pclassName + " " + "p" + j + (j == m.getParameterTypes().length - 2 ? "" : ","));
					mHeaderStatefull += (" " + pclassName + " " + "p" + j + (j == m.getParameterTypes().length - 2 ? "" : ","));
					mHeaderAsObjectName += (" " + pclassName + " " + "p" + j + (j == m.getParameterTypes().length - 2 ? "" : ","));
					
					paramsStr += "p" + j + (j == m.getParameterTypes().length - 2 ? "" : ",");

					if (j == (m.getParameterTypes().length - 2)) {
						if (hasDotDotDot) {
							mHeader += ", Object... args ";
							mHeaderAsRef += ", Object... args ";
							mHeaderStatefull += ", Object... args ";
							mHeaderAsObjectName  += ", Object... args ";
						}
					}

					varargsStr += "params[" + j + "]=p" + j + ";";

				}

				outputWriter.print(mHeader + ") throws java.rmi.RemoteException {org.kchine.r.server.RServices r=org.kchine.r.server.DirectJNI.getInstance().getRServices();");
				outputWriterRemote.print(mHeader + ") throws java.rmi.RemoteException ;\n");
				outputWriterRemoteImpl
						.print(mHeader + ") throws java.rmi.RemoteException {org.kchine.r.server.RServices r=org.kchine.r.server.DirectJNI.getInstance().getRServices();");

				

				String callStrImpl = null;
				if (hasDotDotDot) {
					varargsStr += "for (int k=0; k<args.length;++k) {params[" + (m.getParameterTypes().length - 1) + "+k]=args[k];}";
					callStrImpl = varargsStr + m.getReturnType().getName() + " result= (" + m.getReturnType().getName() + ")r.call(\"" + m.getName()
							+ "\", params);";

				} else {
					callStrImpl = m.getReturnType().getName() + " result= (" + m.getReturnType().getName() + ")r.call(\"" + m.getName() + "\"," + paramsStr
							+ ");";
				}

				String callStrRemoteImpl = " try{ " + m.getReturnType().getName() + " result= ((" + className + ")r.getPackage(\"" + shortClassName + "\"))."
						+ m_name + "(" + paramsStr + (hasDotDotDot ? ",args" : "") + ");";
				outputWriter.print(callStrImpl);
				outputWriterRemoteImpl.print(callStrRemoteImpl);


				if (m.getReturnType() != null) {
					outputWriter.println("return result;");
					outputWriterRemoteImpl.println("return result;");
				}

				outputWriter.println("}");
				outputWriterRemoteImpl.println("} catch (Exception ex) {throw new java.rmi.RemoteException( org.kchine.r.server.Utils.getStackTraceAsString(ex) );}}");
				
				
				if (fattrs.isPublishToWeb()) {
					outputWriterWebservice
							.print(mHeader
									+ ") throws Exception { org.kchine.r.server.RServices r=null;"
									+  "r=(org.kchine.r.server.RServices)org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();");
					outputWriterWebservice.print(callStrRemoteImpl);
					if (m.getReturnType() != null) { outputWriterWebservice.println("return result;"); }
					outputWriterWebservice.println("} catch (Exception ex) {throw new Exception( org.kchine.r.server.Utils.getStackTraceAsString(ex) );} finally {"
							+ "org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);" + "} }");
					

					
					outputWriterWebservice
					.print(mHeaderStatefull
							+ ") throws Exception { " +
							"RServices r = (RServices) org.kchine.r.server.http.RHttpProxy.getDynamicProxy(System.getProperty(\"http.frontend.url\"), sessionId, \"R\", new Class<?>[] { RServices.class }, new HttpClient(new MultiThreadedHttpConnectionManager()));"
									
									+ callStrImpl
									+(m.getReturnType() != null ?  "return result;" : "")
									+" }");

					
					outputWriterWebservice
					.print(mHeaderAsObjectName
							+ ") throws Exception { " +
							"RServices r = (RServices) org.kchine.r.server.http.RHttpProxy.getDynamicProxy(System.getProperty(\"http.frontend.url\"), sessionId, \"R\", new Class<?>[] { RServices.class }, new HttpClient(new MultiThreadedHttpConnectionManager()));"
									
									+ PoolUtils.replaceAll(callStrImpl, ".call", ".callAndGetObjectName")
									+(m.getReturnType() != null ?  "return result;" : "")
									+" }");
									

					
				}

				outputWriter.print(mHeaderAsRef + ") throws java.rmi.RemoteException { org.kchine.r.server.RServices r=org.kchine.r.server.DirectJNI.getInstance().getRServices(); ");
				outputWriterRemote.print(mHeaderAsRef + ") throws java.rmi.RemoteException ;\n");
				outputWriterRemoteImpl.print(mHeaderAsRef
						+ ") throws java.rmi.RemoteException { org.kchine.r.server.RServices r=org.kchine.r.server.DirectJNI.getInstance().getRServices(); ");

				String callStrImplAsRef = null;
				if (hasDotDotDot) {
					callStrImplAsRef = varargsStr + m.getReturnType().getName() + " result= (" + m.getReturnType().getName() + ")r.callAndGetReference(\""
							+ m.getName() + "\", params);";

				} else {
					callStrImplAsRef = m.getReturnType().getName() + " result= (" + m.getReturnType().getName() + ")r.callAndGetReference(\"" + m.getName()
							+ "\", " + paramsStr + ");";
				}

				String callStrRemoteImplAsRef = " try{ " + m.getReturnType().getName() + " result= ((" + className + ")r.getPackage(\"" + shortClassName
						+ "\"))." + m_name + "AsReference" + "(" + paramsStr + (hasDotDotDot ? ",args" : "") + ");";

				outputWriter.print(callStrImplAsRef);
				outputWriterRemoteImpl.print(callStrRemoteImplAsRef);

				if (m.getReturnType() != null) {

					outputWriter.println("return result;");
					outputWriterRemoteImpl.println("return result;");

				}

				outputWriter.println("}");
				outputWriterRemoteImpl.println("} catch (Exception ex) {throw new java.rmi.RemoteException( org.kchine.r.server.Utils.getStackTraceAsString(ex) );}}");

			}

			outputWriter.println("}");
			outputWriter.close();
			outputWriterRemote.println("}");
			outputWriterRemote.close();
			outputWriterRemoteImpl.println("}");
			outputWriterRemoteImpl.close();

			if (_webPublishingEnabled) {

				if (System.getProperty("ws.r.api") == null || System.getProperty("ws.r.api").equals("")
						|| !System.getProperty("ws.r.api").equalsIgnoreCase("false")) {

					outputWriterWebservice
							.println("\npublic String logOn(String session, String login, String pwd, String[] options) throws Exception { "
									+ "java.util.HashMap<String,Object> map=new java.util.HashMap<String,Object>();"
									+ "for (int i=0; i<options.length; ++i) {"
									+ "	int equalIdx=options[i].indexOf('=');"
									+ "	if (equalIdx!=-1) {"
									+ "		map.put(options[i].substring(0,equalIdx),options[i].substring(equalIdx+1));"
									+ "	}"
									+ "}"
									+ "map.put(\"urls\", new java.net.URL[]{http.InterceptorServlet.getRMappingUrl()});"
									+ "try {return org.kchine.r.server.http.RHttpProxy.logOn(System.getProperty(\"http.frontend.url\"), session, login, pwd, map);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void logOff(String session) throws Exception { "
									+ "try {org.kchine.r.server.http.RHttpProxy.logOff(System.getProperty(\"http.frontend.url\"), session);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
					.println("\npublic void logOffAndKill(String session) throws Exception { "
							+ "try {org.kchine.r.server.http.RHttpProxy.logOffAndKill(System.getProperty(\"http.frontend.url\"), session);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
							+ "}");
					
					outputWriterWebservice
							.println("\npublic void interrupt(String session) throws Exception { "
									+ "try {org.kchine.r.server.http.RHttpProxy.interrupt(System.getProperty(\"http.frontend.url\"), session);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String evaluate(String session, String expression) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"evaluate\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String evaluateExpressions(String session, String expression, int n) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"evaluate\", new Class[]{String.class, int.class}, new Object[]{expression,n});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject call(String session, String methodName, Object... args) throws Exception { "
									+ "try {return (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"call\", new Class[]{String.class, Object[].class}, new Object[]{methodName,args});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic Object callAndConvert(String session, String methodName, Object... args) throws Exception { "
									+ "try {return org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"callAndConvert\", new Class[]{String.class, Object[].class}, new Object[]{methodName,args});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject callAndGetObjectName(String session, String methodName, Object... args) throws Exception { "
									+ "try {return (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"callAndGetObjectName\", new Class[]{String.class, Object[].class}, new Object[]{methodName,args});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void callAndAssign(String session, String varName, String methodName, Object... args) throws Exception { "
									+ "try {org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"callAndAssign\", new Class[]{String.class, String.class, RObject[].class}, new Object[]{varName,methodName,args});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void putAndAssign(String session, Object obj, String name) throws Exception { "
									+ "try {org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"putAndAssign\", new Class[]{Object.class, String.class}, new Object[]{obj,name});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject getObject(String session, String expression) throws Exception { "
									+ "try {return (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getObject\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic Object getObjectConverted(String session, String expression) throws Exception { "
									+ "try {return org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getObjectConverted\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject getObjectName(String session, String expression) throws Exception { "
									+ "try {return (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getObjectName\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject realizeObjectName(String session, RObject objectName) throws Exception { "
									+ "try {return (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"realizeObjectName\", new Class[]{RObject.class}, new Object[]{objectName});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic Object realizeObjectNameConverted(String session, RObject objectName) throws Exception { "
									+ "try {return org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"realizeObjectNameConverted\", new Class[]{RObject.class}, new Object[]{objectName});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject realizeObjectNameAndFreeAllReferences(String session, RObject objectName) throws Exception { "
									+ "try {RObject result= (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"realizeObjectName\", new Class[]{RObject.class}, new Object[]{objectName});"
									+ "org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"freeAllReferences\", null, null);"
									+ "return result;"
									+ "} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}" + "}");

					outputWriterWebservice
							.println("\npublic Object realizeObjectNameConvertedAndFreeAllReferences(String session, RObject objectName) throws Exception { "
									+ "try {Object result=org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"realizeObjectNameConverted\", new Class[]{RObject.class}, new Object[]{objectName});"
									+ "org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"freeAllReferences\", null, null);"
									+ "return result;"
									+ "} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}" + "}");

					outputWriterWebservice
							.println("\npublic void freeAllReferences(String session) throws Exception { "
									+ "try { org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"freeAllReferences\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String consoleSubmit(String session, String expression) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"consoleSubmit\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String print(String session, String expression) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"print\", new Class[]{String.class}, new Object[]{expression});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String printExpressions(String session, String[] expressions) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"printExpressions\", new Class[]{String[].class}, new Object[]{expressions});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String sourceFromResource(String session, String resource) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"sourceFromResource\", new Class[]{String.class}, new Object[]{resource});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String sourceFromBuffer(String session, String buffer) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"sourceFromBuffer\", new Class[]{String.class}, new Object[]{buffer});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String getStatus(String session) throws Exception { "
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getStatus\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void stop(String session) throws Exception { "
									+ "try { org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"stop\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String[] getWorkingDirectoryFileNames(String session) throws Exception { "
									+ "try {return (String[])org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getWorkingDirectoryFileNames\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic org.kchine.r.server.FileDescription[] getWorkingDirectoryFileDescriptions(String session) throws Exception { "
									+ "try {return (org.kchine.r.server.FileDescription[])org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getWorkingDirectoryFileDescriptions\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic org.kchine.r.server.FileDescription getWorkingDirectoryFileDescription(String session,String fileName) throws Exception { "
									+ "try {return (org.kchine.r.server.FileDescription)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getWorkingDirectoryFileDescription\", new Class[]{String.class}, new Object[]{fileName} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void createWorkingDirectoryFile(String session,String fileName) throws Exception { "
									+ "try {org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"createWorkingDirectoryFile\", new Class[]{String.class}, new Object[]{fileName} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void removeWorkingDirectoryFile(String session,String fileName) throws Exception { "
									+ "try {org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"removeWorkingDirectoryFile\", new Class[]{String.class}, new Object[]{fileName} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic byte[] readWorkingDirectoryFileBlock(String session, String fileName,long offset, int blocksize) throws Exception { "
									+ "try {return (byte[])org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"readWorkingDirectoryFileBlock\", new Class[]{String.class, long.class, int.class}, new Object[]{fileName,offset,blocksize} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void appendBlockToWorkingDirectoryFile(String session,String fileName, byte[] block) throws Exception { "
									+ "try {org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"appendBlockToWorkingDirectoryFile\", new Class[]{String.class, byte[].class}, new Object[]{fileName,block} );} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String[] getSvg(String session, String expression, int width, int height) throws Exception{"
									+ "try {return (String[])((java.util.Vector<String>)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getSvg\", new Class[]{String.class,int.class,int.class}, new Object[]{expression,width,height})).toArray(new String[0]);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String pythonExec(String session, String pythonCommand) throws Exception{"
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonExec\", new Class[]{String.class}, new Object[]{pythonCommand});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String pythonExecFromWorkingDirectoryFile(String session, String fileName) throws Exception{"
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonExecFromWorkingDirectoryFile\", new Class[]{String.class}, new Object[]{fileName});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String pythonExceFromResource(String session, String resource) throws Exception {"
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonExceFromResource\", new Class[]{String.class}, new Object[]{resource});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String pythonExecFromBuffer(String session, String buffer) throws Exception {"
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonExecFromBuffer\", new Class[]{String.class}, new Object[]{buffer});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject pythonEval(String session, String pythonCommand) throws Exception {"
									+ "try {return (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonEval\", new Class[]{String.class}, new Object[]{pythonCommand});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic Object pythonEvalAndConvert(String session, String pythonCommand) throws Exception {"
									+ "try {return (Object)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonEvalAndConvert\", new Class[]{String.class}, new Object[]{pythonCommand});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject pythonGet(String session, String name) throws Exception {"
									+ "try {return (RObject)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonGet\", new Class[]{String.class}, new Object[]{name});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic Object pythonGetAndConvert(String session, String name) throws Exception {"
									+ "try {return (Object)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonGetAndConvert\", new Class[]{String.class}, new Object[]{name});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void pythonSet(String session, String name, Object value) throws Exception {"
									+ "try { org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"pythonSet\", new Class[]{String.class,Object.class}, new Object[]{name,value});} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String getPythonStatus(String session) throws Exception {"
									+ "try {return (String)org.kchine.r.server.http.RHttpProxy.invoke(System.getProperty(\"http.frontend.url\"), session, \"R\", \"getPythonStatus\", null, null);} catch (http.TunnelingException te) { te.printStackTrace();throw new Exception(getStackTraceAsString(te));}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statelessEvaluate(String expression, int n) throws Exception { "
									+ "org.kchine.r.server.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null && System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "org.kchine.r.server.DirectJNI.init();r=org.kchine.r.server.DirectJNI.getInstance().getRServices();"
											: "r=(org.kchine.r.server.RServices)org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { String result =  r.evaluate(expression,n); return result;} catch (Exception ex) {throw new Exception(org.kchine.r.server.Utils.getStackTraceAsString(ex));}"
									+ "finally {org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject statelessCall(String methodName, Object... args) throws Exception { "
									+ "org.kchine.r.server.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null && System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "org.kchine.r.server.DirectJNI.init();r=org.kchine.r.server.DirectJNI.getInstance().getRServices();"
											: "r=(org.kchine.r.server.RServices)org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { RObject result =  r.call(methodName, args); return result;} catch (Exception ex) {throw new Exception(org.kchine.r.server.Utils.getStackTraceAsString(ex));}"
									+ "finally {org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic void statelessCallAndAssign(String varName, String methodName, Object... args) throws Exception { "
									+ "org.kchine.r.server.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null && System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "org.kchine.r.server.DirectJNI.init();r=org.kchine.r.server.DirectJNI.getInstance().getRServices();"
											: "r=(org.kchine.r.server.RServices)org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { r.callAndAssign(varName, methodName, args); } catch (Exception ex) {throw new Exception(org.kchine.r.server.Utils.getStackTraceAsString(ex));}"
									+ "finally {org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic RObject statelessGetObject(String expression) throws Exception { "
									+ "org.kchine.r.server.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null && System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "org.kchine.r.server.DirectJNI.init();r=org.kchine.r.server.DirectJNI.getInstance().getRServices();"
											: "r=(org.kchine.r.server.RServices)org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { RObject result =  r.getObject(expression); return result;} catch (Exception ex) {throw new Exception(org.kchine.r.server.Utils.getStackTraceAsString(ex));}"
									+ "finally {org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

					outputWriterWebservice
							.println("\npublic String statelessConsoleSubmit(String expression) throws Exception { "
									+ "org.kchine.r.server.RServices r = null;"
									+ (System.getProperty("SingleThreadedWeb") != null && System.getProperty("SingleThreadedWeb").equalsIgnoreCase("true") ? "org.kchine.r.server.DirectJNI.init();r=org.kchine.r.server.DirectJNI.getInstance().getRServices();"
											: "r=(org.kchine.r.server.RServices)org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();")
									+ "try { String result =  r.consoleSubmit(expression); return result;} catch (Exception ex) {throw new Exception(org.kchine.r.server.Utils.getStackTraceAsString(ex));}"
									+ "finally {org.kchine.rpf.ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);}"
									+ "}");

				}

				outputWriterWebservice
						.println("\npublic void _exportStandardTypesToWSDL(RObject robject, "
								+ " RDataFrame rdataframe, RList rlist, RS3 rs3, REnvironment renvironment,"
								+ " RFactor rfactor, RUnknown runknown, RArray rarray, RMatrix rmatrix, RVector rvector, RNumeric rnumeric, RInteger rinteger,"
								+ " RChar rchar, RComplex rcomplex, RLogical rlogical, RRaw rraw,"
								+ " RNamedArgument rnamedargument, RObjectName robjectname,"
								+ " RDataFrameObjectName rdataframeobjectname, RListObjectName rlistobjectname, RS3ObjectName rs3objectname, REnvironmentObjectName renvironmentobjectname,"
								+ " RFactorObjectName rfactorobjectname, RUnknownObjectName runknownobjectname, RArrayObjectName rarrayobjectname, RMatrixObjectName rmatrixobjectname, RNumericObjectName rnumericobjectname, RIntegerObjectName rintegerobjectname,"
								+ " RCharObjectName rcharobjectname, RComplexObjectName rcomplexobjectname, RLogicalObjectName rlogicalobjectname, RRawObjectName rrawobjectname"

								+ "){}");

				outputWriterWebservice.println("\npublic void _exportMappedTypesToWSDL(");

				int counter = 0;
				for (String k : DirectJNI._s4BeansMappingRevert.keySet()) {
					if (counter > 0) {
						outputWriterWebservice.println(",");
					}
					outputWriterWebservice.println(k + " p" + counter);
					++counter;
				}

				for (String k : DirectJNI._s4BeansMappingRevert.keySet()) {
					if (counter > 0) {
						outputWriterWebservice.println(",");
					}
					outputWriterWebservice.println(k + "ObjectName" + " p" + counter);
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

		for (Iterator<?> iter = DirectJNI._s4BeansHash.keySet().iterator(); iter.hasNext();) {

			String className = (String) iter.next();

			Field[] fields = DirectJNI._s4BeansHash.get(className).getDeclaredFields();
			final String rclass = DirectJNI._s4BeansMappingRevert.get(className);

			final String[][] slotsContainer = new String[1][];
			org.kchine.r.server.DirectJNI.getInstance().runR(new org.kchine.r.server.ExecutionUnit() {
				public void run(org.rosuda.JRI.Rengine e) {
					long slotsId = e.rniEval(e.rniParse("getSlots(\"" + rclass + "\")", 1), 0);
					slotsContainer[0] = e.rniGetStringArray(e.rniGetAttr(slotsId, "names"));
				}
			});

			String classShortName = className.substring(className.lastIndexOf('.') + 1);

			String outputFileName = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR + className.replace('.', Globals.FILE_SEPARATOR) + "Ref" + ".java";
			new File(outputFileName.substring(0, outputFileName.lastIndexOf(FILE_SEPARATOR))).mkdirs();

			log.info("output file:" + outputFileName);
			PrintWriter outputWriter = new PrintWriter(outputFileName);

			outputWriter.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			outputWriter.println("public class " + classShortName + "Ref" + " extends " + classShortName
					+ " implements org.kchine.r.server.ReferenceInterface, java.io.Externalizable {");

			outputWriter.println("private long[] _rObjectIdHolder;\n");
			outputWriter.println("private String _slotsPath;\n");
			outputWriter.println("private org.kchine.r.server.AssignInterface _assignInterface;\n");
			outputWriter.println("public long getRObjectId() {");
			outputWriter.println("	return _rObjectIdHolder[0];");
			outputWriter.println("}\n");
			outputWriter.println("public String getSlotsPath() {");
			outputWriter.println("  	return _slotsPath;");
			outputWriter.println("}\n");

			outputWriter.println("public void setAssignInterface(org.kchine.r.server.AssignInterface assignInterface) {");
			outputWriter.println("  	_assignInterface=assignInterface;");
			outputWriter.println("}\n");
			outputWriter.println("public org.kchine.r.server.AssignInterface getAssignInterface() {");
			outputWriter.println("	return _assignInterface;");
			outputWriter.println("}\n");

			outputWriter
					.println("\npublic org.kchine.r.RObject extractRObject() {try {return _assignInterface.getObjectFromReference(this);} catch (java.rmi.RemoteException re) {throw new RuntimeException(org.kchine.r.server.Utils.getStackTraceAsString(re));}}\n");

			String nullifyFields = "";
			for (int i = 0; i < fields.length; ++i)
				nullifyFields += "super.set" + Utils.captalizeFirstChar(fields[i].getName()) + "(null);";
			outputWriter.println("public " + classShortName + "Ref " + "(){ super(); _rObjectIdHolder=new long[1];" + nullifyFields + " };");
			outputWriter.println("public " + classShortName + "Ref "
					+ "(long rObjectId, String slotsPath){ super(); _rObjectIdHolder=new long[1]; _rObjectIdHolder[0]=rObjectId; _slotsPath=slotsPath; "
					+ nullifyFields + "};");
			outputWriter.println("public " + classShortName + "Ref "
					+ "(long[] rObjectIdHolder, String slotsPath){ super(); _rObjectIdHolder=rObjectIdHolder; _slotsPath=slotsPath; " + nullifyFields + "};");

			for (int i = 0; i < fields.length; ++i) {
				Field f = fields[i];
				String getterName = (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class) ? "is" : "get")
						+ Utils.captalizeFirstChar(f.getName());
				String setterName = "set" + Utils.captalizeFirstChar(f.getName());

				if (f.getDeclaringClass().getName().equals(className)) {
					outputWriter.print("\n public " + "void " + setterName + "(" + f.getType().getName() + " p0" + ")");
					outputWriter.print("{ " + "if ( p0 instanceof org.kchine.r.server.ReferenceInterface ) {" + "super." + setterName + "(p0);" + "} else "
							+ "   {try {_rObjectIdHolder[0]=_assignInterface.assign(_rObjectIdHolder[0],_slotsPath+\"@\"+\"" + slotsContainer[0][i]
							+ "\",p0);} \n catch (Exception ex) {ex.printStackTrace();}}" + " }\n");
					outputWriter.print("\n public " + f.getType().getName() + " " + getterName + "(){");

					if (!DirectJNI._abstractFactories.contains(f.getType().getName())) {

						outputWriter.print("if (super." + getterName + "()==null){ ");
						outputWriter.print(f.getType().getName() + "Ref" + " result=new " + f.getType().getName() + "Ref(_rObjectIdHolder,_slotsPath+\"@\"+\""
								+ slotsContainer[0][i] + "\"); result.setAssignInterface(_assignInterface); " + "super." + setterName + "(result);"
								+ "} return " + "super." + getterName + "();");
					} else {
						outputWriter.print(" return null;/* !!!!!! to be changed */ ");
					}
					outputWriter.print("}");

				}
			}

			outputWriter.println(

			"public void writeExternal(java.io.ObjectOutput out) throws java.io.IOException {" + "	out.writeLong(_rObjectIdHolder[0]);"
					+ "	out.writeUTF(_slotsPath);" + "	out.writeObject(_assignInterface);");

			if (true) {

				outputWriter
						.print("java.lang.reflect.Field[] fields= "
								+ classShortName
								+ ".class.getDeclaredFields();"
								+ "java.util.Vector<java.lang.reflect.Field> nonNullFields=new java.util.Vector<java.lang.reflect.Field>();"
								+ "for (int i=0;i<fields.length;++i) fields[i].setAccessible(true);"
								+ "try {"

								+ "	for (int i=0;i<fields.length;++i) {"
								+ "org.kchine.r.server.ReferenceInterface fValue=(org.kchine.r.server.ReferenceInterface)fields[i].get(this);"
								+ "if ( fValue!= null && (!fValue.getAssignInterface().equals(_assignInterface) || fValue.getRObjectId()!=_rObjectIdHolder[0] ||  org.kchine.r.server.DirectJNI.hasDistributedReferences(fValue)) ) {"
								+ "   nonNullFields.add(fields[i]);" + "}}" + "	out.writeInt(nonNullFields.size());"
								+ "	for (java.lang.reflect.Field f:nonNullFields) {" + "		out.writeUTF(f.getName());" + "       out.writeObject(f.get(this));"
								+ "	}" + "}" + "catch (Exception e) {" + "	e.printStackTrace();"
								+ "} finally {for (int i=0;i<fields.length;++i) fields[i].setAccessible(false);}");

			} else {
				outputWriter.print("int counter=0;");
				for (int i = 0; i < fields.length; ++i) {
					outputWriter.print(" if (super.get" + Utils.captalizeFirstChar(fields[i].getName()) + "()!=null) ++counter;");
				}
				outputWriter.println("out.writeInt(counter);");
				for (int i = 0; i < fields.length; ++i) {
					Field f = fields[i];
					outputWriter.print(" if (super.get" + Utils.captalizeFirstChar(f.getName()) + "()!=null) {");
					outputWriter.print(" out.writeUTF(\"" + f.getName() + "\");");
					outputWriter.print(" out.writeObject(super." + (f.getType().equals(boolean.class) || f.getType().equals(Boolean.class) ? "is" : "get")
							+ Utils.captalizeFirstChar(f.getName()) + "());}");

				}
			}

			outputWriter.println("}");
			outputWriter.println("public void readExternal(java.io.ObjectInput in) throws java.io.IOException, ClassNotFoundException {"
					+ "	_rObjectIdHolder[0]=in.readLong();" + "	_slotsPath=in.readUTF();" + "	_assignInterface=(org.kchine.r.server.AssignInterface)in.readObject();");

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
					+ "result.append(\"A Reference to an object of Class \\\"" + classShortName + "\\\" on the R servant "
					+ "<\"+_assignInterface.getName()+\">  [\"+_rObjectIdHolder[0]+\"/\"+_slotsPath+\"]\\n\");");
			for (int i = 0; i < fields.length; ++i) {
				Field f = fields[i];

				outputWriter.print("result.append(" + "\"Field \\\"" + f.getName() + "\\\":\\n\");");
				outputWriter.print("if (super.get" + Utils.captalizeFirstChar(f.getName()) + "()!=null) result.append(org.kchine.r.server.Utils.indent (super.get"
						+ Utils.captalizeFirstChar(f.getName()) + "().toString(),1)); " + "else result.append(\"null(\"+_rObjectIdHolder[0]+\"@"
						+ slotsContainer[0][i] + ")\\n\");");
			}
			outputWriter.println("} catch (java.rmi.RemoteException e) {e.printStackTrace();}" + "return result.toString();" + "}");

			outputWriter.println("public boolean equals(Object inputObject) {");
			outputWriter.println("if (inputObject==null || !(inputObject instanceof " + classShortName + "Ref" + ")) return false;");
			outputWriter.println("return  ((" + classShortName + "Ref)" + "inputObject)._assignInterface.equals( _assignInterface ) && ((" + classShortName
					+ "Ref" + ")inputObject)._rObjectIdHolder[0]==_rObjectIdHolder[0] && ((" + classShortName + "Ref"
					+ ")inputObject)._slotsPath.equals(_slotsPath);");
			outputWriter.println("}");

			outputWriter.println("}");
			outputWriter.close();

			String objectNameOutputFileName = Globals.GEN_ROOT_SRC + Globals.FILE_SEPARATOR + className.replace('.', Globals.FILE_SEPARATOR) + "ObjectName"
					+ ".java";
			PrintWriter objectNameOutputWriter = new PrintWriter(objectNameOutputFileName);

			objectNameOutputWriter.println("package " + className.substring(0, className.lastIndexOf('.')) + ";");
			objectNameOutputWriter.println("import org.kchine.r.*;");
			objectNameOutputWriter.println("public class " + classShortName + "ObjectName" + " extends " + classShortName
					+ " implements org.kchine.r.ObjectNameInterface {");
			objectNameOutputWriter.println("private String _name; private String _env;");
			objectNameOutputWriter.println("public String getRObjectName() {return _name;}");
			objectNameOutputWriter.println("public void setRObjectName(String _name) {this._name = _name;}");
			objectNameOutputWriter.println("public String getRObjectEnvironment() {return _env;}");
			objectNameOutputWriter.println("public void setRObjectEnvironment(String _env) {this._env = _env;}");

			objectNameOutputWriter.println("public boolean equals(Object obj) {");
			objectNameOutputWriter.println("	if (obj == null || !(obj instanceof ObjectNameInterface) )	return false;");
			objectNameOutputWriter
					.println("	return (((ObjectNameInterface) obj).getRObjectName().equals(this._name)) && (((ObjectNameInterface) obj).getRObjectEnvironment().equals(_env));");
			objectNameOutputWriter.println("}");

			objectNameOutputWriter.println("public String toString() {");
			objectNameOutputWriter.println("	return \"" + classShortName + "ObjectName" + ":\"+_env+\"$\"+_name;");
			objectNameOutputWriter.println("}");

			objectNameOutputWriter.println("public void writeExternal(java.io.ObjectOutput out)");
			objectNameOutputWriter.println("    throws java.io.IOException {");
			objectNameOutputWriter.println("	out.writeUTF(_env);");
			objectNameOutputWriter.println("	out.writeUTF(_name);");
			objectNameOutputWriter.println("}");

			objectNameOutputWriter.println("public void readExternal(java.io.ObjectInput in)");
			objectNameOutputWriter.println("    throws java.io.IOException, ClassNotFoundException {");
			objectNameOutputWriter.println("	_env=in.readUTF();");
			objectNameOutputWriter.println("	_name=in.readUTF();");
			objectNameOutputWriter.println("}");

			objectNameOutputWriter.println("}");
			objectNameOutputWriter.close();

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


		public String getRenameTo() {
			return renameTo;
		}

		public boolean isPublishToWeb() {
			return publishToWeb;
		}

		public void setPublishToWeb(boolean publishToWeb) {
			this.publishToWeb = publishToWeb;
		}


		public FAttributes(String renameTo, boolean publishToWeb) {
			this.renameTo = renameTo;
			this.publishToWeb = publishToWeb;
		}

		public String toString() {
			return "{renameTo=" + renameTo + "  publishToWeb=" + publishToWeb + "}";
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
