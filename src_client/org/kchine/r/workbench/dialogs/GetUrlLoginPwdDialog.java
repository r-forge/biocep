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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.kchine.rpf.PoolUtils;

public class GetUrlLoginPwdDialog extends JDialog {
	
	static String url_str="http://127.0.0.1:8080/rvirtual/cmd";
	static String login_str="guest";
	static String pwd_str="guest";
	
	private boolean _closedOnOK = false;
	final JTextField url;
	final JTextField login;
	final JTextField pwd;

	public UrlLoginPwd getUrlLoginPwd() {
		if (_closedOnOK)
			try {
				return new UrlLoginPwd(url_str, login_str, pwd_str);
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}

	public GetUrlLoginPwdDialog(Component father) {
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

		url=new JTextField(url_str);
		login=new JTextField(login_str);
		pwd=new JTextField(pwd_str);
		
		p1.add(new JLabel("URL"));
		p1.add(new JLabel("Login"));
		p1.add(new JLabel("Pwd"));
		
		JButton fixIt=new JButton("Fix It");
		
		fixIt.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (!url.getText().startsWith("http://")){
					url.setText("http://"+url.getText()+":8080/rvirtual/cmd");
				} else {
					try {
						url.setText("http://"+new URL(url.getText()).getHost()+":8080/rvirtual/cmd");						
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
		
		JPanel urlPanel=new JPanel(new BorderLayout());
		urlPanel.add(url,BorderLayout.CENTER);
		urlPanel.add(fixIt,BorderLayout.EAST);
		
		p2.add(urlPanel);
		p2.add(login);
		p2.add(pwd);


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
		
		url.addKeyListener(keyListener);
		login.addKeyListener(keyListener);
		pwd.addKeyListener(keyListener);
		
		
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

		setSize(new Dimension(610, 140));

		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {
		
		url_str=url.getText();
		login_str=login.getText();
		pwd_str=pwd.getText();
		
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

        GetUrlLoginPwdDialog newContentPane = new GetUrlLoginPwdDialog(frame);
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