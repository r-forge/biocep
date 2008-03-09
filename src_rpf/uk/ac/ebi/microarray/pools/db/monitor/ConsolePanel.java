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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class ConsolePanel extends JPanel implements ClipboardOwner {

	private static SimpleAttributeSet BOLD_BLACK = new SimpleAttributeSet();
	private static SimpleAttributeSet BLACK = new SimpleAttributeSet();
	private JTextPane _textArea = new JTextPane();
	private SubmitInterface _sInterface;
	private AbstractAction[] _actions;
	private JTextField _textField;
	JScrollPane _scrollPane = null;
	Vector<String> _commandsHistory = new Vector<String>();
	int _commandsHistoryIndex = 0;

	public ConsolePanel(SubmitInterface sInterface) {
		this(sInterface, null);
	}

	public Vector<String> getCommandHistory() {
		return _commandsHistory;
	}

	public void setCommandHistory(Vector<String> history) {
		_commandsHistory = history;
		_commandsHistoryIndex = _commandsHistory.size();
	}

	public void print(final String cmd, final String log) {
		if (cmd != null)
			insertText("> " + cmd + "\n", BOLD_BLACK);
		if (log != null)
			insertText(log, BLACK);
		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						JScrollBar scrollBar = _scrollPane.getVerticalScrollBar();
						scrollBar.setValue(scrollBar.getMaximum());
					}
				});
			}
		}).start();

		_textField.setText("");
		_textField.setEnabled(true);
		_textField.requestFocus();

	}

	public void play(String command, boolean demo) {

		if (!demo) {
			_textField.setText(command);
		} else {
			for (int i = 0; i <= command.length(); ++i) {
				_textField.setText(command.substring(0, i));
				_textField.setCaretPosition(_textField.getText().length());
				//Toolkit.getDefaultToolkit().beep();  
				try {
					Thread.sleep(30);
				} catch (Exception e) {
				}
			}
		}

		new Thread(new Runnable() {
			String log;

			public void run() {

				try {

					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							_textField.setEnabled(false);
						}
					});

					_commandsHistory.add(_textField.getText());
					_commandsHistoryIndex = _commandsHistory.size();
					final String cmd = _textField.getText();
					log = _sInterface.submit(cmd);

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							print(cmd, log);
						}
					});

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}).start();
	}

	public ConsolePanel(SubmitInterface sInterface, AbstractAction[] actions) {
		_sInterface = sInterface;
		_actions = actions;
		StyleConstants.setForeground(BOLD_BLACK, Color.blue);
		StyleConstants.setFontFamily(BOLD_BLACK, "Monospaced.plain");
		StyleConstants.setFontSize(BOLD_BLACK, _textArea.getFont().getSize());

		StyleConstants.setForeground(BLACK, Color.black);
		StyleConstants.setFontFamily(BLACK, "Monospaced.plain");
		StyleConstants.setFontSize(BLACK, _textArea.getFont().getSize());

		_textArea.setEditable(false);
		_scrollPane = new JScrollPane(_textArea);
		_scrollPane.setBorder(BorderFactory.createLineBorder(Color.blue, 2));

		_textField = new JTextField();

		_textArea.setFont(new Font("Monospaced.plain", Font.PLAIN, _textArea.getFont().getSize()));
		_textField.setFont(new Font("Monospaced.plain", Font.PLAIN, _textField.getFont().getSize()));

		_textField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {

					if (_textField.getText().trim().equals(""))
						return;
					
					System.out.println("before play");
					play(_textField.getText(), false);
					System.out.println("after play");

				} else {

					if (e.getKeyCode() == 38) {
						if (_commandsHistoryIndex == 0)
							return;
						--_commandsHistoryIndex;
						_textField.setText(_commandsHistory.elementAt(_commandsHistoryIndex));

					} else if (e.getKeyCode() == 40) {
						if (_commandsHistoryIndex >= (_commandsHistory.size() - 1))
							return;
						++_commandsHistoryIndex;
						_textField.setText(_commandsHistory.elementAt(_commandsHistoryIndex));
					}

				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

		});

		setLayout(new BorderLayout());
		add(_scrollPane, BorderLayout.CENTER);
		add(_textField, BorderLayout.SOUTH);

		_textArea.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				checkPopup(e);
			}

			public void mouseClicked(MouseEvent e) {
				checkPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				checkPopup(e);
			}

			private void checkPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					JPopupMenu popupMenu = new JPopupMenu();

					if (_actions != null) {
						for (int i = 0; i < _actions.length; ++i) {
							if (_actions[i] == null) {
								popupMenu.addSeparator();
							} else {
								popupMenu.add(_actions[i]);
							}
						}

						popupMenu.addSeparator();
					}

					popupMenu.add(new AbstractAction("Copy History To Clipboard") {
						public void actionPerformed(ActionEvent e) {
							StringBuffer sb = new StringBuffer();
							for (int i = 0; i < _commandsHistory.size(); ++i) {
								sb.append(_commandsHistory.elementAt(i));
								sb.append("\n");
							}

							StringSelection stringSelection = new StringSelection(sb.toString());
							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(stringSelection, ConsolePanel.this);
						}

						@Override
						public boolean isEnabled() {
							return true;
						}
					});

					popupMenu.add(new AbstractAction("Paste History From Clipboard") {
						public void actionPerformed(ActionEvent e) {

							try {
								BufferedReader in = new BufferedReader(new StringReader((String) Toolkit
										.getDefaultToolkit().getSystemClipboard().getContents(ConsolePanel.this)
										.getTransferData(DataFlavor.stringFlavor))

								);
								String line;

								while ((line = in.readLine()) != null) {
									_commandsHistory.add(line);
								}
								_commandsHistoryIndex = _commandsHistory.size();
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}

						@Override
						public boolean isEnabled() {
							return true;
						}
					});

					popupMenu.add(new AbstractAction("Clear History") {
						public void actionPerformed(ActionEvent e) {
							_commandsHistory = new Vector<String>();
							_commandsHistoryIndex = 0;
						}

						@Override
						public boolean isEnabled() {
							return true;
						}
					});

					popupMenu.addSeparator();
					popupMenu.add(new AbstractAction("Clean") {
						public void actionPerformed(ActionEvent e) {
							_textArea.setText("");
						}

						@Override
						public boolean isEnabled() {
							return !_textArea.getText().equals("");
						}
					});

					popupMenu.show(_textArea, e.getX(), e.getY());
				}
			}
		});

		_textField.requestFocus();

	}

	protected void insertText(String text, AttributeSet set) {
		try {
			_textArea.getDocument().insertString(_textArea.getDocument().getLength(), text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	protected void setEndSelection() {
		_textArea.setSelectionStart(_textArea.getDocument().getLength());
		_textArea.setSelectionEnd(_textArea.getDocument().getLength());
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
	}

}
