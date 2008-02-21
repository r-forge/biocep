package graphics.rmi;

import static uk.ac.ebi.microarray.pools.PoolUtils.isMacOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.unzip;
import http.local.LocalClassServlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.JOptionPane;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.CreationCallBack;
import uk.ac.ebi.microarray.pools.MainPsToolsDownload;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServantCreationTimeout;

public class ServerLauncher {
	public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 60000 * 3;
	public static int BUFFER_SIZE = 8192 * 5;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new Thread(new Runnable() {
			public void run() {

				final Acme.Serve.Serve srv = new Acme.Serve.Serve() {
					public void setMappingTable(PathTreeDictionary mappingtable) {
						super.setMappingTable(mappingtable);
					}
				};

				java.util.Properties properties = new java.util.Properties();
				properties.put("port", GUtils.getLocalTomcatPort());
				properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
				srv.arguments = properties;
				System.out.println("properties:" + properties + "  server: " + srv);
				srv.addServlet("/classes/", new LocalClassServlet());

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
			System.out.println("R:" + createR(256,256));
		} catch (Exception e) {
			System.out.println("Things went wrong");
			e.printStackTrace();
		}
	}

	public static RServices createR(int memoryMinMegabytes, int memoryMaxMegabytes) throws Exception {

		String urlprefix = null;
		try {
			Class<?> ServiceManagerClass = GDDesktopLauncher.class.getClassLoader().loadClass("javax.jnlp.ServiceManager");
			Object basicServiceInstance = ServiceManagerClass.getMethod("lookup", String.class).invoke(null, "javax.jnlp.BasicService");
			Class<?> BasicServiceClass = GDDesktopLauncher.class.getClassLoader().loadClass("javax.jnlp.BasicService");
			urlprefix = BasicServiceClass.getMethod("getCodeBase").invoke(basicServiceInstance).toString();
		} catch (Exception e) {

		}
		System.out.println("url prefix:" + urlprefix);
		URL rjbURL = null;
		System.out.println(ServerLauncher.class.getResource("/graphics/rmi/ServerLauncher.class"));

		if (urlprefix != null) {
			rjbURL = new URL(urlprefix + "appletlibs/RJB.jar");
		} else {
			String thisUrl = ServerLauncher.class.getResource("/graphics/rmi/ServerLauncher.class").toString();
			if (thisUrl.indexOf("http:") != -1) {
				rjbURL = new URL(thisUrl.substring(thisUrl.indexOf("http:"), thisUrl.indexOf("RJB.jar") + "RJB.jar".length()));
			}
		}

		String root = GUtils.INSTALL_DIR;
		new File(root).mkdir();

		String[] rinfo = GUtils.getRInfo(null);
		if (rinfo == null && System.getenv("R_HOME") != null) {
			String home = System.getenv("R_HOME");
			if (isWindowsOs() && !home.endsWith("\\")) {
				home = home + "\\";
			}
			if (!isWindowsOs() && !home.endsWith("/")) {
				home = home + "/";
			}
			rinfo = GUtils.getRInfo(home);
		}

		String rpath = rinfo != null ? rinfo[0].substring(0, rinfo[0].length() - "library".length()) : (System.getenv("R_HOME") != null ? System
				.getenv("R_HOME") : null);
		System.out.println("rpath=" + rpath);
		System.out.println("rversion=" + (rinfo != null ? rinfo[1] : ""));

		if (rpath == null) {

			if (isWindowsOs()) {

				int n = JOptionPane.showConfirmDialog(null, "R is not accessible from the command line\nWould you like to use the Embedded R?", "",
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.OK_OPTION) {
					String rZipFileName = null;
					if (isWindowsOs()) {
						if (!new File(root + "R/R-2.6.0/bin/R.dll").exists()) {
							rZipFileName = "R-2.6.0Win.zip";
						} else {
							rZipFileName = null;
						}
					} else if (isMacOs()) {
						if (!new File(root + "R/R-2.6.0/lib/libR.dylib").exists()) {
							rZipFileName = "R-2.6.0MacOSX.zip";
						} else {
							rZipFileName = null;
						}
					}

					if (rZipFileName != null) {
						URL rUrl = new URL(rjbURL.toString().substring(0, rjbURL.toString().indexOf("/appletlibs")) + "/jawslibs/" + rZipFileName);

						InputStream is = null;
						try {
							is = rUrl.openConnection().getInputStream();
						} catch (Exception e) {
							rUrl = new URL("http://www.ebi.ac.uk/microarray-srv/frontendapp/" + "jawslibs/" + rZipFileName);
							is = rUrl.openConnection().getInputStream();
						}

						unzip(is, root + "R/", null, BUFFER_SIZE, true, "Unzipping R..", 3816);

					}

					rpath = root + "R/R-2.6.0/";

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
				if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
					missingLibs.add(requiredPackages[i]);
				}
			}

			if (missingLibs.size() > 0) {
				System.out.println("The following packages probably couldn't be automatically installed\n" + missingLibs);
			}

		}

		String bootstrap = (root + "classes/bootstrap").replace('\\', '/');
		System.out.println(bootstrap);
		if (!new File(bootstrap).exists())
			new File(bootstrap).mkdirs();
		InputStream is = ServerLauncher.class.getResourceAsStream("/bootstrap/Boot.class");
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

		new File(root + "PsTools").mkdirs();
		MainPsToolsDownload.main(new String[] { root + "PsTools" });

		// ---------------------------------------
		
		String jripath = getLibraryPath("rJava", rpath, rlibs) + "jri/";
		System.out.println("jripath:" + jripath + "\n");

		String cp = root + "classes";

		ManagedServant[] servantHolder = new ManagedServant[1];
		RemoteException[] exceptionHolder = new RemoteException[1];

		CreationCallBack callBack = null;

		try {
			callBack = new CreationCallBack(servantHolder, exceptionHolder);
			String listenerStub = PoolUtils.stubToHex(callBack);

			Vector<String> command = new Vector<String>();
			command.add(System.getProperty("java.home") + "/bin/java");
			
			command.add((isWindowsOs() ? "\"" : "") + "-DXms"+ memoryMinMegabytes +"m" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-DXmx"+ memoryMaxMegabytes +"m" + (isWindowsOs() ? "\"" : ""));
			
			command.add("-classpath");
			command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));
			
			command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase=http://127.0.0.1:" + GUtils.getLocalTomcatPort() + "/classes/"
					+ (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dservantclass=server.RServantImpl" + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dprivate=true" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dlistener.stub=" + listenerStub + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dpreprocess.help=true" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dapply.sandbox=true" + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + root + "wdir" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dpstools.home=" + root + "PsTools/" + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dregistryhost=127.0.0.1" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dregistryport=" + GUtils.getLocalRmiRegistryPort() + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
					+ (isWindowsOs() ? "\"" : ""));

			command.add("bootstrap.Boot");
			command.add("" + GUtils.getLocalTomcatPort());

			final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]), envVector.toArray(new String[0]));
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
			while (servantHolder[0] == null && exceptionHolder[0] == null) {
				//System.out.println(new Date());
				if (System.currentTimeMillis() - t1 >= SERVANT_CREATION_TIMEOUT_MILLISEC)
					throw new ServantCreationTimeout();
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}

			if (exceptionHolder[0] != null) {
				throw exceptionHolder[0];
			}
			return (RServices) servantHolder[0];
		} finally {
			if (callBack != null) {
				UnicastRemoteObject.unexportObject(callBack, true);
			}
		}

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
