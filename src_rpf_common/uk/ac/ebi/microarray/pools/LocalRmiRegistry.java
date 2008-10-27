package uk.ac.ebi.microarray.pools;

import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;

public class LocalRmiRegistry {

	private static Registry _server = null;
	private static Integer _lock = new Integer(0);
	private static int _port; 

	public static Registry getInstance() {
		if (_server != null)
			return _server;
		synchronized (_lock) {
			if (_server == null) {
				try {
					if (System.getProperty("localrmiregistry.port") == null || System.getProperty("localrmiregistry.port").equals("")) {
						ServerSocket ss = new ServerSocket(0);
						_port = ss.getLocalPort();
						ss.close();

						Runtime.getRuntime().gc();
						
						Random rnd=new Random();rnd.setSeed(System.currentTimeMillis());
						while (true) {
							try {
								_server = LocateRegistry.createRegistry(_port);
								break;
							} catch (Exception e) {
								_port=2000+rnd.nextInt(1000);
							}
						}
						
						System.out.println("local port :" + _port);
							
					} else {
						_server = LocateRegistry.createRegistry(Integer.decode(System.getProperty("localrmiregistry.port")));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return _server;
		}
	}


	public static Integer getLocalRmiRegistryPort() {
		getInstance();
		return _port;
	}
	

}
