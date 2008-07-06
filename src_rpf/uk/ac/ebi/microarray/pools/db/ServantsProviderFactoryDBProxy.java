package uk.ac.ebi.microarray.pools.db;

import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.ServantProvider;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.TimeoutException;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class ServantsProviderFactoryDBProxy extends ServantProviderFactory {

	private final Log log = org.apache.commons.logging.LogFactory.getLog(ServantsProviderFactoryDBProxy.class);
	private ServantProvider _servantProvider = null;
	private HashMap<String, PoolDataDB> _poolHashMap = new HashMap<String, PoolDataDB>();
	private String _defaultPoolName;
	private DBLayerInterface _dbLayer = null;

	public ServantsProviderFactoryDBProxy() throws Exception {
		super();
		
		_dbLayer =  ((DBLayerProvider)Class.forName(System.getProperty("pools.dbproxymode.class")).newInstance()).getDBLayer();
		_defaultPoolName = System.getProperty("pools.dbproxymode.defaultpoolname");
		
		_poolHashMap = _dbLayer.getPoolDataHashMap();
		_servantProvider = new ServantProvider() {

			public ManagedServant borrowServantProxy(String poolName) throws TimeoutException {

				ManagedServant proxy = null;
				long tstart = System.currentTimeMillis();
				do {
					try {
						proxy = (ManagedServant) ServantProxyPoolSingletonDB.getInstance(poolName, _dbLayer).borrowObject();
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
					proxy = (ManagedServant) ServantProxyPoolSingletonDB.getInstance(poolName, _dbLayer).borrowObject();
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

					ServantProxyPoolSingletonDB.getInstance(_defaultPoolName, _dbLayer).returnObject(proxy);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(0);
				}
			}

			public void returnServantProxy(String poolName, ManagedServant proxy) {
				if (proxy == null)
					return;
				try {

					ServantProxyPoolSingletonDB.getInstance(poolName, _dbLayer).returnObject(proxy);
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
