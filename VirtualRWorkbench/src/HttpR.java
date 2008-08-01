/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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
import java.io.File;
import java.io.RandomAccessFile;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.bioconductor.packages.biobase.ExpressionSet;

import remoting.RCallBack;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.db.DBLayer;
import uk.ac.ebi.microarray.pools.db.DBLayerInterface;
import uk.ac.ebi.microarray.pools.db.monitor.ConsoleDialog;
import uk.ac.ebi.microarray.pools.db.monitor.ServantStatus;
import graphics.pop.GDDevice;
import graphics.rmi.JGDPanelPop;
import http.HttpMarker;
import http.RHttpProxy;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class HttpR {

	public static void main(String[] args) throws Throwable {

		{
			final String cmdUrl = "http://127.0.0.1:8080/rvirtual/cmd";
			HashMap<String, Object> options = new HashMap<String, Object>();
			final String sessionId = RHttpProxy.logOnDB(cmdUrl, "", "guest", "guest", options);			
			final DBLayerInterface db = (DBLayerInterface)RHttpProxy.getDynamicProxy(cmdUrl, sessionId, "REGISTRY", new Class<?>[]{DBLayerInterface.class}, new HttpClient(new MultiThreadedHttpConnectionManager()));
			System.out.println(Arrays.toString(db.list()));
			System.exit(0);
		}

		
		
		
		//final String cmdUrl = System.getProperty("url");
		final String cmdUrl = "http://127.0.0.1:8080/rvirtual/cmd";
		HashMap<String, Object> options = new HashMap<String, Object>();
		options.put("privatename", "tata");
		options.put("urls", new URL[]{new URL("http://127.0.0.1:8080/rws/mapping/classes/")});
		final String sessionId = RHttpProxy.logOn(cmdUrl, "", "guest", "guest", options);
		final RServices r = RHttpProxy.getR(cmdUrl, sessionId,true);
		byte[] pdf=r.getPdf("plot(rnorm(66))", 500, 500);
		RandomAccessFile raf=new RandomAccessFile("c:/a0.pdf","rw");
		raf.setLength(0);
		raf.write(pdf);
		raf.close();
		
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			public void run() {
				System.out.println("Shutdown Hook Called");
				try {
					((HttpMarker)r).stopThreads();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					RHttpProxy.logOff(cmdUrl, sessionId);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}));

		
		
		r.addRCallback(new RCallBack(){
			public void notify(HashMap<String, String> parameters) throws RemoteException {
				System.out.println("@@@"+parameters);				
			}
		});
		
		
		
		r.evaluate("democallback<-function() { .PrivateEnv$notifyJavaListeners('percentageDone=0.1');"+
				".PrivateEnv$notifyJavaListeners('percentageDone=0.2');"+"" +
				".PrivateEnv$notifyJavaListeners('percentageDone=0.5'); .PrivateEnv$notifyJavaListeners('percentageDone=1');}");
		System.out.println("***" + r.evaluate("democallback()"));

		Thread.sleep(100);
		System.exit(0);
		
		GDDevice d = r.newDevice(100, 100);

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
