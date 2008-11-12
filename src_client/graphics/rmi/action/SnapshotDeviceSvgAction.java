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
package graphics.rmi.action;

import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.apache.batik.swing.JSVGCanvas;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SnapshotDeviceSvgAction extends AbstractAction {
	RGui _rgui;

	public SnapshotDeviceSvgAction(RGui rgui) {
		super("Create SVG Snapshot");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		if (_rgui.getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}
		new Thread(new Runnable() {
			public void run() {

				try {
					_rgui.getRLock().lock();

					JGDPanelPop panel = (JGDPanelPop) GDApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);

					Vector<String> result = panel.getGdDevice().getSVGAsText();
					final String tempFile = System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg";
					PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
					for (int i = 0; i < result.size(); ++i)
						pw.println(result.elementAt(i));
					pw.close();

					final JSVGCanvas svgCanvas = new JSVGCanvas();
					svgCanvas.setEnableZoomInteractor(true);

					_rgui.createView(svgCanvas, "SVG Snapshot");

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								svgCanvas.setURI(new File(tempFile).toURI().toURL().toString());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					_rgui.getRLock().unlock();
				}

			}
		}).start();

	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}
}
