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
import graphics.pop.GDDevice;
import graphics.rmi.JGDPanelPop;
import graphics.rmi.dialogs.GetExprDialog;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JPanel;
import remoting.RServices;
import server.DirectJNI;
import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class DirectGraphics {

	public static void main(String[] args) throws Exception {

		
		
		DirectJNI.init();
		final RServices r = DirectJNI.getInstance().getRServices();
		

		GDDevice d1 = r.newDevice(400, 500);
		JPanel panel1 = new JGDPanelPop(d1);


		JFrame f1 = new JFrame();
		f1.getContentPane().setLayout(new BorderLayout());
		f1.getContentPane().add(panel1, BorderLayout.CENTER);
		panel1.repaint();
		f1.pack();
		f1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f1.setVisible(true);

		r.consoleSubmit("demo(graphics)");
		
		System.out.println(r.consoleSubmit("9+8"));

	}

}
