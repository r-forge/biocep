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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.graphics.JBufferedImagePanel;
import org.kchine.r.workbench.graphics.JGDPanelPop;

import java.awt.Component;
import java.io.RandomAccessFile;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SaveDeviceAsOdgAction extends AbstractAction {

	RGui _rgui;

	public SaveDeviceAsOdgAction(RGui rgui) {
		super("Save as ODG");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		if (_rgui.getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}
		final JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Save Graphics as ODG");
		int returnVal = chooser.showSaveDialog(_rgui.getRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			new Thread(new Runnable() {
				public void run() {
					try {
						_rgui.getRLock().lock();
						JGDPanelPop panel = (JGDPanelPop) WorkbenchApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);
						byte[] result = panel.getGdDevice().getOdg();
						RandomAccessFile raf = new RandomAccessFile(org.kchine.rpf.PoolUtils.fixExtension(chooser.getSelectedFile(), "odg"), "rw");
						raf.setLength(0);
						raf.write(result);
						raf.close();

						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								JOptionPane.showMessageDialog(_rgui.getRootComponent(), org.kchine.rpf.PoolUtils.fixExtension(chooser.getSelectedFile(), "odg")
										.getAbsolutePath()
										+ " created successfully");
							}
						});

					} catch (Exception ex) {

						ex.printStackTrace();
						try {
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									JOptionPane.showMessageDialog(_rgui.getRootComponent(), "Couldn't generate ODG, check that "
											+ "\n1. You have installed the ooconvert extension (requires Java 6)" + "\n2. You have installed open office 3 "
											+ "\n3. soffice is in your system path (accessible from your command line)");
								}
							});
						} catch (Exception e) {

						}
					} finally {
						_rgui.getRLock().unlock();
					}

				}
			}).start();

		}
	}

	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
