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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import uk.ac.ebi.microarray.pools.db.monitor.SupervisorUtils.Identification;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class LoginDialog extends JDialog {

	private String _login_str = null;
	private String _pwd_str = null;

	public Identification getIndentification() {
		if (_login_str == null && _pwd_str == null)
			return null;
		else
			return new Identification(_login_str, _pwd_str);
	}

	public LoginDialog(Frame aFrame) {
		super(aFrame, true);

		setTitle("Enter Login");
		setLocationRelativeTo(aFrame);

		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		p1.add(new JLabel("  Login"));
		p1.add(new JLabel("  Password"));
		p1.add(new JLabel(""));

		final JTextField login = new JTextField();
		final JPasswordField pwd = new JPasswordField();
		p2.add(login);
		p2.add(pwd);
		p2.add(new JLabel(""));

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				_login_str = login.getText();
				_pwd_str = new String(pwd.getPassword());
				LoginDialog.this.setVisible(false);
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				LoginDialog.this.setVisible(false);
			}
		});

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(300, 130));

	}

}
