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
import java.io.RandomAccessFile;
import java.rmi.RemoteException;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import remoting.RCallBack;
import remoting.RServices;
import graphics.pop.GDDevice;
import graphics.rmi.JGDPanelPop;
import http.HttpMarker;
import http.RHttpProxy;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class HttpR {

	static String sessionId;
	static String cmdUrl = "http://127.0.0.1:8080/rvirtual/cmd";
	static RServices r;

	public static void main(String[] args) throws Throwable {

		try {

			HashMap<String, Object> options = new HashMap<String, Object>();
			//options.put("nopool", "false");
			//options.put("poolname", "R2");
			options.put("memorymin", "256");
			options.put("memorymax", "256");
			options.put("privatename", "toto");
			sessionId = RHttpProxy.logOn(cmdUrl, "", "guest", "guest", options);
			r = RHttpProxy.getR(cmdUrl, sessionId, false, 50);
			r.consoleSubmit("ls()");
			System.out.println(r.getStatus());
			r.consoleSubmit("library(vsn);data(kidney);justvsn(kidney);x=45");
			r.consoleSubmit("x");
			System.out.println(r.getStatus());

		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			RHttpProxy.logOff(cmdUrl, sessionId);
		}
		
		System.exit(0);
		

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

			public void run() {
				System.out.println("Shutdown Hook Called");
				try {
					((HttpMarker) r).stopThreads();
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

		r.addRCallback(new RCallBack() {
			public void notify(HashMap<String, String> parameters) throws RemoteException {
				System.out.println("@@@" + parameters);
			}
		});

		r.evaluate("democallback<-function() { .PrivateEnv$notifyJavaListeners('percentageDone=0.1');"
				+ ".PrivateEnv$notifyJavaListeners('percentageDone=0.2');" + ""
				+ ".PrivateEnv$notifyJavaListeners('percentageDone=0.5'); .PrivateEnv$notifyJavaListeners('percentageDone=1');}");
		System.out.println("***" + r.evaluate("democallback()"));

		byte[] pdf = r.getPdf("plot(rnorm(66))", 500, 500);
		RandomAccessFile raf = new RandomAccessFile("a0.pdf", "rw");
		raf.setLength(0);
		raf.write(pdf);
		raf.close();

		final GDDevice d = r.newDevice(300, 300);

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					JGDPanelPop panel = new JGDPanelPop(d, true, true, null);
					JFrame f = new JFrame();
					f.getContentPane().setLayout(new BorderLayout());
					f.getContentPane().add(panel, BorderLayout.CENTER);
					f.pack();
					f.setVisible(true);
					f.setSize(300, 300);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}
}
