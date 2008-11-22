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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import javax.swing.JFrame;

import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RemotePanel;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DeviceDialog extends JFrame {

	RemotePanel _remotePanel = null;

	public DeviceDialog(Frame aFrame, final ManagedServant servant) throws RemoteException {

		setTitle("Servant <" + servant.getServantName() + "> Graphics Device");
		setLocationRelativeTo(aFrame);
		setLocation(PoolUtils.deriveLocation(getLocation(), 50));

		getContentPane().setLayout(new BorderLayout());
		_remotePanel = servant.getPanel(500, 400);
		_remotePanel.init();
		setSize(new Dimension(500, 400));
		getContentPane().add(_remotePanel);
		addWindowListener(new WindowListener() {

			public void windowActivated(WindowEvent e) {
			}

			public void windowClosed(WindowEvent e) {
				try {
					System.out.println("disposing");
					_remotePanel.dispose();
				} catch (RemoteException re) {
					re.printStackTrace();
				}
			}

			public void windowClosing(WindowEvent e) {
			}

			public void windowDeactivated(WindowEvent e) {
			}

			public void windowDeiconified(WindowEvent e) {
			}

			public void windowIconified(WindowEvent e) {
			}

			public void windowOpened(WindowEvent e) {
			}

		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
