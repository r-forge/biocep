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
package org.kchine.rpf.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ConsolePanel extends JPanel implements ClipboardOwner {

	public static SimpleAttributeSet BOLD_BLACK = null;
	public static SimpleAttributeSet BLACK = null;
	public static SimpleAttributeSet RED = null;

	private JTextPane _logArea = new JTextPane();
	private SubmitInterface _sInterface;
	private AbstractAction[] _actions;
	private JTextPane _commandInputField;
	JScrollPane _scrollPane = null;
	Vector<String> _commandsHistory = new Vector<String>();
	private int _commandsHistoryIndex = 0;
	private UndoManager um = new UndoManager();
	private boolean _stopLogThread = false;

	public Vector<String> getCommandHistory() {
		return _commandsHistory;
	}

	public void setCommandHistory(Vector<String> history) {
		_commandsHistory = history;
		_commandsHistoryIndex = _commandsHistory.size();
	}

	private Vector<LogUnit> _logActions = new Vector<LogUnit>();

	synchronized public Vector<LogUnit> popAllLogActions(int maxNbrLogActions) {
		if (_logActions.size() == 0)
			return null;
		Vector<LogUnit> result = (Vector<LogUnit>) _logActions.clone();
		if (maxNbrLogActions != -1 && result.size() > maxNbrLogActions) {
			int delta = result.size() - maxNbrLogActions;
			for (int i = 0; i < delta; ++i) {
				result.remove(result.size() - 1);
			}
		}
		for (int i = 0; i < result.size(); ++i)
			_logActions.remove(0);
		return result;
	}

	public void print(final String cmd, final String log, SimpleAttributeSet logAttributeSet) {
		_logActions.add(new LogUnit(cmd, log, logAttributeSet));
	}

	public void print(final String cmd, final String log) {
		print(cmd, log, BLACK);
	}

	public void play(final String command, boolean demo) {

		if (!demo) {
			_commandInputField.setText(command);
		} else {
			for (int i = 0; i <= command.length(); ++i) {
				_commandInputField.setText(command.substring(0, i));
				_commandInputField.setCaretPosition(_commandInputField.getText().length());
				// Toolkit.getDefaultToolkit().beep();
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
							_commandInputField.setEnabled(false);
						}
					});

					_commandsHistory.add(command);
					_commandsHistoryIndex = _commandsHistory.size();

					log = _sInterface.submit(command);

					if (log != null) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								print(command, log);
							}
						});
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				} finally {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							_commandInputField.setText("");
							_commandInputField.setEnabled(true);
							_commandInputField.requestFocus();
							um = new UndoManager();
						}
					});

				}
			}
		}).start();
	}

	HashMap<String, Action> inputFiledActionsTable = new HashMap<String, Action>();
	public ConsolePanel(SubmitInterface sInterface, String textFieldLabel, Color textFieldColor, String buttonLabel, final boolean undoRedoEnabled, AbstractAction[] actions) {
		_sInterface = sInterface;
		_actions = actions;
		BOLD_BLACK = new SimpleAttributeSet();
		StyleConstants.setForeground(BOLD_BLACK, Color.blue);
		StyleConstants.setFontFamily(BOLD_BLACK, "Monospaced.plain");
		StyleConstants.setFontSize(BOLD_BLACK, _logArea.getFont().getSize());

		BLACK = new SimpleAttributeSet();
		StyleConstants.setForeground(BLACK, Color.black);
		StyleConstants.setFontFamily(BLACK, "Monospaced.plain");
		StyleConstants.setFontSize(BLACK, _logArea.getFont().getSize());

		RED = new SimpleAttributeSet();
		StyleConstants.setForeground(RED, Color.red);
		StyleConstants.setFontFamily(RED, "Monospaced.plain");
		StyleConstants.setFontSize(RED, _logArea.getFont().getSize());

		_logArea.setEditable(false);
		_scrollPane = new JScrollPane(_logArea);
		//_scrollPane.setBorder(BorderFactory.createLineBorder(Color.blue, 2));

		_commandInputField = new JTextPane();

		_logArea.setFont(new Font("Monospaced.plain", Font.PLAIN, _logArea.getFont().getSize()));
		_commandInputField.setFont(new Font("Monospaced.plain", Font.PLAIN, _commandInputField.getFont().getSize()));

		Action[] inputFieldActions = _commandInputField.getEditorKit().getActions();
		for (int i = 0; i < inputFieldActions.length; ++i) {
			inputFiledActionsTable.put((String) inputFieldActions[i].getValue(Action.NAME), inputFieldActions[i]);
		}
		final Action copyAction = inputFiledActionsTable.get(DefaultEditorKit.copyAction);
		final Action pasteAction = inputFiledActionsTable.get(DefaultEditorKit.pasteAction);
		final Action cutAction = inputFiledActionsTable.get(DefaultEditorKit.cutAction);

		final Action insertBlankLine = new AbstractAction("Insert Blank Line    Ctrl-Enter") {
			public void actionPerformed(ActionEvent e) {
				String text = _commandInputField.getText();
				int cartetPosition = _commandInputField.getCaretPosition();
				_commandInputField.setText(text.substring(0, cartetPosition) + "\n" + text.substring(cartetPosition));
				_commandInputField.setCaretPosition(cartetPosition + 1);
			}

			public boolean isEnabled() {
				return true;
			}
		};

		if (undoRedoEnabled) {
			_commandInputField.getDocument().addUndoableEditListener(new UndoableEditListener() {
				public void undoableEditHappened(UndoableEditEvent e) {
					if (_commandInputField.getText().length() > 200 * 10) {
						e.getEdit().undo();
						Toolkit.getDefaultToolkit().beep();
					} else {
						um.addEdit(e.getEdit());
					}
				}
			});
		}

		_commandInputField.addVetoableChangeListener(new VetoableChangeListener() {
			public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
				System.out.println(evt);

			}
		});

		_commandInputField.addMouseListener(new MouseAdapter() {
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

					popupMenu.add(insertBlankLine);
					popupMenu.addSeparator();
					popupMenu.add(new AbstractAction("Copy                 Ctrl-C") {
						public void actionPerformed(ActionEvent e) {
							copyAction.actionPerformed(e);
						}

						@Override
						public boolean isEnabled() {
							return copyAction.isEnabled();
						}
					});

					popupMenu.add(new AbstractAction("Paste                Ctrl-V") {
						public void actionPerformed(ActionEvent e) {
							pasteAction.actionPerformed(e);
						}

						@Override
						public boolean isEnabled() {
							return pasteAction.isEnabled();
						}
					});

					popupMenu.add(new AbstractAction("Cut                    Ctrl-X") {
						public void actionPerformed(ActionEvent e) {
							cutAction.actionPerformed(e);
						}

						@Override
						public boolean isEnabled() {
							return cutAction.isEnabled();
						}
					});

					if (undoRedoEnabled) {

						popupMenu.addSeparator();

						popupMenu.add(new AbstractAction("Undo                 Ctrl-Z") {
							public void actionPerformed(ActionEvent e) {
								try {
									um.undo();
								} catch (CannotUndoException ex) {
									ex.printStackTrace();
								}
							}

							@Override
							public boolean isEnabled() {
								return um.canUndo();
							}
						});

						popupMenu.add(new AbstractAction("Redo                 Ctrl-Y") {
							public void actionPerformed(ActionEvent e) {
								try {
									um.redo();
								} catch (CannotRedoException ex) {
									ex.printStackTrace();
								}
							}

							@Override
							public boolean isEnabled() {
								return um.canRedo();
							}
						});
					}

					popupMenu.show(_commandInputField, e.getX(), e.getY());
				}
			}
		});

		_commandInputField.addKeyListener(new KeyListener() {

			public int countCRBeforeCartet() {
				int result = 0;
				for (int i = 0; i < _commandInputField.getCaretPosition(); ++i)
					if (_commandInputField.getText().charAt(i) == '\n')
						++result;
				return result;
			}

			public int countCRAfterCartet() {
				int result = 0;
				for (int i = _commandInputField.getCaretPosition(); i < _commandInputField.getText().length(); ++i)
					if (_commandInputField.getText().charAt(i) == '\n')
						++result;
				return result;
			}
			
			public int countCR() {
				int result = 0;
				for (int i = 0; i < _commandInputField.getText().length(); ++i)
					if (_commandInputField.getText().charAt(i) == '\n')
						++result;
				return result;
			}

			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {

					if ( (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK
							|| (e.getModifiers() & KeyEvent.SHIFT_MASK) == KeyEvent.SHIFT_MASK
					
					) {
						insertBlankLine.actionPerformed(null);
					} else {
						// if (_commandInputField.getText().trim().equals(""))
						// return;
						play(_commandInputField.getText(), false);
					}
				}
				if (e.getKeyCode() == 38) {

					if (countCR() == 0 || ((e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK)) {
						if (_commandsHistoryIndex == 0)
							return;
						--_commandsHistoryIndex;
						_commandInputField.setText(_commandsHistory.elementAt(_commandsHistoryIndex));
					}

				} else if (e.getKeyCode() == 40) {

					if (countCR() == 0 || ((e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK)) {
						if (_commandsHistoryIndex >= (_commandsHistory.size() - 1))
							return;
						++_commandsHistoryIndex;
						_commandInputField.setText(_commandsHistory.elementAt(_commandsHistoryIndex));
					}

				} else if (e.getKeyCode() == 90 && (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK) {
					if (um.canUndo())
						um.undo();
					else
						Toolkit.getDefaultToolkit().beep();
				} else if (e.getKeyCode() == 89 && (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK) {
					if (um.canRedo())
						um.redo();
					else
						Toolkit.getDefaultToolkit().beep();
				}

			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

		});

		setLayout(new BorderLayout());
		add(_scrollPane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());

		JLabel label = new JLabel(textFieldLabel + " : ");
		label.setForeground(textFieldColor);

		bottomPanel.add(label, BorderLayout.WEST);		
		bottomPanel.add(_commandInputField, BorderLayout.CENTER);

		if (buttonLabel!=null) {
			JButton b=new JButton(buttonLabel);
			b.setFont(b.getFont().deriveFont(4));
			b.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					play(_commandInputField.getText(), false);				
				}
			});
			bottomPanel.add(b, BorderLayout.EAST);
		}
		
		
		add(bottomPanel, BorderLayout.SOUTH);

		_logArea.addMouseListener(new MouseAdapter() {
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
								BufferedReader in = new BufferedReader(new StringReader((String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(
										ConsolePanel.this).getTransferData(DataFlavor.stringFlavor))

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
							_logArea.setText("");
						}

						@Override
						public boolean isEnabled() {
							return !_logArea.getText().equals("");
						}
					});

					popupMenu.show(_logArea, e.getX(), e.getY());
				}
			}
		});

		_commandInputField.requestFocus();

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (_stopLogThread)
						break;
					if (_logActions.size() > 0) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								Vector<LogUnit> logActions = popAllLogActions(-1);
								if (logActions != null) {
									for (int i = 0; i < logActions.size(); ++i) {
										String cmd = logActions.elementAt(i).getCmd();
										String log = logActions.elementAt(i).getLog();
										SimpleAttributeSet logAttributeSet = logActions.elementAt(i).getLogAttributeSet();
										if (cmd != null)
											insertText("> " + cmd + "\n", BOLD_BLACK);
										if (log != null)
											insertText(log, logAttributeSet);
									}

									if (_logActions.size() == 0) {
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
									}

								}

							};
						});
					}
					try {
						Thread.sleep(50);
					} catch (Exception e) {
					}
				}
			}
		}).start();

	}

	protected void insertText(String text, AttributeSet set) {
		try {
			_logArea.getDocument().insertString(_logArea.getDocument().getLength(), text, set);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	protected void setEndSelection() {
		_logArea.setSelectionStart(_logArea.getDocument().getLength());
		_logArea.setSelectionEnd(_logArea.getDocument().getLength());
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {

	}

	public void setCursor(Cursor cursor) {
		super.setCursor(cursor);
		_logArea.setCursor(cursor);
	}

	public void stopLogThread() {
		_stopLogThread = true;
	}

	public JTextPane getCommandInputField() {
		return _commandInputField;
	}
	
	public void pasteToConsoleEditor() {
		final Clipboard clipboard = getToolkit().getSystemClipboard();
		Transferable clipData = clipboard.getContents(clipboard);
		if (clipData != null) {
				if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					try {
						String s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
						_commandInputField.setText(_commandInputField.getText()+s);	
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		}
	}

}
