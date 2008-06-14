package views;

import freemind.main.FreeMindApplet;
import graphics.rmi.GDApplet;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JPanel;

import server.LocalHttpServer;

public class BiocepMindMapView extends DynamicView {
	FreeMindApplet _freeMindApplet;

	public BiocepMindMapView(String title, Icon icon, int id) {
		super(title, icon, new JPanel(), id);
		((JPanel) getComponent()).setLayout(new BorderLayout());

		final HashMap<String, String> params = new HashMap<String, String>();
		params.put("modes", "freemind.modes.browsemode.BrowseMode");
		params.put("initial_mode", "Browse");
		params.put("selection_method", "selection_method_direct");

		if (GDApplet.class.getResource("/Biocep.mm") != null) {
			params.put("browsemode_initial_map", "http://127.0.0.1:" + LocalHttpServer.getLocalHttpServerPort() + "/classes/Biocep.mm");
		} else {
			params.put("browsemode_initial_map", "http://biocep-distrib.r-forge.r-project.org/Biocep.mm");
		}
		_freeMindApplet = new FreeMindApplet(params);
		_freeMindApplet.init();

		((JPanel) getComponent()).add(_freeMindApplet, BorderLayout.CENTER);
	}

	public FreeMindApplet getFreeMindApplet() {
		return _freeMindApplet;
	}
}

