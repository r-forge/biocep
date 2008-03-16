package graphics.rmi;

import static uk.ac.ebi.microarray.pools.PoolUtils.isMacOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;
import static uk.ac.ebi.microarray.pools.PoolUtils.unzip;
import java.awt.BorderLayout;
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
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import bootstrap.BootSsh;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.CreationCallBack;
import uk.ac.ebi.microarray.pools.MainPsToolsDownload;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemoteLogListener;
import uk.ac.ebi.microarray.pools.ServantCreationTimeout;
import uk.ac.ebi.microarray.pools.http.LocalClassServlet;
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

	static Server server ;
	public static void main(String[] args) throws Exception {

		server = new Server(PoolUtils.getLocalTomcatPort());
		Context root = new Context(server,"/",Context.SESSIONS);
		root.addServlet(new ServletHolder(new LocalClassServlet()), "/classes/*");
		server.start();
				
		
		while (!server.isStarted()){	
			try {Thread.sleep(20);} catch (Exception e) {}
		}


		new Thread(new Runnable() {
			public void run() {
				System.out.println("local rmiregistry port : " + PoolUtils.getLocalRmiRegistryPort());
				try {
					LocateRegistry.createRegistry(PoolUtils.getLocalRmiRegistryPort());

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
		
		  	
		  RServices r = createRSsh(false, PoolUtils.getHostIp(),
		  PoolUtils.getLocalTomcatPort(), PoolUtils.getHostIp(),
		  PoolUtils.getLocalRmiRegistryPort(), 256, 256, "192.168.189.131", "ebi", "ebibiocep", false);
		  
		  //System.out.println("make cluster result : "+r.cloneServer());
		  
		  String processId = r.getProcessId();
		  System.out.println("Local process ID:"+PoolUtils.getProcessId());
		  System.out.println("R process ID:"+processId);
			  		  
		  //System.exit(0);
	
	}

	static JTextArea createRSshProgressArea;
	static JProgressBar createRSshProgressBar;
	static JFrame createRSshProgressFrame;

	public static RServices createRSsh(boolean keepAlive, String codeServerHostIp, int codeServerPort, String rmiRegistryHostIp, int rmiRegistryPort,
			int memoryMinMegabytes, int memoryMaxMegabytes, String sshHostIp, String sshLogin, String sshPwd, boolean showProgress) throws BadSshHostException,
			BadSshLoginPwdException, Exception {

		if (showProgress) {
			createRSshProgressArea = new JTextArea();
			createRSshProgressBar = new JProgressBar(0, 100);
			createRSshProgressFrame = new JFrame("Create R Server via SSH");

			Runnable runnable = new Runnable() {
				public void run() {
					createRSshProgressArea.setFocusable(false);
					createRSshProgressBar.setIndeterminate(true);
					JPanel p = new JPanel(new BorderLayout());
					p.add(createRSshProgressBar, BorderLayout.SOUTH);
					p.add(new JScrollPane(createRSshProgressArea), BorderLayout.CENTER);
					createRSshProgressFrame.add(p);
					createRSshProgressFrame.pack();
					createRSshProgressFrame.setSize(300, 90);
					createRSshProgressFrame.setVisible(true);
					createRSshProgressFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					PoolUtils.locateInScreenCenter(createRSshProgressFrame);
				}
			};

			if (SwingUtilities.isEventDispatchThread())
				runnable.run();
			else {
				SwingUtilities.invokeLater(runnable);
			}
		}

		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			try {
				conn.connect();
			} catch (Exception e) {
				throw new BadSshHostException();
			}
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new BadSshLoginPwdException();

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
				sess = conn.openSession();
				sess.execCommand("mkdir -p RWorkbench/classes/bootstrap");
				sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
			} finally {
				try {
					if (sess != null)
						sess.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			new SCPClient(conn).put(bootstrapDir + "/BootSsh.class", "RWorkbench/classes/bootstrap");
			try {
				sess = conn.openSession();
				sess.execCommand("java -classpath RWorkbench/classes bootstrap.BootSsh" + " " + new Boolean(keepAlive) + " " + codeServerHostIp + " "
						+ codeServerPort + " " + rmiRegistryHostIp + " " + rmiRegistryPort + " " + memoryMinMegabytes + " " + memoryMaxMegabytes+" "+"System.out");

				InputStream stdout = new StreamGobbler(sess.getStdout());
				final BufferedReader brOut = new BufferedReader(new InputStreamReader(stdout));

				InputStream stderr = new StreamGobbler(sess.getStderr());
				final BufferedReader brErr = new BufferedReader(new InputStreamReader(stderr));
				final StringBuffer sshOutput = new StringBuffer();
				new Thread(new Runnable() {
					public void run() {
						try {
							while (true) {
								String line = brOut.readLine();
								if (line == null)
									break;
								sshOutput.append(line + "\n");
								System.out.println(line);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println("Out Log Thread Died");
					}
				}).start();

				new Thread(new Runnable() {
					public void run() {
						try {
							while (true) {
								String line = brErr.readLine();
								if (line == null)
									break;
								System.out.println(line);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						System.out.println("Err Log Thread Died");
					}
				}).start();

				sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);

				int eIndex = sshOutput.indexOf(BootSsh.STUB_END_MARKER);
				if (eIndex != -1) {
					int bIndex = sshOutput.indexOf(BootSsh.STUB_BEGIN_MARKER);
					String stub = sshOutput.substring(bIndex + BootSsh.STUB_BEGIN_MARKER.length(), eIndex);
					return (RServices) PoolUtils.hexToStub(stub, ServerLauncher.class.getClassLoader());
				} else {
					return null;
				}

			} finally {
				try {
					if (sess != null)
						sess.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (showProgress) {
				createRSshProgressFrame.setVisible(false);
			}
		}

	}

	
	static JTextArea createRLocalProgressArea;
	static JProgressBar createRLocalProgressBar;
	static JFrame createRLocalProgressFrame;

	public static RServices createRLocal(boolean keepAlive, String codeServerHostIp, int codeServerPort, String rmiRegistryHostIp, int rmiRegistryPort,
			int memoryMinMegabytes, int memoryMaxMegabytes, boolean showProgress) throws Exception {

		if (showProgress) {
			createRLocalProgressArea = new JTextArea();
			createRLocalProgressBar = new JProgressBar(0, 100);
			createRLocalProgressFrame = new JFrame("Create R Server on Local Host");

			Runnable runnable = new Runnable() {
				public void run() {
					createRLocalProgressArea.setFocusable(false);
					createRLocalProgressBar.setIndeterminate(true);
					JPanel p = new JPanel(new BorderLayout());
					p.add(createRLocalProgressBar, BorderLayout.SOUTH);
					p.add(new JScrollPane(createRLocalProgressArea), BorderLayout.CENTER);
					createRLocalProgressFrame.add(p);
					createRLocalProgressFrame.pack();
					createRLocalProgressFrame.setSize(300, 90);
					createRLocalProgressFrame.setVisible(true);
					createRLocalProgressFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					PoolUtils.locateInScreenCenter(createRLocalProgressFrame);
				}
			};

			if (SwingUtilities.isEventDispatchThread())
				runnable.run();
			else {
				SwingUtilities.invokeLater(runnable);
			}
		}

		
		
		try {	
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

			
			String logFileDir=System.getProperty("user.home") + "/RWorkbench/" +"log/";
			new File(logFileDir).mkdirs();
			String logFile=logFileDir+"log"+System.currentTimeMillis()+".txt";
			new File(logFile).delete();
			
			Vector<String> command = new Vector<String>();
			if (isWindowsOs()) {
				String psexecCommand=System.getProperty("pstools.home") + "/psexec.exe" ;
				if (!new File(psexecCommand).exists()) {
					String psToolsHome=System.getProperty("user.home") + "/RWorkbench/" + "PsTools";
					psexecCommand=psToolsHome + "/psexec.exe" ;
					if (!new File(psexecCommand).exists()) {
						new File(psToolsHome).mkdirs();
						MainPsToolsDownload.main(new String[] { psToolsHome });
					}			
				}	
				command.add(psexecCommand);
				command.add("-d");
			}
			
			command.add((isWindowsOs() ? "\"" : "")+System.getProperty("java.home") + "/bin/java"+ (isWindowsOs() ? "\"" : ""));
			command.add("-classpath");			
			command.add((isWindowsOs() ? "\"" : "") + System.getProperty("user.home") + "/RWorkbench/" + "classes" + (isWindowsOs() ? "\"" : ""));
			command.add("bootstrap.BootSsh");			
			command.add(new Boolean(keepAlive).toString());
			command.add(codeServerHostIp);
			command.add(""+codeServerPort);
			command.add(rmiRegistryHostIp);
			command.add(""+rmiRegistryPort);
			command.add(""+memoryMinMegabytes);
			command.add(""+memoryMaxMegabytes);
			command.add(logFile);
			final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]), null);

			while (!new File(logFile).exists()) {
				try {Thread.sleep(100);} catch (Exception e) {}
			}
			StringBuffer outPrint=new StringBuffer();
			while (outPrint.indexOf(BootSsh.STUB_END_MARKER)==-1) {
				try {Thread.sleep(100);} catch (Exception e) {}
				outPrint=new StringBuffer();
				BufferedReader br=new BufferedReader(new FileReader(logFile));
				while (true) {
					String line=br.readLine(); if (line==null) break;
					outPrint.append(line+"\n");
				}
				br.close();
			}
			
			new File(logFile).delete();
			
			
			String processId=outPrint.substring(outPrint.indexOf(BootSsh.PROCESS_ID_BEGIN_MARKER)+BootSsh.PROCESS_ID_BEGIN_MARKER.length(), outPrint.indexOf(BootSsh.PROCESS_ID_END_MARKER));
			String rprocessId=outPrint.substring(outPrint.indexOf(BootSsh.R_PROCESS_ID_BEGIN_MARKER)+BootSsh.R_PROCESS_ID_BEGIN_MARKER.length(), outPrint.indexOf(BootSsh.R_PROCESS_ID_END_MARKER));
			
			System.out.println("(1) intermediate process id:"+processId);
			System.out.println("(2) r process id:"+rprocessId);
			//PoolUtils.killLocalWinProcess(processId, true);
			
			int eIndex = outPrint.indexOf(BootSsh.STUB_END_MARKER);
			if (eIndex != -1) {
				int bIndex = outPrint.indexOf(BootSsh.STUB_BEGIN_MARKER);
				String stub = outPrint.substring(bIndex + BootSsh.STUB_BEGIN_MARKER.length(), eIndex);
				return (RServices) PoolUtils.hexToStub(stub, ServerLauncher.class.getClassLoader());
			} else {
				return null;
			}

		} finally {
			if (showProgress) {
				createRLocalProgressFrame.setVisible(false);
			}
		}

	}

	
	
	
	static JTextArea createRProgressArea;
	static JProgressBar createRProgressBar;
	static JFrame createRProgressFrame;

	public static RServices createR(boolean keepAlive, String codeServerHostIp, int codeServerPort, String rmiRegistryHostIp, int rmiRegistryPort,
			int memoryMinMegabytes, int memoryMaxMegabytes, boolean showProgress) throws Exception {

		if (showProgress) {
			createRProgressArea = new JTextArea();
			createRProgressBar = new JProgressBar(0, 100);
			createRProgressFrame = new JFrame("Create R Server on Local Host");

			Runnable runnable = new Runnable() {
				public void run() {
					createRProgressArea.setFocusable(false);
					createRProgressBar.setIndeterminate(true);
					JPanel p = new JPanel(new BorderLayout());
					p.add(createRProgressBar, BorderLayout.SOUTH);
					p.add(new JScrollPane(createRProgressArea), BorderLayout.CENTER);
					createRProgressFrame.add(p);
					createRProgressFrame.pack();
					createRProgressFrame.setSize(300, 90);
					createRProgressFrame.setVisible(true);
					createRProgressFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
					PoolUtils.locateInScreenCenter(createRProgressFrame);
				}
			};
			if (SwingUtilities.isEventDispatchThread())
				runnable.run();
			else {
				SwingUtilities.invokeLater(runnable);
			}
		}

		try {
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
				urlprefix = "http://biocep.r-forge.r-project.org/appletlibs/";
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
						JOptionPane.showMessageDialog(null,
								"please add R to your System path or set R_HOME to the root Directory of your local R installation\n");
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

				command.add((isWindowsOs() ? "\"" : "") + "-DXms" + memoryMinMegabytes + "m" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-DXmx" + memoryMaxMegabytes + "m" + (isWindowsOs() ? "\"" : ""));

				command.add("-classpath");
				command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));

				if (keepAlive) {
					command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase=" + "http://" + codeServerHostIp + ":" + codeServerPort + "/classes/"
							+ " " + urlprefix + "JRI.jar" + " " + urlprefix + "commons-logging-1.1.jar" + " " + urlprefix + "log4j-1.2.14.jar" + " "
							+ urlprefix + "htmlparser.jar" + " " + urlprefix + "derbyclient.jar" + " " + urlprefix + "RJB.jar" + " " + urlprefix
							+ "mapping.jar" + (isWindowsOs() ? "\"" : ""));
				} else {
					command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase=http://" + codeServerHostIp + ":" + codeServerPort + "/classes/"+ (isWindowsOs() ? "\"" : ""));
				}

				command.add((isWindowsOs() ? "\"" : "") + "-Dservantclass=server.RServantImpl" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dprivate=true" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlistener.stub=" + listenerStub + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dpreprocess.help=true" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dapply.sandbox=true" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + root + "wdir" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dpstools.home=" + root + "PsTools/" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dregistryhost=" + rmiRegistryHostIp + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dregistryport=" + rmiRegistryPort + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.rootCategory=INFO,A1,A2"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1=org.apache.log4j.ConsoleAppender"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout=org.apache.log4j.PatternLayout"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout.ConversionPattern= [%-5p] - %m%n"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=uk.ac.ebi.microarray.pools.RemoteAppender"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout=org.apache.log4j.PatternLayout"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout.ConversionPattern= [%-5p] - %m%n"+ (isWindowsOs() ? "\"" : ""));				
								
				command.add("bootstrap.Boot");
				command.add(new Boolean(keepAlive).toString());
				command.add(codeServerHostIp);
				command.add("" + codeServerPort);
				if (keepAlive) {
					command.add(urlprefix);
				}

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

				long t1 = System.currentTimeMillis();
				while (servantHolder[0] == null && exceptionHolder[0] == null) {
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
		} finally {
			if (showProgress) {
				createRProgressFrame.setVisible(false);
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
	
	
	public static class RemoteLogListenerImpl extends UnicastRemoteObject implements RemoteLogListener {

		public RemoteLogListenerImpl() throws RemoteException {
			super();
		}

		public void flush() throws RemoteException {
		}

		public void write(final byte[] b) throws RemoteException {
			System.out.print(new String(b));
		}

		public void write(final byte[] b, final int off, final int len) throws RemoteException {
			System.out.print(new String(b, off, len));
		}

		public void write(final int b) throws RemoteException {
			System.out.print(new String(new byte[] { (byte) b, (byte) (b >> 8) }));
		}

	}

}
