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
import graphics.rmi.JGDPanelPop;
import java.awt.BorderLayout;
import java.rmi.Naming;
import javax.swing.JFrame;
import javax.swing.JPanel;
import remoting.RServices;
import org.kchine.rpf.YesSecurityManager;
import org.kchine.rpf.db.monitor.ConsoleDialog;
import org.kchine.rpf.db.monitor.ServantStatus;

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