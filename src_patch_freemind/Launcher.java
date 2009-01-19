/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
import freemind.main.FreeMindApplet;
import graphics.rmi.GDApplet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.kchine.r.server.http.LocalHttpServer;




public class Launcher {

	
	static FreeMindApplet _freeMindApplet=null;
	static public void createDesktopApplication() {
		
		
		System.out.println();
		System.out.println(LocalHttpServer.getLocalHttpServerPort());

		try {

			
			
			final HashMap<String, String> params = new HashMap<String, String>();
			params.put("modes", "freemind.modes.browsemode.BrowseMode");			
			params.put("initial_mode" ,"Browse");
			params.put("selection_method","selection_method_direct");

			if (GDApplet.class.getResource("/Biocep.mm")!=null) {
				params.put("browsemode_initial_map", "http://127.0.0.1:"+LocalHttpServer.getLocalHttpServerPort()+"/classes/Biocep.mm");
			} else {
				params.put("browsemode_initial_map", "http://biocep-distrib.r-forge.r-project.org/Biocep.mm");
			}						
			_freeMindApplet = new FreeMindApplet(params);
			_freeMindApplet.init();

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					_freeMindApplet.destroy();
				}
			}));


			JFrame mainframe = new JFrame();
			mainframe.getContentPane().setLayout(new BorderLayout());
			mainframe.getContentPane().add(_freeMindApplet.getContentPane(), BorderLayout.CENTER);
			mainframe.setPreferredSize(new Dimension(400, 400));
			mainframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			mainframe.pack();
			mainframe.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) throws Exception {
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					createDesktopApplication();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}
