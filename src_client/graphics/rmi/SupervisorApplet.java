package graphics.rmi;

import http.RHttpProxy;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.JApplet;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

import uk.ac.ebi.microarray.pools.db.DBLayerInterface;
import uk.ac.ebi.microarray.pools.db.SupervisorInterface;
import uk.ac.ebi.microarray.pools.db.monitor.Supervisor;

public class SupervisorApplet extends JApplet {
	
	@Override
	public void init() {
		super.init();
		getContentPane().setLayout(new BorderLayout());
		try {
			HashMap<String, Object> options = new HashMap<String, Object>();
			final String sessionId = RHttpProxy.logOnDB(getParameter("url"), "", getParameter("login"), getParameter("password"), options);			
			DBLayerInterface db = (DBLayerInterface)RHttpProxy.getDynamicProxy(getParameter("url"), sessionId, "REGISTRY", new Class<?>[]{DBLayerInterface.class}, new HttpClient(new MultiThreadedHttpConnectionManager()));
			SupervisorInterface supervisorInterface=(SupervisorInterface)RHttpProxy.getDynamicProxy(getParameter("url"), sessionId, "SUPERVISOR", new Class<?>[]{SupervisorInterface.class}, new HttpClient(new MultiThreadedHttpConnectionManager()));			
			getContentPane().add(new Supervisor(db,supervisorInterface).run());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void destroy() {
		super.destroy();
	}
}