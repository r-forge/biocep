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
package graphics.rmi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import splash.SplashWindow;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
				params.put("login", "guest");
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

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					gDApplet.destroy();
				}
			}));

			try {
				UIManager.setLookAndFeel(gDApplet.getLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			JFrame mainframe = new JFrame();
			mainframe.getContentPane().setLayout(new BorderLayout());
			mainframe.getContentPane().add(gDApplet.getContentPane(), BorderLayout.CENTER);
			mainframe.setPreferredSize(new Dimension(840, 720));
			mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			mainframe.pack();
			mainframe.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		SplashWindow.splash(Toolkit.getDefaultToolkit().createImage(GDAppletLauncher.class.getResource("/splash/splashscreen.png")));
		try {
			Thread.sleep(200);
		} catch (Exception e) {
			e.printStackTrace();
		}
		SwingUtilities.invokeLater(new Runnable() {
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
