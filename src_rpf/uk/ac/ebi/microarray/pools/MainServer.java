/*
 * Copyright (C) 2007 EMBL-EBI
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.microarray.pools;


import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import uk.ac.ebi.microarray.pools.db.DBLayer;
import uk.ac.ebi.microarray.pools.db.NodeDataDB;
import uk.ac.ebi.microarray.pools.http.LocalClassServlet;
import static uk.ac.ebi.microarray.pools.ServerDefaults.*;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class MainServer {

	private static String _mainServantClassName = System.getProperty("servantclass");

	private static Class<?> mainServantClass = null;

	private static Registry rmiRegistry = null;

	private static String servantName = null;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(MainServer.class);

	private static ManagedServant mservant = null;
	public static ServantCreationListener servantCreationListener = null;
	public static void main(String[] args) throws Exception {

		System.out.println("*************************$$$$$$$$$$$$");
		new Thread(new Runnable() {
			public void run() {
				try {
					
					Server server = new Server(PoolUtils.getLocalTomcatPort());
					Context root = new Context(server,"/",Context.SESSIONS);
					root.addServlet(new ServletHolder(new LocalClassServlet()), "/classes/*");
					System.out.println("+++++++++++++++++++ going to start local http server port : " + PoolUtils.getLocalTomcatPort());
					server.start();
					
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}).start();

		/*
		System.out.println("*************************$$$$$$$$$$$$");
		new Thread(new Runnable() {
			public void run() {
				try {
					System.out.println("++++++++++++++++oops");
				Acme.Serve.Serve srv = new Acme.Serve.Serve();
				java.util.Properties properties = new java.util.Properties();
				properties.put("port", PoolUtils.getLocalTomcatPort());
				properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
				srv.arguments = properties;
				System.out.println("++++++++++++++++properties:" + properties + "  server: " + srv);
				srv.addServlet("/classes/", new LocalClassServlet());
				System.out.println("++++++++++++++++ going to start code server on port :"+PoolUtils.getLocalTomcatPort());
				srv.serve();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}).start();
		*/

		new Thread(new Runnable() {
			public void run() {
				System.out.println("+++++++++++++++++++ going to start local rmiregistry port : " + PoolUtils.getLocalRmiRegistryPort());
				try {
					LocateRegistry.createRegistry(PoolUtils.getLocalRmiRegistryPort());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		
		try {

			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new YesSecurityManager());
			}

			if (System.getProperty("node") != null && !System.getProperty("node").equalsIgnoreCase("")) {
				NodeDataDB nodeData = null;
				try {
					rmiRegistry = DBLayer.getRmiRegistry();
					nodeData = ((DBLayer) rmiRegistry).getNodeData("NODE_NAME='" + System.getProperty("node") + "'")
							.elementAt(0);
				} catch (Exception e) {
					log.info("Couldn't retrieve Node Info for node <" + System.getProperty("node") + ">");
					e.printStackTrace();
					return;
				}
				System.setProperty("autoname", "true");
				_servantPoolPrefix = nodeData.getPoolPrefix();

				System.out.println("nodedata:" + nodeData);
			}

			if (System.getProperty("autoname") != null && System.getProperty("autoname").equalsIgnoreCase("true")) {
				log.info("Instantiating " + _mainServantClassName + " with autonaming, prefix " + _servantPoolPrefix);
				servantName = null;
			} else {
				// no autonaming, check the name here
				if (System.getProperty("name") != null && !System.getProperty("name").equals("")) {
					servantName = System.getProperty("name");
				}
				log.info("Instantiating " + _mainServantClassName + " with name " + servantName + " , prefix "
						+ _servantPoolPrefix);
			}

			if (rmiRegistry == null)
				rmiRegistry = DBLayer.getRmiRegistry();

			
			System.out.println("### code base:"+System.getProperty("java.rmi.server.codebase"));
			
			ClassLoader cl = new URLClassLoader(PoolUtils.getURLS(System.getProperty("java.rmi.server.codebase")),
					MainServer.class.getClassLoader());
			Thread.currentThread().setContextClassLoader(cl);
			System.out.println(Arrays.toString(PoolUtils.getURLS(System.getProperty("java.rmi.server.codebase"))));
			
			mainServantClass = cl.loadClass(_mainServantClassName);

			boolean isPrivateServant = (System.getProperty("private") != null && System.getProperty("private")
					.equalsIgnoreCase("true"));

			String servantCreationListenerStub = System.getProperty("listener.stub");
			if (servantCreationListenerStub != null && !servantCreationListenerStub.equals("")) {
				servantCreationListener = (ServantCreationListener) PoolUtils.hexToObject(servantCreationListenerStub);
			}

			if (!isPrivateServant) {
				mservant = (ManagedServant) mainServantClass.getConstructor(
						new Class[] { String.class, String.class, Registry.class }).newInstance(
						new Object[] { servantName, _servantPoolPrefix, rmiRegistry });

			} else {

				mservant = (ManagedServant) mainServantClass.getConstructor(
						new Class[] { String.class, String.class, Registry.class }).newInstance(
						new Object[] { null, "PRIVATE_", rmiRegistry });

			}

			//System.out.println("clone:"+mservant.cloneServer());
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener,mservant, null);
			}

			String sname = mservant.getServantName();
			log.info("sname :::" + sname);
			if (rmiRegistry instanceof DBLayer) {
				if (System.getProperty("node") != null && !System.getProperty("node").equalsIgnoreCase("")) {
					((DBLayer) rmiRegistry).updateServantNodeName(sname, System.getProperty("node"));
				} else {
					Vector<NodeDataDB> nodes = ((DBLayer) rmiRegistry).getNodeData("");
					for (int i = 0; i < nodes.size(); ++i) {
						String nodeName = nodes.elementAt(i).getNodeName();
						String nodeIp = nodes.elementAt(i).getHostIp();
						String nodePrefix = nodes.elementAt(i).getPoolPrefix();
						if (sname.startsWith(nodePrefix) && nodeIp.equals(PoolUtils.getHostIp())) {
							((DBLayer) rmiRegistry).updateServantNodeName(sname, nodeName);
							break;
						}
					}
				}

				HashMap<String, Object> attributes = new HashMap<String, Object>();
				Enumeration<Object> sysPropKeys = (Enumeration<Object>) System.getProperties().keys();
				while (sysPropKeys.hasMoreElements()) {
					String key = (String) sysPropKeys.nextElement();
					if (key.startsWith("attr.")) {
						attributes.put(key, System.getProperty(key));
					}
				}

				((DBLayer) rmiRegistry).updateServantAttributes(sname, attributes);
			}
			log.info("*************************$$$$$$$$$$$$");
			log.info("Servant " + sname + " instantiated successfully.");

		} catch (InvocationTargetException ite) {
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener,null, new RemoteException("",ite.getTargetException()));
			}
			throw new Exception(PoolUtils.getStackTraceAsString(ite.getTargetException()));

		} catch (Exception e) {
			
			log.info("----------------------");
			log.info(PoolUtils.getStackTraceAsString(e));
			e.printStackTrace();
			log.error(e);
			
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener,null, new RemoteException("",e));
			}

			System.exit(1);
		}
	}
}
