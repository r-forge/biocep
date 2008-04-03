import static uk.ac.ebi.microarray.pools.PoolUtils.isMacOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.unzip;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.JOptionPane;
import server.ServerManager;
import uk.ac.ebi.microarray.pools.MainPsToolsDownload;


public class ToolsMain {

	public static void runGen(URL[] codeUrls, HashMap<String, String> argMap) throws Exception {

		int memoryMinMegabytes = 256;
		int memoryMaxMegabytes = 256;
		

		String root = ServerManager.INSTALL_DIR;
		new File(root).mkdir();
		String[] rinfo = ServerManager.getRInfo(null);
		System.out.println("+rinfo:" + rinfo + " " + Arrays.toString(rinfo));
		String rpath = rinfo != null ? rinfo[0].substring(0, rinfo[0].length() - "library".length()) : null;
		System.out.println("rpath=" + rpath);
		System.out.println("rversion=" + (rinfo != null ? rinfo[1] : ""));

		if (rpath == null) {

			if (isWindowsOs()) {

				int n = JOptionPane.showConfirmDialog(null, "R is not accessible from the command line\nWould you like to use the Embedded R?", "",
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.OK_OPTION) {
					String rZipFileName = null;
					if (isWindowsOs()) {
						if (!new File(root + "R/R-2.6.2/bin/R.dll").exists()) {
							rZipFileName = "http://biocep-distrib.r-forge.r-project.org/r/R-2.6.2-Win.zip";
						} else {
							rZipFileName = null;
						}
					} else if (isMacOs()) {
						if (!new File(root + "R/R-2.6.2/lib/libR.dylib").exists()) {
							rZipFileName = "http://biocep-distrib.r-forge.r-project.org/r/R-2.6.2-Mac.zip";
						} else {
							rZipFileName = null;
						}
					}

					if (rZipFileName != null) {
						URL rUrl = new URL(rZipFileName);
						InputStream is = rUrl.openConnection().getInputStream();
						unzip(is, root + "R/", null, ServerManager.BUFFER_SIZE, true, "Unzipping R..", 3606);
					}

					rpath = root + "R/R-2.6.2/";

				} else {
					JOptionPane.showMessageDialog(null, "please add R to your System path or set R_HOME to the root Directory of your local R installation\n");
					System.exit(0);
				}

			} else {
				JOptionPane
						.showMessageDialog(null,
								"R is not accessible from the command line\n please add R to your System path \nor set R_HOME to the root Directory of your local R installation\n");
				System.exit(0);
			}

		}

		if (!rpath.endsWith("/") && !rpath.endsWith("\\")) {
			rpath += "/";
		}
		String rlibs = (root + "library").replace('\\', '/');
		new File(rlibs).mkdir();

		Vector<String> envVector = new Vector<String>();
		{
			Map<String, String> osenv = System.getenv();
			Map<String, String> env = new HashMap<String, String>(osenv);
			env.put("Path", rpath + (isWindowsOs() ? "bin" : "lib"));
			env.put("LD_LIBRARY_PATH", rpath + (isWindowsOs() ? "bin" : "lib"));
			env.put("R_HOME", rpath);
			env.put("R_LIBS", rlibs + (System.getenv("R_LIBS") != null ? ";" + System.getenv("R_LIBS") : ""));
			for (String k : env.keySet()) {
				envVector.add(k + "=" + env.get(k));
			}
		}
		
		System.out.println("env:"+envVector);

		
		
		String[] requiredPackages = null;

		if (isWindowsOs()) {
			requiredPackages = new String[] { "rJava", "JavaGD", "TypeInfo", "Cairo" };
		} else {
			requiredPackages = new String[] { "rJava", "JavaGD", "TypeInfo" };
		}

		Vector<String> installLibBatch = new Vector<String>();
		installLibBatch.add("source('http://bioconductor.org/biocLite.R')");

		for (int i = 0; i < requiredPackages.length; ++i) {
			if (ServerManager.getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
				installLibBatch.add("biocLite('" + requiredPackages[i] + "',lib='" + rlibs + "')");
			}
		}

		if (installLibBatch.size() > 1) {

			File installPackagesFile = new File(root + "installRequiredPackages.R");
			File installPackagesOutputFile = new File(root + "installRequiredPackages.Rout");

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
				if (ServerManager.getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
					missingLibs.add(requiredPackages[i]);
				}
			}

			if (missingLibs.size() > 0) {
				System.out.println("The following packages probably couldn't be automatically installed\n" + missingLibs);
			}

		}

		new File(root + "PsTools").mkdirs();
		MainPsToolsDownload.main(new String[] { root + "PsTools" });

		String jripath = ServerManager.getLibraryPath("rJava", rpath, rlibs) + "jri/";
		System.out.println("jripath:" + jripath + "\n");
		
		String cp = root + "classes";
		if (codeUrls != null && codeUrls.length > 0) {
			for (int i = 0; i < codeUrls.length; ++i) {
				URL codeUrl = codeUrls[i];
				if (codeUrl.toString().toLowerCase().startsWith("file:")) {
					cp += System.getProperty("path.separator") + codeUrl.toString().substring("file:".length());
				}
			}
		}
		
		System.out.println("++cp="+cp);

		Vector<String> command = new Vector<String>();
		command.add(System.getProperty("java.home") + "/bin/java");
		command.add((isWindowsOs() ? "\"" : "") + "-DXms" + memoryMinMegabytes + "m" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-DXmx" + memoryMaxMegabytes + "m" + (isWindowsOs() ? "\"" : ""));
		command.add("-classpath");
		command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dpstools.home=" + root + "PsTools/" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.rootCategory=DEBUG,A1,A2" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1=org.apache.log4j.ConsoleAppender" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=uk.ac.ebi.microarray.pools.RemoteAppender" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
		
		for (String arg:argMap.keySet()) command.add((isWindowsOs() ? "\"" : "") + "-D"+arg+"="+argMap.get(arg)+ (isWindowsOs() ? "\"" : "")); 

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

	public static void main(String[] args) throws Exception{
		
		HashMap<String, String> argMap=new HashMap<String, String>();
		
		String[] argNames=new String[]{"file","dir", "outputdir", "mappingjar", "warname", "propsembed","keepintermediate", "formatsource" , "ws.r.api", "targetjdk" };
		for (int i=0; i<argNames.length;++i) {
			if (System.getProperty(argNames[i])!=null && !System.getProperty(argNames[i]).equals("")) argMap.put(argNames[i], System.getProperty(argNames[i]));
		} 		
		String classUrl1=ToolsMain.class.getResource("/ToolsMain.class").toString();
		URL[] codeUrls=null;
		Vector<URL> v=new Vector<URL>();
		String classPath = System.getProperty("java.class.path");

	    StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
	    while (st.hasMoreTokens()) {
	    	v.add(new File((String)st.nextElement()).toURL());
	    }
		codeUrls=(URL[])v.toArray(new URL[0]);

		System.out.println("jarfile:"+Arrays.toString(codeUrls));
		runGen(codeUrls,argMap);

	}

}
