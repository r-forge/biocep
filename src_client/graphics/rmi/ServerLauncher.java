package graphics.rmi;

import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import http.ClassServlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import remoting.RServices;
import uk.ac.ebi.microarray.pools.CreationCallBack;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServantCreationTimeout;

public class ServerLauncher {
	public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 60000 * 3;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		new Thread(new Runnable() {
			public void run() {
				
				final Acme.Serve.Serve srv = new Acme.Serve.Serve() {					
					public void setMappingTable(PathTreeDictionary mappingtable) {
						super.setMappingTable(mappingtable);
					}
				};
			
				java.util.Properties properties = new java.util.Properties();
				properties.put("port", Integer.decode(System.getProperty("localtomcat.port")));
				properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
				srv.arguments = properties;
				System.out.println("properties:" + properties + "  server: " + srv);
				srv.addServlet("/classes/", new ClassServlet());

				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
					public void run() {
						try {
							srv.notifyStop();
						} catch (java.io.IOException ioe) {
							ioe.printStackTrace();
						}
						srv.destroyAllServlets();
					}
				}));
				
				srv.serve();
			}
		}).start();

		try {
			System.out.println("R:"+createR());
		} catch (Exception e) {
			System.out.println("Things went wrong");
			e.printStackTrace();
		}
	}	
	public static RServices createR() throws Exception {

		
		
		String root = GUtils.INSTALL_DIR;
		new File(root).mkdir();
		
		String[] rinfo = GDDesktopLauncher.getRInfo(null);
		if (rinfo == null && System.getenv("R_HOME") != null) {
			String home = System.getenv("R_HOME");
			if (isWindowsOs() && !home.endsWith("\\")) {
				home = home + "\\";
			}
			if (!isWindowsOs() && !home.endsWith("/")) {
				home = home + "/";
			}
			rinfo = GDDesktopLauncher.getRInfo(home);
		}
		
		String rpath = rinfo != null ? rinfo[0].substring(0, rinfo[0].length() - "library".length()) : (System
				.getenv("R_HOME") != null ? System.getenv("R_HOME") : null);
		System.out.println("rpath=" + rpath);
		System.out.println("rversion=" + (rinfo != null ? rinfo[1] : ""));

		if (!rpath.endsWith("/") && !rpath.endsWith("\\"))
			rpath += "/";
		// String rlibs = System.getenv("R_LIBS") != null ?
		// System.getenv("R_LIBS") : (rinfo != null ? rinfo[0] : rpath+
		// "library");
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
		
		String[] requiredPackages = new String[] { "rJava", "JavaGD", "TypeInfo", "gplots" };
		Vector<String> installLibBatch = new Vector<String>();
		installLibBatch.add("source('http://bioconductor.org/biocLite.R')");

		for (int i = 0; i < requiredPackages.length; ++i) {
			if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
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

			final Process installProc = Runtime.getRuntime().exec(installCommand.toArray(new String[0]),
					envVector.toArray(new String[0]));
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

			if (installPackagesOutputFile.exists()
					&& installPackagesOutputFile.lastModified() > installPackagesFile.lastModified()) {
				BufferedReader br = new BufferedReader(new FileReader(installPackagesOutputFile));
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			}

			Vector<String> missingLibs = new Vector<String>();

			for (int i = 0; i < requiredPackages.length; ++i) {
				if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
					missingLibs.add(requiredPackages[i]);
				}
			}

			if (missingLibs.size() > 0) {
				System.out.println("The following packages probably couldn't be automatically installed\n"
						+ missingLibs);
			}

		}
		// ---------------------------------------
		String jripath = getLibraryPath("rJava", rpath, rlibs) + "jri/";
		System.out.println("jripath:" + jripath + "\n");
			
		String cp = "J:/workspace/client/bin"+ System.getProperty("path.separator")+"J:/Program Files/R/R-2.6.1/library/rJava/jri/JRI.jar"
					+System.getProperty("path.separator")+"J:/hop";
		
		ManagedServant[] servantHolder = new ManagedServant[1];
		RemoteException[] exceptionHolder = new RemoteException[1];
		
		CreationCallBack callBack = new CreationCallBack(servantHolder, exceptionHolder);
		String listenerStub = PoolUtils.stubToHex(callBack);

		
		Vector<String> command = new Vector<String>();
		command.add(System.getProperty("java.home") + "/bin/java");
		command.add("-classpath");
		command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));
		
		
		command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase=http://127.0.0.1:2566/classes/" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dservantclass=server.RServantImpl" + (isWindowsOs() ? "\"" : ""));
		
		command.add((isWindowsOs() ? "\"" : "") + "-Dprivate=true" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlistener.stub="+listenerStub+ (isWindowsOs() ? "\"" : ""));
		
		command.add((isWindowsOs() ? "\"" : "") + "-Dpreprocess.help=true" + (isWindowsOs() ? "\"" : ""));		
		command.add((isWindowsOs() ? "\"" : "") + "-Dapply.sandbox=false" + (isWindowsOs() ? "\"" : ""));
		
		
		command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + root + "wdir" + (isWindowsOs() ? "\"" : ""));				
		command.add((isWindowsOs() ? "\"" : "") + "-Dpstools.home=" + root + "PsTools/" + (isWindowsOs() ? "\"" : ""));
		
		
		
		command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog" + (isWindowsOs() ? "\"" : ""));
		/*
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.rootCategory=INFO,A1,A2" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1=org.apache.log4j.ConsoleAppender" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout.ConversionPattern= [%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=uk.ac.ebi.microarray.pools.RemoteAppender" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout.ConversionPattern= [%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
		*/
		
		
		
		command.add("vv.Main");
		command.add(System.getProperty("localtomcat.port"));


		final Process proc = Runtime.getRuntime()
				.exec(command.toArray(new String[0]), envVector.toArray(new String[0]));
		final Vector<String> killPrint = new Vector<String>();
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
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						killPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		
		long t1 = System.currentTimeMillis();
		while (servantHolder[0] == null && exceptionHolder[0]==null) {
			System.out.println(new Date());
			if (System.currentTimeMillis() - t1 >= SERVANT_CREATION_TIMEOUT_MILLISEC)
				throw new ServantCreationTimeout();
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		}
		
		if (exceptionHolder[0]!=null) {
			throw exceptionHolder[0];
		}
		return (RServices)servantHolder[0];

	}

	private static String getLibraryPath(String libName, String rpath, String rlibs) {
		if (!rpath.endsWith("/") && !rpath.endsWith("\\")) {
			rpath += "/";
		}
		if (rlibs != null && !rlibs.equals("") && !rlibs.endsWith("/") && !rlibs.endsWith("\\")) {
			rlibs += "/";
		}
		if (rlibs != null && !rlibs.equals("") && new File(rlibs + libName).exists()) {
			return rlibs + libName + "/";
		} else if (new File(rpath + "library/" + libName).exists()) {
			return rpath + "library/" + libName + "/";
		} else {
			return null;
		}
	}
}
