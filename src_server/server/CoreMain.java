package server;

import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;

public class CoreMain {

	public static void main(String[] args) throws Exception {
		RServices r=null;
		if (PoolUtils.isWindowsOs()) {
				r = ServerManager.createRLocal(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),PoolUtils
						.getHostIp(), 1099, 256, 256,false);
		} else {
			r = ServerManager.createR(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), PoolUtils
					.getHostIp(), 1099, 256, 256, false);
		}
		System.exit(0);
	}

}
