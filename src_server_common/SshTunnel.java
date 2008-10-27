import uk.ac.ebi.microarray.pools.PoolUtils;

import com.jcraft.jsch.*;

public class SshTunnel {
	public static void main(String[] args) {
		SshTunnel t = new SshTunnel();
		try {
			t.go();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void go() throws Exception {
		String host = System.getProperty("host");
		String user = System.getProperty("user");
		String password = System.getProperty("password");
		int port = 22;
		try {
			if (System.getProperty("port")!=null && !System.getProperty("port").equals("")) port=Integer.decode(System.getProperty("port"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int tunnelLocalPort = 8080;
		try {
			if (System.getProperty("tunnel.local.port")!=null && !System.getProperty("tunnel.local.port").equals("")) tunnelLocalPort=Integer.decode(System.getProperty("tunnel.local.port"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String tunnelRemoteHost = System.getProperty("tunnel.remote.host");
		if (tunnelRemoteHost==null || tunnelRemoteHost.equals("")) {
			tunnelRemoteHost=host;
		}
		
		int tunnelRemotePort = 8080;
		try {
			if (System.getProperty("tunnel.remote.port")!=null && !System.getProperty("tunnel.remote.port").equals("")) tunnelRemotePort=Integer.decode(System.getProperty("tunnel.remote.port"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		JSch jsch = new JSch();
		Session session = jsch.getSession(user, host, port);
		session.setPassword(password);
		localUserInfo lui = new localUserInfo();
		session.setUserInfo(lui);
		session.connect();
		session.setPortForwardingL(tunnelLocalPort, tunnelRemoteHost, tunnelRemotePort);
		
		System.out.println(PoolUtils.getHostIp()+":"+tunnelLocalPort+ " is now mapping "+tunnelRemoteHost+":"+tunnelRemotePort+" via an SSH tunnel (user:"+user+")"); 
		
		System.out.println("Connected");

	}

	class localUserInfo implements UserInfo {
		String passwd;

		public String getPassword() {
			return passwd;
		}

		public boolean promptYesNo(String str) {
			return true;
		}

		public String getPassphrase() {
			return null;
		}

		public boolean promptPassphrase(String message) {
			return true;
		}

		public boolean promptPassword(String message) {
			return true;
		}

		public void showMessage(String message) {
		}
	}
}