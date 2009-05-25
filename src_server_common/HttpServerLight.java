import static org.kchine.rpf.PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT;
import static org.kchine.rpf.PoolUtils.cacheJar;
import static org.kchine.rpf.PoolUtils.getDBType;
import static org.kchine.rpf.PoolUtils.getHostIp;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.kchine.r.server.SendEmailMain;
import org.kchine.r.server.http.frontend.FreeResourcesListener;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.db.ConnectionProvider;
import org.kchine.rpf.db.DBLayer;
import org.kchine.rpf.db.SupervisorInterface;
import org.kchine.rpf.db.monitor.SupervisorUtils;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.HashSessionIdManager;
import org.mortbay.jetty.servlet.HashSessionManager;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;

public class HttpServerLight {

	public static void main(String[] args) throws Exception {

		if (System.getProperty("cloud.service") != null && !System.getProperty("cloud.service").equals("")) {
			if (!System.getProperty("cloud.service").equals("ec2"))
				return;
			final Properties props = PoolUtils.getAMIUserData();
			if (props.getProperty("start") == null || !props.getProperty("start").equalsIgnoreCase("true"))
				return;

			if (props.getProperty("port") != null)
				System.setProperty("port", props.getProperty("port"));
			if (props.getProperty("login") != null)
				System.setProperty("login", props.getProperty("login"));
			if (props.getProperty("pwd") != null)
				System.setProperty("pwd", props.getProperty("pwd"));

			if (props.getProperty("email") != null) {

				int guessport = 8080;
				try {
					if (System.getProperty("port") != null && !System.getProperty("port").equals("")) {
						guessport = Integer.decode(System.getProperty("port"));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				String guessLogin = "guest";
				if (System.getProperty("login") != null && !System.getProperty("login").equals("")) {
					guessLogin = System.getProperty("login");
				}

				String guessPwd = "guest";
				if (System.getProperty("pwd") != null && !System.getProperty("pwd").equals("")) {
					guessPwd = System.getProperty("pwd");
				}

				try {
					SendEmailMain client = new SendEmailMain();
					String server = "smtp.gmail.com";
					String from = "biocep@gmail.com";
					String to = props.getProperty("email");

					String subject = "EC2-R URL INFO";
					String message = "";
					message = message + "\n\nClick on the following link to get Direct Access to the an R Server on the EC2 Virtual Machine :\n"
							+ "http://www.biocep.net/rworkbench.jnlp?mode=http&url=" + "http://" + PoolUtils.getAMIHostName() + ":" + guessport
							+ "/rvirtual/cmd" + "&login=" + URLEncoder.encode(guessLogin, "UTF-8") + "&password=" + URLEncoder.encode(guessPwd, "UTF-8")
							+ "&privatename=my_EC2_R" + "&noconfirmation=true" + "\n\n" +

							"\n" + "Or Connect Using the R Workbench (R HTTP) with the following URL : " + "http://" + PoolUtils.getAMIHostName() + ":"
							+ guessport + "/rvirtual/cmd" + "\n";

					// String[] filenames ={"c:/somefile.txt"};

					client.sendMail(server, from, to, subject, message, null);

				} catch (Exception e) {
					e.printStackTrace(System.out);
				}

			}

			new Thread(new Runnable() {
				public void run() {

					try {
						System.setProperty("create", "true");
						System.setProperty("node.host", "127.0.0.1");
						System.setProperty("node.ip", "127.0.0.1");
						DbRegistry.main(null);
					} catch (Exception e) {
						e.printStackTrace();
					}

					int workers = 0;
					if (props.getProperty("workers") != null) {
						try {
							workers = Integer.decode(props.getProperty("workers"));
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							DBLayer dbLayer = DBLayer.getLayer(getDBType(ServerDefaults._dbUrl), new ConnectionProvider() {
								public Connection newConnection() throws SQLException {
									return DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
								}
							});

							SupervisorInterface supervisor = new SupervisorUtils(dbLayer);

							for (int i = 0; i < workers; ++i) {
								supervisor.launch("N1", "", false);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}).start();

			/*
			 * if (props.getProperty("pool.size") != null) { try {
			 * 
			 * int poolSize = Integer.decode(props.getProperty("pool.size"));
			 * int DB_CREATION_TIMEOUT_MILLISEC = 10000; int guessdbport = 1527;
			 * try { if (System.getProperty("db.port") != null &&
			 * !System.getProperty("db.port").equals("")) { guessdbport =
			 * Integer.decode(System.getProperty("db.port")); } } catch
			 * (Exception e) { e.printStackTrace(); }
			 * 
			 * long startTime = System.currentTimeMillis(); boolean dbAvailable
			 * = false; while ((System.currentTimeMillis() - startTime) <
			 * DB_CREATION_TIMEOUT_MILLISEC) { try { new Socket("127.0.0.1",
			 * guessdbport).close(); dbAvailable = true; break; } catch
			 * (Exception e) { // e.printStackTrace(); } }
			 * 
			 * try { Thread.sleep(3000); } catch (Exception e) { }
			 * 
			 * if (dbAvailable) { System.setProperty("node", "N1"); if
			 * (PoolUtils.isAmazonCloud()) System.setProperty("cloud", "ec2");
			 * for (int i = 0; i < poolSize; ++i) { new Thread(new Runnable(){
			 * public void run() { try { RmiServer.main(new String[0]); } catch
			 * (Exception e) { e.printStackTrace(); } } }).start(); } }
			 * 
			 * } catch (Exception e) { e.printStackTrace(); }
			 * 
			 * }
			 */

		}

		if (System.getProperty("pools.provider.factory") == null || System.getProperty("pools.provider.factory").equals("")) {
			System.setProperty("pools.provider.factory", "org.kchine.rpf.db.ServantsProviderFactoryDB");
		}

		if (System.getProperty("pools.dbmode.defaultpoolname") == null || System.getProperty("pools.dbmode.defaultpoolname").equals("")) {
			System.setProperty("pools.dbmode.defaultpoolname", "R");
		}

		if (System.getProperty("login") == null || System.getProperty("login").equals("")) {
			System.setProperty("login", "guest");
		}

		if (System.getProperty("pwd") == null || System.getProperty("pwd").equals("")) {
			System.setProperty("pwd", "guest");
		}

		int port = 8080;
		try {
			if (System.getProperty("port") != null && !System.getProperty("port").equals("")) {
				port = Integer.decode(System.getProperty("port"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		Server _virtualizationServer = new Server(port);
		_virtualizationServer.setSessionIdManager(new HashSessionIdManager(new java.util.Random()));
		_virtualizationServer.setStopAtShutdown(true);
		
		
		Context root = new Context(_virtualizationServer, "/rvirtual", Context.SESSIONS | Context.NO_SECURITY);
		

		
		final HttpSessionListener sessionListener = new FreeResourcesListener();
		root.getSessionHandler().setSessionManager(new HashSessionManager() {
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

		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.CommandServlet()), "/cmd/*");
		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.HelpServlet()), "/helpme/*");
		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.ConfigServlet()), "/config/*");
		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.GraphicsServlet(true)), "/graphics/*");
		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.RESTServlet()), "/rest/*");
		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.RebindServlet()), "/rebind/*");
		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.WWWDirectoryServlet(ServerManager.WWW_DIR,"/www")), "/www/*");
		root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.WWWDirectoryServlet(ServerManager.WWW_DIR,"/appletlibs")), "/appletlibs/*");		
		
		boolean rsoapEnabled= args.length==0 && System.getProperty("soapenabled")!=null && System.getProperty("soapenabled").equals("true");
		if (rsoapEnabled) {
			if (!new File(ServerManager.INSTALL_DIR+"/"+"rws.war").exists()) {
				cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/appletlibs/rws.war"), ServerManager.INSTALL_DIR , LOG_PRGRESS_TO_SYSTEM_OUT, false);
			}			
    		String contextPath="/rws";
	        WebAppContext wac = new WebAppContext();
	        wac.setContextPath(contextPath);
	        wac.setWar(ServerManager.INSTALL_DIR+"/"+"rws.war");   
	        _virtualizationServer.addHandler(wac);
    	}
		
		System.out.println("+ going to start virtualization http server port : " + port);
		_virtualizationServer.start();

		System.out.println("Light R-HTTP URL: http://" + getHostIp() + ":" + port + "/rvirtual/cmd");
		System.out.println("--> From the Virtual R Workbench, in Http mode, connect via the following URL:" + "http://" + getHostIp() + ":" + port
				+ "/rvirtual/cmd");
	}
}