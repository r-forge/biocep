/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Copy;
import org.apache.tools.ant.taskdefs.Delete;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Jar;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.taskdefs.Manifest;
import org.apache.tools.ant.taskdefs.Mkdir;
import org.apache.tools.ant.taskdefs.Rmic;
import org.apache.tools.ant.taskdefs.War;
import org.apache.tools.ant.taskdefs.Manifest.Attribute;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.kchine.r.server.Utils;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.PoolUtils.EqualNameFilter;
import org.rosuda.JRI.Rengine;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import com.sun.tools.ws.ant.WsGen;
import de.hunsicker.jalopy.plugin.ant.AntPlugin;
import server.DirectJNI;
import server.ExecutionUnit;
import server.Globals;
import static org.kchine.rpf.PoolUtils.unzip;
import static server.Globals.*;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class Gen {

	private static Vector<String[]> _functionsVector = new Vector<String[]>();
	private static Project _project = new Project();

	private static String[] rwebservicesScripts = { "/R/AllMkMapClasses.R", "/R/mkJavaBean.R", "/R/ArrayAndMatrix-class.R", "/R/javaReservedWord.R",
			"/R/unpackAntScript.R", "/R/basicConvert.R", "/R/mkConverter.R", "/R/mkTest.R", "/R/zzz.R", "/R/basicConvert2.R", "/R/mkDataMap.R", "/R/sink.R",
			"/R/cConvert.R", "/R/testUtil.R", "/R/mkMapUtil.R", "/R/mkFuncMap.R", "/R/typeInfoToJava.R", "/R/createMap.R" };

	private static StringBuffer initScriptBuffer = new StringBuffer();

	private static StringBuffer embedScriptBuffer = new StringBuffer();

	private static HashMap<String, StringBuffer> packageEmbedScriptHashMap = new HashMap<String, StringBuffer>();

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(Gen.class);

	private static int BUFFER_SIZE=1024 * 16;
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

			protected void log(String str) {
				log.info(str);
			}

			protected void printMessage(String arg0, PrintStream arg1, int arg2) {
			}
		});

	}

	public static void main(String[] args) throws Exception {
		
		File[] files = null;
		if (System.getProperty("dir") != null && !System.getProperty("dir").equals("")) {
			files = new File(System.getProperty("dir")).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toUpperCase().endsWith(".XML");
				};
			});
		} else {
			String fileName = System.getProperty("file") != null && !System.getProperty("file").equals("") ? System.getProperty("file") : "rjmap.xml";
			files = new File[] { new File(fileName) };
		}

		log.info("files : " + Arrays.toString(files));

		if (files == null || files.length == 0) {
			log.info("no files to parse");
			System.exit(0);
		}

		boolean formatsource = true;
		if (System.getProperty("formatsource") != null && !System.getProperty("formatsource").equals("")
				&& System.getProperty("formatsource").equalsIgnoreCase("false")) {
			formatsource = false;
		}

		GEN_ROOT = System.getProperty("outputdir");
		
		if (GEN_ROOT == null || GEN_ROOT.equals("")) {
			GEN_ROOT = new File(files[0].getAbsolutePath()).getParent() + FILE_SEPARATOR + "distrib";
		}
		
		GEN_ROOT=new File(GEN_ROOT).getAbsolutePath().replace('\\', '/');
		if (GEN_ROOT.endsWith("/")) GEN_ROOT=GEN_ROOT.substring(0,GEN_ROOT.length()-1);
		
		System.out.println("GEN ROOT:" + GEN_ROOT);

		MAPPING_JAR_NAME = System.getProperty("mappingjar") != null && !System.getProperty("mappingjar").equals("") ? System.getProperty("mappingjar")
				: "mapping.jar";
		if (!MAPPING_JAR_NAME.endsWith(".jar"))
			MAPPING_JAR_NAME += ".jar";

		GEN_ROOT_SRC = GEN_ROOT + FILE_SEPARATOR +  "src";
		GEN_ROOT_LIB = GEN_ROOT + FILE_SEPARATOR + "";

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
				boolean embed = attrs.getNamedItem("embed") != null && attrs.getNamedItem("embed").getNodeValue().equalsIgnoreCase("true");
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
			Utils.catchNodes(Utils.catchNode(document.getDocumentElement(), "scripts"), "packageScript", packageInitNodes);
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

				
				 //if (!packageName.equals("rGlobalEnvFunction")) {
				 //vbuffer.append("library("+packageName.substring(0,packageName.lastIndexOf("Function"))+")\n"); }
				 

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

				boolean forWeb = attrs.getNamedItem("forWeb") != null && attrs.getNamedItem("forWeb").getNodeValue().equalsIgnoreCase("true");

				String signature = (attrs.getNamedItem("signature") == null ? "" : attrs.getNamedItem("signature").getNodeValue() + ",");
				String renameTo = (attrs.getNamedItem("renameTo") == null ? null : attrs.getNamedItem("renameTo").getNodeValue());

				HashMap<String, FAttributes> sigMap = Globals._functionsToPublish.get(functionName);

				if (sigMap == null) {
					sigMap = new HashMap<String, FAttributes>();
					Globals._functionsToPublish.put(functionName, sigMap);

					if (attrs.getNamedItem("returnType") == null) {
						_functionsVector.add(new String[] { functionName });
					} else {
						_functionsVector.add(new String[] { functionName, attrs.getNamedItem("returnType").getNodeValue() });
					}

				}

				sigMap.put(signature, new FAttributes(renameTo, forWeb));

				if (forWeb)
					_webPublishingEnabled = true;

			}

			if (System.getProperty("targetjdk") != null && !System.getProperty("targetjdk").equals("") && System.getProperty("targetjdk").compareTo("1.5") < 0) {
				if (_webPublishingEnabled || (System.getProperty("ws.r.api") != null && System.getProperty("ws.r.api").equalsIgnoreCase("true"))) {
					log.info("be careful, web publishing disabled beacuse target JDK<1.5");
				}
				_webPublishingEnabled = false;
			} else {

				if (System.getProperty("ws.r.api") == null || System.getProperty("ws.r.api").equals("") || !System.getProperty("ws.r.api").equalsIgnoreCase("false")) {
					_webPublishingEnabled = true;
				}

				if (_webPublishingEnabled && System.getProperty("java.version").compareTo("1.5") < 0) {
					log.info("be careful, web publishing disabled beacuse a JDK<1.5 is in use");
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
				String genBeansScriptlet = "setGeneric('" + PUBLISH_S4_HEADER + "', function(" + formalArgs + ") standardGeneric('" + PUBLISH_S4_HEADER
						+ "'));" + "setMethod('" + PUBLISH_S4_HEADER + "', signature(" + signature + ") , function(" + formalArgs + ") {   })";
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
				DirectJNI.getInstance().sourceFromBuffer(initScriptBuffer.toString());
				log.info(" init  script status : " + DirectJNI.getInstance().cutStatusSinceMarker());

				for (int i = 0; i < _functionsVector.size(); ++i) {

					String[] functionPair = _functionsVector.elementAt(i);
					log.info("dealing with : " + functionPair[0]);

					regenerateDir(GEN_ROOT_SRC);

					String createMapStr = "createMap(";
					boolean isGeneric = e.rniGetBoolArrayI(e.rniEval(e.rniParse("isGeneric(\"" + functionPair[0] + "\")", 1), 0))[0] == 1;

					log.info("is Generic : " + isGeneric);
					if (isGeneric) {
						createMapStr += functionPair[0];
					} else {
						createMapStr += "\"" + functionPair[0] + "\"";
					}
					createMapStr += ", outputDirectory=\"" + GEN_ROOT_SRC.substring(0, GEN_ROOT_SRC.length() - "/src".length()).replace('\\', '/') + "\"";
					createMapStr += ", typeMode=\"robject\"";
					createMapStr += (functionPair.length == 1 || functionPair[1] == null || functionPair[1].trim().equals("") ? ""
							: ", S4DefaultTypedSig=TypedSignature(returnType=\"" + functionPair[1] + "\")");
					createMapStr += ")";

					log.info("------------------------------------------");
					log.info("-- createMapStr=" + createMapStr);
					DirectJNI.getInstance().toggleMarker();
					e.rniEval(e.rniParse(createMapStr, 1), 0);
					String createMapStatus = DirectJNI.getInstance().cutStatusSinceMarker();
					log.info(" createMap status : " + createMapStatus);
					log.info("------------------------------------------");

					deleteDir(GEN_ROOT_SRC + "/org/kchine/r/rserviceJms");
					compile(GEN_ROOT_SRC);
					jar(GEN_ROOT_SRC, GEN_ROOT_LIB + FILE_SEPARATOR + TEMP_JARS_PREFIX + i + ".jar", null);

					URL url = null;
					try {
						url = new URL("jar:file:" + (GEN_ROOT_LIB + FILE_SEPARATOR + TEMP_JARS_PREFIX + i + ".jar").replace('\\', '/') + "!/");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					DirectJNI.generateMaps(url, true);
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

		for (String k : DirectJNI._rPackageInterfacesHash.keySet()) {
			Rmic rmicTask = new Rmic();
			rmicTask.setProject(_project);
			rmicTask.setTaskName("rmic_packages");
			rmicTask.setClasspath(new Path(_project, GEN_ROOT_SRC));
			rmicTask.setBase(new File(GEN_ROOT_SRC));
			rmicTask.setClassname(k + "ImplRemote");
			rmicTask.init();
			rmicTask.execute();
		}

		//DirectJNI._rPackageInterfacesHash=new HashMap<String, Vector<Class<?>>>();
		//DirectJNI._rPackageInterfacesHash.put("org.bioconductor.packages.rGlobalEnv.rGlobalEnvFunction",new Vector<Class<?>>());

		if (_webPublishingEnabled) {

			jar(GEN_ROOT_SRC, GEN_ROOT_LIB + FILE_SEPARATOR + "__temp.jar", null);
			URL url = new URL("jar:file:" + (GEN_ROOT_LIB + FILE_SEPARATOR + "__temp.jar").replace('\\', '/') + "!/");
			ClassLoader cl = new URLClassLoader(new URL[] { url }, Globals.class.getClassLoader());

			for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
				if (cl.loadClass(className + "Web").getDeclaredMethods().length == 0)
					continue;
				log.info("######## " + className);
								
				WsGen wsgenTask = new WsGen();
				wsgenTask.setProject(_project);
				wsgenTask.setTaskName("wsgen");
				
				FileSet rjb_fileSet = new FileSet();
				rjb_fileSet.setProject(_project);
				rjb_fileSet.setDir(new File("."));
				rjb_fileSet.setIncludes("RJB.jar");
				
				DirSet src_dirSet = new DirSet();
				src_dirSet.setDir(new File(GEN_ROOT_LIB + FILE_SEPARATOR + "src/"));								
				Path classPath = new Path(_project);
				classPath.addFileset(rjb_fileSet);
				classPath.addDirset(src_dirSet);				
				wsgenTask.setClasspath(classPath);					            
				wsgenTask.setKeep(true);
				wsgenTask.setDestdir(new File(GEN_ROOT_LIB + FILE_SEPARATOR + "src/"));
				wsgenTask.setResourcedestdir(new File(GEN_ROOT_LIB + FILE_SEPARATOR + "src/"));
				wsgenTask.setSei(className + "Web");

				wsgenTask.init();
				wsgenTask.execute();
			}

			new File(GEN_ROOT_LIB + FILE_SEPARATOR + "__temp.jar").delete();

		}

		embedRScripts();

		HashMap<String, String> marker = new HashMap<String, String>();
		marker.put("RJBMAPPINGJAR", "TRUE");

		Properties props = new Properties();
		props.put("PACKAGE_NAMES", PoolUtils.objectToHex(DirectJNI._packageNames));
		props.put("S4BEANS_MAP", PoolUtils.objectToHex(DirectJNI._s4BeansMapping));
		props.put("S4BEANS_REVERT_MAP", PoolUtils.objectToHex(DirectJNI._s4BeansMappingRevert));
		props.put("FACTORIES_MAPPING", PoolUtils.objectToHex(DirectJNI._factoriesMapping));
		props.put("S4BEANS_HASH", PoolUtils.objectToHex(DirectJNI._s4BeansHash));
		props.put("R_PACKAGE_INTERFACES_HASH", PoolUtils.objectToHex(DirectJNI._rPackageInterfacesHash));
		props.put("ABSTRACT_FACTORIES", PoolUtils.objectToHex(DirectJNI._abstractFactories));
		new File(GEN_ROOT_SRC + "/" + "maps").mkdirs();
		FileOutputStream fos = new FileOutputStream(GEN_ROOT_SRC + "/" + "maps/rjbmaps.xml");
		props.storeToXML(fos, null);
		fos.close();

		jar(GEN_ROOT_SRC, GEN_ROOT_LIB + FILE_SEPARATOR + MAPPING_JAR_NAME, marker);

		if (_webPublishingEnabled)
			genWeb();

		DirectJNI._mappingClassLoader = null;

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
		
		compileTask.setSource("1.5");
		compileTask.setTarget("1.5");

		
		Path classPath = new Path(_project);
	
		
		String jar=Gen.class.getResource("/Gen.class").toString();
		if (jar.contains("biocep-tools.jar")) {
			File jarfile=new File(jar.substring("jar:file:".length(), jar.length()-"/Gen.class".length()-1));			
			FileSet cp_fileSet = new FileSet();
			cp_fileSet.setDir(jarfile.getParentFile());
			cp_fileSet.setIncludes("biocep-tools.jar");
			classPath.addFileset(cp_fileSet);
		} else 	{
			FileSet cp_fileSet = new FileSet();
			cp_fileSet.setDir(new File("lib"));
			cp_fileSet.setIncludes("**/*.jar");
			DirSet cp_dirSet = new DirSet();
			cp_dirSet.setDir(new File("bin"));
			classPath.addFileset(cp_fileSet);
			classPath.addDirset(cp_dirSet);
		}

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

		for (Iterator<?> iter = DirectJNI._rPackageInterfacesHash.keySet().iterator(); iter.hasNext();) {
			String className = (String) iter.next();			
			String packageName = className.substring(className.lastIndexOf('.') + 1);
			File packScriptFile = new File(GEN_ROOT_SRC + FILE_SEPARATOR + className.replace('.', FILE_SEPARATOR) + ".R");
			PrintWriter pw = new PrintWriter(new FileWriter(packScriptFile));
			pw.println(packageEmbedScriptHashMap.get(packageName) != null ? packageEmbedScriptHashMap.get(packageName) : "## No Init Required");
			pw.close();
		}
	}

	public static void genWeb() throws Exception {

		String GEN_WEBINF = GEN_ROOT + FILE_SEPARATOR + "war" + FILE_SEPARATOR + "WEB-INF";

		String WAR_NAME = System.getProperty("warname") != null && !System.getProperty("warname").equals("") ? System.getProperty("warname") : MAPPING_JAR_NAME
				.substring(0, MAPPING_JAR_NAME.length() - "jar".length())
				+ "war";
		if (!WAR_NAME.endsWith(".war"))
			WAR_NAME += ".war";

		String PROPS_EMBED = System.getProperty("propsembed") != null && !System.getProperty("propsembed").equals("") ? System.getProperty("propsembed") : null;

		deleteDir(GEN_ROOT + FILE_SEPARATOR + "war");

		regenerateDir(GEN_WEBINF + FILE_SEPARATOR + "classes");
		regenerateDir(GEN_WEBINF + FILE_SEPARATOR + "lib");

		Vector<String> warJars = new Vector<String>();
		warJars.add(GEN_ROOT_LIB + FILE_SEPARATOR + MAPPING_JAR_NAME);

		InputStream inputStreamCore = Gen.class.getResourceAsStream("/biocep-core-tomcat.jar");
		if (inputStreamCore != null) {
			try {				
				byte data[] = new byte[BUFFER_SIZE];
				FileOutputStream fos = new FileOutputStream(GEN_WEBINF + FILE_SEPARATOR + "lib" + "/biocep-core.jar");
				int count=0;
				while ((count = inputStreamCore.read(data, 0, BUFFER_SIZE)) != -1) {
					fos.write(data, 0, count);
				}
				fos.flush();
				fos.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

			warJars.add("RJB.jar");

			warJars.add("lib/desktop/JRI.jar");

			FilenameFilter jarsFilter = new FilenameFilter() {
				public boolean accept(File arg0, String arg1) {
					return arg1.endsWith(".jar");
				}
			};

			{
				String[] derby_jdbc_jars = new File("lib/jdbc").list(jarsFilter);
				for (int i = 0; i < derby_jdbc_jars.length; ++i) {
					warJars.add("lib/jdbc" + FILE_SEPARATOR + derby_jdbc_jars[i]);
				}
			}

			{
				String[] pool_jars = new File("lib/pool").list(jarsFilter);
				for (int i = 0; i < pool_jars.length; ++i) {
					warJars.add("lib/pool" + FILE_SEPARATOR + pool_jars[i]);
				}
			}

			{
				String[] httpclient_jars = new File("lib/j2ee").list(jarsFilter);
				for (int i = 0; i < httpclient_jars.length; ++i) {
					warJars.add("lib/j2ee" + FILE_SEPARATOR + httpclient_jars[i]);
				}
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

		unzip(Gen.class.getResourceAsStream("/jaxws.zip"), GEN_WEBINF + FILE_SEPARATOR + "lib", new EqualNameFilter("activation.jar","jaxb-api.jar", "jaxb-impl.jar",
				"jaxb-xjc.jar", "jaxws-api.jar", "jaxws-libs.jar", "jaxws-rt.jar", "jaxws-tools.jar", "jsr173_api.jar", "jsr181-api.jar", "jsr250-api.jar",
				"saaj-api.jar", "saaj-impl.jar", "sjsxp.jar", "FastInfoset.jar", "http.jar", "mysql-connector-java-5.1.0-bin.jar", "ojdbc-14.jar"), BUFFER_SIZE,
				false, "Unzipping psTools..", 17);

		PrintWriter pw_web_xml = new PrintWriter(GEN_WEBINF + FILE_SEPARATOR + "web.xml");
		pw_web_xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw_web_xml
				.println("<web-app version=\"2.4\" xmlns=\"http://java.sun.com/xml/ns/j2ee\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd\">");
		pw_web_xml.println("<listener><listener-class>com.sun.xml.ws.transport.http.servlet.WSServletContextListener</listener-class></listener>");

		for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);
			pw_web_xml
					.println("<servlet><servlet-name>"
							+ shortClassName
							+ "_servlet</servlet-name><servlet-class>http.InterceptorServlet</servlet-class><load-on-startup>1</load-on-startup></servlet>");
		}
		
		pw_web_xml
		.println("<servlet><servlet-name>"
				+ "WSServlet"
				+ "</servlet-name><servlet-class>com.sun.xml.ws.transport.http.servlet.WSServlet</servlet-class><load-on-startup>1</load-on-startup></servlet>");

		pw_web_xml
		.println("<servlet><servlet-name>"
				+ "MappingClassServlet"
				+ "</servlet-name><servlet-class>http.MappingClassServlet</servlet-class><load-on-startup>1</load-on-startup></servlet>");


		for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);
			pw_web_xml.println("<servlet-mapping><servlet-name>" + shortClassName + "_servlet</servlet-name><url-pattern>/" + shortClassName
					+ "</url-pattern></servlet-mapping>");
		}
		
		pw_web_xml.println("<servlet-mapping><servlet-name>" + "MappingClassServlet" +"</servlet-name><url-pattern>" + "/mapping/classes/*"
				+ "</url-pattern></servlet-mapping>");
		
		
		pw_web_xml.println("<session-config><session-timeout>30</session-timeout></session-config>");
		pw_web_xml.println("</web-app>");
		pw_web_xml.flush();
		pw_web_xml.close();

		PrintWriter pw_sun_jaxws_xml = new PrintWriter(GEN_WEBINF + FILE_SEPARATOR + "sun-jaxws.xml");
		pw_sun_jaxws_xml.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		pw_sun_jaxws_xml.println("<endpoints xmlns='http://java.sun.com/xml/ns/jax-ws/ri/runtime' version='2.0'>");

		for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);
			pw_sun_jaxws_xml.println("   <endpoint    name='name_" + shortClassName + "'   implementation='" + className + "Web" + "' url-pattern='/"
					+ shortClassName + "'/>");
		}

		pw_sun_jaxws_xml.println("</endpoints>");
		pw_sun_jaxws_xml.flush();
		pw_sun_jaxws_xml.close();

		if (PROPS_EMBED != null) {
			InputStream is = new FileInputStream(PROPS_EMBED);
			byte[] buffer = new byte[is.available()];
			is.read(buffer);
			RandomAccessFile raf = new RandomAccessFile(GEN_WEBINF + FILE_SEPARATOR + "classes" + FILE_SEPARATOR + "globals.properties", "rw");
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