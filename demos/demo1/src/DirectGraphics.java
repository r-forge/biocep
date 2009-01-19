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
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.RServices;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.workbench.graphics.JGDPanelPop;
import org.kchine.rpf.gui.ConsolePanel;
import org.kchine.rpf.gui.SubmitInterface;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DirectGraphics {

	public static void main(String[] args) throws Exception {
		DirectJNI.init();
		final RServices r = DirectJNI.getInstance().getRServices();

		GDDevice d1 = r.newDevice(400, 500);
		JPanel panel1 = new JGDPanelPop(d1);

		GDDevice d2 = r.newDevice(400, 500);
		JPanel panel2 = new JGDPanelPop(d2);

		//RemotePanel panel=r.getPanel(450, 600);
		//panel.init();

		JFrame f1 = new JFrame();
		f1.getContentPane().setLayout(new BorderLayout());
		f1.getContentPane().add(panel1, BorderLayout.CENTER);
		panel1.repaint();
		f1.pack();
		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f1.setVisible(true);

		JFrame f2 = new JFrame();
		f2.getContentPane().setLayout(new BorderLayout());
		f2.getContentPane().add(panel2, BorderLayout.CENTER);
		panel2.repaint();
		f2.pack();
		f2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f2.setVisible(true);

		r.evaluate("hist(rnorm(451))", 1);

		ConsolePanel console = new ConsolePanel(new SubmitInterface() {
			public String submit(String expression) {
				String result = null;

				try {
					result = r.consoleSubmit(expression);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return result;
			}
		}, "Evaluate",Color.black,true,null);

		JFrame fconsole = new JFrame();
		fconsole.getContentPane().setLayout(new BorderLayout());
		fconsole.getContentPane().add(console, BorderLayout.CENTER);
		fconsole.pack();
		fconsole.setSize(400, 400);
		fconsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fconsole.setVisible(true);

	}

}
