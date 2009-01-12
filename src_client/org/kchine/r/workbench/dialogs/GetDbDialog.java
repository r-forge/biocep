/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
package org.kchine.r.workbench.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.kchine.rpf.PoolUtils;

public class GetDbDialog extends JDialog {
	
	public static String dbDriver_str = "DERBY";
	public static String dbHostIp_str = "127.0.0.1";
	public static Integer dbHostPort_int = 1527;
	public static String dbName_str = "DWEP";
	public static String dbUser_str = "DWEP";
	public static String dbPwd_str = "DWEP";

	
	
	private boolean _closedOnOK = false;
	
	private JComboBox _dbDriver;
	private JTextField _dbHostIp;
	private JTextField _dbHostPort;
	private JTextField _dbName;
	private JTextField _dbUser;
	private JTextField _dbPwd;

	
	
	public DbInfo getDbInfo() {
		if (_closedOnOK)
			try {
				return new DbInfo(dbDriver_str.toLowerCase(), dbHostIp_str, dbHostPort_int, dbName_str , dbUser_str, dbPwd_str);
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}

	public GetDbDialog(Component father) {
		super(new JFrame(), true);
		setLocationRelativeTo(father);
		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		
		_dbDriver = new JComboBox(new Object[] { "DERBY", "ORACLE", "MySQL" });
		_dbDriver.setSelectedItem(dbDriver_str);
		_dbHostIp = new JTextField(dbHostIp_str);
		_dbHostPort = new JTextField(new Integer(dbHostPort_int).toString());
		_dbName = new JTextField(dbName_str);
		_dbUser = new JTextField(dbUser_str);
		_dbPwd = new JTextField(dbPwd_str);

		p1.add(new JLabel("  DB Driver")); p2.add(_dbDriver);
		p1.add(new JLabel("  DB Host Name or IP"));	p2.add(_dbHostIp);
		p1.add(new JLabel("  DB Host port")); p2.add(_dbHostPort);
		p1.add(new JLabel("  DB Name")); p2.add(_dbName);
		p1.add(new JLabel("  DB User")); p2.add(_dbUser);
		p1.add(new JLabel("  DB Pwd")); p2.add(_dbPwd);
		
		
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
		
		_dbDriver.addKeyListener(keyListener);
		_dbHostIp.addKeyListener(keyListener);
		_dbHostPort.addKeyListener(keyListener);
		_dbName.addKeyListener(keyListener);
		_dbUser.addKeyListener(keyListener);
		_dbPwd.addKeyListener(keyListener);
				
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

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(610, 220));

		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {		
		dbDriver_str = (String)_dbDriver.getSelectedItem();
		dbHostIp_str = _dbHostIp.getText();
		try {
			dbHostPort_int = Integer.decode(_dbHostPort.getText());
		} catch (Exception e) {
			dbHostPort_int = null;
		}
		dbName_str = _dbName.getText();
		dbUser_str = _dbUser.getText();
		dbPwd_str = _dbPwd.getText();		
		_closedOnOK = true;
		setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		setVisible(false);
	}
	
	private static void createAndShowGUI() {
        JFrame frame = new JFrame("DialogDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GetDbDialog newContentPane = new GetDbDialog(frame);
        newContentPane.setVisible(true);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

	

}