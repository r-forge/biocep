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
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import uk.ac.ebi.microarray.pools.db.NodeDataDB;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class NodeDialog extends JDialog {

	public static String _nodeName = "";
	public static String _host_ip_str = "";
	public static String _host_name_str = "";
	public static String _login_str = "";
	public static String _pwd_str = "";
	public static String _homeDir_str = "";
	public static String _command_str = "";
	public static String _kill_command_str = "";
	public static String _os_str = "";
	public static String _servant_nbr_min = "0";
	public static String _servant_nbr_max = "0";
	public static String _prefix = "";
	public static String _processCounter = "0";

	private JTextField nodeName;
	private JTextField hostIp;
	private JTextField hostName;
	private JTextField login;
	private JPasswordField pwd;
	private JTextField homeDir;
	private JTextField command;
	private JTextField killCommand;
	private JTextField os;
	private JTextField servantNbrMin;
	private JTextField servantNbrMax;
	private JTextField prefix;
	private JTextField processCounter;

	boolean isOk = false;

	public NodeDataDB getLaunchInfo() {

		if (isOk)
			return new NodeDataDB(_nodeName, _host_ip_str, _host_name_str, _login_str, _pwd_str, _homeDir_str,
					_command_str, _kill_command_str, _os_str, Integer.decode(_servant_nbr_min), Integer
							.decode(_servant_nbr_max), _prefix, Integer.decode(_processCounter));
		else
			return null;
	}

	public NodeDialog(Frame aFrame, boolean addTeditF) {
		super(aFrame, true);

		setTitle(addTeditF ? "Add Node" : "Edit Node");

		setLocationRelativeTo(aFrame);

		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);

		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		p1.add(new JLabel("  Node Name"));
		p1.add(new JLabel("  Host IP"));
		p1.add(new JLabel("  Host Name"));
		p1.add(new JLabel("  Login"));
		p1.add(new JLabel("  Password"));
		p1.add(new JLabel("  Home Dir"));
		p1.add(new JLabel("  Create Command"));
		p1.add(new JLabel("  Kill Command"));
		p1.add(new JLabel("  Os"));
		p1.add(new JLabel("  Servants Number Min"));
		p1.add(new JLabel("  Servants Number Max"));
		p1.add(new JLabel("  Prefix"));
		p1.add(new JLabel("  Process Counter"));

		p1.add(new JLabel(" "));

		nodeName = new JTextField();
		nodeName.setText(_nodeName);
		if (!addTeditF)
			nodeName.setEditable(false);

		hostIp = new JTextField();
		hostIp.setText(_host_ip_str);
		//if (!addTeditF)	hostIp.setEditable(false);

		hostName = new JTextField();
		hostName.setText(_host_name_str);
		//if (!addTeditF)	hostName.setEditable(false);

		login = new JTextField();
		login.setText(_login_str);

		pwd = new JPasswordField();
		pwd.setText(_pwd_str);

		homeDir = new JTextField();
		homeDir.setText(_homeDir_str);

		command = new JTextField();
		command.setText(_command_str);
		killCommand = new JTextField();
		killCommand.setText(_kill_command_str);

		os = new JTextField();
		os.setText(_os_str);

		servantNbrMin = new JTextField();
		servantNbrMin.setText(new Integer(_servant_nbr_min).toString());

		servantNbrMax = new JTextField();
		servantNbrMax.setText(new Integer(_servant_nbr_max).toString());

		prefix = new JTextField();
		prefix.setText(_prefix);

		processCounter = new JTextField();
		processCounter.setText(new Integer(_processCounter).toString());

		//if (!addTeditF)	prefix.setEditable(false);

		p2.add(nodeName);
		p2.add(hostIp);
		p2.add(hostName);
		p2.add(login);
		p2.add(pwd);
		p2.add(homeDir);
		p2.add(command);
		p2.add(killCommand);

		p2.add(os);
		p2.add(servantNbrMin);
		p2.add(servantNbrMax);
		p2.add(prefix);
		p2.add(processCounter);

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

		nodeName.addKeyListener(keyListener);
		hostIp.addKeyListener(keyListener);
		hostName.addKeyListener(keyListener);
		login.addKeyListener(keyListener);
		pwd.addKeyListener(keyListener);
		homeDir.addKeyListener(keyListener);
		command.addKeyListener(keyListener);
		killCommand.addKeyListener(keyListener);
		os.addKeyListener(keyListener);
		servantNbrMin.addKeyListener(keyListener);
		servantNbrMax.addKeyListener(keyListener);
		prefix.addKeyListener(keyListener);
		processCounter.addKeyListener(keyListener);

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(450, 330));

	}

	private void okMethod() {

		_nodeName = (nodeName.getText());
		_host_ip_str = (hostIp.getText());
		_host_name_str = (hostIp.getText());
		_login_str = (login.getText());
		_pwd_str = (new String(pwd.getPassword()));
		_homeDir_str = (homeDir.getText());
		_command_str = (command.getText());
		_kill_command_str = (killCommand.getText());
		_os_str = (os.getText());
		_servant_nbr_min = (servantNbrMin.getText());
		_servant_nbr_max = (servantNbrMax.getText());
		_prefix = (prefix.getText());
		_processCounter = (processCounter.getText());

		isOk = true;
		NodeDialog.this.setVisible(false);
	}

	private void cancelMethod() {
		_nodeName = (nodeName.getText());
		_host_ip_str = (hostIp.getText());
		_host_name_str = (hostName.getText());
		_login_str = (login.getText());
		_pwd_str = (new String(pwd.getPassword()));
		_homeDir_str = (homeDir.getText());
		_command_str = (command.getText());
		_kill_command_str = (killCommand.getText());
		_os_str = (os.getText());

		_servant_nbr_min = (servantNbrMin.getText());
		_servant_nbr_max = (servantNbrMax.getText());
		_prefix = (prefix.getText());
		_processCounter = (processCounter.getText());
		isOk = false;
		NodeDialog.this.setVisible(false);
	}

}
