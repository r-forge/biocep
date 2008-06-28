package uk.ac.ebi.microarray.pools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class SSHUtils {

	public static void killSshProcess(String processId, String sshHostIp, String sshLogin, String sshPwd, boolean forcedKill) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			Session sess = null;

			sess = conn.openSession();
			sess.execCommand("kill" + " " + (forcedKill ? "-9" : "") + " " + processId);

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null)
								break;
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
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
							System.out.println(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);

		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void execSsh(String command, String sshHostIp, String sshLogin, String sshPwd) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			Session sess = null;

			sess = conn.openSession();
			
			sess.execCommand(command);

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;
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
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
							System.out.println(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);

		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static void putFileSsh(String localFile, String remoteTargetDirectory, String sshHostIp, String sshLogin, String sshPwd) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			Session sess = null;
			sess = conn.openSession();			
			new SCPClient(conn).put(localFile, remoteTargetDirectory);			
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void getFileSsh(String remoteFile, String localFile, String sshHostIp, String sshLogin, String sshPwd) throws Exception {
		Connection conn = null;
		try {
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");

			Session sess = null;

			sess = conn.openSession();
			sess.execCommand("cat "+remoteFile);

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			final Vector<String> out=new Vector<String>();
			
			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;
							out.add(line);
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
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
							System.out.println(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);			
			
			PrintWriter pw=new PrintWriter(localFile);
			for (int i=0; i<out.size(); ++i) pw.println(out.elementAt(i));
			pw.close();
			
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}	
		

}
