
import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.unzip;
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
import server.ServerManager;
import uk.ac.ebi.microarray.pools.ServerDefaults;
import static server.ServerManager.*;

public class ToolsMain {

	public static void runGen(URL[] codeUrls, HashMap<String, String> argMap) throws Exception {

		int memoryMinMegabytes = ServerDefaults._memoryMin;
		int memoryMaxMegabytes = ServerDefaults._memoryMax;

		new File(INSTALL_DIR).mkdir();

		String rpath = null;
		String rversion = null;

		if (new File(INSTALL_DIR + "R/" + EMBEDDED_R).exists()) {
			rpath = INSTALL_DIR + "R/" + EMBEDDED_R;
			rversion = EMBEDDED_R;
		} else {
			String[] rinfo = null;
			if (System.getenv("R_HOME") == null)
				rinfo = getRInfo(null);
			else
				rinfo = getRInfo(System.getenv("R_HOME"));
			System.out.println("+rinfo:" + rinfo + " " + Arrays.toString(rinfo));
			rpath = rinfo != null ? rinfo[0].substring(0, rinfo[0].length() - "library".length()) : null;
			rversion = (rinfo != null ? rinfo[1] : "");
		}

		System.out.println("rpath:" + rpath);
		System.out.println("rversion:" + rversion);
		if (rpath == null) {

			if (isWindowsOs()) {

				rpath = INSTALL_DIR + "R/" + EMBEDDED_R;

				int n = JOptionPane.showConfirmDialog(null, "R is not accessible from the command line\nWould you like to use the Embedded R?", "",
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.OK_OPTION) {
					String rZipFileName = null;
					rZipFileName = "http://biocep-distrib.r-forge.r-project.org/r/" + EMBEDDED_R + ".zip";
					URL rUrl = new URL(rZipFileName);
					InputStream is = rUrl.openConnection().getInputStream();
					unzip(is, INSTALL_DIR + "R/", null, BUFFER_SIZE, true, "Unzipping R..", ENTRIES_NUMBER);
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
		// String rlibs = System.getenv("R_LIBS") != null ?
		// System.getenv("R_LIBS") : (rinfo != null ? rinfo[0] : rpath+
		// "library");
		String rlibs = (INSTALL_DIR + "library").replace('\\', '/');
		new File(rlibs).mkdir();

		Vector<String> envVector = new Vector<String>();
		{
			Map<String, String> osenv = System.getenv();
			Map<String, String> env = new HashMap<String, String>(osenv);
			env.put("PATH", rpath + (isWindowsOs() ? "bin" : "lib"));
			env.put("LD_LIBRARY_PATH", rpath + (isWindowsOs() ? "bin" : "lib"));
			env.put("R_HOME", rpath);
			String R_LIBS = rlibs + System.getProperty("path.separator")
					+ (System.getenv("R_LIBS") != null ? System.getProperty("path.separator") + System.getenv("R_LIBS") : "");
			System.out.println("R_LIBS:" + R_LIBS);
			env.put("R_LIBS", R_LIBS);
			for (String k : env.keySet()) {
				envVector.add(k + "=" + env.get(k));
			}
		}

		String[] requiredPackages = null;

		if (isWindowsOs()) {
			requiredPackages = new String[] { "rJava", "JavaGD", "iplots", "TypeInfo", "Cairo" };
		} else {
			requiredPackages = new String[] { "rJava", "JavaGD", "iplots", "TypeInfo" };
		}

		Vector<String> installLibBatch = new Vector<String>();
		installLibBatch.add("source('http://bioconductor.org/biocLite.R')");

		for (int i = 0; i < requiredPackages.length; ++i) {
			if (!new File(rlibs + "/" + requiredPackages[i]).exists()) {
				installLibBatch.add("biocLite('" + requiredPackages[i] + "',lib='" + rlibs + "')");
			}
			/*
			 * if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
			 * installLibBatch.add("biocLite('" + requiredPackages[i] +
			 * "',lib='" + rlibs + "')"); }
			 */
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
			installCommand.add(installPackagesFile.getAbsolutePath());
			installCommand.add(installPackagesOutputFile.getAbsolutePath());

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
				/*
				 * if (getLibraryPath(requiredPackages[i], rpath, rlibs) ==
				 * null) { missingLibs.add(requiredPackages[i]); }
				 */
			}

			if (missingLibs.size() > 0) {
				System.out.println("The following packages probably couldn't be automatically installed\n" + missingLibs);
			}

		}

		String bootstrap = (INSTALL_DIR + "classes/bootstrap").replace('\\', '/');
		System.out.println(bootstrap);
		if (!new File(bootstrap).exists())
			new File(bootstrap).mkdirs();
		InputStream is = ServerManager.class.getResourceAsStream("/bootstrap/Boot.class");
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
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=uk.ac.ebi.microarray.pools.RemoteAppender" + (isWindowsOs() ? "\"" : ""));
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

		String[] embedPropertiesNames = new String[] { "pools.provider.factory", "regsitry.host", "regsitry.port", "naming.mode", "pools.dbmode.type",
				"pools.dbmode.host", "pools.dbmode.port", "pools.dbmode.name", "pools.dbmode.user", "pools.dbmode.password", "pools.dbmode.defaultpoolname",
				"pools.dbmode.killused", "node.manager.name", "private.servant.node.name", "http.frontend.url" };

		String[] embedPropertiesDefaultValues = new String[] { null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null };

		Vector<String> genArgs = new Vector<String>();
		genArgs.add(tempFile.getAbsolutePath());
		for (int i = 0; i < embedPropertiesNames.length; ++i) {
			if (embedPropertiesDefaultValues[i] != null)
				genArgs.add(embedPropertiesNames[i] + "=" + embedPropertiesDefaultValues[i]);
		}
		uk.ac.ebi.microarray.pools.PropertiesGenerator.main((String[]) genArgs.toArray(new String[0]));

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
			String jarfile = jar.substring("jar:file:".length(), jar.length() - "/ToolsMain.class".length() - 1);
			jarfile.replace('\\', '/');
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
		runGen(codeUrls, argMap);

	}

}
