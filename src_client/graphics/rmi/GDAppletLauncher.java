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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

//import splash.SplashWindow;
import splash.SplashWindow;

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
			params.put("debug", System.getProperty("debug"));
			if (System.getProperty("login") != null && !System.getProperty("login").equals("")) {
				params.put("login", System.getProperty("login"));
			} else {
				params.put("login", System.getProperty("user.name"));
			}
			params.put("save", System.getProperty("save"));
			params.put("mode", System.getProperty("mode"));
			params.put("lf", System.getProperty("lf"));
			params.put("stub", System.getProperty("stub"));
			params.put("name", System.getProperty("name"));
			params.put("registryhost", System.getProperty("registryhost"));
			params.put("registryport", System.getProperty("registryport"));
			System.out.println("params=" + params);
			final GDApplet gDApplet = new GDApplet(params);
			gDApplet.init();
			
			Runtime.getRuntime().addShutdownHook(new Thread( new Runnable() {
				public void run() {
					gDApplet.destroy();
				}
			}));
			gDApplet.destroy();
			
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
					System.exit(0);
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
		SplashWindow.splash(Toolkit.getDefaultToolkit().createImage(GDAppletLauncher.class.getResource("/splash/splashscreen.png")));
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
