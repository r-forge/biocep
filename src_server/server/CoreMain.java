package server;

import java.net.URL;
import java.util.Vector;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServerDefaults;

public class CoreMain {

	public static void main(String[] args) throws Exception {
				
		Vector<URL> codeUrls=new Vector<URL>();		
		if (args.length > 0) {
			for (int i=0;i<args.length;++i) {
				codeUrls.add(new URL(args[i]));
			}
		} else {
			String jar=CoreMain.class.getResource("/server/CoreMain.class").toString();
			if (jar.startsWith("jar:")) {
				String jarfile=jar.substring("jar:".length(), jar.length()-"/server/CoreMain.class".length()-1);
				System.out.println("jarfile:"+jarfile);
				try {codeUrls.add(new URL(jarfile));} catch (Exception e) {e.printStackTrace();}
			}
		}		
				
		RServices r=null;
		
		if (ServerDefaults.isRegistryAccessible()) {
			boolean local;
			if (System.getProperty("local")!=null && !System.getProperty("local").equals("")) local=new Boolean(System.getProperty("local"));
			else local=PoolUtils.isWindowsOs(); 
			if (local ) {
					r = ServerManager.createRLocal(true, 
							PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),
							ServerDefaults._registryHost, ServerDefaults._registryPort , 
							ServerDefaults._memoryMin, ServerDefaults._memoryMax,System.getProperty("name"), false,(URL[])codeUrls.toArray(new URL[0]));
					
			} else {
				r = ServerManager.createR(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), 
						ServerDefaults._registryHost, ServerDefaults._registryPort , 
								ServerDefaults._memoryMin, ServerDefaults._memoryMax, System.getProperty("name"), false,(URL[])codeUrls.toArray(new URL[0]) );
			}
			
		} else {			
			System.out.println("Can't Launch R Server, Rmi Registry is not accessible!!");
		}
		
		System.exit(0);
	}

}
