import freemind.main.FreeMindApplet;
import graphics.rmi.GDApplet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.HashMap;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import server.LocalHttpServer;



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
