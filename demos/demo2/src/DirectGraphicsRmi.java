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
import graphics.rmi.JGDPanelPop;

import java.awt.BorderLayout;
import java.rmi.Naming;
import javax.swing.JFrame;
import javax.swing.JPanel;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.YesSecurityManager;
import uk.ac.ebi.microarray.pools.db.monitor.ConsoleDialog;
import uk.ac.ebi.microarray.pools.db.monitor.ServantStatus;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DirectGraphicsRmi {
	public static void main(String[] args) throws Exception {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}
		final RServices r = ((RServices) Naming.lookup("RSERVANT_1"));

		JPanel panel = new JGDPanelPop(r.newDevice(400, 500));
		//RemotePanel panel=r.getPanel(450, 600);
		//panel.init();

		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(panel, BorderLayout.CENTER);
		panel.repaint();
		f.pack();
		f.setVisible(true);

		r.evaluate("hist(rnorm(451))");

		ConsoleDialog console = new ConsoleDialog(null, r, new ServantStatus() {
			public boolean isLocked() {
				return true;
			}
		});
		console.setVisible(true);
		console.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}