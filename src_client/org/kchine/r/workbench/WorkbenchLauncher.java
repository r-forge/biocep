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
package org.kchine.r.workbench;

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
// org.kchine.r.workbench.WorkbenchLauncher
public class WorkbenchLauncher {

	static public WorkbenchApplet createApplet() {

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
		
		if (System.getProperty("password") != null && !System.getProperty("password").equals("")) {
			params.put("password", System.getProperty("password"));
		} else {
			params.put("password", "guest");
		}

		params.put("save", System.getProperty("save"));
		params.put("mode", System.getProperty("mode"));
		params.put("rmi_mode", System.getProperty("rmi_mode"));
		params.put("lf", System.getProperty("lf"));
		params.put("stub", System.getProperty("stub"));
		params.put("name", System.getProperty("name"));
		params.put("registry_host", System.getProperty("registry_host"));
		params.put("registry_port", System.getProperty("registry_port"));
		params.put("url", System.getProperty("url"));
		params.put("privatename", System.getProperty("privatename"));
		params.put("noconfirmation", System.getProperty("noconfirmation"));
		params.put("gui_url", System.getProperty("gui_url"));
		params.put("gui_name", System.getProperty("gui_name"));
		params.put("gui_selector", System.getProperty("gui_selector"));
		
		params.put("proxy_host", System.getProperty("proxy_host"));
		params.put("proxy_port", System.getProperty("proxy_port"));
		params.put("use_embedded_r", System.getProperty("use_embedded_r"));
		params.put("gui_no_r", System.getProperty("gui_no_r"));
		params.put("gui_no_workbench", System.getProperty("gui_no_workbench"));
		
		if (System.getProperty("application_type")!=null && !System.getProperty("application_type").equals("")) {
			params.put("application_type", System.getProperty("application_type"));
		} else {
			params.put("application_type", "standard");
		}
		
		params.put("javaws", System.getProperty("javaws"));
		params.put("selfish", System.getProperty("selfish"));
		params.put("preprocess.help", System.getProperty("preprocess.help"));

		System.out.println("params=" + params);
		final WorkbenchApplet gDApplet = new WorkbenchApplet(params);
		gDApplet.init();

		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				gDApplet.destroy();
			}
		}));

		return gDApplet;

	}

	public static void main(String[] args) throws Exception {

		SplashWindow.splash(Toolkit.getDefaultToolkit().createImage(SplashWindow.getSplashPng()));
		try {
			Thread.sleep(200);
		} catch (Exception e) {
			e.printStackTrace();
		}

		final WorkbenchApplet gDApplet = createApplet();

		if (System.getProperty("gui_no_workbench")==null || System.getProperty("gui_no_workbench").equals("") || ! new Boolean(System.getProperty("gui_no_workbench"))) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
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
	
				}
			});
		}

		SplashWindow.disposeSplash();
	}
}
