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

import java.rmi.RemoteException;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.PoolableObjectFactory;
import org.kchine.rpf.InitializingException;
import org.kchine.rpf.LookUpTimeout;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.PoolDataDB;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ServantProxyFactoryDB implements PoolableObjectFactory {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServantProxyFactoryDB.class);

	DBLayerInterface _dbLayer = null;
	String _poolName = null;
	PoolDataDB _poolData = null;

	public ServantProxyFactoryDB(String poolName, DBLayerInterface dbLayer) throws Exception {
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