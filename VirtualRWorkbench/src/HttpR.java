/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
 * Copyright (C) 2007 - 2008  Karim Chine
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.db.monitor.ConsoleDialog;
import uk.ac.ebi.microarray.pools.db.monitor.ServantStatus;
import graphics.pop.GDDevice;
import graphics.rmi.JGDPanelPop;
import http.RHttpProxy;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class HttpR {

	public static void main(String[] args) throws Throwable {

		//final String cmdUrl = System.getProperty("url");
		final String cmdUrl = "http://127.0.0.1:8080/rv/cmd";
		HashMap<String, Object> options = new HashMap<String, Object>();
		options.put("privatename", "titi");
		final String sessionId = RHttpProxy.logOn(cmdUrl, "", "test", "test", options);
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			public void run() {
				System.out.println("Shutdown Hook Called");
				try {
					RHttpProxy.logOff(cmdUrl, sessionId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));

		RServices r = (RServices) RHttpProxy.getDynamicProxy(cmdUrl, sessionId, "R", new Class<?>[] { RServices.class }, new HttpClient(new MultiThreadedHttpConnectionManager()));

		System.out.println(r.consoleSubmit("x=88"));
		
		System.exit(0);
		
		
		
		GDDevice d = RHttpProxy.newDevice(cmdUrl, sessionId, 100, 100);

		JPanel panel = new JGDPanelPop(d);

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(panel, BorderLayout.CENTER);
		panel.repaint();
		f.pack();
		f.setVisible(true);
		f.setSize(300, 300);
		// r.evaluate("hist(rnorm(451))");

		ConsoleDialog dialog = new ConsoleDialog(null, r, new ServantStatus() {
			public boolean isLocked() {
				return true;
			}
		});
		dialog.setVisible(true);

		dialog.addWindowListener(new WindowListener() {
			public void windowActivated(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
				System.exit(0);
			}

			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
			}
		});

	}
}
