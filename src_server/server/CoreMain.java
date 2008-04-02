package server;

import java.net.URL;
import java.util.Vector;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;

public class CoreMain {

	public static void main(String[] args) throws Exception {
				
		Vector<URL> codeUrls=new Vector<URL>();		
		if (args.length > 0) {
			for (int i=0;i<args.length;++i) {
				codeUrls.add(new URL(args[i]));
			}
		} else {
			String jar=CoreMain.class.getResource("/server/CoreMain.class").toString();
			String jarfile=jar.substring("jar:".length(), jar.length()-"/server/CoreMain.class".length()-1);
			System.out.println("jarfile:"+jarfile);
			try {codeUrls.add(new URL(jarfile));} catch (Exception e) {e.printStackTrace();}
		}		
				
		RServices r=null;
		if (PoolUtils.isWindowsOs()) {
				r = ServerManager.createRLocal(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),PoolUtils
						.getHostIp(), 1099, 256, 256,false,(URL[])codeUrls.toArray(new URL[0]));
				
		} else {
			r = ServerManager.createR(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), PoolUtils
					.getHostIp(), 1099, 256, 256, "", false,(URL[])codeUrls.toArray(new URL[0]) );
		}
		
		System.exit(0);
	}

}
