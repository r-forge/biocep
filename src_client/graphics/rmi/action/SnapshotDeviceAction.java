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
import graphics.rmi.RGui;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class SnapshotDeviceAction extends AbstractAction {
	RGui _rgui;

	public SnapshotDeviceAction(RGui rgui) {
		super("Create Snapshot");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {

								JBufferedImagePanel bufferedImagePanel = null;

								bufferedImagePanel = (JBufferedImagePanel) GDApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);

								final JBufferedImagePanel panelclone = new JBufferedImagePanel(bufferedImagePanel.getImage());

								final AbstractAction[] actions = new AbstractAction[] { new SnapshotDeviceAction(_rgui), new SaveDeviceAsPngAction(_rgui),
										new SaveDeviceAsJpgAction(_rgui) };

								panelclone.addMouseListener(new MouseAdapter() {
									public void mousePressed(MouseEvent e) {
										checkPopup(e);
									}

									public void mouseClicked(MouseEvent e) {
										checkPopup(e);
									}

									public void mouseReleased(MouseEvent e) {
										checkPopup(e);
									}

									private void checkPopup(MouseEvent e) {
										if (e.isPopupTrigger() && actions != null) {
											JPopupMenu popupMenu = new JPopupMenu();
											for (int i = 0; i < actions.length; ++i) {
												popupMenu.add(actions[i]);
											}
											popupMenu.show(panelclone, e.getX(), e.getY());
										}
									}
								});

								_rgui.createView(new JScrollPane(panelclone), "Snapshot");

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
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
