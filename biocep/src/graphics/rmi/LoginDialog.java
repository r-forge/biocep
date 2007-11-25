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
package graphics.rmi;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class LoginDialog extends JDialog {

	static String login_str = "guest";
	static String pwd_str = "guest";
	static boolean persistentWorkspace_bool = false;
	static boolean nopool_bool = false;
	static boolean waitForResource_bool = false;
	static boolean playDemo_bool = false;

	private boolean _closedOnOK = false;
	private JCheckBox _persistentWorkspaceCheckBox;
	private JCheckBox _nopoolCheckBox;
	private JCheckBox _waitForResourceBox;
	private JCheckBox _playDemoBox;
	private JTextField _login;
	private JPasswordField _pwd;
	private JButton _ok;
	private JButton _cancel;

	public Identification getIndentification() {
		if (_closedOnOK)
			return new Identification(login_str, pwd_str, persistentWorkspace_bool, nopool_bool, waitForResource_bool,
					playDemo_bool);
		else
			return null;
	}

	public LoginDialog(Component c, int mode) {
		super((Frame) null, true);

		setTitle("Please enter your login and password");

		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		_login = new JTextField(login_str);
		_pwd = new JPasswordField(pwd_str);
		_persistentWorkspaceCheckBox = new JCheckBox("Persistent Workspace", persistentWorkspace_bool);
		_nopoolCheckBox = new JCheckBox("Create Private R", nopool_bool);
		_waitForResourceBox = new JCheckBox("Wait Until R Resource Available", waitForResource_bool);
		_playDemoBox = new JCheckBox("Play Demo", playDemo_bool);

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
		_login.addKeyListener(keyListener);
		_pwd.addKeyListener(keyListener);
		_persistentWorkspaceCheckBox.addKeyListener(keyListener);
		_nopoolCheckBox.addKeyListener(keyListener);
		_waitForResourceBox.addKeyListener(keyListener);
		_playDemoBox.addKeyListener(keyListener);

		if (mode != GDApplet.HTTP_MODE) {
			_pwd.setText("");
			_pwd.setEnabled(false);
			_nopoolCheckBox.setEnabled(false);
			_waitForResourceBox.setEnabled(false);
		}

		p2.add(_login);
		p2.add(_pwd);

		p2.add(new JLabel(""));
		p2.add(new JLabel(""));
		p2.add(new JLabel(""));
		p2.add(new JLabel(""));
		p2.add(new JLabel(""));
		p2.add(new JLabel(""));

		p1.add(new JLabel("  Login"));
		p1.add(new JLabel("  Password"));

		p1.add(new JLabel(""));
		p1.add(_persistentWorkspaceCheckBox);
		p1.add(_nopoolCheckBox);
		p1.add(_waitForResourceBox);
		p1.add(_playDemoBox);
		p1.add(new JLabel(""));

		_ok = new JButton("Ok");
		_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okMethod();
			}
		});

		_cancel = new JButton("Cancel");
		_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelMethod();
			}
		});

		p1.add(_ok);
		p2.add(_cancel);

		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						_login.requestFocus();
					}
				});
			}
		}).start();
		setSize(new Dimension(410, 300));
		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {
		login_str = _login.getText();
		pwd_str = new String(_pwd.getPassword());
		persistentWorkspace_bool = _persistentWorkspaceCheckBox.isSelected();
		nopool_bool = _nopoolCheckBox.isSelected();
		waitForResource_bool = _waitForResourceBox.isSelected();
		playDemo_bool = _playDemoBox.isSelected();
		_closedOnOK = true;
		LoginDialog.this.setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		LoginDialog.this.setVisible(false);
	}

}