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
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.graphics.JBufferedImagePanel;
import org.kchine.r.workbench.graphics.JGDPanelPop;
import org.kchine.r.workbench.views.PDFPanel;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SnapshotDevicePdfAction extends AbstractAction {
	RGui _rgui;

	public SnapshotDevicePdfAction(RGui rgui) {
		super("Create PDF Snapshot");
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

					final JGDPanelPop panel = (JGDPanelPop) WorkbenchApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);
					final PDFPanel pdfPanel=new PDFPanel();					
					_rgui.createView(pdfPanel, "PDF Snapshot");
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								pdfPanel.setPDFContent(panel.getGdDevice().getPdf());
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
