package server;

import java.net.URL;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.logging.Log;

import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.ServerDefaults;

public class CoreMain {

	
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(CoreMain.class);
	
	public static void main(String[] args) throws Exception {
		PoolUtils.initLog4J();
		
		if (System.getProperty("rmi.port.start")!=null && !System.getProperty("rmi.port.start").equals("")) {
			int rmi_port_start=Integer.decode(System.getProperty("rmi.port.start"));
			log.info("rmi.port.start #1:"+System.getProperty("rmi.port.start"));		
			Random rnd=new Random(System.currentTimeMillis());
			rmi_port_start=rmi_port_start+5*rnd.nextInt(50);
			System.setProperty("rmi.port.start",""+rmi_port_start );			
			log.info("rmi.port.start #2:"+System.getProperty("rmi.port.start"));
		}
		
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
							ServerManager.getNamingInfo() , 
							ServerDefaults._memoryMin, ServerDefaults._memoryMax,System.getProperty("name"), false,(URL[])codeUrls.toArray(new URL[0]), System.getProperty("log.file"));
					
					
					
			} else {
				
				
				r = ServerManager.createR(true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), 
						ServerManager.getNamingInfo(), 
								ServerDefaults._memoryMin, ServerDefaults._memoryMax, System.getProperty("name"), false,(URL[])codeUrls.toArray(new URL[0]), System.getProperty("log.file") );
				
				
				
			}
			
		} else {			
			System.out.println("Can't Launch R Server, Rmi Registry is not accessible!!");
		}
		
		if (System.getProperty("wait")!=null && System.getProperty("wait").equalsIgnoreCase("true")) {
			while (true) {
				try {Thread.sleep(100);}catch (Exception e) {}
			}
		} else {		
			System.exit(0);
		}
	}

}
