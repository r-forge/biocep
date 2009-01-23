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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;

import org.kchine.r.server.manager.ServantCreationFailed;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;

import static org.kchine.r.server.manager.ServerManager.*;
import static org.kchine.rpf.PoolUtils.isWindowsOs;
import static org.kchine.rpf.PoolUtils.unzip;

public class ToolsMain {

	public static void runGen(String RBinPath, URL[] codeUrls, HashMap<String, String> argMap) throws Exception {

		int memoryMinMegabytes = ServerDefaults._memoryMin;
		int memoryMaxMegabytes = ServerDefaults._memoryMax;

		
		new File(INSTALL_DIR).mkdir();

		String rpath=null;
		String rversion=null;						
		String[] rinfo = null;
		
		if (RBinPath!=null && !RBinPath.equals("")) {
			rinfo = getRInfo(RBinPath);
			if (rinfo==null) {
				System.exit(0);
			}
			rpath = rinfo[0];
			rversion =rinfo[1];
		} else if (new File(INSTALL_DIR+"R/"+EMBEDDED_R).exists()){
			
			rinfo = getRInfo(INSTALL_DIR+"R/"+EMBEDDED_R+"/bin/R.exe");
			if (rinfo==null) {
				throw new ServantCreationFailed();
			}
			rpath = rinfo[0];
			rversion =rinfo[1];
			
			
		} else {
			
			String rhome=System.getenv("R_HOME");
			if (rhome == null) {
				rinfo = getRInfo(null);
			} else {
				if (!rhome.endsWith("/")) {rhome = rhome + "/";}
				System.out.println("R_HOME is set to :" + rhome);
				rinfo = getRInfo(rhome+"bin/R");
			}
			
			System.out.println("+rinfo:" + rinfo + " " + Arrays.toString(rinfo));
			rpath = rinfo != null ? rinfo[0] : null;
			rversion = (rinfo != null ? rinfo[1] : "");
		}

		
		
		
		System.out.println("rpath:" + rpath);
		System.out.println("rversion:" + rversion);
		if (rpath == null) {

			if (isWindowsOs()) {


				int n = JOptionPane.showConfirmDialog(null, "R is not accessible from the command line\nWould you like to use the Embedded R?", "",
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.OK_OPTION) {
					String rZipFileName = null;
					rZipFileName = "http://biocep-distrib.r-forge.r-project.org/r/" + EMBEDDED_R + ".zip";
					URL rUrl = new URL(rZipFileName);
					InputStream is = rUrl.openConnection().getInputStream();
					unzip(is, INSTALL_DIR + "R/", null, BUFFER_SIZE, true, "Unzipping R..", ENTRIES_NUMBER);
					
					rinfo = getRInfo(INSTALL_DIR+"R/"+EMBEDDED_R+"/bin/R.exe");
					if (rinfo==null) {
						throw new ServantCreationFailed();
					}
					rpath = rinfo[0];
					rversion =rinfo[1];
					
				} else {
					JOptionPane.showMessageDialog(null, "please add R to your System path or set R_HOME to the root Directory of your local R installation\n");
					System.exit(0);
				}

			} else {

				System.out
						.println("R is not accessible from the command line\n please add R to your System path \nor set R_HOME to the root Directory of your local R installation");

				System.exit(0);
			}

		}

		if (!rpath.endsWith("/") && !rpath.endsWith("\\"))
			rpath += "/";
		
		
		String rlibs = (INSTALL_DIR + "library/"+rversion.substring(0,rversion.lastIndexOf(' ')).replace(' ', '-')).replace('\\', '/');
		new File(rlibs).mkdirs();

		Vector<String> envVector = new Vector<String>();
		{
			
			
			Map<String, String> osenv = System.getenv();
			Map<String, String> env = new HashMap<String, String>(osenv);
			String OS_PATH=osenv.get("PATH");
			if (OS_PATH==null) OS_PATH=osenv.get("Path");
			if (OS_PATH==null) OS_PATH="";
			env.put("Path", rpath + (isWindowsOs() ? "bin" : "lib") + System.getProperty("path.separator")+ OS_PATH);
			env.put("LD_LIBRARY_PATH", rpath + (isWindowsOs() ? "bin" : "lib"));
			env.put("R_HOME", rpath);
			String R_LIBS = rlibs + System.getProperty("path.separator")
					+ (System.getenv("R_LIBS") != null ? System.getProperty("path.separator") + System.getenv("R_LIBS") : "");
			System.out.println("R_LIBS:" + R_LIBS);
			env.put("R_LIBS", R_LIBS);
			
			// !!!!!
			if (System.getenv("JDK_HOME")!=null) env.put("JAVA_HOME", System.getenv("JDK_HOME"));
			
			for (String k : env.keySet()) {
				envVector.add(k + "=" + env.get(k));
			}
		}

		String[] requiredPackages = null;

		if ((System.getenv("BIOCEP_USE_DEFAULT_LIBS") != null && System.getenv("BIOCEP_USE_DEFAULT_LIBS").equalsIgnoreCase("false"))
				|| (System.getProperty("use.default.libs") != null && System.getProperty("use.default.libs").equalsIgnoreCase("true"))) {
			requiredPackages = new String[0];
		} else {
			if (isWindowsOs()) {
				requiredPackages = new String[] { "rJava", "JavaGD", "iplots", "TypeInfo", "Cairo" };
			} else {
				requiredPackages = new String[] { "rJava", "JavaGD", "iplots", "TypeInfo" };
			}
		}

		Vector<String> installLibBatch = new Vector<String>();
		installLibBatch.add("source('http://bioconductor.org/biocLite.R')");

		for (int i = 0; i < requiredPackages.length; ++i) {
			if (!new File(rlibs + "/" + requiredPackages[i]).exists()) {
				installLibBatch.add("biocLite('" + requiredPackages[i] + "',lib='" + rlibs + "')");
			}
		}

		if (installLibBatch.size() > 1) {

			File installPackagesFile = new File(INSTALL_DIR + "installRequiredPackages.R");
			File installPackagesOutputFile = new File(INSTALL_DIR + "installRequiredPackages.Rout");

			FileWriter fw = new FileWriter(installPackagesFile);
			PrintWriter pw = new PrintWriter(fw);
			for (int i = 0; i < installLibBatch.size(); ++i) {
				pw.println(installLibBatch.elementAt(i));
			}
			fw.close();

			Vector<String> installCommand = new Vector<String>();
			installCommand.add(rpath + "bin/R");
			installCommand.add("CMD");
			installCommand.add("BATCH");
			installCommand.add("--no-save");
			installCommand.add((isWindowsOs() ? "\"" : "")+installPackagesFile.getAbsolutePath()+(isWindowsOs() ? "\"" : ""));
			installCommand.add((isWindowsOs() ? "\"" : "")+installPackagesOutputFile.getAbsolutePath()+(isWindowsOs() ? "\"" : ""));

			System.out.println(installCommand);

			final Process installProc = Runtime.getRuntime().exec(installCommand.toArray(new String[0]), envVector.toArray(new String[0]));
			final Vector<String> installPrint = new Vector<String>();
			final Vector<String> installErrorPrint = new Vector<String>();

			new Thread(new Runnable() {
				public void run() {
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(installProc.getErrorStream()));
						String line = null;
						while ((line = br.readLine()) != null) {
							System.out.println(line);
							installErrorPrint.add(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			new Thread(new Runnable() {
				public void run() {
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(installProc.getInputStream()));
						String line = null;
						while ((line = br.readLine()) != null) {
							System.out.println(line);
							installPrint.add(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			installProc.waitFor();

			if (installPackagesOutputFile.exists() && installPackagesOutputFile.lastModified() > installPackagesFile.lastModified()) {
				BufferedReader br = new BufferedReader(new FileReader(installPackagesOutputFile));
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			}

			Vector<String> missingLibs = new Vector<String>();

			for (int i = 0; i < requiredPackages.length; ++i) {
				if (!new File(rlibs + "/" + requiredPackages[i]).exists()) {
					missingLibs.add(requiredPackages[i]);
				}
			}

			if (missingLibs.size() > 0) {
				System.out.println("The following packages probably couldn't be automatically installed\n" + missingLibs);
			}

		}

		String bootstrap = (INSTALL_DIR + "classes/org/kchine/r/server/manager/bootstrap").replace('\\', '/');
		System.out.println(bootstrap);
		if (!new File(bootstrap).exists())
			new File(bootstrap).mkdirs();
		InputStream is = ServerManager.class.getResourceAsStream("/org/kchine/r/server/manager/bootstrap/Boot.class");
		byte[] buffer = new byte[is.available()];
		try {
			for (int i = 0; i < buffer.length; ++i) {
				int b = is.read();
				buffer[i] = (byte) b;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		RandomAccessFile raf = new RandomAccessFile(bootstrap + "/Boot.class", "rw");
		raf.setLength(0);
		raf.write(buffer);
		raf.close();

		// ---------------------------------------

		// String jripath = getLibraryPath("rJava", rpath, rlibs) + "jri/";
		String jripath = rlibs + "/rJava/jri/";
		System.out.println("jripath:" + jripath + "\n");

		String cp = INSTALL_DIR + "classes";

		if (codeUrls != null && codeUrls.length > 0) {
			for (int i = 0; i < codeUrls.length; ++i) {
				URL codeUrl = codeUrls[i];
				if (codeUrl.toString().toLowerCase().startsWith("file:")) {
					cp += System.getProperty("path.separator") + codeUrl.toString().substring("file:".length());
				}
			}
		}

		System.out.println("++cp=" + cp);

		Vector<String> command = new Vector<String>();
		command.add(System.getProperty("java.home") + "/bin/java");
		command.add((isWindowsOs() ? "\"" : "") + "-DXms" + memoryMinMegabytes + "m" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-DXmx" + memoryMaxMegabytes + "m" + (isWindowsOs() ? "\"" : ""));
		command.add("-classpath");
		command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));

		command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.rootCategory=DEBUG,A1,A2" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1=org.apache.log4j.ConsoleAppender" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=org.kchine.rpf.RemoteAppender" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));

		for (String arg : argMap.keySet())
			command.add((isWindowsOs() ? "\"" : "") + "-D" + arg + "=" + argMap.get(arg) + (isWindowsOs() ? "\"" : ""));

		command.add("Gen");

		final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]), envVector.toArray(new String[0]));

		final Vector<String> outPrint = new Vector<String>();
		final Vector<String> errorPrint = new Vector<String>();

		System.out.println(" command : " + command);

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						errorPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println();
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						outPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public static void main(String[] args) throws Exception {

		File tempFile = null;

		tempFile = new File(System.getProperty("java.io.tmpdir") + "/temp_globals.properties").getCanonicalFile();
		if (tempFile.exists())
			tempFile.delete();

		
		
		
		String[] embedPropertiesNames = new String[] { 
				
				"regsitry.host", "regsitry.port", "naming.mode",
				
				"db.type", "db.host", "db.port", "db.dir", "db.name", "db.user", "db.password", 			
				"httpregistry.url", "httpregistry.login", "httpregistry.password", 
				
				"pools.provider.factory",  "pools.dbmode.type",	"pools.dbmode.host", "pools.dbmode.port", "pools.dbmode.dir", "pools.dbmode.name", "pools.dbmode.user", "pools.dbmode.password", 
				"pools.dbmode.defaultpoolname",				
				"pools.dbmode.killused", 
				
				"node.manager.name", "private.servant.node.name", 
				"http.frontend.url",
				
				"cloud"};

		String[] embedPropertiesDefaultValues = new String[embedPropertiesNames.length]; // all null

		Vector<String> genArgs = new Vector<String>();
		genArgs.add(tempFile.getAbsolutePath());
		for (int i = 0; i < embedPropertiesNames.length; ++i) {
			if (embedPropertiesDefaultValues[i] != null)
				genArgs.add(embedPropertiesNames[i] + "=" + embedPropertiesDefaultValues[i]);
		}
		org.kchine.rpf.PropertiesGenerator.main((String[]) genArgs.toArray(new String[0]));

		HashMap<String, String> argMap = new HashMap<String, String>();
		String[] argNames = new String[] { "file", "dir", "outputdir", "mappingjar", "warname", "propsembed", "keepintermediate", "formatsource", "ws.r.api",
				"targetjdk" };
		for (int i = 0; i < argNames.length; ++i) {
			if (System.getProperty(argNames[i]) != null && !System.getProperty(argNames[i]).equals(""))
				argMap.put(argNames[i], System.getProperty(argNames[i]));
		}

		if (argMap.get("propsembed") == null) {
			argMap.put("propsembed", tempFile.getAbsolutePath());
		}

		URL[] codeUrls = null;
		Vector<URL> v = new Vector<URL>();
		String classPath = System.getProperty("java.class.path");

		StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
		while (st.hasMoreTokens()) {
			v.add(new File((String) st.nextElement()).toURL());
		}

		String jar = ToolsMain.class.getResource("/ToolsMain.class").toString();
		if (jar.startsWith("jar:")) {
			String jarurl=jar.substring("jar:".length(), jar.length()-"/ToolsMain.class".length()-1);				
			String jarfile = PoolUtils.getFileFromURL(new URL(jarurl)).getAbsolutePath();
			File toolsDotJarFile = new File(jarfile.substring(0, jarfile.lastIndexOf("/")) + "/tools.jar");
			System.out.println("tools jar file :" + toolsDotJarFile);
			if (toolsDotJarFile.exists()) {
				try {
					v.add(toolsDotJarFile.toURL());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		codeUrls = (URL[]) v.toArray(new URL[0]);

		System.out.println("jarfile:" + Arrays.toString(codeUrls));
		String rbinary=System.getProperty("r.binary");	
		runGen(rbinary,codeUrls, argMap);

	}

}
