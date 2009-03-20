/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
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
package org.kchine.rpf.db.monitor;

import javax.swing.SwingUtilities;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.NodeDataDB;
import org.kchine.rpf.db.SupervisorInterface;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SupervisorUtils implements SupervisorInterface {
	
	DBLayerInterface dbLayer;
	public SupervisorUtils(DBLayerInterface dbLayer) {
		this.dbLayer=dbLayer;
	}
	
	class CancelException extends Exception {
	};

	static class Identification {

		public String user;

		public String pwd;

		public Identification(String user, String pwd) {
			this.user = user;
			this.pwd = pwd;
		}
	}

	class LaunchInfo {
		public String host;

		public String user;

		public String pwd;

		public String homeDir;

		public String command;

		public String os;

		public LaunchInfo(String host, String user, String pwd, String homeDir, String command, String os) {
			this.host = host;
			this.user = user;
			this.pwd = pwd;
			this.homeDir = homeDir;
			this.command = command;
			this.os = os;
		}
	}

	private HashMap<String, Identification> identificationsCache = new HashMap<String, Identification>();

	public void killProcess(String servantName, boolean useKillCommand, Frame referenceFrame) throws Exception {

		HashMap<String, Object> servantInfo = dbLayer.getTableData("SERVANTS", "NAME='" + servantName + "'").elementAt(0);

		NodeDataDB nd = null;
		try {
			nd = dbLayer.getNodeData("NODE_NAME='" + servantInfo.get("NODE_NAME") + "'").elementAt(0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		String hostIp = (String) servantInfo.get("HOST_IP");
		String processId = (String) servantInfo.get("PROCESS_ID");
		HashMap<String, Object> attributes = (HashMap<String, Object>) PoolUtils.hexToObject((String) servantInfo.get("ATTRIBUTES_HEX"));
		System.out.println("attributes : " + attributes);

		if ( PoolUtils.isLoopBackIP(hostIp) || PoolUtils.getHostIp().equals(hostIp) ) {
			System.out.println("####>> Local Killl");
			if (PoolUtils.isWindowsOs()) {
				PoolUtils.killLocalWinProcess(processId, useKillCommand);
			} else {
				PoolUtils.killLocalUnixProcess(processId, useKillCommand);
			}
			return;
		}

		System.out.println("####>> SSH Killl");
		String killCommand = useKillCommand ? (nd != null && nd.getKillServantCommand() != null && !nd.getKillServantCommand().equals("") ? nd
				.getKillServantCommand() : "kill -9 ${PROCESS_ID}") : "kill ${PROCESS_ID}";
		killCommand = PoolUtils.replaceAll(killCommand, "${PROCESS_ID}", processId);

		if (attributes != null) {
			Iterator<String> attrKeyIter = attributes.keySet().iterator();
			while (attrKeyIter.hasNext()) {
				String attrName = attrKeyIter.next();
				String attrValue = attributes.get(attrName).toString();
				killCommand = PoolUtils.replaceAll(killCommand, "${" + attrName + "}", attrValue);
			}
		}

		System.out.println("Kill command : " + killCommand);

		Identification ident = null;
		if (nd != null) {
			ident = new Identification(nd.getLogin(), nd.getPwd());
		}

		if (ident == null) {
			ident = identificationsCache.get(hostIp);
		}

		if (ident == null) {
			if (referenceFrame == null) {
				throw new Exception("No Valid Login/Pwd For Node");
			} else {
				LoginDialog d = new LoginDialog(referenceFrame);
				d.setVisible(true);
				ident = d.getIndentification();
				if (ident == null) {
					throw new CancelException();
				} else {
					identificationsCache.put(hostIp, ident);
				}
			}
		}

		Connection conn = new Connection(hostIp);
		conn.connect();
		boolean isAuthenticated = conn.authenticateWithPassword(ident.user, ident.pwd);
		if (isAuthenticated == false)
			throw new IOException("Authentication failed.");
		Session sess = conn.openSession();
		sess.execCommand(killCommand);
		InputStream stdout = new StreamGobbler(sess.getStdout());
		BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
		while (true) {
			String line = br.readLine();
			if (line == null)
				break;
			System.out.println(line);
		}

		System.out.println("ExitCode: " + sess.getExitStatus());
		sess.close();
		conn.close();
	}

	public void launchLocalProcess(final boolean showConsole, String homeDir, String command, String prefix, boolean isForWindows) throws Exception {
		System.out.println("launchLocalProcess");

		Vector<String> cmd = new Vector<String>();
		
		if (command.startsWith("ant")) {
			
			cmd.add(isForWindows ? "cmd" : "/bin/sh");
			cmd.add(isForWindows ? "/c" : "-c");
			if (isForWindows) {			
				Vector<String> tokens=PoolUtils.tokenizeWindowsCommand(command);
				for (int i=0;i<tokens.size();++i) cmd.add(tokens.elementAt(i));			
			} else {
				cmd.add(command);
			}
		} else {
			
			Vector<String> tokens=PoolUtils.tokenizeWindowsCommand(command);
			for (int i=0;i<tokens.size();++i) cmd.add(tokens.elementAt(i));
			
		}

		System.out.println(cmd);

		Runtime rt = Runtime.getRuntime();
		final Process proc = homeDir.trim().equals("") ? rt.exec(cmd.toArray(new String[0])) : rt.exec(cmd.toArray(new String[0]), null, new File(homeDir));

		final ProcessLogDialog pdialog = (showConsole ? new ProcessLogDialog(null, "127.0.0.1", "localhost", prefix) : null);
		if (pdialog != null)
			pdialog.setVisible(true);

		final BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {

						// if (!pdialog.isVisible()) break;

						final String line = br.readLine();
						if (line == null)
							break;
						if (showConsole && pdialog.isVisible()) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									pdialog.append(line + "\n");
								}
							});
						}

						Thread.sleep(200);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("out console print ends ");

			}
		}).start();

		final BufferedReader berr = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						// if (!pdialog.isVisible()) break;
						final String line = berr.readLine();
						if (line == null)
							break;
						if (showConsole && pdialog.isVisible()) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									pdialog.append(line + "\n");
								}
							});
						}

						Thread.sleep(200);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("err console print ends ");

			}
		}).start();

		// int exitVal = proc.waitFor();
		// if (exitVal != 0) throw new Exception(cmd+" exit code : " + exitVal);

	}

	public void launch(final String nodeName, final String options, final boolean showConsole) throws Exception {

		new Thread(new Runnable() {
			public void run() {
				try {

					dbLayer.incrementNodeProcessCounter(nodeName);
					final NodeDataDB info = dbLayer.getNodeData("NODE_NAME='" + nodeName + "'").elementAt(0);
					String command = info.getCreateServantCommand();

					if (PoolUtils.isLoopBackIP(info.getHostIp()) || info.getHostIp().equals(PoolUtils.getHostIp())) {

						command = PoolUtils.replaceAll(command, "${OPTIONS}", options);
						command = PoolUtils.replaceAll(command, "${INSTALL_DIR}", new File(info.getInstallDir()).getCanonicalPath().replace('\\', '/'));
						command = PoolUtils.replaceAll(command, "${PROCESS_COUNTER}", new Integer(info.getProcessCounter()).toString());

						System.out.println("--> Launching local process : " + command);
						if (PoolUtils.isWindowsOs()) {
							launchLocalProcess(showConsole, new File(info.getInstallDir()).getCanonicalPath(), command, info.getPoolPrefix(), true);
						} else {
							launchLocalProcess(showConsole, new File(info.getInstallDir()).getCanonicalPath(), command, info.getPoolPrefix(), false);
						}
						return;
					} else {

						command = PoolUtils.replaceAll(command, "${OPTIONS}", options);
						command = PoolUtils.replaceAll(command, "${INSTALL_DIR}", info.getInstallDir());
						command = PoolUtils.replaceAll(command, "${PROCESS_COUNTER}", new Integer(info.getProcessCounter()).toString());

						System.out.println("--> Launching process via SSH : " + command);

						Connection conn = new Connection(info.getHostIp().trim().equals("") ? info.getHostName() : info.getHostIp());
						conn.connect();
						boolean isAuthenticated = conn.authenticateWithPassword(info.getLogin(), info.getPwd());
						if (isAuthenticated == false)
							throw new IOException("Authentication failed.");

						final Session session = conn.openSession();
						session.execCommand(command);

						final ProcessLogDialog pdialog = showConsole ? new ProcessLogDialog(null, info.getHostIp(), info.getHostName(), info.getPoolPrefix())
								: null;
						if (pdialog != null)
							pdialog.setVisible(true);

						final InputStream stdout = new StreamGobbler(session.getStdout());
						final BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

						new Thread(new Runnable() {
							public void run() {
								try {
									while (true) {
										// if (!pdialog.isVisible()) break;

										final String line = br.readLine();
										if (line == null)
											break;
										if (showConsole && pdialog.isVisible()) {
											SwingUtilities.invokeLater(new Runnable() {
												public void run() {
													pdialog.append(line + "\n");
													// System.out.println("session("+session+"):"+line);
												}
											});
										}

										Thread.sleep(200);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}).start();

						InputStream stderr = new StreamGobbler(session.getStderr());
						final BufferedReader berr = new BufferedReader(new InputStreamReader(stderr));

						new Thread(new Runnable() {
							public void run() {
								try {
									while (true) {
										// if (!pdialog.isVisible()) break;
										final String line = berr.readLine();
										if (line == null)
											break;

										if (showConsole && pdialog.isVisible()) {
											SwingUtilities.invokeLater(new Runnable() {
												public void run() {
													pdialog.append(line + "\n");
												}
											});
										}

										Thread.sleep(200);
									}
								} catch (Exception e) {
									e.printStackTrace();
								}

							}
						}).start();

						session.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
						System.out.println("ExitCode: " + session.getExitStatus());
						session.close();
						conn.close();

						// pdialog.setVisible(false);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();

	}
	
	static public void main(String [] args ) throws Exception{
		Properties props = new Properties();

		props.put("naming.mode", "db");
		props.put("db.type", "derby");
		props.put("db.host", "localhost");
		props.put("db.port", "1527");
		props.put("db.name", "DWEP");
		props.put("db.user", "DWEP");
		props.put("db.password", "DWEP");

		DBLayerInterface db = null;
		try {
			db = (DBLayerInterface) ServerDefaults.getRegistry(props);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		SupervisorUtils su=new SupervisorUtils(db);
		
		su.launchLocalProcess(true,"F:\\Documents and Settings\\Karim Chine\\Bureau\\Moz\\javaservice", "\"F:/Program Files/Java/jdk1.5.0_16/jre/bin/java\" -Dnaming.mode=db -Ddb.type=derby -Ddb.host=10.0.1.6 -Ddb.port=1527 -Ddb.name=DWEP -Ddb.user=DWEP -Ddb.password=DWEP -Dnode=N1 \"-Dbiocep.home=F:\\Documents and Settings\\Karim Chine\\Bureau\\Moz\\javaservice\\biocep_home\" -Duse.default.libs=true -cp \"F:\\Documents and Settings\\Karim Chine\\Bureau\\Moz\\javaservice\\biocep.jar\" RmiServer", "RSERVANT_", true);
		
	}
	
}
