/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2007 - 2008  Karim Chine
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.microarray.pools.db.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import javax.swing.JFrame;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemotePanel;

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
