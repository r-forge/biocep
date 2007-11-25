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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.impl.GenericObjectPool;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class ServantProxyPoolSingletonDB {
	static java.util.Hashtable<String, GenericObjectPool> _pool = new Hashtable<String, GenericObjectPool>();
	static Integer lock = new Integer(0);

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServantProxyPoolSingletonDB.class);
	private static boolean _shuttingDown = false;

	public static GenericObjectPool getInstance(String poolName, String driver, final String url, final String user,
			final String password) {
		String key = driver + "%" + poolName + "%" + url + "%" + user + "%" + password;
		if (_pool.get(key) != null)
			return _pool.get(key);
		synchronized (lock) {
			if (_pool.get(key) == null) {
				Connection conn = null;
				try {
					Class.forName(driver);
					final Vector<Object> borrowedObjects = new Vector<Object>();
					DBLayer dbLayer = DBLayer.getLayer(getDBType(url), new ConnectionProvider() {
						public Connection newConnection() throws SQLException {
							return DriverManager.getConnection(url, user, password);
						}
					});
					final GenericObjectPool p = new GenericObjectPool(new ServantProxyFactoryDB(poolName, dbLayer)) {
						@Override
						public synchronized Object borrowObject() throws Exception {
							if (_shuttingDown)
								throw new NoSuchElementException();
							Object result = super.borrowObject();
							borrowedObjects.add(result);
							return result;
						}

						@Override
						public synchronized void returnObject(Object obj) throws Exception {
							super.returnObject(obj);
							borrowedObjects.remove(obj);
						}
					};

					Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
						public void run() {
							synchronized (p) {

								final Vector<Object> bo = (Vector<Object>) borrowedObjects.clone();

								_shuttingDown = true;
								try {
									for (int i = 0; i < bo.size(); ++i)
										p.returnObject(bo.elementAt(i));
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}));

					_pool.put(key, p);
					p.setMaxIdle(0);
					p.setTestOnBorrow(true);
					p.setTestOnReturn(true);
				} catch (Exception e) {
					throw new RuntimeException(getStackTraceAsString(e));
				}

			}
			return _pool.get(key);
		}
	}
}