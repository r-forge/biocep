package http;

import java.rmi.registry.Registry;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import uk.ac.ebi.microarray.pools.db.DBLayerInterface;
import uk.ac.ebi.microarray.pools.db.monitor.RegistryProvider;

public class DBHttpProxy implements RegistryProvider{ 
	public Registry getRegistry() throws Exception {
		HashMap<String, Object> options = new HashMap<String, Object>();
		final String sessionId = RHttpProxy.logOnDB(System.getProperty("dbhttp.url"), "", System.getProperty("dbhttp.login"), System.getProperty("dbhttp.password"), options);			
		DBLayerInterface db = (DBLayerInterface)RHttpProxy.getDynamicProxy(System.getProperty("dbhttp.url"), sessionId, "REGISTRY", new Class<?>[]{DBLayerInterface.class}, new HttpClient(new MultiThreadedHttpConnectionManager()));
		return db;
	}
}
