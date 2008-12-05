import static org.kchine.rpf.PoolUtils.getHostIp;
import http.FreeResourcesListener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHolder;
public class HttpServerLight {

	public static void main(String[] args) throws Exception {
		
		if (System.getProperty("pools.provider.factory")==null || System.getProperty("pools.provider.factory").equals("")) {
			System.setProperty("pools.provider.factory", "org.kchine.rpf.db.ServantsProviderFactoryDB");
		}
		
		if (System.getProperty("pools.dbmode.defaultpoolname")==null || System.getProperty("pools.dbmode.defaultpoolname").equals("")) {
			System.setProperty("pools.dbmode.defaultpoolname", "R");
		}
		
		if (System.getProperty("login")==null || System.getProperty("login").equals("")) {
			System.setProperty("login", "guest");
		}
		
		if (System.getProperty("pwd")==null || System.getProperty("pwd").equals("")) {
			System.setProperty("pwd", "guest");
		}
		
		
		int port=8080;
		try {
			if (System.getProperty("port")!=null && !System.getProperty("port").equals("")) {
				port=Integer.decode(System.getProperty("port"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Server _virtualizationServer = new Server(port);
		_virtualizationServer.setStopAtShutdown(true);
		Context root = new Context(_virtualizationServer, "/", Context.SESSIONS|Context.NO_SECURITY);
		
		final HttpSessionListener sessionListener=new FreeResourcesListener();				
		root.getSessionHandler().setSessionManager(new HashSessionManager(){
			@Override
			protected void addSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session session, boolean arg1) {
				super.addSession(session, arg1);
				sessionListener.sessionCreated(new HttpSessionEvent(session.getSession()));
			}
			
			@Override
			protected void addSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session session) {
				super.addSession(session);
			}
			
			@Override
			public void removeSession(HttpSession session, boolean invalidate) {
				super.removeSession(session, invalidate);
				sessionListener.sessionDestroyed(new HttpSessionEvent(session));
			}
			
			@Override
			public void removeSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session session, boolean arg1) {
				super.removeSession(session, arg1);
				sessionListener.sessionDestroyed(new HttpSessionEvent(session));
			}
			
			@Override
			protected void removeSession(String clusterId) {
				super.removeSession(clusterId);
			}
		});
		
		root.addServlet(new ServletHolder(new http.CommandServlet()), "/rvirtual/cmd/*");
		root.addServlet(new ServletHolder(new http.HelpServlet()), "/rvirtual/helpme/*");
		root.addServlet(new ServletHolder(new http.ConfigServlet()), "/rvirtual/config/*");
		root.addServlet(new ServletHolder(new http.GraphicsServlet()), "/rvirtual/graphics/*");
		root.addServlet(new ServletHolder(new http.RESTServlet()), "/rvirtual/rest/*");
		root.addServlet(new ServletHolder(new http.RebindServlet()), "/rvirtual/rebind/*");
		
		System.out.println("+ going to start virtualization http server port : " + port);
		_virtualizationServer.start();				

		
		System.out.println("Light R-HTTP URL: http://"+getHostIp()+":"+port+"/rvirtual/cmd");
		System.out.println("--> From the Virtual R Workbench, in Http mode, connect via the following URL:"+ "http://"+getHostIp()+":"+port+"/rvirtual/cmd");	
	}
}