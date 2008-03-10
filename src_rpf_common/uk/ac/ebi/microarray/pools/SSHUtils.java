package uk.ac.ebi.microarray.pools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHUtils {

	public static void killSshProcess(String processId, String sshHostIp, String sshLogin, String sshPwd, boolean forcedKill) throws Exception {
		Connection conn = null;
		try {
			conn=new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			
				
			Session sess = null;
			
			sess = conn.openSession();
			sess.execCommand("kill"+" "+(forcedKill?"-9":"")+ " "+processId);
			
			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));			
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));			
			new Thread(new Runnable(){
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
			
			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
			
		} finally {
			try {conn.close();} catch (Exception e) {e.printStackTrace();}
		}
		
	}

}
