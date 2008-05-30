package server;

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
import java.net.Socket;
import java.net.URL;

import java.rmi.RemoteException;

import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

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
import model.TableModelRemoteImpl;
import bootstrap.BootSsh;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.CreationCallBack;
import uk.ac.ebi.microarray.pools.MainPsToolsDownload;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemoteLogListener;
import uk.ac.ebi.microarray.pools.SSHUtils;
import uk.ac.ebi.microarray.pools.ServantCreationTimeout;
import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class ServerManager {
	public static String INSTALL_DIR = new File(System.getProperty("user.home") + "/RWorkbench/").getAbsolutePath() + "/";
	public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 60000 * 5;
	public static int BUFFER_SIZE = 8192 * 5;
	private static final String RLIBSTART = "R$LIB$START";
	private static final String RLIBEND = "R$LIB$END";
	private static final String RVERSTART = "R$VER$START";
	private static final String RVEREND = "R$VER$END";	
	
	
	
	
	
	public static TableModelRemoteImpl tmri;
	public static void main(String[] args) throws Exception {
		

		System.exit(0);
		
		
		/*
        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        connector.setHost("127.0.0.1");
        server.addConnector(connector);

        WebAppContext wac = new WebAppContext();
        wac.setContextPath("/rvirtual");
        wac.setWar("J:/jetty/webapps/rvirtual.war");    // this is path to .war OR TO expanded, existing webapp; WILL FIND web.xml and parse it
        server.addHandler(wac);
        
        WebAppContext wac2 = new WebAppContext();
        wac2.setContextPath("/rws");
        wac2.setWar("J:/jetty/webapps/rws.war");    // this is path to .war OR TO expanded, existing webapp; WILL FIND web.xml and parse it
        server.addHandler(wac2);
                        
        server.setStopAtShutdown(true);

        server.start();
        */
        
        
		/*
		RServices r=ServerManager.createR(false, "127.0.0.1", LocalHttpServer.getLocalHttpServerPort(), "127.0.0.1", LocalRmiRegistry.getLocalRmiRegistryPort(), 256, 256, "test",
				false, new URL[]{new File("J:/workspace/biocep/VirtualRWorkbench/distrib/mapping.jar").toURL()});
		System.out.println(r.consoleSubmit("76+9"));
		*/
	}

	private static JTextArea createRSshProgressArea;
	private static JProgressBar createRSshProgressBar;
	private static JFrame createRSshProgressFrame;

	public static RServices createRSsh(boolean keepAlive, String codeServerHostIp, int codeServerPort, String rmiRegistryHostIp, int rmiRegistryPort,
			int memoryMinMegabytes, int memoryMaxMegabytes, String sshHostIp, int sshPort, String sshLogin, String sshPwd, String name, boolean showProgress, URL[] codeUrls)
			throws BadSshHostException, BadSshLoginPwdException, Exception {

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
			conn = new Connection(sshHostIp, sshPort);
			try {
				conn.connect();
			} catch (Exception e) {
				throw new BadSshHostException();
			}
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new BadSshLoginPwdException();

			InputStream is = ServerManager.class.getResourceAsStream("/bootstrap/BootSsh.class");
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
				
				String command="java -classpath RWorkbench/classes bootstrap.BootSsh" + " " + new Boolean(keepAlive) + " " + codeServerHostIp + " "
				+ codeServerPort + " " + rmiRegistryHostIp + " " + rmiRegistryPort + " " + memoryMinMegabytes + " " + memoryMaxMegabytes + " "
				+ "System.out" + " " +	((name==null || name.trim().equals("")) ? BootSsh.NO_NAME : name);
				
				if (codeUrls!=null && codeUrls.length>0) {
					for (int i=0; i<codeUrls.length; ++i) {
						command=command+" "+codeUrls[i];
					}
				}			
				
				System.out.println("createRSsh command:"+command);
				sess.execCommand(command);

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
					return (RServices) PoolUtils.hexToStub(stub, ServerManager.class.getClassLoader());
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

	private static JTextArea createRLocalProgressArea;
	private static JProgressBar createRLocalProgressBar;
	private static JFrame createRLocalProgressFrame;

	public static RServices createRLocal(boolean keepAlive, String codeServerHostIp, int codeServerPort, String rmiRegistryHostIp, int rmiRegistryPort,
			int memoryMinMegabytes, int memoryMaxMegabytes, String name, boolean showProgress, URL[] codeUrls) throws Exception {

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
			InputStream is = ServerManager.class.getResourceAsStream("/bootstrap/BootSsh.class");
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

			String logFileDir = System.getProperty("user.home") + "/RWorkbench/" + "log/";
			new File(logFileDir).mkdirs();
			String logFile = logFileDir + "log" + System.currentTimeMillis() + ".txt";
			new File(logFile).delete();

			Vector<String> command = new Vector<String>();
			if (isWindowsOs()) {
				String psexecCommand = System.getProperty("pstools.home") + "/psexec.exe";
				if (!new File(psexecCommand).exists()) {
					String psToolsHome = System.getProperty("user.home") + "/RWorkbench/" + "PsTools";
					psexecCommand = psToolsHome + "/psexec.exe";
					if (!new File(psexecCommand).exists()) {
						new File(psToolsHome).mkdirs();
						MainPsToolsDownload.main(new String[] { psToolsHome });
					}
				}
				command.add(psexecCommand);
				command.add("-d");
			}

			command.add((isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java" + (isWindowsOs() ? "\"" : ""));
			command.add("-classpath");
			command.add((isWindowsOs() ? "\"" : "") + System.getProperty("user.home") + "/RWorkbench/" + "classes" + (isWindowsOs() ? "\"" : ""));
			command.add("bootstrap.BootSsh");
			command.add(new Boolean(keepAlive).toString());
			command.add(codeServerHostIp);
			command.add("" + codeServerPort);
			command.add(rmiRegistryHostIp);
			command.add("" + rmiRegistryPort);
			command.add("" + memoryMinMegabytes);
			command.add("" + memoryMaxMegabytes);
			command.add(logFile);
			command.add(  (name==null || name.trim().equals("")) ? BootSsh.NO_NAME : name);
						
			if (codeUrls != null && codeUrls.length > 0) {
				for (int i = 0; i < codeUrls.length; ++i) {
					command.add(codeUrls[i].toString());
				}
			}			
			
			final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]), null);

			while (!new File(logFile).exists()) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}
			StringBuffer outPrint = new StringBuffer();
			while (outPrint.indexOf(BootSsh.STUB_END_MARKER) == -1) {
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
				outPrint = new StringBuffer();
				BufferedReader br = new BufferedReader(new FileReader(logFile));
				while (true) {
					String line = br.readLine();
					if (line == null)
						break;
					outPrint.append(line + "\n");
				}
				br.close();
			}

			new File(logFile).delete();

			String processId = outPrint.substring(outPrint.indexOf(BootSsh.PROCESS_ID_BEGIN_MARKER) + BootSsh.PROCESS_ID_BEGIN_MARKER.length(), outPrint
					.indexOf(BootSsh.PROCESS_ID_END_MARKER));
			String rprocessId = outPrint.substring(outPrint.indexOf(BootSsh.R_PROCESS_ID_BEGIN_MARKER) + BootSsh.R_PROCESS_ID_BEGIN_MARKER.length(), outPrint
					.indexOf(BootSsh.R_PROCESS_ID_END_MARKER));

			System.out.println("(1) intermediate process id:" + processId);
			System.out.println("(2) r process id:" + rprocessId);
			// PoolUtils.killLocalWinProcess(processId, true);

			int eIndex = outPrint.indexOf(BootSsh.STUB_END_MARKER);
			if (eIndex != -1) {
				int bIndex = outPrint.indexOf(BootSsh.STUB_BEGIN_MARKER);
				String stub = outPrint.substring(bIndex + BootSsh.STUB_BEGIN_MARKER.length(), eIndex);
				return (RServices) PoolUtils.hexToStub(stub, ServerManager.class.getClassLoader());
			} else {
				return null;
			}

		} finally {
			if (showProgress) {
				createRLocalProgressFrame.setVisible(false);
			}
		}

	}

	private static JTextArea createRProgressArea;
	private static JProgressBar createRProgressBar;
	private static JFrame createRProgressFrame;

	public static RServices createR(String name) throws Exception {
		return createR(false, "127.0.0.1", LocalHttpServer.getLocalHttpServerPort(), "127.0.0.1", LocalRmiRegistry.getLocalRmiRegistryPort(), 256, 256, name,
				false, null);
	}

	public static RServices createR(boolean keepAlive, String codeServerHostIp, int codeServerPort, String rmiRegistryHostIp, int rmiRegistryPort,
			int memoryMinMegabytes, int memoryMaxMegabytes, String name, boolean showProgress, URL[] codeUrls) throws Exception {

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

			String root = INSTALL_DIR;
			new File(root).mkdir();

			String[] rinfo = getRInfo(null);
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
							unzip(is, root + "R/", null, BUFFER_SIZE, true, "Unzipping R..", 3606);
						}

						rpath = root + "R/R-2.6.2/";

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
				env.put("R_LIBS", rlibs + (System.getenv("R_LIBS") != null ? System.getProperty("path.separator") + System.getenv("R_LIBS") : ""));
				for (String k : env.keySet()) {
					envVector.add(k + "=" + env.get(k));
				}
			}

			String[] requiredPackages = null;

			if (isWindowsOs()) {
				requiredPackages = new String[] { "rJava", "JavaGD", "TypeInfo", "Cairo" };
			} else {
				requiredPackages = new String[] { "rJava", "JavaGD", "TypeInfo" };
			}

			Vector<String> installLibBatch = new Vector<String>();
			installLibBatch.add("source('http://bioconductor.org/biocLite.R')");

			for (int i = 0; i < requiredPackages.length; ++i) {
				if (!new File(rlibs+"/"+requiredPackages[i]).exists()) {
					installLibBatch.add("biocLite('" + requiredPackages[i] + "',lib='" + rlibs + "')");
				}
				/*
				if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
					installLibBatch.add("biocLite('" + requiredPackages[i] + "',lib='" + rlibs + "')");
				}
				*/
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
					if (!new File(rlibs+"/"+requiredPackages[i]).exists()) {
						missingLibs.add(requiredPackages[i]);
					}
					/*
					if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
						missingLibs.add(requiredPackages[i]);
					}
					*/
				}

				if (missingLibs.size() > 0) {
					System.out.println("The following packages probably couldn't be automatically installed\n" + missingLibs);
				}

			}

			String bootstrap = (root + "classes/bootstrap").replace('\\', '/');
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

			new File(root + "PsTools").mkdirs();
			MainPsToolsDownload.main(new String[] { root + "PsTools" });

			// ---------------------------------------

			if (isWindowsOs() && !new File(root + "VRWorkbench.bat").exists()) {
				try {
					String launcherFile = root + "VRWorkbench.bat";
					FileWriter fw = new FileWriter(launcherFile);
					PrintWriter pw = new PrintWriter(fw);
					pw.println("javaws http://biocep-distrib.r-forge.r-project.org/rworkbench.jnlp");
					fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (!isWindowsOs() && !new File(root + "VRWorkbench.sh").exists()) {
				try {
					String launcherFile = root + "VRWorkbench.sh";
					FileWriter fw = new FileWriter(launcherFile);
					PrintWriter pw = new PrintWriter(fw);
					pw.println("javaws http://biocep-distrib.r-forge.r-project.org/rworkbench.jnlp");
					fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// ---------------------------------------

			//String jripath = getLibraryPath("rJava", rpath, rlibs) + "jri/";
			String jripath = rlibs+"/rJava/jri/";
			System.out.println("jripath:" + jripath + "\n");
						
			String cp = root + "classes";
			
			if (keepAlive) {
				try {
					downloadBioceCore(PoolUtils.LOG_PRGRESS_TO_LOGGER);
					cp=cp+System.getProperty("path.separator")+new File(root+"biocep-core.jar").getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
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

				command.add("-cp");
				command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));

				String codeBase= "http://" + codeServerHostIp + ":" + codeServerPort + "/classes/";
				

				if (codeUrls!=null && codeUrls.length>0) {for (int i=0; i<codeUrls.length;++i)  codeBase+=" "+codeUrls[i].toString();}
				command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase="+ codeBase+(isWindowsOs() ? "\"" : ""));
				if (keepAlive) {
					command.add((isWindowsOs() ? "\"" : "") + "-Dpreloadall=true" + (isWindowsOs() ? "\"" : ""));
				}
			
				command.add((isWindowsOs() ? "\"" : "") + "-Dservantclass=server.RServantImpl" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dprivate=true" + (isWindowsOs() ? "\"" : ""));
				if (name != null && !name.equals("")) {
					command.add((isWindowsOs() ? "\"" : "") + "-Dname=" + name + (isWindowsOs() ? "\"" : ""));
				}

				command.add((isWindowsOs() ? "\"" : "") + "-Dlistener.stub=" + listenerStub + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dpreprocess.help=true" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dapply.sandbox=false" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + root + "wdir" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dpstools.home=" + root + "PsTools/" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Dregistryhost=" + rmiRegistryHostIp + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dregistryport=" + rmiRegistryPort + (isWindowsOs() ? "\"" : ""));

				/*
				command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger"+ (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.rootCategory=DEBUG,A1,A2" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1=org.apache.log4j.ConsoleAppender" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=uk.ac.ebi.microarray.pools.RemoteAppender" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
				*/

				command.add("bootstrap.Boot");
				command.add(new Boolean(keepAlive).toString());
				command.add(codeServerHostIp);
				command.add("" + codeServerPort);
				
				if (codeUrls != null && codeUrls.length > 0) {
					for (int i = 0; i < codeUrls.length; ++i) {
						command.add(codeUrls[i].toString());
					}
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

	// incomplete
	public static String getLibraryPath(String libName, String rpath, String rlibs) {
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

	public static void killLocalUnixProcess(String processId, boolean isKILLSIG) throws Exception {
		PoolUtils.killLocalUnixProcess(processId, isKILLSIG);
	}

	public static void killLocalWinProcess(String processId, boolean isKILLSIG) throws Exception {
		PoolUtils.killLocalWinProcess(processId, isKILLSIG);
	}

	public static void killLocalProcess(String processId, boolean isKILLSIG) throws Exception {
		if (isWindowsOs())
			PoolUtils.killLocalWinProcess(processId, isKILLSIG);
		else
			PoolUtils.killLocalUnixProcess(processId, isKILLSIG);
	}

	public static void killSshProcess(String processId, String sshHostIp, String sshLogin, String sshPwd, boolean forcedKill) throws Exception {
		SSHUtils.killSshProcess(processId, sshHostIp, sshLogin, sshPwd, forcedKill);
	}

	public static String[] getRInfo(String rhome) {

		File getInfoFile = new File(INSTALL_DIR + "getInfo.R");

		File getInfoOutputFile = new File(INSTALL_DIR + "getInfo.Rout");

		String rversion = null;

		String rlibraypath = null;

		try {

			FileWriter fw = new FileWriter(getInfoFile);

			PrintWriter pw = new PrintWriter(fw);

			pw.println("paste('" + RLIBSTART + "',.Library, '" + RLIBEND + "',sep='%')");

			pw.println("paste('" + RVERSTART + "', R.version.string , '" + RVEREND + "', sep='%')");

			fw.close();

			Vector<String> getInfoCommand = new Vector<String>();

			if (rhome != null) {
				getInfoCommand.add(rhome + "bin/R");
				getInfoCommand.add("CMD");
				getInfoCommand.add("BATCH");
				getInfoCommand.add("--no-save");
				getInfoCommand.add(getInfoFile.getAbsolutePath());
				getInfoCommand.add(getInfoOutputFile.getAbsolutePath());

			} else {

				if (isWindowsOs()) {

					getInfoCommand.add(System.getenv().get("ComSpec"));
					getInfoCommand.add("/C");
					getInfoCommand.add("R");
					getInfoCommand.add("CMD");
					getInfoCommand.add("BATCH");
					getInfoCommand.add("--no-save");
					getInfoCommand.add(getInfoFile.getAbsolutePath());
					getInfoCommand.add(getInfoOutputFile.getAbsolutePath());

				} else {
					getInfoCommand.add(/* System.getenv().get("SHELL") */"/bin/sh");
					getInfoCommand.add("-c");
					getInfoCommand.add("R CMD BATCH --no-save " + getInfoFile.getAbsolutePath() + " " + getInfoOutputFile.getAbsolutePath());
				}
			}

			Vector<String> systemEnvVector = new Vector<String>();
			{

				Map<String, String> osenv = System.getenv();

				Map<String, String> env = new HashMap<String, String>(osenv);

				for (String k : env.keySet()) {

					systemEnvVector.add(k + "=" + env.get(k));

				}

			}

			System.out.println("exec->" + getInfoCommand);

			System.out.println(systemEnvVector);

			final Process getInfoProc = Runtime.getRuntime().exec(getInfoCommand.toArray(new String[0]),

			systemEnvVector.toArray(new String[0]));

			new Thread(new Runnable() {

				public void run() {

					try {

						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getErrorStream()));

						String line = null;

						while ((line = br.readLine()) != null) {

							System.out.println(line);

						}

					} catch (Exception e) {

						e.printStackTrace();

					}

				}

			}).start();

			new Thread(new Runnable() {

				public void run() {

					try {

						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getInputStream()));

						String line = null;

						while ((line = br.readLine()) != null) {

							System.out.println(line);

						}

					} catch (Exception e) {

						e.printStackTrace();

					}

				}

			}).start();

			getInfoProc.waitFor();

			if (getInfoOutputFile.exists() && getInfoOutputFile.lastModified() > getInfoFile.lastModified()) {

				BufferedReader br = new BufferedReader(new FileReader(getInfoOutputFile));

				String line = null;

				while ((line = br.readLine()) != null) {

					// System.out.println(line);

					if (line.contains(RLIBSTART + "%")) {

						rlibraypath = line.substring(line.indexOf(RLIBSTART + "%") + (RLIBSTART + "%").length(), (line.indexOf("%" + RLIBEND) > 0 ? line
								.indexOf("%" + RLIBEND) : line.length()));

					}

					if (line.contains(RVERSTART + "%")) {

						rversion = line.substring(line.indexOf(RVERSTART + "%") + (RVERSTART + "%").length(), line.indexOf("%" + RVEREND));

					}

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		if (rversion != null && rlibraypath != null) {

			return new String[] { rlibraypath, rversion };

		} else {

			return null;

		}

	}

	public static boolean isPortInUse(String hostIp, int port) {
		Socket s = null;
		try {
			s = new Socket(hostIp, port);
		} catch (Exception e) {
			return false;
		} finally {
			if (s != null)
				try {
					s.close();
				} catch (Exception ex) {
				}
		}
		return true;
	}

	public static void startPortInUseDogwatcher(final String hostIp, final int port, final int periodicitySec, final int maxFailure) {
		new Thread(new Runnable() {
			int failureCounter = maxFailure;

			public void run() {
				while (true) {
					if (!isPortInUse(hostIp, port))
						--failureCounter;
					if (failureCounter == 0) {
						System.out.println("The Creator Process doesn't respond, going to die");
						System.exit(0);
					}
					try {
						Thread.sleep(1000 * periodicitySec);
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}
	synchronized public static void downloadBioceCore(int logInfo) throws Exception{
		PoolUtils.cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/biocep-core.jar"), INSTALL_DIR, logInfo);
	}
	
}
