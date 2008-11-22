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
package org.kchine.rpf.db;

import static org.kchine.rpf.PoolUtils.DEFAULT_DB_HOST;
import static org.kchine.rpf.PoolUtils.DEFAULT_DB_NAME;
import static org.kchine.rpf.PoolUtils.DEFAULT_DB_PASSWORD;
import static org.kchine.rpf.PoolUtils.DEFAULT_DB_PORT;
import static org.kchine.rpf.PoolUtils.DEFAULT_DB_TYPE;
import static org.kchine.rpf.PoolUtils.DEFAULT_DB_USER;
import static org.kchine.rpf.PoolUtils.getDBType;

import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.ServantProvider;
import org.kchine.rpf.TimeoutException;
import org.kchine.rpf.db.DBLayer;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.PoolDataDB;

public class ServantProviderDB implements ServantProvider{
	
	private String _driver;
	private String _url;
	private String _user;
	private String _password;
	private String _defaultPoolName;
	private DBLayerInterface _dbLayer = null;	
	private HashMap<String, PoolDataDB> _poolHashMap = new HashMap<String, PoolDataDB>();
	
	public ServantProviderDB() throws Exception{
		
		String _DB_TYPE =  System.getProperty("pools.dbmode.type") != null && !System.getProperty("pools.dbmode.type").equals("") ? System.getProperty("pools.dbmode.type") : DEFAULT_DB_TYPE;
		String _DB_HOST = System.getProperty("pools.dbmode.host") != null && !System.getProperty("pools.dbmode.host").equals("") ? System.getProperty("pools.dbmode.host") : DEFAULT_DB_HOST;
		int    _DB_PORT = System.getProperty("pools.dbmode.port") != null && !System.getProperty("pools.dbmode.port").equals("") ? Integer.decode(System.getProperty("pools.dbmode.port")) : DEFAULT_DB_PORT;		
		String _DB_NAME = System.getProperty("pools.dbmode.name") != null && !System.getProperty("pools.dbmode.name").equals("") ? System.getProperty("pools.dbmode.name") : DEFAULT_DB_NAME;	
		
		if (_DB_TYPE.equals("derby")) {
			_url = "jdbc:derby://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME+";create=true";
			_driver="org.apache.derby.jdbc.ClientDriver";
		} else if (_DB_TYPE.equals("mysql")) {			
			_url = "jdbc:mysql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;			
			_driver="org.gjt.mm.mysql.Driver";
				
		} else if (_DB_TYPE.equals("oracle")) {			
			_url = "jdbc:oracle:thin:@"+_DB_HOST+":"+_DB_PORT+":"+_DB_NAME; 
			_driver="oracle.jdbc.driver.OracleDriver";
		}
		
		_user = System.getProperty("pools.dbmode.user") != null && !System.getProperty("pools.dbmode.user").equals("") ? System.getProperty("pools.dbmode.user") : DEFAULT_DB_USER;
		_password = System.getProperty("pools.dbmode.password") != null && !System.getProperty("pools.dbmode.password").equals("") ? System.getProperty("pools.dbmode.password") : DEFAULT_DB_PASSWORD;			
		_defaultPoolName = System.getProperty("pools.dbmode.defaultpoolname");
				
		System.out.println("db type:"+_url); 
		System.out.println("db driver:"+_driver);
		System.out.println("db user:"+_user);
		System.out.println("db password:"+_password);			
		System.out.println("db default pool name:"+_defaultPoolName);

		{
			Class.forName(_driver);
			Connection conn = DriverManager.getConnection(_url, _user, _password);
			_dbLayer = DBLayer.getLayer(getDBType(_url), conn);			
			_poolHashMap = _dbLayer.getPoolDataHashMap();
			conn.commit();
			// conn.close();
		}
	}


	public ManagedServant borrowServantProxy(String poolName) throws TimeoutException {

		ManagedServant proxy = null;
		long tstart = System.currentTimeMillis();
		do {
			try {
				proxy = (ManagedServant) ServantProxyPoolSingletonDB.getInstance(poolName, _driver, _url, _user, _password).borrowObject();
			} catch (NoSuchElementException e) {
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			if (proxy != null) {
				try {
					// log .info("<" + Thread.currentThread().getName()+
					// "> obtained resource : "+
					// proxy.getServantName());
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

			// log.info("<" + Thread.currentThread().getName() + ">
			// thread waiting for resource for : "+
			// ((System.currentTimeMillis() - tstart) / 1000)+ "
			// seconds");

		} while (true);

		return proxy;
	}

	public ManagedServant borrowServantProxyNoWait(String poolName) {
		ManagedServant proxy = null;
		try {
			proxy = (ManagedServant) ServantProxyPoolSingletonDB.getInstance(poolName, _driver, _url, _user, _password).borrowObject();
		} catch (NoSuchElementException e) {
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return proxy;
	}

	public void returnServantProxy(ManagedServant proxy) {
		if (proxy == null)
			return;
		try {
			ServantProxyPoolSingletonDB.getInstance(_defaultPoolName, _driver, _url, _user, _password).returnObject(proxy);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void returnServantProxy(String poolName, ManagedServant proxy) {
		if (proxy == null)
			return;
		try {
			ServantProxyPoolSingletonDB.getInstance(poolName, _driver, _url, _user, _password).returnObject(proxy);
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

}
