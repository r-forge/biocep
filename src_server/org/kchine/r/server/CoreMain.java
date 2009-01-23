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
package org.kchine.r.server;

import java.net.ServerSocket;
import java.net.URL;
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.kchine.r.server.RServices;
import org.kchine.r.server.http.local.LocalHttpServer;
import org.kchine.r.server.impl.RServantImpl;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;


public class CoreMain {

	
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(CoreMain.class);
	
	public static void main(String[] args) throws Exception {
		PoolUtils.initLog4J();
		
		if (System.getProperty("rmi.port.start")!=null && !System.getProperty("rmi.port.start").equals("")) {
			
			int width=300;
			if (System.getProperty("submit.ssh.rmi.port.width")!=null && !System.getProperty("submit.ssh.rmi.port.width").equals("")) {
				width=Integer.decode(System.getProperty("submit.ssh.rmi.port.width"));
			}			
			
			int rmi_port_start=Integer.decode(System.getProperty("rmi.port.start"));
			Integer valid_port=null;
			for (int i=0;i<(width/RServantImpl.PORT_RANGE_WIDTH);++i) {
				try {
					ServerSocket s=new ServerSocket(rmi_port_start+i*RServantImpl.PORT_RANGE_WIDTH);
					s.close();
					valid_port=rmi_port_start+i*RServantImpl.PORT_RANGE_WIDTH;
					break;
				} catch (Exception e) {
				}
			}
			if (valid_port==null) {
				log.info("all available ports are taken, can't create server");
				System.exit(0);
			}
			
			System.setProperty("rmi.port.start",""+valid_port );			
			log.info("rmi.port.start:"+System.getProperty("rmi.port.start"));
		}
		
		Vector<URL> codeUrls=new Vector<URL>();		
		if (args.length > 0) {
			for (int i=0;i<args.length;++i) {
				codeUrls.add(new URL(args[i]));
			}
		} else {
			String jar=CoreMain.class.getResource("/org/kchine/r/server/CoreMain.class").toString();
			if (jar.startsWith("jar:")) {
				String jarfile=jar.substring("jar:".length(), jar.length()-"/org/kchine/r/server/CoreMain.class".length()-1);
				System.out.println("jarfile:"+jarfile);
				try {codeUrls.add(new URL(jarfile));} catch (Exception e) {e.printStackTrace();}
			}
		}		
				
		RServices r=null;
		
		if (ServerDefaults.isRegistryAccessible()) {
			String name=System.getProperty("name");			
			String rbinary=System.getProperty("r.binary");			
			r = ServerManager.createR(rbinary, true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), 
					ServerManager.getNamingInfo(), 
							ServerDefaults._memoryMin, ServerDefaults._memoryMax,name , false,(URL[])codeUrls.toArray(new URL[0]), System.getProperty("log.file") );

			
		} else {			
			System.out.println("Can't Launch R Server, Rmi Registry is not accessible!!");
		}
		
		/*
		int httpServerPort=-1;
		try {
			if (System.getProperty("http.port")!=null && !System.getProperty("http.port").equals("")) {
				httpServerPort=Integer.decode(System.getProperty("http.port"));				
			}			
		} catch (Exception e) {}
		if (httpServerPort!=-1) {
			r.startHttpServer(httpServerPort);
		}
		*/
		
		
		boolean wait= System.getProperty("wait")==null || System.getProperty("wait").equals("") || new Boolean(System.getProperty("wait"));
		if (wait) {
			while (true) {
				try {Thread.sleep(100);}catch (Exception e) {}
			}
		} else {		
			System.exit(0);
		}
	}

}
