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
package org.kchine.r.workbench.views;

import freemind.main.FreeMindApplet;
import graphics.rmi.GDApplet;

import java.awt.BorderLayout;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.kchine.r.server.LocalHttpServer;


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

