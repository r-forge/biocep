package graphics.rmi;

import static uk.ac.ebi.microarray.pools.PoolUtils.isMacOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.unzip;
import http.local.LocalClassServlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.JOptionPane;

import bootstrap.BootSsh;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.CreationCallBack;
import uk.ac.ebi.microarray.pools.MainPsToolsDownload;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServantCreationTimeout;
import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class ServerLauncher {
	public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 60000 * 5;
	public static int BUFFER_SIZE = 8192 * 5;

	/**
	 * @param args
	 */
	public static Acme.Serve.Serve srv;
	public static void main(String[] args) throws Exception {
		
		
		new Thread(new Runnable() {
			public void run() {

				srv = new Acme.Serve.Serve() {
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
				srv.serve();
			}
		}).start();
		
		
		new Thread(new Runnable() {
			public void run() {
				System.out.println("local rmiregistry port : " + GUtils.getLocalTomcatPort());
				try {
					LocateRegistry.createRegistry(GUtils.getLocalRmiRegistryPort());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		RServices r = createRSsh(true, 
				PoolUtils.getHostIp(), GUtils.getLocalTomcatPort(),
				PoolUtils.getHostIp(), GUtils.getLocalRmiRegistryPort(), 256, 256, 
				"192.168.189.128", "ebi", "ebibiocep");
		System.out.println("rr:"+r);
		
		//BootSsh.main(new String[]{"false","127.0.0.1",""+GUtils.getLocalTomcatPort(),"127.0.0.1",""+GUtils.getLocalRmiRegistryPort(),"256","256"});
		/*
		try {
			RServices r=createR(true, "127.0.0.1",GUtils.getLocalTomcatPort(), "127.0.0.1", GUtils.getLocalRmiRegistryPort(),256,256);
			String processId=r.getProcessId();
			System.out.println("R:" + r);
			try {
				if (PoolUtils.isWindowsOs()) {
					PoolUtils.killLocalWinProcess(processId, true);
				} else {
					PoolUtils.killLocalUnixProcess(processId, true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}			
		} catch (Exception e) {
			System.out.println("Things went wrong");
			e.printStackTrace();
		}
		*/

		if (srv!=null) {
			
			try {
				srv.notifyStop();
			} catch (java.io.IOException ioe) {
				ioe.printStackTrace();
			}
			
			try {
				srv.destroyAllServlets();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	
		
	}

	
	public static RServices createRSsh(boolean keepAlive, String codeServerHostIp, int codeServerPort, 
            String rmiRegistryHostIp, int rmiRegistryPort , 
            int memoryMinMegabytes, int memoryMaxMegabytes,
            String sshHostIp, String sshLogin, String sshPwd) throws Exception {

		Connection conn = null;
		try {
			conn=new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			
			InputStream is = ServerLauncher.class.getResourceAsStream("/bootstrap/BootSsh.class");
			byte[] buffer = new byte[is.available()];
			try {
				for (int i = 0; i < buffer.length; ++i) {
					int b = is.read();
					buffer[i] = (byte) b;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String bootstrapDir = System.getProperty("user.home") + "/RWorkbench/" + "classes/bootstrap";
			new File(bootstrapDir).mkdirs();
			RandomAccessFile raf = new RandomAccessFile(bootstrapDir + "/BootSsh.class", "rw");
			raf.setLength(0);
			raf.write(buffer);
			raf.close();	
				
			Session sess = null;
			try {
				sess=conn.openSession();
				sess.execCommand("mkdir -p RWorkbench/classes/bootstrap");
				sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);				
			} finally {
				try {sess.close();} catch (Exception e) {e.printStackTrace();}
			}
						
			
			new SCPClient(conn).put(bootstrapDir + "/BootSsh.class", "RWorkbench/classes/bootstrap");
			
			sess = conn.openSession();
			sess.execCommand("java -classpath RWorkbench/classes bootstrap.BootSsh"+" "+new Boolean(keepAlive)+" "
					+codeServerHostIp+" "+codeServerPort+" "
					+rmiRegistryHostIp+" "+rmiRegistryPort+" "
					+memoryMinMegabytes+" "+memoryMaxMegabytes);
			
			InputStream stdout = new StreamGobbler(sess.getStdout());
			final BufferedReader brOut = new BufferedReader(new InputStreamReader(stdout));
			
			InputStream stderr = new StreamGobbler(sess.getStderr());
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(stderr));
			
			final StringBuffer sshOutput=new StringBuffer();
			final RServices[] rHolder=new RServices[1];
			new Thread(new Runnable(){
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;
							sshOutput.append(line+"\n");
							System.out.println(line);
							
							int eIndex=sshOutput.indexOf(BootSsh.STUB_END_MARKER);
							if (eIndex!=-1) {
								int bIndex=sshOutput.indexOf(BootSsh.STUB_BEGIN_MARKER);
								String stub=sshOutput.substring(bIndex+BootSsh.STUB_BEGIN_MARKER.length(), eIndex);
								rHolder[0]=(RServices)PoolUtils.hexToStub(stub,ServerLauncher.class.getClassLoader());								
								break;
							}
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
			}).start();
			
			new Thread(new Runnable(){
				public void run() {
					try {
						while (true) {
							String line = brErr.readLine();
							if (line == null) break;
							System.out.println(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}					
				}
			}).start();
			
			
			long t1 = System.currentTimeMillis();
			while (rHolder[0] == null) {
				if (System.currentTimeMillis() - t1 >= SERVANT_CREATION_TIMEOUT_MILLISEC)
					throw new ServantCreationTimeout();
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}

			return rHolder[0];
			
		} finally {
			try {conn.close();} catch (Exception e) {e.printStackTrace();}
		}
		
	}
	
	public static RServices createR(boolean keepAlive, String codeServerHostIp, int codeServerPort, 
			                        String rmiRegistryHostIp, int rmiRegistryPort , 
			                        int memoryMinMegabytes, int memoryMaxMegabytes) throws Exception {

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
		
		if (urlprefix == null) {
			urlprefix="http://biocep.r-forge.r-project.org/appletlibs/";
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

		String[] requiredPackages = new String[] { "rJava", "JavaGD", "TypeInfo" };
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

			if (keepAlive) {
				command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase="+
						"http://"+codeServerHostIp+":" + codeServerPort + "/classes/"+ " "+
						urlprefix+"JRI.jar"+" "+
						urlprefix+"commons-logging-1.1.jar"+" "+
						urlprefix+"log4j-1.2.14.jar"+" "+
						urlprefix+"htmlparser.jar"+" "+
						urlprefix+"derbyclient.jar"+" "+
						urlprefix+"RJB.jar"+" "+
						urlprefix+"mapping.jar"+ (isWindowsOs() ? "\"" : ""));
			} else {				
				command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase=http://"+codeServerHostIp+":" + codeServerPort + "/classes/" + (isWindowsOs() ? "\"" : ""));
			}
			
			command.add((isWindowsOs() ? "\"" : "") + "-Dservantclass=server.RServantImpl" + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dprivate=true" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dlistener.stub=" + listenerStub + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dpreprocess.help=true" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dapply.sandbox=true" + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + root + "wdir" + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dpstools.home=" + root + "PsTools/" + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dregistryhost="+ rmiRegistryHostIp + (isWindowsOs() ? "\"" : ""));
			command.add((isWindowsOs() ? "\"" : "") + "-Dregistryport=" + rmiRegistryPort + (isWindowsOs() ? "\"" : ""));

			command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog"
					+ (isWindowsOs() ? "\"" : ""));

			command.add("bootstrap.Boot");
			command.add(new Boolean(keepAlive).toString());
			command.add(codeServerHostIp );
			command.add(""+codeServerPort );
			if (keepAlive) {				
				command.add(urlprefix);
			}

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
