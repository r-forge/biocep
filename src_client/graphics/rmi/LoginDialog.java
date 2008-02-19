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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class LoginDialog extends JDialog {

	public static int mode_int = GDApplet.LOCAL_MODE;
	public static String url_str = "http://127.0.0.1:8080/cmd";
	public static String login_str = "guest";
	public static String pwd_str = "guest";
	public static boolean nopool_bool = false;
	public static boolean waitForResource_bool = false;

	public static String rmiregistryIp_str = "127.0.0.1";
	public static int rmiregistryPort_int = 1099;
	public static String servantName_str = "";
	public static String stub_str = "";
	public static String memoryMax_str = "256m";

	public static boolean persistentWorkspace_bool = false;
	public static boolean playDemo_bool = false;

	JRadioButton localModeButton;
	JRadioButton httpModeButton;
	JRadioButton rmiModeButton;

	private JTextField _url;
	private JTextField _login;
	private JPasswordField _pwd;
	private JCheckBox _nopoolCheckBox;
	private JCheckBox _waitForResourceBox;

	private JTextField _rmiregistryIp;
	private JTextField _rmiregistryPort;
	private JTextField _servantName;
	private JTextField _stub;

	private JTextField _memoryMax;

	private JCheckBox _persistentWorkspaceCheckBox;
	private JCheckBox _playDemoBox;

	private JButton _ok;
	private JButton _cancel;

	JPanel dynamicPanel;
	
	private boolean _closedOnOK = false;

	public Identification getIndentification() {
		if (_closedOnOK)
			return new Identification(mode_int, url_str, login_str, pwd_str, nopool_bool, waitForResource_bool,

			rmiregistryIp_str, rmiregistryPort_int, servantName_str, stub_str, memoryMax_str,

			persistentWorkspace_bool, playDemo_bool);

		else
			return null;
	}

	public void recreateDynamicPanel() {
		dynamicPanel.removeAll();
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		dynamicPanel.add(p1);		
		dynamicPanel.add(p2);
		
		if (localModeButton.isSelected()) {
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("  Memory Max")); p2.add(_memoryMax);
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(_persistentWorkspaceCheckBox); p2.add(new JLabel(""));
			p1.add(_playDemoBox); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			

		} else if (httpModeButton.isSelected()) {
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("  Url"));p2.add(_url);			
			p1.add(new JLabel("  Login"));p2.add(_login);			
			p1.add(new JLabel("  Password"));p2.add(_pwd);
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(_nopoolCheckBox);p2.add(new JLabel(""));
			p1.add(_waitForResourceBox);p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(_persistentWorkspaceCheckBox); p2.add(new JLabel(""));
			p1.add(_playDemoBox); p2.add(new JLabel(""));
			
		} else {
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("  Rmi Registry Host Name or IP"));p2.add(_rmiregistryIp);			
			p1.add(new JLabel("  Rmi Registry Port"));p2.add(_rmiregistryPort);			
			p1.add(new JLabel("  R Servant Name"));p2.add(_servantName);
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("  Stub - if no Rmi Registry"));p2.add(_stub);
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(_persistentWorkspaceCheckBox); p2.add(new JLabel(""));
			p1.add(_playDemoBox); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
		}
		p1.add(new JLabel("")); p2.add(new JLabel(""));
		dynamicPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		dynamicPanel.updateUI();
		dynamicPanel.repaint();

	}
	
	public LoginDialog(Component c, int mode) {
		super((Frame) null, true);

		setTitle("Please Enter Your R Session Parameters");

		JPanel modePanel = new JPanel();
		modePanel.setLayout(new GridLayout(1, 4));

	    dynamicPanel = new JPanel();
		dynamicPanel.setLayout(new GridLayout(1, 2));

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(1, 2));

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(modePanel, BorderLayout.NORTH);
		getContentPane().add(dynamicPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		localModeButton = new JRadioButton("Local R");
		localModeButton.setSelected(mode == GDApplet.LOCAL_MODE);
		localModeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateDynamicPanel();				
			}
		});
		httpModeButton = new JRadioButton("Http R");
		httpModeButton.setSelected(mode == GDApplet.HTTP_MODE);
		httpModeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateDynamicPanel();				
			}
		});
		rmiModeButton = new JRadioButton("Rmi R");
		rmiModeButton.setSelected(mode == GDApplet.RMI_MODE);
		rmiModeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateDynamicPanel();				
			}
		});
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(localModeButton);
		buttonGroup.add(httpModeButton);
		buttonGroup.add(rmiModeButton);
		modePanel.add(new JLabel("Connect to : "));
		modePanel.add(localModeButton);
		modePanel.add(httpModeButton);
		modePanel.add(rmiModeButton);

		_url=new JTextField(url_str);
		_login = new JTextField(login_str);
		_pwd = new JPasswordField(pwd_str);
		_nopoolCheckBox = new JCheckBox("Create Private R", nopool_bool);
		_waitForResourceBox = new JCheckBox("Wait Until R Resource Available", waitForResource_bool);
		
		_rmiregistryIp=new JTextField(rmiregistryIp_str);
		_rmiregistryPort=new JTextField(new Integer(rmiregistryPort_int).toString());
		_servantName=new JTextField(servantName_str);
		_stub=new JTextField(stub_str);
		
		_memoryMax=new JTextField(memoryMax_str);

		_persistentWorkspaceCheckBox = new JCheckBox("Persistent Workspace", persistentWorkspace_bool);
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
		
		_url.addKeyListener(keyListener);
		_login.addKeyListener(keyListener);
		_pwd.addKeyListener(keyListener);
		_nopoolCheckBox.addKeyListener(keyListener);
		_waitForResourceBox.addKeyListener(keyListener);

		_rmiregistryIp.addKeyListener(keyListener);
		_rmiregistryPort.addKeyListener(keyListener);
		_servantName.addKeyListener(keyListener);
		_stub.addKeyListener(keyListener);
		
		_memoryMax=new JTextField(memoryMax_str);
		
		_persistentWorkspaceCheckBox.addKeyListener(keyListener);
		_playDemoBox.addKeyListener(keyListener);
		
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

		
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		buttonsPanel.setLayout(new GridLayout(1, 2));		
		buttonsPanel.add(p1);buttonsPanel.add(p2);
		p1.add(new JLabel("")); p2.add(new JLabel(""));
		p1.add(_ok);p2.add(_cancel);

		
		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						recreateDynamicPanel();
						_login.requestFocus();
					}
				});
			}
		}).start();
		setSize(new Dimension(460, 440));
		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {

		if (localModeButton.isSelected())
			mode_int = GDApplet.LOCAL_MODE;
		else if (httpModeButton.isSelected())
			mode_int = GDApplet.HTTP_MODE;
		else
			mode_int = GDApplet.RMI_MODE;

		url_str = _url.getText();
		login_str = _login.getText();
		pwd_str = new String(_pwd.getPassword());
		nopool_bool = _nopoolCheckBox.isSelected();
		waitForResource_bool = _waitForResourceBox.isSelected();

		rmiregistryIp_str = _rmiregistryIp.getText();
		rmiregistryPort_int = Integer.decode(_rmiregistryPort.getText());
		servantName_str = _servantName.getText();
		stub_str = _stub.getText();

		memoryMax_str = _memoryMax.getText();

		playDemo_bool = _playDemoBox.isSelected();
		persistentWorkspace_bool = _persistentWorkspaceCheckBox.isSelected();

		_closedOnOK = true;
		LoginDialog.this.setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		LoginDialog.this.setVisible(false);
	}

}