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
package org.kchine.rpf.db.monitor;

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

import org.kchine.rpf.db.monitor.SupervisorUtils.Identification;


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
