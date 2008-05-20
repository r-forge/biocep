import java.io.File;
import java.net.URL;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

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

        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(port);
        connector.setHost(getHostIp());
        server.addConnector(connector);

        
        Connector connectorLocal = new SelectChannelConnector();
        connectorLocal.setPort(port);
        connectorLocal.setHost("127.0.0.1");
        server.addConnector(connectorLocal);

        if (args.length==0) {
        	cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/rvirtual.war"), System.getProperty("user.home") + "/RWorkbench/", LOG_PRGRESS_TO_SYSTEM_OUT);
        	cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/rws.war"), System.getProperty("user.home") + "/RWorkbench/", LOG_PRGRESS_TO_SYSTEM_OUT);
        	args=new String[]{ System.getProperty("user.home") + "/RWorkbench/"+"rvirtual.war", 
        			           System.getProperty("user.home") + "/RWorkbench/"+"rws.war"};
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
		System.out.println("--> From the Virtual R Workbench, in Http mode, connect via the following URL:"+ "http://"+getHostIp()+":"+port+"/rvirtual/cmd");
		System.out.println("--> The SOAP-R WSDL :"+ "http://"+getHostIp()+":"+port+"/rws/rGlobalEnvFunction?wsdl");
		
	}
}
