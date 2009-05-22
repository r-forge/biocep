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
package org.kchine.rpf.db;

import java.io.Serializable;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.kchine.rpf.CreationCallBack;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.ManagedServantAbstract;
import org.kchine.rpf.NodeManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RemoteLogListener;
import org.kchine.rpf.RemotePanel;
import org.kchine.rpf.ServantCreationTimeout;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.db.DBLayer;
import org.kchine.rpf.db.NodeDataDB;
import org.kchine.rpf.db.monitor.SupervisorUtils;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class NodeManagerImpl extends UnicastRemoteObject implements NodeManager {
	DBLayer _dbLayer;
	public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 0;

	public NodeManagerImpl(DBLayer dbLayer) throws RemoteException {
		super();
		_dbLayer = dbLayer;
	}

	public ManagedServant createPrivateServant(String nodeName) throws RemoteException {
		return create(_dbLayer, nodeName, true);
	}

	public static ManagedServant create(DBLayer dbLayer, String nodeName, boolean isPrivate) throws RemoteException {

		ManagedServant[] servantHolder = new ManagedServant[1];
		RemoteException[] exceptionHolder = new RemoteException[1];

		CreationCallBack callBack = new CreationCallBack(servantHolder, exceptionHolder);
		try {
			String listenerStub = PoolUtils.stubToHex(callBack);
			new SupervisorUtils(dbLayer).launch(nodeName, "-Dprivate=" + new Boolean(isPrivate).toString() + " -Dlistener.stub=" + listenerStub, false);
			long t1 = System.currentTimeMillis();
			while (servantHolder[0] == null && exceptionHolder[0] == null) {
				if (SERVANT_CREATION_TIMEOUT_MILLISEC>0 && (System.currentTimeMillis() - t1 >= SERVANT_CREATION_TIMEOUT_MILLISEC)) throw new ServantCreationTimeout();
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}

		} catch (Exception e) {
			throw new RemoteException("", e);
		} finally {
			if (callBack != null) {
				UnicastRemoteObject.unexportObject(callBack, true);
			}
		}

		if (exceptionHolder[0] != null)
			throw exceptionHolder[0];

		return servantHolder[0];
	}

	public ManagedServant createServant(String nodeName) throws RemoteException {
		return create(_dbLayer, nodeName, false);
	}

	public void kill(ManagedServant servant) throws RemoteException {
		Vector<HashMap<String, Object>> servants = _dbLayer.getTableData("SERVANTS", "STUB_HEX='" + PoolUtils.stubToHex(servant) + "'");
		if (servants.size() != 1)
			throw new RemoteException("Servant Not In The DB Registry");

		final String servantName = (String) servants.elementAt(0).get("NAME");

		new Thread(new Runnable() {
			public void run() {
				try {
					new SupervisorUtils(_dbLayer).killProcess(servantName, true);
					_dbLayer.unbind(servantName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void kill(final String servantName) throws RemoteException {
		new Thread(new Runnable() {
			public void run() {
				try {
					new SupervisorUtils(_dbLayer).killProcess(servantName, true);
					_dbLayer.unbind(servantName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void die(final ManagedServant servant) throws RemoteException {
		new Thread(new Runnable() {
			public void run() {
				try {
					PoolUtils.die(servant);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void incrementPingFailure(ManagedServant servant) throws RemoteException {
		Vector<HashMap<String, Object>> servants = _dbLayer.getTableData("SERVANTS", "STUB_HEX='" + PoolUtils.stubToHex(servant) + "'");
		if (servants.size() != 1)
			throw new RemoteException("Servant Not In The DB Registry");
		try {
			_dbLayer.registerPingFailure((String) servants.elementAt(0).get("NAME"));
		} catch (Exception e) {
			throw new RemoteException("", e);
		}
	}

	public String[] getNodeNames() throws RemoteException {
		Vector<NodeDataDB> nodes = _dbLayer.getNodeData("");
		String[] result = new String[nodes.size()];
		for (int i = 0; i < result.length; ++i) {
			result[i] = nodes.elementAt(i).getNodeName();
		}
		return result;
	}

	public void addErrListener(RemoteLogListener listener) throws RemoteException {
	}

	public void addOutListener(RemoteLogListener listener) throws RemoteException {
	}

	public String consoleSubmit(String cmd) throws RemoteException {
		return null;
	}
	
	public String consoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {
		return null;
	}
	
	public String getStatus() throws RemoteException {
		return null;
	}

	public void die() throws RemoteException {
		System.exit(0);
	}

	public String getLogs() throws RemoteException {
		return null;
	}

	public RemotePanel getPanel(int w, int h) throws RemoteException {
		return null;
	}

	public String getServantName() throws RemoteException {
		return null;
	}

	public boolean hasConsoleMode() throws RemoteException {
		return false;
	}

	public boolean hasGraphicMode() throws RemoteException {
		return false;
	}

	public boolean hasPushPopMode() throws RemoteException {
		return false;
	}

	public boolean isResetEnabled() throws RemoteException {
		return false;
	}

	public String[] listSymbols() throws RemoteException {
		return null;
	}

	public void logInfo(String message) throws RemoteException {
	}

	public String ping() throws RemoteException {
		return null;
	}

	public Serializable pop(String symbol) throws RemoteException {
		return null;
	}

	public void push(String symbol, Serializable object) throws RemoteException {
	}

	public void removeAllErrListeners() throws RemoteException {
	}

	public void removeAllOutListeners() throws RemoteException {
	}

	public void removeErrListener(RemoteLogListener listener) throws RemoteException {
	}

	public void removeOutListener(RemoteLogListener listener) throws RemoteException {
	}

	public void reset() throws RemoteException {
	}

	public void setResetEnabled(boolean enable) throws RemoteException {
	}

	public String getProcessId() throws RemoteException {
		return PoolUtils.getProcessId();
	}

	public String getHostIp() throws RemoteException {
		return PoolUtils.getHostIp();
	}

	public String getHostName() throws RemoteException {
		return PoolUtils.getHostName();
	}
	
	public Map<String, String> getSystemEnv() throws RemoteException {
		return System.getenv();
	}
	
	public Properties getSystemProperties() throws RemoteException {
		return System.getProperties();
	}
	
	public ManagedServant cloneServer() throws RemoteException {
		return null;
	}

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
	}

	public void asynchronousConsoleSubmit(String cmd, HashMap<String, Object> clientProperties) throws RemoteException {		
	}
	
	
	public boolean isBusy() throws RemoteException {
		return false;
	}
	
	public String getJobId() throws RemoteException {
		return null;
	}
	
	public void setJobId(String jobId) throws RemoteException {		
	}
	
	public String getStub() throws RemoteException {
		return PoolUtils.stubToHex(this);
	}
	
	public String export(Properties namingRegistryProperties, String prefixOrName, boolean autoName) throws RemoteException {
		try {			
			Registry registry=ServerDefaults.getRegistry(namingRegistryProperties);
			if (autoName) {
				String newname = null;			
				while (true) {
					newname = ManagedServantAbstract.makeName(prefixOrName, registry);
					try {
						registry.bind(newname,  java.rmi.server.RemoteObject.toStub(this));
						break;
					} catch (AlreadyBoundException e) {
					}
				}
				return newname;
			} else {
				registry.rebind(prefixOrName,  java.rmi.server.RemoteObject.toStub(this) );	
				return prefixOrName;
			}			
		} catch (Exception e) {
			throw new RemoteException("",e);
		}
	}
}