/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
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
package org.kchine.rpf.db.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.gui.ConsolePanel;
import org.kchine.rpf.gui.SubmitInterface;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ConsoleDialog extends JFrame {

	private ServantStatus _servantStatus;
	private ConsolePanel cp;

	public ConsolePanel getCP() {
		return cp;
	}
	
	public ConsoleDialog(Frame aFrame, final ManagedServant servant, ServantStatus servantStatus) {

		_servantStatus = servantStatus;

		try {
			setTitle("Console For Servant <" + servant.getServantName() + "> ");
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		setLocationRelativeTo(aFrame);
		setLocation(PoolUtils.deriveLocation(getLocation(), 50));
		getContentPane().setLayout(new BorderLayout());
		cp=new ConsolePanel(new SubmitInterface() {
			public String submit(String cmd) {

				if (!_servantStatus.isLocked()) {
					return "You should lock the servant before using the console\n";
				}

				String log = null;
				try {
					if (cmd.equalsIgnoreCase("open.device")) {
						if (servant.hasGraphicMode()) {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									try {
										new DeviceDialog(ConsoleDialog.this, servant).setVisible(true);
									} catch (RemoteException re) {
										re.printStackTrace();
									}
								}
							});
							log = "device opened\n";
						} else {
							log = "no graphics mode available\n";
						}
					} else {
						log = servant.consoleSubmit(cmd);
					}
				} catch (Exception e) {
					log = PoolUtils.getStackTraceAsString(e);
				}

				return log;

			}
		},"Evaluate", Color.black,true,null);
		getContentPane().add(cp);

		setSize(new Dimension(540, 430));

	}

}
