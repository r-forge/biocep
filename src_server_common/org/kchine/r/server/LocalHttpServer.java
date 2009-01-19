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

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import server.LocalClassServlet;


public class LocalHttpServer {

	private static Server _server = null;

	private static Integer _lock = new Integer(0);

	private static Context _root = null;

	public static Server getInstance() {
		if (_server != null)
			return _server;
		synchronized (_lock) {
			if (_server == null) {
				if (System.getProperty("localtomcat.port") == null || System.getProperty("localtomcat.port").equals("")) {
					_server = new Server(0);
				} else {
					_server = new Server(Integer.decode(System.getProperty("localtomcat.port")));
				}

				_root = new Context(_server, "/", Context.SESSIONS);
				_root.addServlet(new ServletHolder(new LocalClassServlet()), "/classes/*");
				try {
					_server.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
				while (!_server.isStarted()) {
					try {
						Thread.sleep(20);
					} catch (Exception ex) {
					}
				}
			}
			return _server;
		}
	}

	public static Integer getLocalHttpServerPort() {
		return getInstance().getConnectors()[0].getLocalPort();
	}

	public static Context getRootContext() {
		getInstance();
		return _root;
	}

	/*
	 * 

 	public static Server getInstance() {
		if (_server != null)
			return _server;
		synchronized (_lock) {
			if (_server == null) {
				Random rnd=new Random();rnd.setSeed(System.currentTimeMillis());				
				int port=0;
				while (true) {
					try {
						if (System.getProperty("localtomcat.port") == null || System.getProperty("localtomcat.port").equals("")) {
							_server = new Server(port);
						} else {
							port=Integer.decode(System.getProperty("localtomcat.port"));
							if (ServerManager.isPortInUse("127.0.0.1", port)) {
								throw new RuntimeException("port "+port +" is already used");
							}
							_server = new Server(port);
						}
						_root = new Context(_server, "/", Context.SESSIONS);
						_root.addServlet(new ServletHolder(new LocalClassServlet()), "/classes/*");
						_server.start();
						break;
					} catch (BindException e) {
						port=2000+rnd.nextInt(1000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				while (!_server.isStarted()) {
					try {
						Thread.sleep(20);
					} catch (Exception ex) {
					}
				}
				
				System.out.println("## Local HTTP Server Started");
			}
			return _server;
		}
	}
 
 
	 * 
	 */

	
	
	
}
