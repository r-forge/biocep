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
 * @author Karim Chine   kchine@ebi.ac.uk
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
