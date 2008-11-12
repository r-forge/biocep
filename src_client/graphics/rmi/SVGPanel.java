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
package graphics.rmi;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgent;

public class SVGPanel extends JSVGCanvas {

	public SVGPanel() {
	}

	public SVGPanel(SVGUserAgent ua, boolean eventsEnabled, boolean selectableText) {
		super(ua, eventsEnabled, selectableText);
	}

	public void setSVGContent(Vector<String> v) {
		try {
			final String tempFile = System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg";
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			for (int i = 0; i < v.size(); ++i)
				pw.println(v.elementAt(i));
			pw.close();
			setURI(new File(tempFile).toURI().toURL().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setSVGContent(String[] v) {
		try {
			final String tempFile = System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg";
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			for (int i = 0; i < v.length; ++i)
				pw.println(v[i]);
			pw.close();
			setURI(new File(tempFile).toURI().toURL().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
