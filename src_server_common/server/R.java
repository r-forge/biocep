package server;

import java.rmi.registry.Registry;

import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServerDefaults;

public class R {
	public static RServices _instance;
	static public RServices getInstance() {
		return _instance;
	}	
	static public Registry getRegistry() throws Exception {
		return ServerDefaults.getRmiRegistry();
	}
}
