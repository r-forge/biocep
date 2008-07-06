package uk.ac.ebi.microarray.pools.db;

import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_HOST;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_NAME;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_PASSWORD;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_PORT;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_TYPE;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_USER;
import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;

import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.ServantProvider;
import uk.ac.ebi.microarray.pools.TimeoutException;

public class ServantProviderDB implements ServantProvider{
	
	private String _driver;
	private String _url;
	private String _user;
	private String _password;
	private String _defaultPoolName;
	private DBLayerInterface _dbLayer = null;	
	private Hashtable<ManagedServant, String> _borrowedServants = new Hashtable<ManagedServant, String>();
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

		_borrowedServants.put(proxy, poolName);
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
