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
package uk.ac.ebi.microarray.pools.db.monitor;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import uk.ac.ebi.microarray.pools.db.DBLayer;
import uk.ac.ebi.microarray.pools.db.PoolDataDB;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class PoolDialog extends JDialog {

	public static String _poolName = "";
	public static String _timeOut = "0";
	public static String _prefix = "";

	private JTextField poolName;
	private JTextField timeOut;
	private JTextField prefix;

	boolean isOk = false;

	public PoolDataDB getPoolInfo() {

		if (isOk)
			return new PoolDataDB(_poolName, DBLayer.getPrefixes(_prefix), Integer.decode(_timeOut));
		else
			return null;
	}

	public PoolDialog(Frame aFrame, boolean edit) {
		super(aFrame, true);

		setTitle("Add Pool");
		setLocationRelativeTo(aFrame);
		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		p1.add(new JLabel("  Pool Name"));
		p1.add(new JLabel("  Prefixes"));
		p1.add(new JLabel("  Timeout"));
		p1.add(new JLabel(" "));

		poolName = new JTextField();
		poolName.setText(_poolName);
		if (edit)
			poolName.setEnabled(false);

		timeOut = new JTextField();
		timeOut.setText(new Integer(_timeOut).toString());

		prefix = new JTextField();
		prefix.setText(_prefix);

		p2.add(poolName);
		p2.add(prefix);
		p2.add(timeOut);
		p2.add(new JLabel(" "));

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okMethod();
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelMethod();
			}
		});

		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					okMethod();
				} else if (e.getKeyCode() == 27) {
					cancelMethod();
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		};

		poolName.addKeyListener(keyListener);
		prefix.addKeyListener(keyListener);
		timeOut.addKeyListener(keyListener);

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(450, 230));

	}

	private void okMethod() {
		_poolName = (poolName.getText());
		_prefix = (prefix.getText());
		_timeOut = (timeOut.getText());
		isOk = true;
		PoolDialog.this.setVisible(false);
	}

	private void cancelMethod() {
		_poolName = (poolName.getText());
		_prefix = (prefix.getText());
		_timeOut = (timeOut.getText());
		isOk = false;
		PoolDialog.this.setVisible(false);
	}

}
