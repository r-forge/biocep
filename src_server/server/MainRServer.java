package server;

import uk.ac.ebi.microarray.pools.MainServer;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class MainRServer {

	public static void main(String[] args) throws Exception {
		Class<?> servantClass=RServantImpl.class;
		System.setProperty("servantclass", servantClass.getName());
		MainServer.main(args);		
	}

}
