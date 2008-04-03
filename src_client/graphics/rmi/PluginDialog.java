/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
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
import java.io.File;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarFile;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class PluginDialog extends JDialog {

	public static String pluginJar_str = "";
	public static String pluginViewClass_str = "";
	private JTextField _pluginJar;
	private JComboBox _pluginViewClass;
	private JButton _chooseFile;
	private JButton _refreshButton;

	private JButton _ok;
	private JButton _cancel;

	private boolean _closedOnOK = false;

	public String[] getPluginViewDetail() {
		if (_closedOnOK)
			return new String[] { pluginJar_str, pluginViewClass_str };
		else
			return null;
	}

	public PluginDialog(Component c) {
		super((Frame) null, true);

		setTitle("Please Enter Your Plugin View Info");

		JPanel centralPanel = new JPanel(new GridLayout(0, 1));
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(centralPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		_pluginJar = new JTextField(pluginJar_str);

		_pluginViewClass = new JComboBox(new Object[] { pluginViewClass_str });
		_pluginViewClass.setSelectedItem(pluginViewClass_str);
		_pluginViewClass.setEditable(true);

		_chooseFile = new JButton("Choose Jar");

		_chooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showOpenDialog(PluginDialog.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						_pluginJar.setText(chooser.getSelectedFile().getAbsolutePath());

						Vector<String> viewClassList = getPluginViewsClasses(new File(_pluginJar.getText()).toURI().toURL());
						_pluginViewClass.removeAllItems();
						for (String s : viewClassList)
							_pluginViewClass.addItem(s);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				}
			}
		});

		_refreshButton = new JButton("Refresh");

		_refreshButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Vector<String> viewClassList = getPluginViewsClasses(new File(_pluginJar.getText()).toURI().toURL());
					_pluginViewClass.removeAllItems();
					for (String s : viewClassList)
						_pluginViewClass.addItem(s);
				} catch (Exception ex) {
					ex.printStackTrace();
				}

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

		_pluginJar.addKeyListener(keyListener);
		_pluginViewClass.addKeyListener(keyListener);

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

		JPanel p1 = new JPanel(new GridLayout(1, 0));
		JPanel p2 = new JPanel(new GridLayout(1, 0));
		centralPanel.add(new JLabel(" "));
		centralPanel.add(p1);
		centralPanel.add(p2);
		centralPanel.add(new JLabel(" "));

		buttonsPanel.add(_ok);
		buttonsPanel.add(_cancel);

		p1.add(new JLabel("   Plugin Jar"));
		p1.add(_pluginJar);
		p1.add(_chooseFile);
		p2.add(new JLabel("   Plugin View Class"));
		p2.add(_pluginViewClass);
		p2.add(_refreshButton);

		setSize(new Dimension(460, 160));
		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {

		pluginJar_str = _pluginJar.getText();
		pluginViewClass_str = (String) _pluginViewClass.getSelectedItem();

		_closedOnOK = true;
		PluginDialog.this.setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		PluginDialog.this.setVisible(false);
	}

	public static Vector<String> getPluginViewsClasses(URL jarUrl) throws Exception {
		if (!jarUrl.toString().startsWith("jar:")) {
			jarUrl = new URL("jar:" + jarUrl + "!/");
		}
		URLClassLoader cl = new URLClassLoader(new URL[] { jarUrl }, PluginDialog.class.getClassLoader());
		Vector<String> list = new Vector<String>();
		JarURLConnection jarConnection = (JarURLConnection) jarUrl.openConnection();
		JarFile jarfile = jarConnection.getJarFile();
		Enumeration<?> enu = jarfile.entries();
		while (enu.hasMoreElements()) {
			String entry = enu.nextElement().toString();
			if (entry.endsWith(".class")) {
				String className = entry.replace('/', '.').substring(0, entry.length() - ".class".length());
				Class<?> c_ = cl.loadClass(className);
				try {
					c_.getConstructor(RGui.class);
					Vector<Class<?>> interfaces = new Vector<Class<?>>();
					for (int i = 0; i < c_.getInterfaces().length; ++i)
						interfaces.add(c_.getInterfaces()[i]);
					if (interfaces.contains(PluginView.class)) {
						list.add(className);
					}
				} catch (NoSuchMethodException e) {

				}
			}
		}
		return list;
	}

}
