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
 * @author Karim Chine karim.chine@m4x.org
 */
public class ServantProxyPoolSingletonDB {
	static java.util.Hashtable<String, GenericObjectPool> _pool = new Hashtable<String, GenericObjectPool>();
	static Integer lock = new Integer(0);

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServantProxyPoolSingletonDB.class);
	private static boolean _shuttingDown = false;

	public static GenericObjectPool getInstance(String poolName, String driver, final String url, final String user, final String password) {
		String key = driver + "%" + poolName + "%" + url + "%" + user + "%" + password;
		if (_pool.get(key) != null)
			return _pool.get(key);
		synchronized (lock) {
			if (_pool.get(key) == null) {
				Connection conn = null;
				try {
					Class.forName(driver);
					final Vector<Object> borrowedObjects = new Vector<Object>();
					DBLayerInterface dbLayer = DBLayer.getLayer(getDBType(url), new ConnectionProvider() {
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

					if (System.getProperty("pools.dbmode.shutdownhook.enabled") != null
							&& System.getProperty("pools.dbmode.shutdownhook.enabled").equalsIgnoreCase("false")) {

					} else {
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
					}

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

	public static GenericObjectPool getInstance(String poolName, DBLayerInterface dbLayer) {
		String key = poolName + "%" + dbLayer.toString();

		if (_pool.get(key) != null)
			return _pool.get(key);
		synchronized (lock) {
			if (_pool.get(key) == null) {
				Connection conn = null;
				try {
					final Vector<Object> borrowedObjects = new Vector<Object>();

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

					if (System.getProperty("pools.dbmode.shutdownhook.enabled") != null
							&& System.getProperty("pools.dbmode.shutdownhook.enabled").equalsIgnoreCase("false")) {

					} else {
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
					}

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