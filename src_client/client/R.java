package client;

import java.rmi.registry.Registry;

import graphics.rmi.GDApplet;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServerDefaults;

public class R {
	static public RServices getInstance() {
		if (GDApplet._instance==null) return null;
		else return GDApplet._instance.getR();
	}
	static public Registry getRegistry() throws Exception {
		return ServerDefaults.getRmiRegistry();
	}
}
