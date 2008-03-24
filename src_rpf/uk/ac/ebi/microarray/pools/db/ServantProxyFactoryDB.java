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

import java.rmi.RemoteException;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.PoolableObjectFactory;

import uk.ac.ebi.microarray.pools.InitializingException;
import uk.ac.ebi.microarray.pools.LookUpTimeout;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class ServantProxyFactoryDB implements PoolableObjectFactory {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServantProxyFactoryDB.class);

	DBLayer _dbLayer = null;
	String _poolName = null;
	PoolDataDB _poolData = null;

	public ServantProxyFactoryDB(String poolName, DBLayer dbLayer) throws Exception {
		super();
		_poolName = poolName;
		_dbLayer = dbLayer;
		_poolData = _dbLayer.getPoolDataHashMap().get(poolName);
	}

	public void activateObject(Object obj) throws Exception {
	}

	public void destroyObject(Object obj) throws Exception {
		_dbLayer.lock();
		try {
			String servantName = _dbLayer.getNameFromStub((ManagedServant) obj);
			_dbLayer.unReserve(servantName);
			boolean killUsed = System.getProperty("pools.dbmode.killused") != null && System.getProperty("pools.dbmode.killused").equalsIgnoreCase("true");
			if (killUsed) {
				_dbLayer.registerPingFailure(servantName);
			}
		} finally {
			_dbLayer.unlock();
			_dbLayer.commit();
		}
	}

	public Object makeObject() throws Exception {
		Vector<String> servantNames = new Vector<String>();

		try {

			try {

				_dbLayer.lock();
				servantNames.addAll(_dbLayer.list(_poolData.getPrefixes()));
				// System.out.println("servant Names : " + servantNames);
			} catch (Exception e) {
				e.printStackTrace();
				throw new NoSuchElementException("No R Servant available / No DB ");
			}

			Vector<Integer> order = null;
			if (servantNames.size() > 0)
				order = PoolUtils.getRandomOrder(servantNames.size());

			for (int i = 0; i < servantNames.size(); ++i) {
				try {

					ManagedServant servant = (ManagedServant) _dbLayer.lookup(servantNames.elementAt(order.elementAt(i)));
					PoolUtils.ping(servant);
					_dbLayer.reserve(servantNames.elementAt(order.elementAt(i)));
					return servant;
				} catch (LookUpTimeout e) {
					_dbLayer.registerPingFailure(servantNames.elementAt(order.elementAt(i)));
				} catch (RemoteException re) {

					if (re.getCause() instanceof InitializingException) {
					} else {
						_dbLayer.registerPingFailure(servantNames.elementAt(order.elementAt(i)));
					}

				}
			}

		} finally {
			_dbLayer.unlock();
			_dbLayer.commit();
		}

		throw new NoSuchElementException("No Servant available");
	}

	public void passivateObject(Object obj) throws Exception {
		PoolUtils.reset((ManagedServant) obj);
	}

	public boolean validateObject(Object obj) {
		try {
			PoolUtils.ping((ManagedServant) obj);
			return true;
		} catch (RemoteException re) {

			if (re.getCause() instanceof InitializingException) {
			} else {
				try {

					_dbLayer.lock();
					try {
						String servantName = _dbLayer.getNameFromStub((ManagedServant) obj);
						_dbLayer.registerPingFailure(servantName);
						_dbLayer.unReserve(servantName);
					} finally {
						_dbLayer.unlock();
						_dbLayer.commit();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			log.info("## Validation failed, couldn't ping");
			return false;
		}
	}

}