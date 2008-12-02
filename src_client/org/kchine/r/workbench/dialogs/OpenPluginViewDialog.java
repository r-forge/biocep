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
package org.kchine.r.workbench.dialogs;

import static org.kchine.rpf.PoolUtils.unzip;


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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarFile;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.kchine.r.server.Utils;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.plugins.PluginViewDescriptor;
import org.kchine.rpf.PoolUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import server.ServerManager;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class OpenPluginViewDialog extends JDialog {

	public static final int JAR_MODE = 0;
	//public static final int ZIP_MODE = 1;
	public static final int CLASSES_MODE = 2;
	public static final int URL_MODE = 3;

	public static String pluginJar_str = "";
	public static String pluginViewName_str = "";
	private JTextField _pluginJar;
	private JComboBox _pluginViewClass;
	private JButton _chooseFile;
	private JButton _refreshButton;

	private JButton _ok;
	private JButton _cancel;

	Vector<PluginViewDescriptor> viewClassList = new Vector<PluginViewDescriptor>();

	private boolean _closedOnOK = false;

	public PluginViewDescriptor getPluginViewDetail() {
		if (_closedOnOK) {
			for (int i = 0; i < viewClassList.size(); ++i)
				if (viewClassList.elementAt(i).getName().equals(pluginViewName_str)) return viewClassList.elementAt(i);
			return null;
		} else {
			return null;
		}
	}

	public OpenPluginViewDialog(Component c,final int mode, boolean install) {
		super((Frame) null, true);

		setTitle("Please Choose Your Plugin " + (mode == JAR_MODE ? "Jar" : (mode == CLASSES_MODE ? "Classes Directory" : "Jar URL")));

		JPanel centralPanel = new JPanel(new GridLayout(0, 1));
		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2));
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(centralPanel, BorderLayout.CENTER);
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		_pluginJar = new JTextField(pluginJar_str);

		_pluginViewClass = new JComboBox(new Object[] { pluginViewName_str });
		_pluginViewClass.setSelectedItem(pluginViewName_str);
		_pluginViewClass.setEditable(true);

		_chooseFile = new JButton("Choose "+(mode==JAR_MODE?"Jar": "Classes Dir"));
		_chooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				final JFileChooser chooser = new JFileChooser();

				if (mode==JAR_MODE) {
					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				} else if (mode==CLASSES_MODE) {
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				} 

				int returnVal = chooser.showOpenDialog(OpenPluginViewDialog.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					try {
						_pluginJar.setText(chooser.getSelectedFile().getAbsolutePath());
						if (chooser.getSelectedFile().isDirectory()) {
							_pluginJar.setText(_pluginJar.getText() + "/");
						}
						viewClassList = getPluginViews(_pluginJar.getText());
						_pluginViewClass.removeAllItems();
						for (PluginViewDescriptor s : viewClassList)
							_pluginViewClass.addItem(s.getName());
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
					viewClassList = getPluginViews(_pluginJar.getText());
					_pluginViewClass.removeAllItems();
					for (PluginViewDescriptor s : viewClassList)
						_pluginViewClass.addItem(s.getName());
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

		p1.add(new JLabel("   Plugin " + (mode == JAR_MODE ? "Jar" : (mode == CLASSES_MODE ? "Classes Directory" : "Jar URL"))));
		p1.add(_pluginJar);
		if (mode != URL_MODE) p1.add(_chooseFile); else p1.add(_refreshButton);
		p2.add(new JLabel("   Plugin View "));
		p2.add(_pluginViewClass);
		if (mode != URL_MODE) p2.add(_refreshButton); else p2.add(new JLabel(""));
		setSize(new Dimension(560, 160));
		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {

		pluginJar_str = _pluginJar.getText();
		pluginViewName_str = (String) _pluginViewClass.getSelectedItem();

		_closedOnOK = true;
		OpenPluginViewDialog.this.setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		OpenPluginViewDialog.this.setVisible(false);
	}

	public static void scanClassesFiles(File node, Vector<String> result, int beginIndex) {
		if (!node.isDirectory() && node.getName().endsWith(".class")) {
			result.add(node.getAbsolutePath().substring(beginIndex));
			return;
		}
		File[] list = node.listFiles();
		if (list != null) {
			for (int i = 0; i < list.length; ++i) {
				scanClassesFiles(list[i], result, beginIndex);
			}
		}
	}

	public static void getViewsList(InputStream is, Vector<PluginViewDescriptor> result) throws Exception {
		{
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			domFactory.setValidating(false);
			DocumentBuilder documentBuilder = domFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(is);

			Vector<Node> viewNodes = new Vector<Node>();
			Utils.catchNodes(Utils.catchNode(document.getDocumentElement(), "plugin"), "view", viewNodes);
			for (int i = 0; i < viewNodes.size(); ++i) {
				NamedNodeMap attrs = viewNodes.elementAt(i).getAttributes();
				String c = attrs.getNamedItem("class").getNodeValue();
				String vname = null;
				if (attrs.getNamedItem("name") != null)
					vname = attrs.getNamedItem("name").getNodeValue();
				boolean ispanel = true;
				result.add(new PluginViewDescriptor(vname, c, null, null));
			}
		}

	}

	public static Vector<PluginViewDescriptor> getPluginViews(String pluginCodeBase) throws Exception {
		pluginCodeBase = pluginCodeBase.replace('\\', '/');

		boolean jarMode = !pluginCodeBase.endsWith("/");
		Vector<PluginViewDescriptor> viewsList = new Vector<PluginViewDescriptor>();
		Vector<String> classesList = new Vector<String>();
		URLClassLoader cl = null;

		URL jarUrl = null;
		if (pluginCodeBase.toLowerCase().startsWith("http:") || pluginCodeBase.toLowerCase().startsWith("file:")) {
			jarUrl = new URL(pluginCodeBase);
		} else {
			jarUrl = new File(pluginCodeBase).toURI().toURL();
		}

		if (jarMode) {

			if (!jarUrl.toString().startsWith("jar:")) {
				jarUrl = new URL("jar:" + jarUrl + "!/");
			}
			cl = new URLClassLoader(new URL[] { jarUrl }, OpenPluginViewDialog.class.getClassLoader());
			URLClassLoader descriptorClassLoader = new URLClassLoader(new URL[] { jarUrl }, null);
			if (descriptorClassLoader.getResource("descriptor.xml") != null) {
				getViewsList(descriptorClassLoader.getResourceAsStream("descriptor.xml"), viewsList);
			} else {

				JarURLConnection jarConnection = null;
				jarConnection = (JarURLConnection) jarUrl.openConnection();
				JarFile jarfile = jarConnection.getJarFile();
				Enumeration<?> enu = jarfile.entries();
				while (enu.hasMoreElements()) {
					String entry = enu.nextElement().toString();
					if (entry.endsWith(".class")) {
						classesList.add(entry);
					}
				}

				for (String entry : classesList) {
					String className = entry.replace('/', '.').replace('\\', '.').substring(0, entry.length() - ".class".length());
					Class<?> c_ = cl.loadClass(className);
					try {
						c_.getConstructor(RGui.class);
						viewsList.add(new PluginViewDescriptor(null, className, null, null));
					} catch (NoSuchMethodException e) {

					}
				}

			}

		} else {

			File descriptor = new File(new File(pluginCodeBase).getAbsoluteFile() + "/descriptor.xml");
			File classesDir = new File(new File(pluginCodeBase).getAbsoluteFile() + "/classes");
			File libDir = new File(new File(pluginCodeBase).getAbsoluteFile() + "/lib");

			File[] libList = new File[0];
			if (libDir.exists()) {
				libList = libDir.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".jar");
					}
				});
			}

			System.out.println(Arrays.toString(libList));

			Vector<URL> cl_urls = new Vector<URL>();
			if (classesDir.exists())
				cl_urls.add(classesDir.toURI().toURL());
			for (int i = 0; i < libList.length; ++i)
				cl_urls.add(libList[i].toURI().toURL());

			System.out.println(cl_urls);
			cl = new URLClassLoader(cl_urls.toArray(new URL[0]), OpenPluginViewDialog.class.getClassLoader());

			if (descriptor.exists()) {
				getViewsList(new FileInputStream(descriptor), viewsList);
			} else {

				if (classesDir.exists()) {
					scanClassesFiles(classesDir, classesList, classesDir.getAbsolutePath().length() + 1);
					for (String entry : classesList) {
						String className = entry.replace('/', '.').replace('\\', '.').substring(0, entry.length() - ".class".length());
						Class<?> c_ = cl.loadClass(className);
						try {
							c_.getConstructor(RGui.class);
							viewsList.add(new PluginViewDescriptor(null, className, null, null));
						} catch (NoSuchMethodException e) {

						}
					}
				}

				if (libList.length > 0) {
					for (int i = 0; i < libList.length; ++i)
						viewsList.addAll(getPluginViews(libList[i].toURI().toURL().toString()));
				}

			}
		}

		String pluginName = jarMode ? pluginCodeBase.substring(pluginCodeBase.lastIndexOf("/") + 1, pluginCodeBase.lastIndexOf(".")) : new File(pluginCodeBase)
				.getName();

		for (int i = 0; i < viewsList.size(); ++i) {
			PluginViewDescriptor pvd = viewsList.elementAt(i);
			pvd.setPluginName(pluginName);
			pvd.setPluginClassLoader(cl);
			if (pvd.getName() == null || pvd.getName().equals("")) {
				pvd.setName(pvd.getClassName().indexOf(".") != -1 ? pvd.getClassName().substring(pvd.getClassName().lastIndexOf(".") + 1) : pvd.getClassName());
			}
		}

		return viewsList;

	}

}
