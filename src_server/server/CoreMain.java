package server;

import java.net.ServerSocket;
import java.net.URL;
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
			
			int width=300;
			if (System.getProperty("submit.ssh.rmi.port.width")!=null && !System.getProperty("submit.ssh.rmi.port.width").equals("")) {
				width=Integer.decode(System.getProperty("submit.ssh.rmi.port.width"));
			}			
			
			int rmi_port_start=Integer.decode(System.getProperty("rmi.port.start"));
			Integer valid_port=null;
			for (int i=0;i<(width/RServantImpl.PORT_RANGE_WIDTH);++i) {
				try {
					ServerSocket s=new ServerSocket(rmi_port_start+i*RServantImpl.PORT_RANGE_WIDTH);
					s.close();
					valid_port=rmi_port_start+i*RServantImpl.PORT_RANGE_WIDTH;
					break;
				} catch (Exception e) {
				}
			}
			if (valid_port==null) {
				log.info("all available ports are taken, can't create server");
				System.exit(0);
			}
			
			System.setProperty("rmi.port.start",""+valid_port );			
			log.info("rmi.port.start:"+System.getProperty("rmi.port.start"));
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
			String name=System.getProperty("name");
			
			r = ServerManager.createR(null, true, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), 
					ServerManager.getNamingInfo(), 
							ServerDefaults._memoryMin, ServerDefaults._memoryMax,name , false,(URL[])codeUrls.toArray(new URL[0]), System.getProperty("log.file") );

			
		} else {			
			System.out.println("Can't Launch R Server, Rmi Registry is not accessible!!");
		}
		
		boolean wait= System.getProperty("wait")==null || System.getProperty("wait").equals("") || new Boolean(System.getProperty("wait"));
		if (wait) {
			while (true) {
				try {Thread.sleep(100);}catch (Exception e) {}
			}
		} else {		
			System.exit(0);
		}
	}

}
