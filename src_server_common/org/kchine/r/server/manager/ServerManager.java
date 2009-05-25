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
package org.kchine.r.server.manager;

import static org.kchine.rpf.PoolUtils.isWindowsOs;
import static org.kchine.rpf.PoolUtils.unzip;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
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
import java.util.Properties;
import java.util.UUID;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.RServices;
import org.kchine.r.server.http.local.LocalHttpServer;
import org.kchine.r.server.manager.bootstrap.BootSsh;
import org.kchine.r.server.spreadsheet.TableModelRemoteImpl;
import org.kchine.rpf.CreationCallBack;
import org.kchine.rpf.LocalRmiRegistry;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RemoteLogListener;
import org.kchine.rpf.SSHUtils;
import org.kchine.rpf.ServantCreationTimeout;
import org.kchine.rpf.ServerDefaults;


import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ServerManager {

	public static void main(String[] args) throws Exception {
		

	}

	public static String INSTALL_DIR = null;
	public static String PLUGINS_DIR = null;
	public static String EXTENSIONS_DIR = null;
	public static String DOWNLOAD_DIR = null;
	public static String WWW_DIR = null;
	public static final String EMBEDDED_R = "R-version-2.9.0";
	public static final int ENTRIES_NUMBER = 5354;	
	public static String SCI_HOME = null;
	

	static {
		if (System.getenv("BIOCEP_HOME") != null) {
			INSTALL_DIR = System.getenv("BIOCEP_HOME");
		} else if (System.getProperty("biocep.home")!=null && !System.getProperty("biocep.home").equals("")) {			
			INSTALL_DIR = System.getProperty("biocep.home");
		} else if (new File(System.getProperty("user.dir") + "/biocep.txt").exists()) {
			INSTALL_DIR = System.getProperty("user.dir");
		} else {
			String codeUrl = ServerManager.class.getResource("/org/kchine/r/server/manager/ServerManager.class").toString();
			if (codeUrl.startsWith("jar:file:")) {
				try {
					String jarurl=codeUrl.substring("jar:".length(), codeUrl.length()-"/org/kchine/r/server/manager/ServerManager.class".length()-1);				
					String jarfile = PoolUtils.getFileFromURL(new URL(jarurl)).getAbsolutePath().replace('\\','/');
					if (new File(new File(jarfile).getParent() + "/biocep.txt").exists()) {
						INSTALL_DIR = jarfile.substring(0, jarfile.lastIndexOf("/"));
					} else {
						INSTALL_DIR = System.getProperty("user.home") + "/RWorkbench/";
					}
				} catch (Exception e) {
					e.printStackTrace();
					INSTALL_DIR = System.getProperty("user.home") + "/RWorkbench/";
				}
			} else {
				INSTALL_DIR = System.getProperty("user.home") + "/RWorkbench/";
			}
		}
		INSTALL_DIR = new File(INSTALL_DIR).getAbsolutePath() + "/";

		new File(INSTALL_DIR).mkdirs();
		if (!new File(INSTALL_DIR + "/biocep.txt").exists()) {
			try {
				PrintWriter pw = new PrintWriter(INSTALL_DIR + "/biocep.txt");
				pw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println("@@INSTALL_DIR=" + INSTALL_DIR);
				
		if (System.getenv("BIOCEP_PLUGINS_HOME") != null) {
			PLUGINS_DIR = System.getenv("BIOCEP_PLUGINS_HOME");
		} else if (System.getProperty("plugins.home")!=null && !System.getProperty("plugins.home").equals("")) {			
			PLUGINS_DIR = System.getProperty("plugins.home");
		} else {
			PLUGINS_DIR = INSTALL_DIR + "plugins";
		}
		if (!new File(PLUGINS_DIR).exists())  new File(PLUGINS_DIR).mkdirs();

		
		
		if (System.getenv("BIOCEP_EXTENSIONS_HOME") != null) {
			EXTENSIONS_DIR = System.getenv("BIOCEP_EXTENSIONS_HOME");
		} else if (System.getProperty("extensions.home")!=null && !System.getProperty("extensions.home").equals("")) {			
			EXTENSIONS_DIR = System.getProperty("extensions.home");
		} else {
			EXTENSIONS_DIR = new File(INSTALL_DIR + "extensions").getAbsolutePath();
		}
		if (!new File(EXTENSIONS_DIR).exists())  new File(EXTENSIONS_DIR).mkdirs();
		
		
		if (System.getenv("BIOCEP_DOWNLOAD_DIR") != null) {
			DOWNLOAD_DIR = System.getenv("BIOCEP_DOWNLOAD_DIR");
		} else if (System.getProperty("download.home")!=null && !System.getProperty("download.home").equals("")) {			
			DOWNLOAD_DIR = System.getProperty("download.home");
		} else {
			DOWNLOAD_DIR = new File(INSTALL_DIR + "download").getAbsolutePath();
		}
		if (!new File(DOWNLOAD_DIR).exists())  new File(DOWNLOAD_DIR).mkdirs();
		
		
		if (System.getenv("BIOCEP_WWW_DIR") != null) {
			WWW_DIR = System.getenv("BIOCEP_WWW_DIR");
		} else if (System.getProperty("www.home")!=null && !System.getProperty("www.home").equals("")) {			
			WWW_DIR = System.getProperty("www.home");
		} else {
			WWW_DIR = new File(INSTALL_DIR + "www").getAbsolutePath();
		}
		if (!new File(WWW_DIR).exists())  new File(WWW_DIR).mkdirs();
		
		if (System.getenv("SCI") == null) {
			if (System.getenv("SCI_HOME") != null) {
				SCI_HOME = System.getenv("SCI_HOME");
			} else if (System.getProperty("sci.home")!=null && !System.getProperty("sci.home").equals("")) {			
				SCI_HOME = System.getProperty("sci.home");
			} else {			
				File embeddedScilabDir= new File(INSTALL_DIR + "scilab");
				if (!embeddedScilabDir.exists()) embeddedScilabDir.mkdirs(); 
				String[] dirList=embeddedScilabDir.list(new FilenameFilter(){
					public boolean accept(File dir, String name) {
						return dir.isDirectory();
					}
				});
				if (dirList.length>0) {
					SCI_HOME=embeddedScilabDir+"/"+dirList[0];
					
				} else {
					SCI_HOME=null;
				}			
			}
		} else {
			if (isWindowsOs()) {
				SCI_HOME=System.getenv("SCI");
			} else {				
				try {
					SCI_HOME=new File(new File(System.getenv("SCI")).getAbsolutePath()+"/../..").getCanonicalPath();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		if (SCI_HOME!=null) SCI_HOME = new File(SCI_HOME).getAbsolutePath().replace('\\', '/') + "/";
				
		System.out.println("SCI_HOME:"+SCI_HOME);

	}

	public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 60000 * 5;
	public static int BUFFER_SIZE = 8192 * 5;
	private static final String RHOMESTART = "R$HOME$START";
	private static final String RHOMEEND = "R$HOME$END";
	private static final String RVERSTART = "R$VER$START";
	private static final String RVEREND = "R$VER$END";
	private static final String RJAVAPATHSTART = "RJAVA$PATH$START";
	private static final String RJAVAPATHEND = "RJAVA$PATH$END";

	public static TableModelRemoteImpl tmri;

	public static String[] namingVars = new String[] { 
		"naming.mode", "registry.host", "registry.port",  
		"db.type", "db.host", "db.port", "db.dir", "db.name", "db.user", "db.password", 			
		"httpregistry.url", "httpregistry.login", "httpregistry.password", 
		"rmi.port.start", 
		"job.id", "job.name", "notify.email", 
		"node", 
		"http.port",
		"cloud",
		"extensions.home",
		"use.default.libs",
		"r.options",
		"preprocess.help"};

	private static JTextArea createRSshProgressArea;
	private static JProgressBar createRSshProgressBar;
	private static JFrame createRSshProgressFrame;

	public static Properties getRegistryNamingInfo(String registryHost, int registryPort) {
		Properties result = new Properties();
		
		for (int i = 0; i < namingVars.length; ++i) {
			String var = namingVars[i];
			if (System.getProperty(var) != null && !System.getProperty(var).equals("")) {
				result.put(var, System.getProperty(var));
			}
		}
		
		result.put("naming.mode","registry");
		result.put("registry.host", registryHost);
		result.put("registry.port", new Integer(registryPort).toString());
		return result;
	}

	public static Properties getNamingInfo() {
		Properties result = new Properties();
		for (int i = 0; i < namingVars.length; ++i) {
			String var = namingVars[i];
			if (System.getProperty(var) != null && !System.getProperty(var).equals("")) {
				result.put(var, System.getProperty(var));
			}
		}
		return result;
	}

	public static RServices createRSsh(boolean keepAlive, String codeServerHostIp, int codeServerPort, Properties namingInfo, int memoryMinMegabytes,
			int memoryMaxMegabytes, String sshHostIp, int sshPort, String sshLogin, String sshPwd, String name, boolean showProgress, URL[] codeUrls,
			String logFile) throws BadSshHostException, BadSshLoginPwdException, Exception {

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

			InputStream is = ServerManager.class.getResourceAsStream("/org/kchine/r/server/manager/bootstrap/BootSsh.class");
			byte[] buffer = new byte[is.available()];
			try {
				for (int i = 0; i < buffer.length; ++i) {
					int b = is.read();
					buffer[i] = (byte) b;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			String bootstrapDir = INSTALL_DIR + "classes/org/kchine/r/server/manager/bootstrap";
			new File(bootstrapDir).mkdirs();
			RandomAccessFile raf = new RandomAccessFile(bootstrapDir + "/BootSsh.class", "rw");
			raf.setLength(0);
			raf.write(buffer);
			raf.close();

			Session sess = null;
			try {
				sess = conn.openSession();
				sess.execCommand("mkdir -p RWorkbench/classes/org/kchine/r/server/manager/bootstrap");
				sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
			} finally {
				try {
					if (sess != null)
						sess.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			
			new SCPClient(conn).put(bootstrapDir + "/BootSsh.class", "RWorkbench/classes/org/kchine/r/server/manager/bootstrap");
			try {
				sess = conn.openSession();

				String command = "java -classpath RWorkbench/classes org.kchine.r.server.manager.bootstrap.BootSsh" + " " + new Boolean(keepAlive) + " " + codeServerHostIp + " "
						+ codeServerPort + " " + BootSsh.propertiesToString(namingInfo) + " " + "NULL" + " " + memoryMinMegabytes + " " + memoryMaxMegabytes
						+ " " + "System.out" + " " + ((name == null || name.trim().equals("")) ? BootSsh.NO_NAME : name);

				if (codeUrls != null && codeUrls.length > 0) {
					for (int i = 0; i < codeUrls.length; ++i) {
						command = command + " " + codeUrls[i];
					}
				}

				System.out.println("createRSsh command:" + command);
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

	public static RServices createR(String name) throws Exception {
		return createR(null, false, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), getRegistryNamingInfo(PoolUtils.getHostIp(), LocalRmiRegistry
				.getLocalRmiRegistryPort()), ServerDefaults._memoryMin, ServerDefaults._memoryMax, name, false, null, null,true,null);
	}

	private interface ProgessLoggerInterface {
		void logProgress(String message);
	}

	synchronized public static RServices createR(String RBinPath, boolean keepAlive, String codeServerHostIp, int codeServerPort, Properties namingInfo,
			int memoryMinMegabytes, int memoryMaxMegabytes, String name, final boolean showProgress, URL[] codeUrls, String logFile, boolean addLocalJarToClassPath,final Runnable rShutdownHook) throws Exception {

		final JTextArea[] createRProgressArea = new JTextArea[1];
		final JProgressBar[] createRProgressBar = new JProgressBar[1];
		final JFrame[] createRProgressFrame = new JFrame[1];
		ProgessLoggerInterface progressLogger = new ProgessLoggerInterface() {
			public void logProgress(String message) {

				System.out.println(">>" + message);
				try {
					if (showProgress) {
						createRProgressArea[0].setText(message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		if (showProgress) {
			createRProgressArea[0] = new JTextArea();
			createRProgressBar[0] = new JProgressBar(0, 100);
			createRProgressFrame[0] = new JFrame("Creating R Server on Local Host");

			Runnable runnable = new Runnable() {
				public void run() {

					createRProgressFrame[0].setUndecorated(true);

					JPanel p = new JPanel(new BorderLayout());
					createRProgressArea[0].setForeground(Color.white);
					createRProgressArea[0].setBackground(new Color(0x00, 0x80, 0x80));
					createRProgressArea[0].setBorder(BorderFactory.createLineBorder(new Color(0x00, 0x80, 0x80), 3));
					createRProgressArea[0].setEditable(false);
					p.setBorder(BorderFactory.createLineBorder(Color.black, 3));

					createRProgressBar[0].setForeground(Color.white);
					createRProgressBar[0].setBackground(new Color(0x00, 0x80, 0x80));
					createRProgressBar[0].setIndeterminate(true);

					p.setBackground(new Color(0x00, 0x80, 0x80));
					p.add(createRProgressBar[0], BorderLayout.SOUTH);
					p.add(createRProgressArea[0], BorderLayout.CENTER);
					createRProgressFrame[0].add(p);

					createRProgressFrame[0].pack();
					createRProgressFrame[0].setSize(600, 64);
					createRProgressFrame[0].setVisible(true);
					createRProgressFrame[0].setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

					PoolUtils.locateInScreenCenter(createRProgressFrame[0]);
				}
			};
			if (SwingUtilities.isEventDispatchThread())
				runnable.run();
			else {
				SwingUtilities.invokeLater(runnable);
			}
		}

		try {

			progressLogger.logProgress("Inspecting R installation..");
			new File(INSTALL_DIR).mkdir();

			String rpath = null;
			String rversion = null;
			String userrjavapath = null;
			String[] rinfo = null;
			

			if (RBinPath != null && !RBinPath.equals("")) {
				rinfo = getRInfo(RBinPath);
				if (rinfo == null) {
					throw new ServantCreationFailed();
				}
				rpath = rinfo[0];
				rversion = rinfo[1];
				userrjavapath = rinfo[2];
			} else if (new File(INSTALL_DIR + "R/" + EMBEDDED_R).exists()) {

				rinfo = getRInfo(INSTALL_DIR + "R/" + EMBEDDED_R + "/bin/R.exe");
				if (rinfo == null) {
					throw new ServantCreationFailed();
				}
				rpath = rinfo[0];
				rversion = rinfo[1];
				userrjavapath = rinfo[2];
				
				System.setProperty("use.default.libs","true");

			} else {

				String rhome = System.getenv("R_HOME");
				if (rhome == null) {
					rinfo = getRInfo(null);
				} else {
					if (!rhome.endsWith("/")) {
						rhome = rhome + "/";
					}
					System.out.println("R_HOME is set to :" + rhome);
					rinfo = getRInfo(rhome + "bin/R");
				}

				System.out.println("+rinfo:" + rinfo + " " + Arrays.toString(rinfo));
				rpath = rinfo != null ? rinfo[0] : null;
				rversion = (rinfo != null ? rinfo[1] : "");
				userrjavapath = (rinfo != null ? rinfo[2] : "");
			}

			System.out.println("rpath:" + rpath);
			System.out.println("rversion:" + rversion);
			System.out.println("user rjava path:" + userrjavapath);
			if (rpath == null) {

				String noRCause = System.getenv("R_HOME") == null ? "R is not accessible from the command line" : "Your R_HOME is invalid";
				if (isWindowsOs()) {

					int n = JOptionPane.showConfirmDialog(null, noRCause + "\nWould you like to use the Embedded R?", "", JOptionPane.YES_NO_OPTION);
					if (n == JOptionPane.OK_OPTION) {
						String rZipFileName = null;
						rZipFileName = "http://biocep-distrib.r-forge.r-project.org/r/" + EMBEDDED_R + ".zip";
						URL rUrl = new URL(rZipFileName);
						InputStream is = rUrl.openConnection().getInputStream();
						unzip(is, INSTALL_DIR + "R/", null, BUFFER_SIZE, true, "Unzipping R..", ENTRIES_NUMBER);

						rinfo = getRInfo(INSTALL_DIR + "R/" + EMBEDDED_R + "/bin/R.exe");
						if (rinfo == null) {
							throw new ServantCreationFailed();
						}
						rpath = rinfo[0];
						rversion = rinfo[1];
						userrjavapath = rinfo[2];
						System.setProperty("use.default.libs","true");

					} else {
						JOptionPane.showMessageDialog(null,
								"please add R to your System path or set R_HOME to the root Directory of your local R installation\n");
						throw new ServantCreationFailed();
					}

				} else {
					if (showProgress) {
						JOptionPane.showMessageDialog(null, noRCause
								+ "\nplease add R to your System path \nor set R_HOME to the root Directory of your local R installation\n");
					} else {
						System.out.println(noRCause + "\n please add R to your System path \nor set R_HOME to the root Directory of your local R installation");
					}
					throw new ServantCreationFailed();
				}

			}

			progressLogger.logProgress("R installation inspection done.");

			boolean useDefaultUserLibs=(System.getenv("BIOCEP_USE_DEFAULT_LIBS") != null && System.getenv("BIOCEP_USE_DEFAULT_LIBS").equalsIgnoreCase("false"))
			|| (System.getProperty("use.default.libs") != null && System.getProperty("use.default.libs").equalsIgnoreCase("true"));
			
			if (System.getProperty("use.default.libs")==null || System.getProperty("use.default.libs").equals("")) {
				System.setProperty("use.default.libs",new Boolean(useDefaultUserLibs).toString().toLowerCase());
			}

			
			if (!rpath.endsWith("/") && !rpath.endsWith("\\"))
				rpath += "/";

			String rlibs = (INSTALL_DIR + "library/" + rversion.substring(0, rversion.lastIndexOf(' ')).replace(' ', '-')).replace('\\', '/');
			new File(rlibs).mkdirs();

			Vector<String> envVector = new Vector<String>();
			{
				Map<String, String> osenv = System.getenv();
				String OS_PATH=osenv.get("PATH");
				if (OS_PATH==null) OS_PATH=osenv.get("Path");
				if (OS_PATH==null) OS_PATH="";
				
				Map<String, String> env = new HashMap<String, String>(osenv);
				
				env.put("Path", rpath + (isWindowsOs() ? "bin" : "lib") + System.getProperty("path.separator")+ OS_PATH);
				if (SCI_HOME!=null) {
					env.put("Path", SCI_HOME+(isWindowsOs()?"bin":"lib/scilab")+System.getProperty("path.separator")+env.get("Path"));
				}
				
				env.put("LD_LIBRARY_PATH", rpath + (isWindowsOs() ? "bin" : "lib"));
								
				if (SCI_HOME!=null) {
					if (isWindowsOs()) {
						env.put("LD_LIBRARY_PATH", SCI_HOME+"bin"+System.getProperty("path.separator")+env.get("LD_LIBRARY_PATH"));	
					} else {
						env.put("LD_LIBRARY_PATH", SCI_HOME+"lib/scilab"+System.getProperty("path.separator")+SCI_HOME+"lib/thirdparty"+System.getProperty("path.separator")+env.get("LD_LIBRARY_PATH"));
						//env.put("SCI", SCI_HOME + "share/scilab");						
					}
					
					env.put("SCI_DISABLE_TK","1");
					env.put("SCI_JAVA_ENABLE_HEADLESS","1");
					
				}
				
				env.put("R_HOME", rpath);
				
				String R_LIBS = null;
				if (useDefaultUserLibs) {
					R_LIBS = (System.getenv("R_LIBS") != null ? System.getenv("R_LIBS") : "");					
				} else {
					R_LIBS = rlibs + System.getProperty("path.separator") + (System.getenv("R_LIBS") != null ? System.getenv("R_LIBS") : "");
				}
				
				System.out.println("R_LIBS:" + R_LIBS);
				env.put("R_LIBS", R_LIBS);
				
				if (System.getenv("JDK_HOME")!=null) env.put("JAVA_HOME", System.getenv("JDK_HOME"));
				
				for (String k : env.keySet()) {
					envVector.add(k + "=" + env.get(k));
				}
				System.out.println("envVector:"+envVector);
			}

			String[] requiredPackages = null;
			if (useDefaultUserLibs) {
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

			Vector<String> missingPackages = new Vector<String>();
			for (int i = 0; i < requiredPackages.length; ++i) {
				if (!new File(rlibs + "/" + requiredPackages[i]).exists()) {
					installLibBatch.add("biocLite('" + requiredPackages[i] + "',lib='" + rlibs + "')");
					missingPackages.add(requiredPackages[i]);
				}
			}

			progressLogger.logProgress("Installing missing packages " + missingPackages + "..\n"
					+ "This doesn't alter your R installation and may take several minutes. It will be done only once");

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
					throw new ServantCreationFailed();
				}

			}

			progressLogger.logProgress("All Required Packages Are Installed.");

			progressLogger.logProgress("Generating Bootstrap Classes..");
			
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
			progressLogger.logProgress("Bootstrap Classes Generated.");

			// ---------------------------------------

			if (!isWindowsOs() && !new File(INSTALL_DIR + "VRWorkbench.sh").exists()) {
				try {

					progressLogger.logProgress("Generating Launcher Batch..");

					String launcherFile = INSTALL_DIR + "VRWorkbench.sh";
					FileWriter fw = new FileWriter(launcherFile);
					PrintWriter pw = new PrintWriter(fw);
					pw.println("javaws http://biocep-distrib.r-forge.r-project.org/rworkbench.jnlp");
					fw.close();

					progressLogger.logProgress("Launcher Batch generated..");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// ---------------------------------------

			// String jripath = getLibraryPath("rJava", rpath, rlibs) + "jri/";
			String jripath = null;
			if (useDefaultUserLibs) {
				jripath = userrjavapath + "/jri/";
				System.out.println("jripath:" + jripath + "\n");
			} else {
				jripath = rlibs + "/rJava/jri/";
				System.out.println("jripath:" + jripath + "\n");
			}
			
			if (SCI_HOME!=null) {
				jripath+=System.getProperty("path.separator")+SCI_HOME+(isWindowsOs()?"bin":"lib/scilab");
			}

			System.out.println("jri path"+jripath);

			String cp = INSTALL_DIR + "classes";
			
			if (SCI_HOME!=null) {
				if (isWindowsOs()) {
					cp=cp+System.getProperty("path.separator")+SCI_HOME + "modules/javasci/jar/javasci.jar";	
				} else {
					cp=cp+System.getProperty("path.separator")+SCI_HOME + "share/scilab/modules/javasci/jar/javasci.jar";
				}
				
			}
			

			Vector<File> extraJarFiles = new Vector<File>();
			
			try {
				
				File[] flist = new File(INSTALL_DIR).listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
				});
				Arrays.sort(flist);
				for (int i=0; i<flist.length;++i) {
					extraJarFiles.add(flist[i]);
				}
				
				
				System.out.println("Insiders Extra Jars:" + Arrays.toString(flist));

				if (System.getenv().get("BIOCEP_EXTRA_JARS_LOCATION") != null) {
					flist = new File(System.getenv().get("BIOCEP_EXTRA_JARS_LOCATION")).listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".jar");
						}
					});

					Arrays.sort(flist);
					System.out.println("Outsiders Extra Jars:" + Arrays.toString(flist));
					for (int i=0; i<flist.length;++i) {
						extraJarFiles.add(flist[i]);
					}
				}
				

			} catch (Exception e) {
				e.printStackTrace();
			}

			
			ManagedServant[] servantHolder = new ManagedServant[1];
			RemoteException[] exceptionHolder = new RemoteException[1];

			CreationCallBack callBack = null;

			progressLogger.logProgress("Creating R Server..");

			try {
				callBack = new CreationCallBack(servantHolder, exceptionHolder);
				String listenerStub = PoolUtils.stubToHex(callBack);

				String uid = null;

				if (name != null && !name.equals("") && name.contains("%{uid}")) {
					if (uid == null)
						uid = UUID.randomUUID().toString();
					name = PoolUtils.replaceAll(name, "%{uid}", uid);
				}

				if (logFile != null && !logFile.equals("") && logFile.contains("%{uid}")) {
					if (uid == null)
						uid = UUID.randomUUID().toString();
					logFile = PoolUtils.replaceAll(logFile, "%{uid}", uid);
				}

				Vector<String> command = new Vector<String>();

				command.add((isWindowsOs() ? "\"" : "") + System.getProperty("java.home") + "/bin/java" + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-DXms" + memoryMinMegabytes + "m" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-DXmx" + memoryMaxMegabytes + "m" + (isWindowsOs() ? "\"" : ""));

				command.add("-cp");
				command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));

				command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));

				String codeBase = "http://" + codeServerHostIp + ":" + codeServerPort + "/classes/";

				if (codeUrls != null && codeUrls.length > 0) {
					for (int i = 0; i < codeUrls.length; ++i)
						codeBase += " " + codeUrls[i].toString();
				}
				
				if (extraJarFiles.size()>0) {
					for (int i = 0; i < extraJarFiles.size(); ++i)
						codeBase += " " + extraJarFiles.elementAt(i).toURI().toURL().toString();
				}
				
				command.add((isWindowsOs() ? "\"" : "") + "-Djava.rmi.server.codebase=" + codeBase + (isWindowsOs() ? "\"" : ""));
				if (keepAlive) {
					command.add((isWindowsOs() ? "\"" : "") + "-Dpreloadall=true" + (isWindowsOs() ? "\"" : ""));
				}

				command.add((isWindowsOs() ? "\"" : "") + "-Dservantclass=server.RServantImpl" + (isWindowsOs() ? "\"" : ""));

				if (name == null || name.equals("")) {
					command.add((isWindowsOs() ? "\"" : "") + "-Dprivate=true" + (isWindowsOs() ? "\"" : ""));
				} else {
					command.add((isWindowsOs() ? "\"" : "") + "-Dname=" + name + (isWindowsOs() ? "\"" : ""));
				}

				command.add((isWindowsOs() ? "\"" : "") + "-Dlistener.stub=" + listenerStub + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dapply.sandbox=false" + (isWindowsOs() ? "\"" : ""));
				command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + INSTALL_DIR + "wdir" + (isWindowsOs() ? "\"" : ""));

				for (int i = 0; i < namingVars.length; ++i) {
					String var = namingVars[i];
					if (namingInfo.getProperty(var) != null && !namingInfo.getProperty(var).equals("")) {
						command.add((isWindowsOs() ? "\"" : "") + "-D" + var + "=" + namingInfo.get(var) + (isWindowsOs() ? "\"" : ""));
					}
				}

				if (logFile != null && !logFile.equals("")) {
					command.add((isWindowsOs() ? "\"" : "") + "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.Log4JLogger"
							+ (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.rootCategory=DEBUG,A1,A2,A3" + (isWindowsOs() ? "\"" : ""));

					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1=org.apache.log4j.ConsoleAppender" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A1.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));

					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2=org.kchine.rpf.RemoteAppender" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A2.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));

					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3=org.apache.log4j.FileAppender" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3.file=" + logFile + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3.layout=org.apache.log4j.PatternLayout" + (isWindowsOs() ? "\"" : ""));
					command.add((isWindowsOs() ? "\"" : "") + "-Dlog4j.appender.A3.layout.ConversionPattern=[%-5p] - %m%n" + (isWindowsOs() ? "\"" : ""));
				}

				command.add("org.kchine.r.server.manager.bootstrap.Boot");
				command.add(new Boolean(keepAlive).toString());
				command.add(codeServerHostIp);
				command.add("" + codeServerPort);

				if (codeUrls != null && codeUrls.length > 0) {
					for (int i = 0; i < codeUrls.length; ++i) {
						command.add(codeUrls[i].toString());
					}
				}
				
				if (extraJarFiles.size()>0) {
					for (int i = 0; i < extraJarFiles.size(); ++i)
						command.add(extraJarFiles.elementAt(i).toURI().toURL().toString());
				}				
				
				final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]), envVector.toArray(new String[0]));
				if (rShutdownHook!=null) {
					new Thread(new Runnable(){
						public void run() {
							try {
								proc.waitFor();
								rShutdownHook.run();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}

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

				progressLogger.logProgress("R Server Created.");

				return (RServices) servantHolder[0];
			} finally {
				if (callBack != null) {
					UnicastRemoteObject.unexportObject(callBack, true);
				}
			}
		} finally {
			if (showProgress) {
				createRProgressFrame[0].dispose();
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

	public static String[] getRInfo(String rbin) {

		File getInfoFile = new File(INSTALL_DIR + "getInfo.R");

		File getInfoOutputFile = new File(INSTALL_DIR + "getInfo.Rout");

		try {
			getInfoOutputFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String rversion = null;

		String rlibraypath = null;
		
		String rjavapath = null;

		try {

			FileWriter fw = new FileWriter(getInfoFile);

			PrintWriter pw = new PrintWriter(fw);

			pw.println("paste('" + RHOMESTART + "',R.home(), '" + RHOMEEND + "',sep='%')");
			pw.println("paste('" + RVERSTART + "', R.version.string , '" + RVEREND + "', sep='%')");		
			pw.println("library('rJava'); paste('" + RJAVAPATHSTART + "', .path.package('rJava') , '" + RJAVAPATHEND + "', sep='%')");
			
			fw.close();

			Vector<String> getInfoCommand = new Vector<String>();

			String[] roptions=DirectJNI.getROptions();
			
			if (rbin != null && !rbin.equals("")) {
				System.out.println("trying to execute :" + rbin);
				getInfoCommand.add(rbin);
				getInfoCommand.add("CMD");
				getInfoCommand.add("BATCH");
				for (int i=0; i<roptions.length;++i) getInfoCommand.add(roptions[i]);
				getInfoCommand.add(getInfoFile.getAbsolutePath());
				getInfoCommand.add(getInfoOutputFile.getAbsolutePath());

			} else {

				if (isWindowsOs()) {

					getInfoCommand.add(System.getenv().get("ComSpec"));
					getInfoCommand.add("/C");
					getInfoCommand.add("R");
					getInfoCommand.add("CMD");
					getInfoCommand.add("BATCH");
					for (int i=0; i<roptions.length;++i) getInfoCommand.add(roptions[i]);
					getInfoCommand.add(getInfoFile.getAbsolutePath());
					getInfoCommand.add(getInfoOutputFile.getAbsolutePath());

				} else {
					getInfoCommand.add(/* System.getenv().get("SHELL") */"/bin/sh");
					getInfoCommand.add("-c");
					StringBuffer roptions_str=new StringBuffer();
					for (int i=0; i<roptions.length;++i) {roptions_str.append(roptions[i]);roptions_str.append(" ");}
					getInfoCommand.add("R CMD BATCH " + roptions_str+" " + getInfoFile.getAbsolutePath() + " " + getInfoOutputFile.getAbsolutePath());
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

			if (getInfoOutputFile.exists()) {

				BufferedReader br = new BufferedReader(new FileReader(getInfoOutputFile));

				String line = null;

				while ((line = br.readLine()) != null) {

					// System.out.println(line);

					if (line.contains(RHOMESTART + "%")) {

						rlibraypath = line.substring(line.indexOf(RHOMESTART + "%") + (RHOMESTART + "%").length(), (line.indexOf("%" + RHOMEEND) > 0 ? line
								.indexOf("%" + RHOMEEND) : line.length()));
					}

					if (line.contains(RVERSTART + "%")) {
						rversion = line.substring(line.indexOf(RVERSTART + "%") + (RVERSTART + "%").length(), line.indexOf("%" + RVEREND));
					}
					
					if (line.contains(RJAVAPATHSTART + "%")) {
						rjavapath = line.substring(line.indexOf(RJAVAPATHSTART + "%") + (RJAVAPATHSTART + "%").length(), line.indexOf("%" + RJAVAPATHEND));
					}

				}

			} else {
				System.out.println(getInfoOutputFile.toString() + " not found ");
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		System.out.println("+rversion:" + rversion);
		System.out.println("+rlibraypath:" + rlibraypath);
		System.out.println("+rjavapath:" + rjavapath);
		if (rlibraypath != null) {
			return new String[] { rlibraypath, rversion, rjavapath };
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

	synchronized public static void downloadBiocepCore(int logInfo) throws Exception {
		PoolUtils.cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/biocep-core.jar"), INSTALL_DIR, logInfo, false);
	}


}
