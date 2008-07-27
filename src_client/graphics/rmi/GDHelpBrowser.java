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
package graphics.rmi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GDHelpBrowser extends JPanel implements HyperlinkListener, ActionListener, ClipboardOwner {

	private JButton _homeButton;
	private JButton _backButton;
	private JButton _forwardButton;

	private JTextField _urlField;
	private JEditorPane _htmlPane;
	private GDApplet _applet;

	public GDHelpBrowser(GDApplet applet) {
		this._applet = applet;

		JPanel topPanel = new JPanel();
		topPanel.setBackground(Color.lightGray);
		topPanel.setLayout(new BorderLayout());

		_homeButton = new JButton("home");
		_homeButton.addActionListener(this);

		_backButton = new JButton("<");
		_backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cursor > 0) {
					--cursor;
					try {
						setURL(history.elementAt(cursor), false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});

		_forwardButton = new JButton(">");
		_forwardButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cursor < (history.size() - 1)) {
					++cursor;

					try {
						setURL(history.elementAt(cursor), false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					Toolkit.getDefaultToolkit().beep();
				}

			}
		});

		JLabel urlLabel = new JLabel("   URL:  ");
		_urlField = new JTextField(30);
		_urlField.setText(applet.getDefaultHelpUrl());
		_urlField.addActionListener(this);

		_urlField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {

					if (_urlField.getText().trim().equals(""))
						return;

					new Thread(new Runnable() {

						public void run() {

							try {

								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										try {
											setURL(_urlField.getText());
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								});

							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}).start();

				}
			}

			public void keyReleased(KeyEvent e) {

			}

			public void keyTyped(KeyEvent e) {

			}

		});

		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
		buttonPanel.add(_backButton);
		buttonPanel.add(_forwardButton);
		buttonPanel.add(_homeButton);

		JPanel blPanel = new JPanel(new BorderLayout());
		blPanel.add(buttonPanel, BorderLayout.WEST);
		blPanel.add(urlLabel, BorderLayout.EAST);
		topPanel.add(blPanel, BorderLayout.WEST);
		topPanel.add(_urlField, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		add(topPanel, BorderLayout.NORTH);

		try {
			_htmlPane = new JEditorPane(applet.getDefaultHelpUrl() + ";jsessionid=" + applet.getSessionId());
			_htmlPane.setEditable(false);
			_htmlPane.addHyperlinkListener(this);

			JPopupMenu menu = new JPopupMenu();
			menu.add(new AbstractAction("Copy") {
				public void actionPerformed(ActionEvent e) {
					copySelectionToClipboard();
				}
			});

			_htmlPane.addMouseListener(new GDApplet.PopupListener(menu));

			_htmlPane.addKeyListener(new KeyListener() {
				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
					if (((byte) e.getKeyChar()) == 3 && e.getModifiers() == 2) {
						copySelectionToClipboard();
					}
				}
			});

			JScrollPane scrollPane = new JScrollPane(_htmlPane);
			add(scrollPane, BorderLayout.CENTER);
		} catch (IOException ioe) {
			warnUser("Can't build HTML pane for " + applet.getDefaultHelpUrl() + ": " + ioe);
		}

		Dimension screenSize = getToolkit().getScreenSize();
		int width = screenSize.width * 8 / 10;
		int height = screenSize.height * 8 / 10;
		setBounds(width / 8, height / 8, width, height);

	}

	public void copySelectionToClipboard() {
		StringSelection stringSelection = new StringSelection(_htmlPane.getSelectedText());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, GDHelpBrowser.this);
	}

	public void actionPerformed(ActionEvent event) {
		String url;
		if (event.getSource() == _urlField)
			url = _urlField.getText();
		else
			url = _applet.getDefaultHelpUrl();

		try {
			setURL(url);
		} catch (Exception ioe) {
			warnUser("Can't follow link to " + url + ": " + ioe);
		}
	}

	int cursor = -1;
	Vector<String> history = new Vector<String>();

	private void setURL(String url, boolean appendToHistory) throws Exception {

		if (appendToHistory) {
			history.setSize(cursor + 1);
			history.add(url);
			cursor = history.size() - 1;
		}

		if (_applet.getSessionId() == null) {
			System.out.println("No Session");
			JOptionPane.showMessageDialog(this, "you are not logged on");
			return;
		}

		int sp = url.indexOf(";jsessionid");
		if (sp != -1) {
			url = url.substring(0, sp) + url.substring(sp + ";jsessionid=".length() + 32, url.length());
		}

		int sref = url.indexOf("#");
		String ref = null;
		if (sref != -1) {
			ref = url.substring(sref + 1);
			url = url.substring(0, sref);
		}

		if (url.startsWith(_applet.getHelpServletUrl())) {
			_htmlPane.setPage(new URL(url + ";jsessionid=" + _applet.getSessionId()));
		} else {
			_htmlPane.setPage(new URL(url));
		}

		_urlField.setText(new URL(url).toExternalForm());
		if (ref != null) {
			_htmlPane.scrollToReference(ref);
		}

	}

	public void setURL(String url) throws Exception {
		setURL(url, true);
	}

	public void hyperlinkUpdate(HyperlinkEvent event) {

		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {

			try {
				setURL(event.getURL().toString());
			} catch (Exception ioe) {
				warnUser("Can't follow link to " + event.getURL().toExternalForm() + ": " + ioe);
			}
		}
	}

	private void warnUser(String message) {
		JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}
}
