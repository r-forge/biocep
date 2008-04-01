package server;

import java.net.URL;

import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;

public class CoreMain {

	public static void main(String[] args) throws Exception {
		
		URL codeURL=null;
		if (args.length>1) {
			try {
				codeURL=new URL(args[0]);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			String jar=CoreMain.class.getResource("/server/CoreMain.class").toString();
			String jarfile=jar.substring("jar:".length(), jar.length()-"/server/CoreMain.class".length()-1);
			System.out.println("jarfile:"+jarfile);
			try {codeURL=new URL(jarfile);} catch (Exception e) {e.printStackTrace();}			
		}
		RServices r=null;
		if (PoolUtils.isWindowsOs()) {
				r = ServerManager.createRLocal(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),PoolUtils
						.getHostIp(), 1099, 256, 256,false,codeURL);
		} else {
			r = ServerManager.createR(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), PoolUtils
					.getHostIp(), 1099, 256, 256, "", false,codeURL );
		}
		
		System.exit(0);
	}

}
