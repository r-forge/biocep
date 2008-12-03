/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
package graphics.rmi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.kchine.r.workbench.splashscreen.SplashWindow;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GDAppletLauncher {

	static public void createDesktopApplication() {

		try {

			final HashMap<String, String> params = new HashMap<String, String>();
			
			
			params.put("url", System.getProperty("url"));
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
			params.put("registry.host", System.getProperty("registry.host"));
			params.put("registry.port", System.getProperty("registry.port"));
			params.put("url", System.getProperty("url"));			
			params.put("privatename", System.getProperty("privatename"));
			params.put("noconfirmation", System.getProperty("noconfirmation"));
			params.put("desktopapplication", "true");
			params.put("selfish", System.getProperty("selfish"));
			
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
				
		SplashWindow.splash(Toolkit.getDefaultToolkit().createImage(SplashWindow.getSplashPng()));
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
