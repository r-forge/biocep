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
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import remoting.RServices;

import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class LoginDialog extends JDialog {

	public static int mode_int = GDApplet.NEW_R_MODE;
	public static String url_str = "http://127.0.0.1:8080/cmd";
	public static String login_str = "guest";
	public static String pwd_str = "guest";
	public static boolean nopool_bool = false;
	public static boolean waitForResource_bool = false;

	public static int rmiMode_int = GDApplet.RMI_MODE_REGISTRY_MODE;
	
	public static String rmiregistryIp_str = "127.0.0.1";
	public static Integer rmiregistryPort_int = 1099;
	public static String servantName_str = "";
	
	public static String dbDriver_str = "DERBY";	
	public static String dbHostIp_str = "127.0.0.1";
	public static Integer dbHostPort_int = 1527;
	public static String dbName_str = "DWEP";
	
	public static String dbUser_str = "DWEP";
	public static String dbPwd_str = "DWEP";
	public static String dbServantName_str = "";
	
		
	public static String stub_str = "";
	
	public static Integer memoryMin_int = 256;
	public static Integer memoryMax_int = 256;
	
	public static boolean keepAlive_bool = false;
	public static boolean useSsh_bool = false;
	public static String sshHost_str = "";
	public static String sshLogin_str = "";
	public static String sshPwd_str = "";
	

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

	JRadioButton rmiModeRegistryModeButton;
	JRadioButton rmiModeDbModeButton;
	JRadioButton rmiModeStubModeButton;
	
	private JTextField _rmiregistryIp;
	private JTextField _rmiregistryPort;
	private static JComboBox _servantName=null;
	private JButton _refresh;

	private JComboBox _dbDriver;
	private JTextField _dbHostIp;
	private JTextField _dbHostPort;
	private JTextField _dbName;
	
	private JTextField _dbUser;
	private JTextField _dbPwd;
	private static JComboBox _dbServantName;
	
	private JTextField _stub;

	private JTextField _memoryMin;
	private JTextField _memoryMax;
	
	private JCheckBox _keepAlive;
	
	private JRadioButton _useSsh;
	private JRadioButton _useLocalHost; 
	
	private JTextField _sshHostIp;
	private JTextField _sshLogin;
	private JPasswordField _sshPwd;
	
	private JCheckBox _persistentWorkspaceCheckBox;
	private JCheckBox _playDemoBox;

	private JButton _ok;
	private JButton _cancel;

	JPanel dynamicPanel;
	JPanel rmiDynamicPanel;
	
	private boolean _closedOnOK = false;

	public Identification getIndentification() {
		if (_closedOnOK)
			return new Identification(mode_int, url_str, login_str, pwd_str, nopool_bool, waitForResource_bool,
			rmiMode_int,			
			rmiregistryIp_str, rmiregistryPort_int, servantName_str,			
			dbDriver_str, dbHostIp_str, dbHostPort_int, dbName_str, dbUser_str, dbPwd_str, dbServantName_str,
			stub_str, 			
			memoryMin_int,memoryMax_int, keepAlive_bool, useSsh_bool, sshHost_str, sshLogin_str, sshPwd_str,
			persistentWorkspace_bool, playDemo_bool);
		else
			return null;
	}

	public void recreateDynamicPanel() {
		dynamicPanel.removeAll();
		
		if (localModeButton.isSelected()) {
			dynamicPanel.setLayout(new GridLayout(1, 2));
			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(0, 1));
			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout(0, 1));
			dynamicPanel.add(p1);		
			dynamicPanel.add(p2);

			p1.add(_useLocalHost); p2.add(new JLabel(""));			
			p1.add(_useSsh); p2.add(new JLabel(""));
			if (_useSsh.isSelected()) {
				p1.add(new JLabel("  SSH Host")); p2.add(_sshHostIp);
				p1.add(new JLabel("  SSH Login")); p2.add(_sshLogin);
				p1.add(new JLabel("  SSH Pwd")); p2.add(_sshPwd);				
			} 
			p1.add(new JLabel("")); p2.add(new JLabel(""));
						
			if (!_useSsh.isSelected()) {
				p1.add(new JLabel("")); p2.add(new JLabel(""));
				p1.add(new JLabel("")); p2.add(new JLabel(""));
				p1.add(new JLabel("")); p2.add(new JLabel(""));
			}

			p1.add(_keepAlive); p2.add(new JLabel(""));
			p1.add(_persistentWorkspaceCheckBox); p2.add(new JLabel(""));
			p1.add(_playDemoBox); p2.add(new JLabel(""));
			p1.add(new JLabel("  Memory Min (megabytes)")); p2.add(_memoryMin);
			p1.add(new JLabel("  Memory Max (megabytes)")); p2.add(_memoryMax);			
			
			

		} else if (httpModeButton.isSelected()) {
			dynamicPanel.setLayout(new GridLayout(1, 2));
			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(0, 1));
			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout(0, 1));
			dynamicPanel.add(p1);		
			dynamicPanel.add(p2);

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
			
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			
		} else {
			dynamicPanel.setLayout(new BorderLayout());
			JPanel radioPanel=new JPanel(new GridLayout(1,3));
			radioPanel.add(rmiModeRegistryModeButton);
			radioPanel.add(rmiModeDbModeButton);
			radioPanel.add(rmiModeStubModeButton);
			
			JPanel topPanel=new JPanel(new BorderLayout());
			topPanel.add(new JLabel("  Use :  "),BorderLayout.WEST);
			topPanel.add(radioPanel,BorderLayout.CENTER);
			dynamicPanel.add(topPanel, BorderLayout.NORTH);
			dynamicPanel.add(rmiDynamicPanel, BorderLayout.CENTER);
			recreateRmiDynamicPanel();
			
		}
		
		dynamicPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		dynamicPanel.updateUI();
		dynamicPanel.repaint();

	}
	
	public void recreateRmiDynamicPanel() {
		
		rmiDynamicPanel.removeAll();		
		rmiDynamicPanel.setLayout(new GridLayout(1, 2));
		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		rmiDynamicPanel.add(p1);		
		rmiDynamicPanel.add(p2);
		
		p1.add(new JLabel("")); p2.add(new JLabel(""));
		
		if (rmiModeRegistryModeButton.isSelected()) {

			p1.add(new JLabel("  Rmi Registry Host Name or IP"));p2.add(_rmiregistryIp);			
			p1.add(new JLabel("  Rmi Registry Port"));p2.add(_rmiregistryPort);			
			JPanel namePanel=new JPanel(new BorderLayout());
			namePanel.add(_servantName, BorderLayout.CENTER); namePanel.add(_refresh, BorderLayout.EAST);
			p1.add(new JLabel("  R Servant Name"));p2.add(namePanel);
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(_persistentWorkspaceCheckBox); p2.add(new JLabel(""));
			p1.add(_playDemoBox); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			

		} else if (rmiModeDbModeButton.isSelected()) {
			
			p1.add(new JLabel("  DB Driver")); p2.add(_dbDriver);
			p1.add(new JLabel("  DB Host Name or IP"));p2.add(_dbHostIp);
			p1.add(new JLabel("  DB Host port"));p2.add(_dbHostPort);		
			p1.add(new JLabel("  DB Name"));p2.add(_dbName);		
			p1.add(new JLabel("  DB User"));p2.add(_dbUser);			
			p1.add(new JLabel("  DB Pwd"));p2.add(_dbPwd);			
			JPanel namePanel=new JPanel(new BorderLayout());
			namePanel.add(_dbServantName, BorderLayout.CENTER); namePanel.add(_refresh, BorderLayout.EAST);
			p1.add(new JLabel("  R Servant Name"));p2.add(namePanel);			
			p1.add(_persistentWorkspaceCheckBox); p2.add(new JLabel(""));
			p1.add(_playDemoBox); p2.add(new JLabel(""));
			
			
		} else {
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("  Stub "));p2.add(_stub);			
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			p1.add(_persistentWorkspaceCheckBox); p2.add(new JLabel(""));
			p1.add(_playDemoBox); p2.add(new JLabel(""));
			p1.add(new JLabel("")); p2.add(new JLabel(""));
			
		}
		
		rmiDynamicPanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		rmiDynamicPanel.updateUI();
		rmiDynamicPanel.repaint();

	}
	
	public LoginDialog(Component c, int mode) {
		super((Frame) null, true);

		setTitle("Please Enter Your R Session Parameters");

		JPanel modePanel = new JPanel();
		modePanel.setLayout(new GridLayout(1, 4));

	    dynamicPanel = new JPanel();
		rmiDynamicPanel = new JPanel();

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(1, 2));

		JPanel topPanel=new JPanel(new BorderLayout());
		topPanel.add(modePanel, BorderLayout.NORTH);
		topPanel.add(new JLabel(" "), BorderLayout.SOUTH);
		
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(topPanel, BorderLayout.NORTH);
		
		getContentPane().add(dynamicPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		localModeButton = new JRadioButton("New R");
		localModeButton.setSelected(mode == GDApplet.NEW_R_MODE);
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
		
		
		rmiModeRegistryModeButton = new JRadioButton("Rmi Registry");
		rmiModeRegistryModeButton.setSelected(rmiMode_int == GDApplet.RMI_MODE_REGISTRY_MODE);
		rmiModeRegistryModeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateRmiDynamicPanel();				
			}
			
		});
		
		rmiModeDbModeButton = new JRadioButton("Rmi Database");
		rmiModeDbModeButton.setSelected(rmiMode_int == GDApplet.RMI_MODE_DB_MODE);
		rmiModeDbModeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateRmiDynamicPanel();				
			}
			
		});
		
		rmiModeStubModeButton = new JRadioButton("Rmi Stub");
		rmiModeStubModeButton.setSelected(rmiMode_int == GDApplet.RMI_MODE_STUB_MODE);
		rmiModeStubModeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateRmiDynamicPanel();				
			}
			
		});
						
		ButtonGroup rmiButtonGroup = new ButtonGroup();
		rmiButtonGroup.add(rmiModeRegistryModeButton);
		rmiButtonGroup.add(rmiModeDbModeButton);
		rmiButtonGroup.add(rmiModeStubModeButton);
		
		_rmiregistryIp=new JTextField(rmiregistryIp_str);
		_rmiregistryPort=new JTextField(new Integer(rmiregistryPort_int).toString());
		
		if (_servantName==null) _servantName=new JComboBox(new Object[]{servantName_str});
		_servantName.setSelectedItem(servantName_str);
		_servantName.setEditable(true);
		
		_refresh = new JButton("Refresh");
		_refresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rmiModeRegistryModeButton.isSelected()) {
					refreshNamesRmiModeRegistryMode(); 
				} else if (rmiModeDbModeButton.isSelected()) {
					refreshNamesRmiModeDbMode();
				}				
			}
		});
				
		_dbDriver=new JComboBox(new Object[]{"DERBY", "ORACLE", "MySQL"});
		_dbDriver.setSelectedItem(dbDriver_str);				
		_dbHostIp=new JTextField(dbHostIp_str);
		_dbHostPort=new JTextField(new Integer(dbHostPort_int).toString());
		_dbName=new JTextField(dbName_str);		
		_dbUser=new JTextField(dbUser_str);
		_dbPwd=new JTextField(dbPwd_str);
		
		if (_dbServantName==null) _dbServantName=new JComboBox(new Object[]{dbServantName_str});
		_dbServantName.setSelectedItem(dbServantName_str);
		_dbServantName.setEditable(true);
				
		_stub=new JTextField(stub_str);
		
		_memoryMin=new JTextField(new Integer(memoryMin_int).toString());
		_memoryMax=new JTextField(new Integer(memoryMax_int).toString());
		
		_keepAlive=new JCheckBox("Keep Alive");_keepAlive.setSelected(keepAlive_bool);
		
		_useSsh=new JRadioButton("Remote Host via SSH");_useSsh.setSelected(useSsh_bool);
		_useSsh.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateDynamicPanel();				
			}
		});
		_useLocalHost=new JRadioButton("Local Host");_useLocalHost.setSelected(!useSsh_bool);
		_useLocalHost.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				recreateDynamicPanel();				
			}
		});
		
		ButtonGroup useSshButtonGroup = new ButtonGroup();
		useSshButtonGroup.add(_useSsh);
		useSshButtonGroup.add(_useLocalHost);
		
		_sshHostIp=new JTextField(sshHost_str);
		_sshLogin=new JTextField(sshLogin_str);
		_sshPwd=new JPasswordField(sshPwd_str);

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
		
		
		localModeButton.addKeyListener(keyListener);
		httpModeButton.addKeyListener(keyListener);
		rmiModeButton.addKeyListener(keyListener);
		
		
		rmiModeRegistryModeButton.addKeyListener(keyListener);
		rmiModeDbModeButton.addKeyListener(keyListener);
		rmiModeStubModeButton.addKeyListener(keyListener);

		
		_url.addKeyListener(keyListener);
		_login.addKeyListener(keyListener);
		_pwd.addKeyListener(keyListener);
		_nopoolCheckBox.addKeyListener(keyListener);
		_waitForResourceBox.addKeyListener(keyListener);

		_rmiregistryIp.addKeyListener(keyListener);
		_rmiregistryPort.addKeyListener(keyListener);
		_servantName.addKeyListener(keyListener);
		_stub.addKeyListener(keyListener);
		
	
		_persistentWorkspaceCheckBox.addKeyListener(keyListener);
		_playDemoBox.addKeyListener(keyListener);
		
		_dbDriver.addKeyListener(keyListener);
		_dbDriver.addKeyListener(keyListener);				
		_dbHostIp.addKeyListener(keyListener);
		_dbHostPort.addKeyListener(keyListener);
		_dbName.addKeyListener(keyListener);
		_dbUser.addKeyListener(keyListener);
		_dbPwd.addKeyListener(keyListener);

		_memoryMin.addKeyListener(keyListener);
		_memoryMax.addKeyListener(keyListener);
		
		_useSsh.addKeyListener(keyListener);
		_useLocalHost.addKeyListener(keyListener);
		_sshHostIp.addKeyListener(keyListener);
		_sshLogin.addKeyListener(keyListener);
		_sshPwd.addKeyListener(keyListener);

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
			mode_int = GDApplet.NEW_R_MODE;
		else if (httpModeButton.isSelected())
			mode_int = GDApplet.HTTP_MODE;
		else
			mode_int = GDApplet.RMI_MODE;

		url_str = _url.getText();
		login_str = _login.getText();
		pwd_str = new String(_pwd.getPassword());
		nopool_bool = _nopoolCheckBox.isSelected();
		waitForResource_bool = _waitForResourceBox.isSelected();

		
		if (rmiModeRegistryModeButton.isSelected())
			rmiMode_int = GDApplet.RMI_MODE_REGISTRY_MODE;
		else if (rmiModeDbModeButton.isSelected())
			rmiMode_int = GDApplet.RMI_MODE_DB_MODE;
		else
			rmiMode_int = GDApplet.RMI_MODE_STUB_MODE;
		
		rmiregistryIp_str = _rmiregistryIp.getText();
		try {rmiregistryPort_int = Integer.decode(_rmiregistryPort.getText());} catch (Exception e) {rmiregistryPort_int=null;}
		servantName_str = (String)_servantName.getSelectedItem();
		
		dbDriver_str = (String)_dbDriver.getSelectedItem();
		
		dbHostIp_str = _dbHostIp.getText();
		try {dbHostPort_int = Integer.decode(_dbHostPort.getText());} catch (Exception e) {dbHostPort_int=null;}
		dbName_str = _dbName.getText();
		
		dbUser_str = _dbUser.getText();
		dbPwd_str = _dbPwd.getText();
		dbServantName_str = (String) _dbServantName.getSelectedItem();
		
		stub_str = _stub.getText();

		try {memoryMin_int = Integer.decode(_memoryMin.getText());} catch (Exception e) {memoryMin_int=null;}
		try {memoryMax_int = Integer.decode(_memoryMax.getText());} catch (Exception e) {memoryMax_int=null;}
		
		keepAlive_bool=_keepAlive.isSelected();
		useSsh_bool=_useSsh.isSelected();
		sshHost_str=_sshHostIp.getText();
		sshLogin_str=_sshLogin.getText();
		sshPwd_str=_sshPwd.getText();

		playDemo_bool = _playDemoBox.isSelected();
		persistentWorkspace_bool = _persistentWorkspaceCheckBox.isSelected();

		_closedOnOK = true;
		LoginDialog.this.setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		LoginDialog.this.setVisible(false);
	}
	
	private void refreshNamesRmiModeRegistryMode() {
		new Thread(new Runnable(){
			public void run() {
				try {
					Registry registry=LocateRegistry.getRegistry(_rmiregistryIp.getText(), Integer.decode(_rmiregistryPort.getText()));
					String[] list=registry.list();
					final Vector<String> rnames=new Vector<String>();
					for (int i=0; i<list.length; ++i) {
						if (registry.lookup(list[i]) instanceof RServices) rnames.add(list[i]);
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							_servantName=new JComboBox((Object[])rnames.toArray(new Object[0]));
							_servantName.setEditable(true);
							recreateRmiDynamicPanel();
						}
					});
				}
				catch (Exception e) {
					e.printStackTrace();
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(LoginDialog.this, "Coulnd't connect to rmiregistry");
						}
					});
				}
				
			}
		}).start();
	}

	public static String[] getDriverClassAndUrl(String driver, String hostIp, int hostPort, String dbName) {
		String[] result=new String[2];
		if (driver.equalsIgnoreCase("DERBY")) {
			result[0]="org.apache.derby.jdbc.ClientDriver";
			result[1]="jdbc:derby://"+hostIp+":"+hostPort+"/"+dbName+";create=true";
		} else if (driver.equalsIgnoreCase("ORACLE")) {
			result[0]="oracle.jdbc.OracleDriver";
			result[1]="jdbc:oracle:thin:@"+hostIp+":"+hostPort+":"+dbName;
		} else if (driver.equalsIgnoreCase("MySQL")) {
			result[0]="com.mysql.jdbc.Driver";
			result[1]="jdbc:mysql://"+hostIp+":"+hostPort+"/"+dbName;
		}		
		return result;
	}
	private void refreshNamesRmiModeDbMode() {
			
		new Thread(new Runnable(){
			public void run() {
				try {					
					final String[] dbDriverClass_dbUrl=getDriverClassAndUrl((String)_dbDriver.getSelectedItem(), _dbHostIp.getText(), Integer.decode(_dbHostPort.getText()), _dbName.getText());
					final String user = _dbUser.getText();
					final String password = _dbPwd.getText();
					Class.forName(dbDriverClass_dbUrl[0]);
					Registry registry = DBLayer.getLayer(PoolUtils.getDBType(dbDriverClass_dbUrl[1]), new ConnectionProvider() {
						public Connection newConnection() throws java.sql.SQLException {
							return DriverManager.getConnection(dbDriverClass_dbUrl[1], user, password);
						};

					});
					
					String[] list=registry.list();
					final Vector<String> rnames=new Vector<String>();
					for (int i=0; i<list.length; ++i) {
						if (registry.lookup(list[i]) instanceof RServices) rnames.add(list[i]);
					}
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							_dbServantName=new JComboBox((Object[])rnames.toArray(new Object[0]));
							_dbServantName.setEditable(true);
							recreateRmiDynamicPanel();							
						}
					});
				}
				catch (Exception e) {
					e.printStackTrace();
					
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							JOptionPane.showMessageDialog(LoginDialog.this, "Coulnd't connect to db registry");			
						}
					});
				
				}
			}
		}).start();
	}

}

/*
<!--
<property name="demo8.db.driver" value="oracle.jdbc.OracleDriver"/>
<property name="demo8.db.url" value="jdbc:oracle:thin:@progression.ebi.ac.uk:1521:AEDWT" />
<property name="demo8.db.user" value="DWEP" />
<property name="demo8.db.password" value="DWEP" />
<property name="demo8.pslistpath" value="D:/wtool/pslist.exe" />
-->	

<!--
<property name="demo8.db.driver" value="com.mysql.jdbc.Driver"/>
<property name="demo8.db.url" value="jdbc:mysql://172.22.68.47/DWEP" />
<property name="demo8.db.user" value="DWEP" />
<property name="demo8.db.password" value="DWEP" />
<property name="demo8.pslistpath" value="D:/wtool/pslist.exe" />
-->	

<property name="demo8.db.driver" value="org.apache.derby.jdbc.ClientDriver"/>
<property name="demo8.db.url" value="jdbc:derby://localhost:1527/DWEP;create=true" />
<property name="demo8.db.user" value="DWEP" />
<property name="demo8.db.password" value="DWEP" />
<property name="demo8.pslistpath" value="D:/wtool/pslist.exe" />

*/