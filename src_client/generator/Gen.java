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
package generator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
//import org.apache.commons.logging.impl.Log4JLogger;
//import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.taskdefs.Manifest.Attribute;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.rosuda.JRI.Rengine;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import uk.ac.ebi.microarray.pools.PoolUtils;
import de.hunsicker.jalopy.plugin.ant.AntPlugin;
import server.DirectJNI;
import server.ExecutionUnit;
import server.Globals;
import static server.Globals.*;
import util.Utils;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class Gen {

	private static Vector<String[]> _functionsVector = new Vector<String[]>();
	private static Project _project = new Project();

	private static String[] rwebservicesScripts = { "/R/AllMkMapClasses.R", "/R/mkJavaBean.R",
			"/R/ArrayAndMatrix-class.R", "/R/javaReservedWord.R", "/R/unpackAntScript.R", "/R/basicConvert.R",
			"/R/mkConverter.R", "/R/mkTest.R", "/R/zzz.R", "/R/basicConvert2.R", "/R/mkDataMap.R", "/R/sink.R",
			"/R/cConvert.R", "/R/testUtil.R", "/R/mkMapUtil.R", "/R/mkFuncMap.R", "/R/typeInfoToJava.R",
			"/R/createMap.R" };

	private static StringBuffer initScriptBuffer = new StringBuffer();

	private static StringBuffer embedScriptBuffer = new StringBuffer();

	private static HashMap<String, StringBuffer> packageEmbedScriptHashMap = new HashMap<String, StringBuffer>();

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(Gen.class);

	static {
		_project.addBuildListener(new DefaultLogger() {
			{
				if (log.isTraceEnabled())
					setMessageOutputLevel(Project.MSG_VERBOSE);
				else if (log.isDebugEnabled())
					setMessageOutputLevel(Project.MSG_DEBUG);
				else if (log.isInfoEnabled())
					setMessageOutputLevel(Project.MSG_INFO);
				else if (log.isWarnEnabled())
					setMessageOutputLevel(Project.MSG_WARN);
				else if (log.isErrorEnabled())
					setMessageOutputLevel(Project.MSG_ERR);
				else if (log.isFatalEnabled())
					setMessageOutputLevel(Project.MSG_ERR);
				else
					setMessageOutputLevel(Project.MSG_INFO);
			}

			@Override
			protected void log(String str) {
				log.info(str);
			}

			@Override
			protected void printMessage(String arg0, PrintStream arg1, int arg2) {
			}
		});
/*
		if (log instanceof Log4JLogger) {
			Properties log4jProperties = new Properties();
			for (Object sprop : System.getProperties().keySet()) {
				if (((String) sprop).startsWith("log4j.")) {
					log4jProperties.put(sprop, System.getProperties().get(sprop));
				}
			}
			PropertyConfigurator.configure(log4jProperties);
		}
*/		

	}

	public static void main(String[] args) throws Exception {

		boolean formatsource = true;
		if (System.getProperty("formatsource") != null && !System.getProperty("formatsource").equals("")
				&& System.getProperty("formatsource").equalsIgnoreCase("false")) {
			formatsource = false;
		}

		GEN_ROOT = System.getProperty("outputdir");
		MAPPING_JAR_NAME = System.getProperty("mappingjar") != null && !System.getProperty("mappingjar").equals("") ? System
				.getProperty("mappingjar")
				: "mapping.jar";
		if (!MAPPING_JAR_NAME.endsWith(".jar"))
			MAPPING_JAR_NAME += ".jar";

		GEN_ROOT_SRC = GEN_ROOT + FILE_SEPARATOR + "src";
		GEN_ROOT_LIB = GEN_ROOT + FILE_SEPARATOR + "";

		File[] files = null;
		if (System.getProperty("dir") != null && !System.getProperty("dir").equals("")) {
			files = new File(System.getProperty("dir")).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toUpperCase().endsWith(".XML");
				};
			});
		} else {
			String fileName = System.getProperty("file") != null && !System.getProperty("file").equals("") ? System
					.getProperty("file") : "rjmap.xml";
			files = new File[] { new File(fileName) };
		}

		log.info("files : " + Utils.flatArray(files));

		if (files == null || files.length == 0) {
			log.info("no files to parse");
			System.exit(0);
		}

		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		domFactory.setValidating(false);
		DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();

		for (int f = 0; f < files.length; ++f) {
			log.info("parsing file : " + files[f]);
			Document document = documentBuilder.parse(files[f]);

			Vector<Node> initNodes = new Vector<Node>();
			Utils.catchNodes(Utils.catchNode(document.getDocumentElement(), "scripts"), "initScript", initNodes);
			for (int i = 0; i < initNodes.size(); ++i) {
				NamedNodeMap attrs = initNodes.elementAt(i).getAttributes();
				boolean embed = attrs.getNamedItem("embed") != null
						&& attrs.getNamedItem("embed").getNodeValue().equalsIgnoreCase("true");
				StringBuffer vbuffer = new StringBuffer();
				if (attrs.getNamedItem("inline") != null) {
					vbuffer.append(attrs.getNamedItem("inline").getNodeValue());
					vbuffer.append('\n');
				} else {
					String fname = attrs.getNamedItem("name").getNodeValue();
					if (!fname.startsWith("\\") && !fname.startsWith("/") && fname.toCharArray()[1] != ':') {
						String path = files[f].getAbsolutePath();
						path = path.substring(0, path.lastIndexOf(FILE_SEPARATOR));
						fname = new File(path + FILE_SEPARATOR + fname).getCanonicalPath();
					}
					vbuffer.append(Utils.getFileAsStringBuffer(fname));
				}
				initScriptBuffer.append(vbuffer);
				if (embed)
					embedScriptBuffer.append(vbuffer);
			}

			Vector<Node> packageInitNodes = new Vector<Node>();
			Utils.catchNodes(Utils.catchNode(document.getDocumentElement(), "scripts"), "packageScript",
					packageInitNodes);
			for (int i = 0; i < packageInitNodes.size(); ++i) {
				NamedNodeMap attrs = packageInitNodes.elementAt(i).getAttributes();
				String packageName = attrs.getNamedItem("package").getNodeValue();

				if (packageName.equals(""))
					packageName = "rGlobalEnv";

				if (!packageName.endsWith("Function"))
					packageName += "Function";
				if (packageEmbedScriptHashMap.get(packageName) == null) {
					packageEmbedScriptHashMap.put(packageName, new StringBuffer());
				}
				StringBuffer vbuffer = packageEmbedScriptHashMap.get(packageName);

				/*
				 * if (!packageName.equals("rGlobalEnvFunction")) {
				 * vbuffer.append("library("+packageName.substring(0,packageName.lastIndexOf("Function"))+")\n"); }
				 */

				if (attrs.getNamedItem("inline") != null) {
					vbuffer.append(attrs.getNamedItem("inline").getNodeValue() + "\n");
					initScriptBuffer.append(attrs.getNamedItem("inline").getNodeValue() + "\n");
				} else {
					String fname = attrs.getNamedItem("name").getNodeValue();
					if (!fname.startsWith("\\") && !fname.startsWith("/") && fname.toCharArray()[1] != ':') {
						String path = files[f].getAbsolutePath();
						path = path.substring(0, path.lastIndexOf(FILE_SEPARATOR));
						fname = new File(path + FILE_SEPARATOR + fname).getCanonicalPath();
					}
					StringBuffer fileBuffer = Utils.getFileAsStringBuffer(fname);
					vbuffer.append(fileBuffer);
					initScriptBuffer.append(fileBuffer);
				}
			}

			Vector<Node> functionsNodes = new Vector<Node>();
			Utils.catchNodes(Utils.catchNode(document.getDocumentElement(), "functions"), "function", functionsNodes);
			for (int i = 0; i < functionsNodes.size(); ++i) {
				NamedNodeMap attrs = functionsNodes.elementAt(i).getAttributes();
				String functionName = attrs.getNamedItem("name").getNodeValue();

				boolean forWeb = attrs.getNamedItem("forWeb") != null
						&& attrs.getNamedItem("forWeb").getNodeValue().equalsIgnoreCase("true");
				boolean singleThreadedWeb = attrs.getNamedItem("singleThreadedWeb") != null
						&& attrs.getNamedItem("singleThreadedWeb").getNodeValue().equalsIgnoreCase("true");

				String signature = (attrs.getNamedItem("signature") == null ? "" : attrs.getNamedItem("signature")
						.getNodeValue()
						+ ",");
				String renameTo = (attrs.getNamedItem("renameTo") == null ? null : attrs.getNamedItem("renameTo")
						.getNodeValue());

				HashMap<String, FAttributes> sigMap = Globals._functionsToPublish.get(functionName);

				if (sigMap == null) {
					sigMap = new HashMap<String, FAttributes>();
					Globals._functionsToPublish.put(functionName, sigMap);

					if (attrs.getNamedItem("returnType") == null) {
						_functionsVector.add(new String[] { functionName });
					} else {
						_functionsVector.add(new String[] { functionName,
								attrs.getNamedItem("returnType").getNodeValue() });
					}

				}

				sigMap.put(signature, new FAttributes(renameTo, forWeb, singleThreadedWeb));

				if (forWeb)
					_webPublishingEnabled = true;

			}

			if (System.getProperty("targetjdk") != null && !System.getProperty("targetjdk").equals("")
					&& System.getProperty("targetjdk").compareTo("1.6") < 0) {
				if (_webPublishingEnabled
						|| (System.getProperty("ws.r.api") != null && System.getProperty("ws.r.api").equalsIgnoreCase(
								"true"))) {
					log.info("be careful, web publishing disabled beacuse target JDK<1.6");
				}
				_webPublishingEnabled = false;
			} else {

				if (System.getProperty("ws.r.api") != null && System.getProperty("ws.r.api").equalsIgnoreCase("true")) {
					_webPublishingEnabled = true;
				}

				if (_webPublishingEnabled && System.getProperty("java.version").compareTo("1.6") < 0) {
					log.info("be careful, web publishing disabled beacuse a JDK<1.6 is in use");
					_webPublishingEnabled = false;
				}
			}

			Vector<Node> s4Nodes = new Vector<Node>();
			Utils.catchNodes(Utils.catchNode(document.getDocumentElement(), "s4classes"), "class", s4Nodes);

			if (s4Nodes.size() > 0) {
				String formalArgs = "";
				String signature = "";
				for (int i = 0; i < s4Nodes.size(); ++i) {
					NamedNodeMap attrs = s4Nodes.elementAt(i).getAttributes();
					String s4Name = attrs.getNamedItem("name").getNodeValue();
					formalArgs += "p" + i + (i == s4Nodes.size() - 1 ? "" : ",");
					signature += "'" + s4Name + "'" + (i == s4Nodes.size() - 1 ? "" : ",");
				}
				String genBeansScriptlet = "setGeneric('" + PUBLISH_S4_HEADER + "', function(" + formalArgs
						+ ") standardGeneric('" + PUBLISH_S4_HEADER + "'));" + "setMethod('" + PUBLISH_S4_HEADER
						+ "', signature(" + signature + ") , function(" + formalArgs + ") {   })";
				initScriptBuffer.append(genBeansScriptlet);
				_functionsVector.add(new String[] { PUBLISH_S4_HEADER, "numeric" });
			}

		}

		if (!new File(GEN_ROOT_LIB).exists())
			regenerateDir(GEN_ROOT_LIB);
		else {
			clean(GEN_ROOT_LIB, true);
		}

		for (int i = 0; i < rwebservicesScripts.length; ++i)
			DirectJNI.getInstance().getRServices().sourceFromResource(rwebservicesScripts[i]);

		String lastStatus = DirectJNI.getInstance().runR(new ExecutionUnit() {
			public void run(Rengine e) {
				DirectJNI.getInstance().toggleMarker();
				DirectJNI.getInstance().sourceFromBuffer(initScriptBuffer);
				log.info(" init  script status : " + DirectJNI.getInstance().cutStatusSinceMarker());

				for (int i = 0; i < _functionsVector.size(); ++i) {

					String[] functionPair = _functionsVector.elementAt(i);
					log.info("dealing with : " + functionPair[0]);

					regenerateDir(GEN_ROOT_SRC);

					String createMapStr = "createMap(";
					boolean isGeneric = e.rniGetBoolArrayI(e.rniEval(e.rniParse("isGeneric(\"" + functionPair[0]
							+ "\")", 1), 0))[0] == 1;

					log.info("is Generic : " + isGeneric);
					if (isGeneric) {
						createMapStr += functionPair[0];
					} else {
						createMapStr += "\"" + functionPair[0] + "\"";
					}
					createMapStr += ", outputDirectory=\""
							+ GEN_ROOT_SRC.substring(0, GEN_ROOT_SRC.length() - "/src".length()).replace('\\', '/')
							+ "\"";
					createMapStr += ", typeMode=\"robject\"";
					createMapStr += (functionPair.length == 1 || functionPair[1] == null
							|| functionPair[1].trim().equals("") ? ""
							: ", S4DefaultTypedSig=TypedSignature(returnType=\"" + functionPair[1] + "\")");
					createMapStr += ")";

					log.info("------------------------------------------");
					log.info("-- createMapStr=" + createMapStr);
					DirectJNI.getInstance().toggleMarker();
					e.rniEval(e.rniParse(createMapStr, 1), 0);
					String createMapStatus = DirectJNI.getInstance().cutStatusSinceMarker();
					log.info(" createMap status : " + createMapStatus);
					log.info("------------------------------------------");

					deleteDir(GEN_ROOT_SRC + "/org/bioconductor/rserviceJms");
					compile(GEN_ROOT_SRC);
					jar(GEN_ROOT_SRC, GEN_ROOT_LIB + FILE_SEPARATOR + TEMP_JARS_PREFIX + i + ".jar", null);

					URL url = null;
					try {
						url = new URL("jar:file:"
								+ (GEN_ROOT_LIB + FILE_SEPARATOR + TEMP_JARS_PREFIX + i + ".jar").replace('\\', '/')
								+ "!/");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					generateMaps(url, true);
				}

			}
		});
		log.info(lastStatus);

		log.info(DirectJNI._rPackageInterfacesHash);
		regenerateDir(GEN_ROOT_SRC);
		for (int i = 0; i < _functionsVector.size(); ++i) {
			unjar(GEN_ROOT_LIB + FILE_SEPARATOR + TEMP_JARS_PREFIX + i + ".jar", GEN_ROOT_SRC);
		}

		regenerateRPackageClass(true);

		generateS4BeanRef();

		if (formatsource)
			applyJalopy(GEN_ROOT_SRC);

		compile(GEN_ROOT_SRC);

		if (_webPublishingEnabled) {

			jar(GEN_ROOT_SRC, GEN_ROOT_LIB + FILE_SEPARATOR + "__temp.jar", null);
			URL url = new URL("jar:file:" + (GEN_ROOT_LIB + FILE_SEPARATOR + "__temp.jar").replace('\\', '/') + "!/");
			ClassLoader cl = new URLClassLoader(new URL[] { url }, Globals.class.getClassLoader());

			for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
				if (cl.loadClass(className + "Web").getDeclaredMethods().length == 0)
					continue;
				log.info("### " + className);
				Utils.exec(new String[] { "wsgen", "-wsdl", "-d", GEN_ROOT_LIB + FILE_SEPARATOR + "src", "-cp",
						GEN_ROOT_LIB + FILE_SEPARATOR + "src" + System.getProperty("path.separator") + "RJB.jar",
						className + "Web" }, null, null);
			}

			new File(GEN_ROOT_LIB + FILE_SEPARATOR + "__temp.jar").delete();

		}

		embedRScripts();

		HashMap<String, String> marker = new HashMap<String, String>();
		marker.put("RJBMAPPINGJAR", "TRUE");
				
		
		Properties props=new Properties();		
		props.put("PACKAGE_NAMES", PoolUtils.objectToHex(DirectJNI._packageNames) );
		props.put("S4BEANS_MAP", PoolUtils.objectToHex(DirectJNI._s4BeansMapping) );
		props.put("S4BEANS_REVERT_MAP", PoolUtils.objectToHex(DirectJNI._s4BeansMappingRevert) );
		props.put("FACTORIES_MAPPING", PoolUtils.objectToHex(DirectJNI._factoriesMapping) );		
		props.put("S4BEANS_HASH", PoolUtils.objectToHex(DirectJNI._s4BeansHash) );
		props.put("R_PACKAGE_INTERFACES_HASH", PoolUtils.objectToHex(DirectJNI._rPackageInterfacesHash) );
		props.put("ABSTRACT_FACTORIES", PoolUtils.objectToHex(DirectJNI._abstractFactories) );		
		FileOutputStream fos=new FileOutputStream(GEN_ROOT_SRC+"/"+"rjbmaps.properties");
		props.storeToXML(fos, null);
		fos.close();
		
		jar(GEN_ROOT_SRC, GEN_ROOT_LIB + FILE_SEPARATOR + MAPPING_JAR_NAME, marker);

		if (_webPublishingEnabled)
			genWeb();

		DirectJNI._mappingClassLoader = null;
		System.gc();

		System.exit(0);
	}

	static void deleteDir(String dir) {

		Delete deleteTask = new Delete();
		deleteTask.setProject(_project);
		deleteTask.setTaskName("dir_delete");

		deleteTask.setFailOnError(false);
		deleteTask.setDir(new File(dir));

		deleteTask.init();
		deleteTask.execute();
	}

	static void clean(String dir, boolean all) {

		Delete deleteTask = new Delete();
		deleteTask.setProject(_project);
		deleteTask.setFailOnError(true);
		deleteTask.setTaskName("dir_delete");
		FileSet d_fileSet_1 = new FileSet();
		d_fileSet_1.setProject(_project);
		d_fileSet_1.setDir(new File(dir));
		d_fileSet_1.setIncludes(all ? "*.jar" : "_temp*.jar");
		deleteTask.addFileset(d_fileSet_1);
		deleteTask.init();
		deleteTask.execute();

		deleteTask.reconfigure();
		deleteTask.setDir(new File(dir + FILE_SEPARATOR + "src"));
		deleteTask.init();
		deleteTask.execute();

		deleteTask.reconfigure();
		deleteTask.setDir(new File(dir + FILE_SEPARATOR + "test"));
		deleteTask.init();
		deleteTask.execute();

	}

	static void regenerateDir(String dir) {

		Delete deleteTask = new Delete();
		deleteTask.setFailOnError(false);
		deleteTask.setDir(new File(dir));
		deleteTask.setTaskName("dir_delete");
		deleteTask.setProject(_project);
		deleteTask.init();
		deleteTask.execute();
		Mkdir mkdirTask = new Mkdir();
		mkdirTask.setDir(new File(dir));
		mkdirTask.setTaskName("dir_make");
		mkdirTask.setProject(_project);
		mkdirTask.init();
		mkdirTask.execute();
	}

	static void unjar(String jarName, String expandDir) {

		Expand expandTask = new Expand();
		expandTask.setTaskName("unjar");
		expandTask.setSrc(new File(jarName));
		expandTask.setDest(new File(expandDir));
		expandTask.setProject(_project);
		expandTask.init();
		expandTask.execute();
	}

	static void compile(String src_arg) {

		Delete deleteTask = new Delete();
		deleteTask.setProject(_project);
		deleteTask.setTaskName("clean");

		FileSet del_fileSet = new FileSet();
		del_fileSet.setProject(_project);
		del_fileSet.setDir(new File(src_arg));
		del_fileSet.setIncludes("**/*.class");

		deleteTask.addFileset(del_fileSet);

		deleteTask.init();

		deleteTask.execute();

		Javac compileTask = new Javac();
		compileTask.setProject(_project);
		compileTask.setTaskName("compile");

		compileTask.setSrcdir(new Path(_project, src_arg));

		compileTask.setDestdir(new File(src_arg));
		compileTask.setSource(_webPublishingEnabled ? "1.6" : "1.5");
		compileTask.setTarget(_webPublishingEnabled ? "1.6" : "1.5");

		FileSet cp_fileSet = new FileSet();
		cp_fileSet.setDir(new File("lib"));
		cp_fileSet.setIncludes("**/*.jar");

		DirSet cp_dirSet = new DirSet();
		cp_dirSet.setDir(new File("bin"));

		Path classPath = new Path(_project);
		classPath.addFileset(cp_fileSet);
		classPath.addDirset(cp_dirSet);

		compileTask.setClasspath(classPath);

		compileTask.init();
		compileTask.execute();

	}

	static void jar(String src_arg, String jar_arg, HashMap<String, String> manifestAttributes) {

		Jar jarTask = new Jar();
		jarTask.setProject(_project);
		jarTask.setTaskName("jar");

		jarTask.setBasedir(new File(src_arg));
		jarTask.setDestFile(new File(jar_arg));

		if (manifestAttributes != null) {
			try {

				Manifest manifest = new Manifest();

				for (Iterator<String> iter = manifestAttributes.keySet().iterator(); iter.hasNext();) {
					String item = iter.next();
					Attribute attribute = new Attribute(item, manifestAttributes.get(item));
					manifest.addConfiguredAttribute(attribute);
				}

				jarTask.addConfiguredManifest(manifest);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		jarTask.setIncludes("**/*.java,**/*.class, **/*.R, **/*.xml, **/*.properties, **/*.wsdl");
		jarTask.setExcludes("org/bioconductor/rserviceJms/**/*");

		jarTask.init();
		jarTask.execute();
	}

	public static void embedRScripts() throws Exception {
		{
			File embedScriptFile = new File(GEN_ROOT_SRC + FILE_SEPARATOR + "bootstrap.R");
			PrintWriter pw = new PrintWriter(new FileWriter(embedScriptFile));
			pw.println(embedScriptBuffer);
			pw.close();
		}

		for (Iterator iter = DirectJNI._rPackageInterfacesHash.keySet().iterator(); iter.hasNext();) {
			String className = (String) iter.next();
			String packageName = className.substring(className.lastIndexOf('.') + 1);
			File packScriptFile = new File(GEN_ROOT_SRC + FILE_SEPARATOR + className.replace('.', FILE_SEPARATOR)
					+ ".R");
			PrintWriter pw = new PrintWriter(new FileWriter(packScriptFile));
			pw.println(packageEmbedScriptHashMap.get(packageName) != null ? packageEmbedScriptHashMap.get(packageName)
					: "## No Init Required");
			pw.close();
		}
	}

	public static void genWeb() throws Exception {

		String GEN_WEBINF = GEN_ROOT + FILE_SEPARATOR + "war" + FILE_SEPARATOR + "WEB-INF";

		String WAR_NAME = System.getProperty("warname") != null && !System.getProperty("warname").equals("") ? System
				.getProperty("warname") : MAPPING_JAR_NAME.substring(0, MAPPING_JAR_NAME.length() - "jar".length())
				+ "war";
		if (!WAR_NAME.endsWith(".war"))
			WAR_NAME += ".war";

		String PROPS_EMBED = System.getProperty("propsembed") != null && !System.getProperty("propsembed").equals("") ? System
				.getProperty("propsembed")
				: null;

		deleteDir(GEN_ROOT + FILE_SEPARATOR + "war");

		regenerateDir(GEN_WEBINF + FILE_SEPARATOR + "classes");
		regenerateDir(GEN_WEBINF + FILE_SEPARATOR + "lib");

		Vector<String> warJars = new Vector<String>();
		warJars.add(GEN_ROOT_LIB + FILE_SEPARATOR + MAPPING_JAR_NAME);
		warJars.add("RJB.jar");
		warJars.add(System.getProperty("jri.jar"));

		FilenameFilter jarsFilter = new FilenameFilter() {
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".jar");
			}
		};

		{
			String[] jaxws_jars = new File("jaxws").list(jarsFilter);
			for (int i = 0; i < jaxws_jars.length; ++i) {
				warJars.add("jaxws" + FILE_SEPARATOR + jaxws_jars[i]);
			}
		}

		{
			String[] pool_jars = new File("lib/pool").list(jarsFilter);
			for (int i = 0; i < pool_jars.length; ++i) {
				warJars.add("lib/pool" + FILE_SEPARATOR + pool_jars[i]);
			}
		}

		{
			String[] derby_jdbc_jars = new File("lib/jdbc").list(jarsFilter);
			for (int i = 0; i < derby_jdbc_jars.length; ++i) {
				warJars.add("lib/jdbc" + FILE_SEPARATOR + derby_jdbc_jars[i]);
			}
		}

		{
			String[] httpclient_jars = new File("lib/j2ee").list(jarsFilter);
			for (int i = 0; i < httpclient_jars.length; ++i) {
				warJars.add("lib/j2ee" + FILE_SEPARATOR + httpclient_jars[i]);
			}
		}

		log.info(warJars);
		for (int i = 0; i < warJars.size(); ++i) {
			Copy copyTask = new Copy();
			copyTask.setProject(_project);
			copyTask.setTaskName("copy to war");
			copyTask.setTodir(new File(GEN_WEBINF + FILE_SEPARATOR + "lib"));
			copyTask.setFile(new File(warJars.elementAt(i)));
			copyTask.init();
			copyTask.execute();
		}

		PrintWriter pw_web_xml = new PrintWriter(GEN_WEBINF + FILE_SEPARATOR + "web.xml");
		pw_web_xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw_web_xml
				.println("<web-app version=\"2.4\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\">");
		pw_web_xml
				.println("<listener><listener-class>com.sun.xml.ws.transport.http.servlet.WSServletContextListener</listener-class></listener>");

		for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);
			pw_web_xml
					.println("<servlet><servlet-name>"
							+ shortClassName
							+ "_servlet</servlet-name><servlet-class>com.sun.xml.ws.transport.http.servlet.WSServlet</servlet-class><load-on-startup>1</load-on-startup></servlet>");
		}

		for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);
			pw_web_xml.println("<servlet-mapping><servlet-name>" + shortClassName
					+ "_servlet</servlet-name><url-pattern>/" + shortClassName + "</url-pattern></servlet-mapping>");
		}
		pw_web_xml.println("<session-config><session-timeout>30</session-timeout></session-config>");
		pw_web_xml.println("<welcome-file-list><welcome-file>index.jsp</welcome-file></welcome-file-list>");
		pw_web_xml.println("</web-app>");
		pw_web_xml.flush();
		pw_web_xml.close();

		PrintWriter pw_sun_jaxws_xml = new PrintWriter(GEN_WEBINF + FILE_SEPARATOR + "sun-jaxws.xml");
		pw_sun_jaxws_xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw_sun_jaxws_xml.println("<endpoints xmlns='http://java.sun.com/xml/ns/jax-ws/ri/runtime' version='2.0'>");

		for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);
			pw_sun_jaxws_xml.println("   <endpoint    name='name_" + shortClassName + "'   implementation='"
					+ className + "Web" + "' url-pattern='/" + shortClassName + "'/>");
		}

		pw_sun_jaxws_xml.println("</endpoints>");
		pw_sun_jaxws_xml.flush();
		pw_sun_jaxws_xml.close();

		if (PROPS_EMBED != null) {
			InputStream is = new FileInputStream(PROPS_EMBED);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			RandomAccessFile raf = new RandomAccessFile(GEN_WEBINF + FILE_SEPARATOR + "classes" + FILE_SEPARATOR
					+ "globals.properties", "rw");
			raf.setLength(0);
			raf.write(buffer);
			raf.close();
		}

		War warTask = new War();
		warTask.setProject(_project);
		warTask.setTaskName("war");
		warTask.setBasedir(new File(GEN_ROOT + FILE_SEPARATOR + "war"));
		warTask.setDestFile(new File(GEN_ROOT + FILE_SEPARATOR + WAR_NAME));
		warTask.setIncludes("**/*");
		warTask.init();
		warTask.execute();

	}

	public static void applyJalopy(String src_arg) {
		Vector<String> list = new Vector<String>();
		Globals.scanJavaFiles(new File(src_arg + Globals.FILE_SEPARATOR), list);

		AntPlugin jalopyTask = new AntPlugin();
		jalopyTask.setTaskName("jalopy");
		jalopyTask.setProject(_project);

		if (log.isTraceEnabled())
			jalopyTask.setLoglevel("debug");
		else if (log.isDebugEnabled())
			jalopyTask.setLoglevel("info");
		else if (log.isInfoEnabled())
			jalopyTask.setLoglevel("warn");
		else if (log.isWarnEnabled())
			jalopyTask.setLoglevel("warn");
		else if (log.isErrorEnabled())
			jalopyTask.setLoglevel("error");
		else if (log.isFatalEnabled())
			jalopyTask.setLoglevel("fatal");
		else
			jalopyTask.setLoglevel("info");

		for (String f : list) {
			jalopyTask.setFile(new File(f));
			jalopyTask.init();
			jalopyTask.execute();

		}

	}

}