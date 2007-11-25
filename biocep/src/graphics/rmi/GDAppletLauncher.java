/*
 * Copyright (C) 2007 EMBL-EBI
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
package graphics.rmi;

import http.LocalHelpServlet;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import remoting.RServices;
import server.DirectJNI;
import splash.SplashWindow;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class GDAppletLauncher {

	static public void createDesktopApplication() {

		try {

			final HashMap<String, String> params = new HashMap<String, String>();
			params.put("command_servlet_url", System.getProperty("url"));
			params.put("autologon", System.getProperty("autologon"));
			params.put("demo", System.getProperty("demo"));

			if (System.getProperty("login") != null && !System.getProperty("login").equals("")) {
				params.put("login", System.getProperty("login"));
			} else {
				params.put("login", System.getProperty("user.name"));
			}

			params.put("save", System.getProperty("save"));
			params.put("mode", System.getProperty("mode"));
			params.put("lf", System.getProperty("lf"));
			System.out.println("params=" + params);

			final GDApplet gDApplet = new GDApplet(params);
			gDApplet.init();

			if (gDApplet.getMode() == GDApplet.LOCAL_MODE) {
				DirectJNI.getInstance();
				if (System.getProperty("preprocess.help") != null
						&& System.getProperty("preprocess.help").equalsIgnoreCase("true")) {
					new Thread(new Runnable() {
						public void run() {
							DirectJNI.getInstance().preprocessHelp();
						}
					}).start();
				}

				if (System.getProperty("apply.sandbox") != null
						&& System.getProperty("apply.sandbox").equalsIgnoreCase("true")) {
					DirectJNI.getInstance().applySandbox();
				}

			}

			if (gDApplet.getMode() == GDApplet.LOCAL_MODE || gDApplet.getMode() == GDApplet.RMI_MODE) {

				new Thread(new Runnable() {
					public void run() {
						final Acme.Serve.Serve srv = new Acme.Serve.Serve() {
							public void setMappingTable(PathTreeDictionary mappingtable) {
								super.setMappingTable(mappingtable);
							}
						};
						java.util.Properties properties = new java.util.Properties();
						properties.put("port", Integer.decode(System.getProperty("localtomcat.port")));
						properties.setProperty(Acme.Serve.Serve.ARG_NOHUP, "nohup");
						srv.arguments = properties;

						System.out.println("properties:" + properties + "  server: " + srv);

						RServices r = null;
						if (gDApplet.getMode() == GDApplet.LOCAL_MODE) {
							r = DirectJNI.getInstance().getRServices();
						} else if (System.getProperty("stub") != null && !System.getProperty("stub").equals("")) {
							r = (RServices) PoolUtils.hexToStub(System.getProperty("stub"), GDApplet.class
									.getClassLoader());
						} else {
							try {
								r = (RServices) PoolUtils.getRmiRegistry().lookup(System.getProperty("name"));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						srv.addServlet("/helpme/", new LocalHelpServlet(r));

						Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
							public void run() {
								try {
									srv.notifyStop();
								} catch (java.io.IOException ioe) {
									ioe.printStackTrace();
								}
								srv.destroyAllServlets();
							}
						}));
						srv.serve();
					}
				}).start();
			}

			try {
				UIManager.setLookAndFeel(gDApplet.getLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			JFrame fconsole = new JFrame();

			fconsole.getContentPane().setLayout(new BorderLayout());
			fconsole.getContentPane().add(gDApplet.getContentPane(), BorderLayout.CENTER);

			fconsole.setPreferredSize(new Dimension(840, 720));

			fconsole.addWindowListener(new WindowListener() {

				public void windowActivated(WindowEvent e) {
				}

				public void windowClosed(WindowEvent e) {
					try {
						gDApplet.destroy();
						System.exit(0);
					} catch (Exception re) {
						re.printStackTrace();
					}
				}

				public void windowClosing(WindowEvent e) {
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
			fconsole.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			fconsole.pack();
			fconsole.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {

		SplashWindow.splash(Toolkit.getDefaultToolkit().createImage(
				GDAppletLauncher.class.getResource("/splash/splashscreen.png")));

		try {
			Thread.sleep(500);
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				try {
					createDesktopApplication();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		SplashWindow.disposeSplash();

	}
}
