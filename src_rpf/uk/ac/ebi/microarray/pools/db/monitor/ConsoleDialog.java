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
import java.rmi.RemoteException;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ConsoleDialog extends JFrame {

	private ServantStatus _servantStatus;

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
		getContentPane().add(new ConsolePanel(new SubmitInterface() {
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
		},"Evaluate",true,null));

		setSize(new Dimension(540, 430));

	}

}
