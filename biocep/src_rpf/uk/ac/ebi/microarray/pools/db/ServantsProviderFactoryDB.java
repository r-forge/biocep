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
package uk.ac.ebi.microarray.pools.db;

import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.ServantProvider;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.TimeoutException;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class ServantsProviderFactoryDB extends ServantProviderFactory {

	private final Log log = org.apache.commons.logging.LogFactory.getLog(ServantsProviderFactoryDB.class);
	private ServantProvider _servantProvider = null;
	private Hashtable<ManagedServant, String> _borrowedServants = new Hashtable<ManagedServant, String>();
	private HashMap<String, PoolDataDB> _poolHashMap = new HashMap<String, PoolDataDB>();
	private String _driver;
	private String _url;
	private String _user;
	private String _password;
	private String _defaultPoolName;
	private DBLayer _dbLayer = null;

	public ServantsProviderFactoryDB() throws Exception {
		super();

		_driver = System.getProperty("pools.dbmode.driver");
		_url = System.getProperty("pools.dbmode.url");
		_user = System.getProperty("pools.dbmode.user");
		_password = System.getProperty("pools.dbmode.password");
		_defaultPoolName = System.getProperty("pools.dbmode.defaultpoolname");

		{
			Class.forName(_driver);
			Connection conn = DriverManager.getConnection(_url, _user, _password);
			_dbLayer = DBLayer.getLayer(getDBType(_url), conn);
			_poolHashMap = _dbLayer.getPoolDataHashMap();
			conn.commit();
			//conn.close();
		}

		_servantProvider = new ServantProvider() {

			public ManagedServant borrowServantProxy(String poolName) throws TimeoutException {

				ManagedServant proxy = null;
				long tstart = System.currentTimeMillis();
				do {
					try {
						proxy = (ManagedServant) ServantProxyPoolSingletonDB.getInstance(poolName, _driver, _url,
								_user, _password).borrowObject();
					} catch (NoSuchElementException e) {
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					if (proxy != null) {
						try {
							//log	.info("<" + Thread.currentThread().getName()+ "> obtained resource : "+ proxy.getServantName());
						} catch (Exception e) {
						}
						break;
					} else {

					}

					if (System.currentTimeMillis() - tstart > _poolHashMap.get(poolName).getBorrowTimeout())
						throw new TimeoutException();
					try {
						Thread.sleep(20);
					} catch (Exception e) {
					}

					//log.info("<" + Thread.currentThread().getName()	+ "> thread waiting for resource for  : "+ ((System.currentTimeMillis() - tstart) / 1000)+ " seconds");

				} while (true);

				_borrowedServants.put(proxy, poolName);
				return proxy;
			}

			public ManagedServant borrowServantProxyNoWait(String poolName) {
				ManagedServant proxy = null;
				try {
					proxy = (ManagedServant) ServantProxyPoolSingletonDB.getInstance(poolName, _driver, _url, _user,
							_password).borrowObject();
				} catch (NoSuchElementException e) {
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				if (proxy != null)
					_borrowedServants.put(proxy, poolName);
				return proxy;
			}

			public void returnServantProxy(ManagedServant proxy) {
				if (proxy == null)
					return;
				try {
					String poolName = _borrowedServants.get(proxy);
					_borrowedServants.remove(proxy);
					ServantProxyPoolSingletonDB.getInstance(poolName, _driver, _url, _user, _password).returnObject(
							proxy);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}

			public ManagedServant borrowServantProxy() throws TimeoutException {
				return borrowServantProxy(_defaultPoolName);
			}

			public ManagedServant borrowServantProxyNoWait() {
				return borrowServantProxyNoWait(_defaultPoolName);
			}

			public String getDefaultPoolName() {
				return _defaultPoolName;
			}

			public Registry getRegistry() {
				return _dbLayer;
			}

		};

	}

	@Override
	public ServantProvider getServantProvider() {
		return _servantProvider;
	}

}
