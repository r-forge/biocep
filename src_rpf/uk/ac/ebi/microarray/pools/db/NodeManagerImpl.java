/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
 * Copyright (C) 2007 - 2008  Karim Chine
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
package uk.ac.ebi.microarray.pools.db;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;

import uk.ac.ebi.microarray.pools.CreationCallBack;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.NodeManager;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemoteLogListener;
import uk.ac.ebi.microarray.pools.RemotePanel;
import uk.ac.ebi.microarray.pools.ServantCreationTimeout;
import uk.ac.ebi.microarray.pools.db.monitor.SupervisorUtils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class NodeManagerImpl extends UnicastRemoteObject implements NodeManager {
	DBLayer _dbLayer;
	public static long SERVANT_CREATION_TIMEOUT_MILLISEC = 60000 * 3;

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
			SupervisorUtils.launch(nodeName, "-Dprivate=" + new Boolean(isPrivate).toString() + " -Dlistener.stub=" + listenerStub, false, PoolUtils
					.getHostIp());
			long t1 = System.currentTimeMillis();
			while (servantHolder[0] == null && exceptionHolder[0] == null) {
				if (System.currentTimeMillis() - t1 >= SERVANT_CREATION_TIMEOUT_MILLISEC)
					throw new ServantCreationTimeout();
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

	public void createServant(String nodeName) throws RemoteException {
		create(_dbLayer, nodeName, false);
	}

	public void kill(ManagedServant servant) throws RemoteException {
		Vector<HashMap<String, Object>> servants = _dbLayer.getTableData("SERVANTS", "STUB_HEX='" + PoolUtils.stubToHex(servant) + "'");
		if (servants.size() != 1)
			throw new RemoteException("Servant Not In The DB Registry");

		final String servantName = (String) servants.elementAt(0).get("NAME");

		new Thread(new Runnable() {
			public void run() {
				try {
					SupervisorUtils.killProcess(servantName, true, null);
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
					SupervisorUtils.killProcess(servantName, true, null);
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

	public ManagedServant cloneServer() throws RemoteException {
		return null;
	}

	public void asynchronousConsoleSubmit(String cmd) throws RemoteException {
	}

	public boolean isBusy() throws RemoteException {
		return false;
	}
	
	public String getJobId() throws RemoteException {
		return null;
	}
	
	public void setJobId(String jobId) throws RemoteException {		
	}

}
