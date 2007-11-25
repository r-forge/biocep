/*
 * Copyright (C) 2007 EMBL-EBI
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
import graphics.pop.GDDevice;
import graphics.rmi.JGDPanelPop;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JPanel;

import remoting.RServices;
import server.DirectJNI;
import uk.ac.ebi.microarray.pools.RemotePanel;
import uk.ac.ebi.microarray.pools.db.monitor.ConsoleDialog;
import uk.ac.ebi.microarray.pools.db.monitor.ServantStatus;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class DirectGraphics {

	public static void main(String[] args) throws Exception {
		DirectJNI.init();
		RServices r = DirectJNI.getInstance().getRServices();

		JPanel panel = new JGDPanelPop(r.newDevice(400, 500));
		//RemotePanel panel=r.getPanel(450, 600);
		//panel.init();

		JFrame f = new JFrame();
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(panel, BorderLayout.CENTER);
		panel.repaint();
		f.pack();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
