import java.io.File;
import java.net.URL;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import server.ServerManager;

import static uk.ac.ebi.microarray.pools.PoolUtils.*;


public class HttpServer {


	
	public static void main(String[] args) throws Exception {
		int port=8080;
		try {
			if (System.getProperty("port")!=null && !System.getProperty("port").equals("")) {
				port=Integer.decode(System.getProperty("port"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		boolean rhttpEnabled=(args.length==0);
		boolean rsoapEnabled= args.length==0 && System.getProperty("soapenabled")!=null && System.getProperty("soapenabled").equals("true");

        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setHost(getHostIp());
        server.addConnector(connector);
        
        Connector connectorLocal = new SelectChannelConnector();
        connectorLocal.setPort(port);
        connectorLocal.setHost("127.0.0.1");
        server.addConnector(connectorLocal);

        if (rhttpEnabled) {
        	cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/rvirtual.war"), ServerManager.INSTALL_DIR, LOG_PRGRESS_TO_SYSTEM_OUT, false);
        	if (rsoapEnabled) {
        		cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/rws.war"), ServerManager.INSTALL_DIR , LOG_PRGRESS_TO_SYSTEM_OUT, false);
        		args=new String[]{ ServerManager.INSTALL_DIR +"rvirtual.war", 
            			ServerManager.INSTALL_DIR+"rws.war"};
        	} else {
        		args=new String[]{ ServerManager.INSTALL_DIR +"rvirtual.war" };
        	}
        }
                        
		for (int i=0; i<args.length; ++i) {
			File warfile=new File(args[i]);
			if (!warfile.exists()) {
				System.out.println("couldn't find the war file :"+args[i]);System.exit(0);
			}
			
			String contextPath="/"+warfile.getName();
			if (contextPath.endsWith(".war")) contextPath=contextPath.substring(0, contextPath.length()-".war".length());
			
	        WebAppContext wac = new WebAppContext();
	        wac.setContextPath(contextPath);
	        wac.setWar(warfile.getAbsolutePath());   
	        server.addHandler(wac);
		}
		
		server.setStopAtShutdown(true);
        server.start();
        
		while (!server.isStarted()) {
			try {
				Thread.sleep(20);
			} catch (Exception ex) {
			}
		}
		
		System.out.println("--> Http Server Started sucessfully on port "+port);
		
		if (rhttpEnabled) {
			System.out.println("R-HTTP URL: http://"+getHostIp()+":"+port+"/rvirtual/cmd");
			System.out.println("--> From the Virtual R Workbench, in Http mode, connect via the following URL:"+ "http://"+getHostIp()+":"+port+"/rvirtual/cmd");
		}
		
		if (rsoapEnabled) {				
			System.out.println("R-SOAP WSDL:"+ "http://"+getHostIp()+":"+port+"/rws/rGlobalEnvFunction?wsdl");
		}
		
	}
}
