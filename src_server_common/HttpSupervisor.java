import genericnaming.httpregistryClass;

import java.awt.Dimension;
import java.util.Properties;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.kchine.r.server.http.frontend.ConnectionFailedException;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.SupervisorInterface;
import org.kchine.rpf.db.monitor.Supervisor;

public class HttpSupervisor {
	
	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("httpregistry.url", System.getProperty("url")==null || System.getProperty("url").equals("") ? "http://localhost:8080/rvirtual/cmd": System.getProperty("url") );
		props.put("httpregistry.login", System.getProperty("login")==null || System.getProperty("login").equals("") ? "guest" : System.getProperty("login"));
		props.put("httpregistry.password", System.getProperty("password")==null || System.getProperty("password").equals("") ? "guest" : System.getProperty("password"));

		httpregistryClass hr = new genericnaming.httpregistryClass();
		DBLayerInterface db = null;
		try {
			db = (DBLayerInterface) hr.getRegistry(props);
		} catch (ConnectionFailedException ex) {			
			JOptionPane.showMessageDialog(null, "Connection to Server Failed", "", JOptionPane.ERROR_MESSAGE);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Connection to Server Failed", "", JOptionPane.ERROR_MESSAGE);
			return;
		}

		SupervisorInterface supervisorInterface = hr.getSupervisorInterface();
		
		
		
		final Supervisor supervisor = new Supervisor(db, supervisorInterface);
		
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
						
					
					JFrame frame;
					frame = new JFrame("Supervisor");
					frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					frame.getContentPane().add(supervisor.getPanel());					
					frame.pack();
					frame.setVisible(true);
					frame.setPreferredSize(new Dimension(800, 700));
										
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
}
