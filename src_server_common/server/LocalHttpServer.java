package server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import uk.ac.ebi.microarray.pools.http.LocalClassServlet;

public class LocalHttpServer {

	private static Server _server = null;

	private static Integer _lock = new Integer(0);

	private static Context _root = null;

	public static Server getInstance() {
		if (_server != null)
			return _server;
		synchronized (_lock) {
			if (_server == null) {
				if (System.getProperty("localtomcat.port") == null || System.getProperty("localtomcat.port").equals("")) {
					_server = new Server(0);
				} else {
					_server = new Server(Integer.decode(System.getProperty("localtomcat.port")));
				}

				_root = new Context(_server, "/", Context.SESSIONS);
				_root.addServlet(new ServletHolder(new LocalClassServlet()), "/classes/*");
				try {
					_server.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
				while (!_server.isStarted()) {
					try {
						Thread.sleep(20);
					} catch (Exception ex) {
					}
				}
			}
			return _server;
		}
	}

	public static Integer getLocalHttpServerPort() {
		return getInstance().getConnectors()[0].getLocalPort();
	}

	public static Context getRootContext() {
		getInstance();
		return _root;
	}

}
