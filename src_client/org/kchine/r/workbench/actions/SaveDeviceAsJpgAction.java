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
package org.kchine.r.workbench.actions;


import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.graphics.JBufferedImagePanel;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SaveDeviceAsJpgAction extends AbstractAction {

	RGui _rgui;

	public SaveDeviceAsJpgAction(RGui rgui) {
		super("Save as JPG");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				try {

					final JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showOpenDialog(_rgui.getRootComponent());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						JBufferedImagePanel bufferedImagePanel = (JBufferedImagePanel) WorkbenchApplet.getComponentParent((Component) e.getSource(),
								JBufferedImagePanel.class);
						ImageIO.write(bufferedImagePanel.getImage(), "jpg", chooser.getSelectedFile());
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}).start();

	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
