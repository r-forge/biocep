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
package org.kchine.rpf;

import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javasci.Scilab;

import org.apache.commons.logging.Log;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServantCreationListener;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.YesSecurityManager;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.NodeDataDB;

import static org.kchine.rpf.ServerDefaults.*;

/**
 * @author Karim Chine   karim.chine@m4x.org
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

		
		for (int i=0;i<300;++i) Scilab.Exec("disp(22+1);");
		

		
		PoolUtils.initLog4J();
		PoolUtils.ensurePublicIPIsUsedForRMI();
		PoolUtils.noGui();
		
		
		
		try {

			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new YesSecurityManager());
			}

			boolean isNodeProvided=System.getProperty("node") != null && !System.getProperty("node").equals("");
			if (isNodeProvided) {
				NodeDataDB nodeData = null;
				try {
					rmiRegistry = ServerDefaults.getRmiRegistry();
					nodeData = ((DBLayerInterface) rmiRegistry).getNodeData("NODE_NAME='" + System.getProperty("node") + "'").elementAt(0);
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
				log.info("Instantiating " + _mainServantClassName + " with name " + servantName + " , prefix " + _servantPoolPrefix);
			}

			if (rmiRegistry == null)
				rmiRegistry = ServerDefaults.getRmiRegistry();

			System.out.println("### code base:" + System.getProperty("java.rmi.server.codebase"));

			ClassLoader cl = new URLClassLoader(PoolUtils.getURLS(System.getProperty("java.rmi.server.codebase")), MainServer.class.getClassLoader());
			Thread.currentThread().setContextClassLoader(cl);
			System.out.println(Arrays.toString(PoolUtils.getURLS(System.getProperty("java.rmi.server.codebase"))));

			mainServantClass = cl.loadClass(_mainServantClassName);

			boolean isPrivateServant = !isNodeProvided && ( (System.getProperty("private") != null && System.getProperty("private").equalsIgnoreCase("true")) );

			String servantCreationListenerStub = System.getProperty("listener.stub");
			if (servantCreationListenerStub != null && !servantCreationListenerStub.equals("")) {
				servantCreationListener = (ServantCreationListener) PoolUtils.hexToObject(servantCreationListenerStub);
			}			

			if (!isPrivateServant) {
				mservant = (ManagedServant) mainServantClass.getConstructor(new Class[] { String.class, String.class, Registry.class }).newInstance(
						new Object[] { servantName, _servantPoolPrefix, rmiRegistry });

			} else {

				mservant = (ManagedServant) mainServantClass.getConstructor(new Class[] { String.class, String.class, Registry.class }).newInstance(
						new Object[] { null, "PRIVATE_", rmiRegistry });

			}

			//System.out.println("clone:"+mservant.cloneServer());
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, mservant, null);
			}

			String sname = mservant.getServantName();
			log.info("sname :::" + sname);
			if (rmiRegistry instanceof DBLayerInterface) {
				if (System.getProperty("node") != null && !System.getProperty("node").equalsIgnoreCase("")) {
					((DBLayerInterface) rmiRegistry).updateServantNodeName(sname, System.getProperty("node"));
				} else {
					Vector<NodeDataDB> nodes = ((DBLayerInterface) rmiRegistry).getNodeData("");
					for (int i = 0; i < nodes.size(); ++i) {
						String nodeName = nodes.elementAt(i).getNodeName();
						String nodeIp = nodes.elementAt(i).getHostIp();
						String nodePrefix = nodes.elementAt(i).getPoolPrefix();
						if (sname.startsWith(nodePrefix) && nodeIp.equals(PoolUtils.getHostIp())) {
							((DBLayerInterface) rmiRegistry).updateServantNodeName(sname, nodeName);
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

				((DBLayerInterface) rmiRegistry).updateServantAttributes(sname, attributes);
			}
			//log.info("*************************$$$$$$$$$$$$");
			log.info("Servant " + sname + " instantiated successfully.");
	

		} catch (InvocationTargetException ite) {
			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, null, new RemoteException("", ite.getTargetException()));
			}
			throw new Exception(PoolUtils.getStackTraceAsString(ite.getTargetException()));

		} catch (Exception e) {

			log.info("----------------------");
			log.info(PoolUtils.getStackTraceAsString(e));
			e.printStackTrace();
			log.error(e);

			if (servantCreationListener != null) {
				PoolUtils.callBack(servantCreationListener, null, new RemoteException("", e));
			}

			System.exit(1);
		}
	}
}
