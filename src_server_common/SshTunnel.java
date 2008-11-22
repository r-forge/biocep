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
import org.kchine.rpf.PoolUtils;

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