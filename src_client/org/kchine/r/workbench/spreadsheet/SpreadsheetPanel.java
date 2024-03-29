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
package org.kchine.r.workbench.spreadsheet;

import net.infonode.docking.View;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.kchine.r.RObject;
import org.kchine.r.server.RServices;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.scripting.GroovyInterpreter;
import org.kchine.r.server.spreadsheet.AbstractSpreadsheetModel;
import org.kchine.r.server.spreadsheet.Cell;
import org.kchine.r.server.spreadsheet.CellPoint;
import org.kchine.r.server.spreadsheet.CellRange;
import org.kchine.r.server.spreadsheet.ExportInfo;
import org.kchine.r.server.spreadsheet.Formula;
import org.kchine.r.server.spreadsheet.ImportInfo;
import org.kchine.r.server.spreadsheet.ModelUtils;
import org.kchine.r.server.spreadsheet.Node;
import org.kchine.r.server.spreadsheet.SpreadsheetListener;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemote;
import org.kchine.r.server.spreadsheet.SpreadsheetModelRemoteImpl;
import org.kchine.r.server.spreadsheet.SpreadsheetTableModelClipboardInterface;
import org.kchine.r.workbench.CellsChangeListener;
import org.kchine.r.workbench.ConsoleLogger;
import org.kchine.r.workbench.RConnectionListener;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.VariablesChangeListener;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.graphics.JGDPanelPop;
import org.kchine.r.workbench.macros.MacroInterface;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.YesSecurityManager;

import static javax.swing.JOptionPane.*;
import static org.kchine.rpf.PoolUtils.*;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SpreadsheetPanel extends JPanel implements ClipboardOwner {

	public static SpreadsheetModelRemoteImpl tmri;

	public static void main(String[] args) throws Exception {

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new YesSecurityManager());
		}
		// tmri=new SpreadsheetModelRemoteImpl(3,2, new HashMap<String,
		// SpreadsheetModelRemoteImpl>());
		// SpreadsheetModelRemote
		// modelRemote=(SpreadsheetModelRemote)java.rmi.server.RemoteObject.toStub(tmri);

		final RServices r = (RServices) LocateRegistry.getRegistry().lookup("RSERVANT_1");

		r.consoleSubmit("toto<-mean");

		SpreadsheetModelRemote modelRemote = r.newSpreadsheetTableModelRemote(300, 10);
		AbstractSpreadsheetModel abstractTableModel1 = ModelUtils.getSpreadsheetTableModelWrapper(modelRemote);
		AbstractSpreadsheetModel abstractTableModel2 = ModelUtils.getSpreadsheetTableModelWrapper(modelRemote);

		/*
		 * final String cmdUrl = "http://127.0.0.1:8080/rvirtual/cmd";
		 * HashMap<String, Object> options = new HashMap<String, Object>();
		 * options.put("privatename", "tata"); options.put("urls", new URL[] {
		 * new URL("http://127.0.0.1:8080/rws/mapping/classes/") }); final
		 * String sessionId = RHttpProxy.logOn(cmdUrl, "", "test", "test",
		 * options); final RServices r = RHttpProxy.getR(cmdUrl, sessionId,
		 * true);
		 */

		// SpreadsheetModelRemote
		// modelRemote=r.newSpreadsheetTableModelRemote(10, 10);
		// SpreadsheetModelDevice d=modelRemote.newSpreadsheetModelDevice();
		// SpreadsheetModelDevice d =
		// RHttpProxy.newSpreadsheetModelDevice(cmdUrl, sessionId, "", "5",
		// "5");
		// AbstractSpreadsheetModel abstractTableModel1 =
		// ModelUtils.getSpreadsheetTableModelWrapper(new
		// SpreadsheetModelRemoteProxy(d));
		// SpreadsheetModelDevice d2 =
		// RHttpProxy.newSpreadsheetModelDevice(cmdUrl, sessionId,
		// d.getSpreadsheetModelId(), "", "");
		// AbstractSpreadsheetModel abstractTableModel2 =
		// ModelUtils.getSpreadsheetTableModelWrapper(new
		// SpreadsheetModelRemoteProxy(d2));
		RGui rgui = new RGui() {

			ReentrantLock _lock = new ReentrantLock() {
				@Override
				public void lock() {
					super.lock();
				}

				@Override
				public void unlock() {
					super.unlock();
				}

				@Override
				public boolean isLocked() {
					return super.isLocked();
				}
			};

			public ConsoleLogger getConsoleLogger() {
				return null;
			}

			public View createView(Component panel, String title) {
				return null;
			}

			public RServices getR() {
				return r;
			}

			public ReentrantLock getRLock() {
				return _lock;
			}

			public void setCurrentDevice(GDDevice device) {

			}

			public GDDevice getCurrentDevice() {
				return null;
			}

			public Component getRootComponent() {
				return null;
			}

			public JGDPanelPop getCurrentJGPanelPop() {
				return null;
			}

			public GroovyInterpreter getGroovyInterpreter() {
				return null;
			}

			public void upload(File localFile, String fileName) throws Exception {

			}

			public String getUserName() {
				return null;
			}

			public String getUID() {
				return null;
			}
			
			public String getInstallDir() {
				return null;
			}
			public Vector<MacroInterface> getMacros() {
				
				return null;
			}
			
			public void pushTask(Runnable task) {
				
				
			}
			public String getHelpRootUrl() {
			
				return null;
			}
			public String getSessionId() {
			
				return null;
			}
			public void addCellsChangeListener(CellsChangeListener listener) {
			}
			
			public void addVariablesChangeListener(VariablesChangeListener listener) {
			}
			
			public void removeCellsChangeListener(CellsChangeListener listener) {
			}
			
			public void removeVariablesChangeListener(VariablesChangeListener listener) {
			}
			
			public void addRConnectionListener(RConnectionListener listener) {
			
				
			}
			public void removeRConnectionListener(RConnectionListener listener) {
			
				
			}
			
			public String getPluginsDir() {
				return null;
			}
			
			public HashSet<String> getAvailableExtensions() {
				return null;
			}
			
			public void addEmbeddedPanelDescription(EmbeddedPanelDescription embeddedPanelDescription) {
				// TODO Auto-generated method stub
				
			}
			
			public void removeEmbeddedPanelDescription(EmbeddedPanelDescription embeddedPanelDescription) {
				// TODO Auto-generated method stub
				
			}
			
			public Vector<EmbeddedPanelDescription> getEmbeddedPanelDescriptions() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		JFrame f = new JFrame("F1");
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new SpreadsheetPanel(abstractTableModel1, rgui), BorderLayout.CENTER);
		f.setSize(new Dimension(800, 800));
		f.pack();
		f.setVisible(true);

		JFrame f2 = new JFrame("F2");
		f2.getContentPane().setLayout(new BorderLayout());
		f2.getContentPane().add(new SpreadsheetPanel(abstractTableModel2, rgui), BorderLayout.CENTER);
		f2.setSize(new Dimension(800, 800));
		f2.pack();
		f2.setVisible(true);

	}

	private JSpreadsheet ss;
	private CopyAction copy = new CopyAction();
	private CutAction cut = new CutAction();
	private PasteAction paste = new PasteAction();
	private FromRAction fromR = new FromRAction();
	private ToRAction toR = new ToRAction();
	private EvalAction eval = new EvalAction();
	private SelectAllAction selectall = new SelectAllAction();
	private UndoAction undo = new UndoAction();
	private FillAction fill = new FillAction();
	private ClearAction clear = new ClearAction();
	private RedoAction redo = new RedoAction();
	private SortColumnAction sort = new SortColumnAction();
	private InsertColumnAction insertColumn = new InsertColumnAction();
	private InsertRowAction insertRow = new InsertRowAction();
	private RemoveColumnAction removeColumn = new RemoveColumnAction();
	private RemoveRowAction removeRow = new RemoveRowAction();
	private FindAction find = new FindAction();
	private FindNextAction findNext = new FindNextAction();
	private RGui _rgui = null;
	private JTextField rangeTextField;

	private ListSelectionListener sl = new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {

			// new Thread(new Runnable(){ public void run() {

			copy.update();
			cut.update();
			sort.update();
			insertColumn.update();
			removeColumn.update();
			insertRow.update();
			removeRow.update();
			fill.update();
			clear.update();
			fromR.update();
			toR.update();
			eval.update();
			// }}).start();
		}
	};

	private String findValue;

	private boolean matchCase;
	private boolean matchCell;

	private ListSelectionListener selectionListener;

	private void updateSelectionRangeField() {
		if (ss.getSelectedRange() != null) {
			String rect = Formula.getCellString(ss.getSelectedRange().getStartRow(), ss.getSelectedRange().getStartCol()) + ":"
					+ Formula.getCellString(ss.getSelectedRange().getEndRow(), ss.getSelectedRange().getEndCol());
			if (ss.getSelectedRange().getStartCol() == ss.getSelectedRange().getEndCol()
					&& ss.getSelectedRange().getStartRow() == ss.getSelectedRange().getEndRow()) {
				rangeTextField.setText(rect.substring(rect.indexOf(':') + 1));
			} else {
				rangeTextField.setText(rect);
			}

		} else {
			rangeTextField.setText("");
		}		
	}
	
	public SpreadsheetPanel(final AbstractTableModel m, RGui rgui) {
		super();
		_rgui = rgui;
		
		if (m instanceof AbstractSpreadsheetModel) {
			ss = new JSpreadsheet(m, rgui, ((AbstractSpreadsheetModel) m).getSpreadsheetModelId());
		} else {
			ss = new JSpreadsheet(m, rgui, "Local");
		}

		selectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (m instanceof AbstractSpreadsheetModel) {
					((AbstractSpreadsheetModel) m).setSpreadsheetSelection(ss.getId(), ss.getSelectedRange());
				}
				updateSelectionRangeField();
			}
		};

		ss.getTable().getSelectionModel().addListSelectionListener(sl);
		ss.getTable().getColumnModel().getSelectionModel().addListSelectionListener(sl);

		ss.getTable().getSelectionModel().addListSelectionListener(selectionListener);
		ss.getTable().getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);

		try {
			ss.addSpreadsheetListener(new SpreadsheetListener() {

				public void setSelection(final String origin, final CellRange sel) {
					if (origin != null && origin.equals(ss.getId()))
						return;

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {

							try {
								ss.getTable().getSelectionModel().removeListSelectionListener(selectionListener);
								ss.getTable().getColumnModel().getSelectionModel().removeListSelectionListener(selectionListener);

								if (sel == null) {
									ss.getTable().getSelectionModel().setSelectionInterval(0, 0);
									return;
								}
								// validate sel
								int maxRow = ss.getTable().getRowCount() - 1;
								int maxCol = ss.getTable().getColumnCount() - 1;

								int startRow = sel.getStartRow();
								int startCol = sel.getStartCol();
								int endRow = sel.getEndRow();
								int endCol = sel.getEndCol();

								ss.getTable().setColumnSelectionInterval(Math.min(startCol, maxCol), Math.min(endCol, maxCol));
								ss.getTable().setRowSelectionInterval(Math.min(startRow, maxRow), Math.min(endRow, maxRow));

							} catch (Throwable e) {
								e.printStackTrace();
							} finally {
								ss.getTable().getSelectionModel().addListSelectionListener(selectionListener);
								ss.getTable().getColumnModel().getSelectionModel().addListSelectionListener(selectionListener);
							}
							
							updateSelectionRangeField();
						}
					});

					SwingUtilities.invokeLater(new Runnable() {

						public boolean isCellVisible(JTable table, int rowIndex, int vColIndex) {
							if (!(table.getParent() instanceof JViewport)) {
								return false;
							}
							JViewport viewport = (JViewport) table.getParent();
							Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
							Point pt = viewport.getViewPosition();
							rect.setLocation(rect.x - pt.x, rect.y - pt.y);
							return new Rectangle(viewport.getExtentSize()).contains(rect);
						}

						public boolean isOneCell(CellRange range) {
							return range.getEndRow() == range.getStartRow() && range.getEndCol() == range.getStartCol();
						}

						public void run() {
							try {
								Rectangle rectUpLeft = ss.getTable().getCellRect(sel.getStartRow(), sel.getStartCol(), true);
								Rectangle rectDownRight = ss.getTable().getCellRect(sel.getEndRow(), sel.getEndCol(), true);
								Rectangle selectionRect = new Rectangle(rectUpLeft.x, rectUpLeft.y, rectDownRight.x + rectDownRight.width, rectDownRight.y
										+ rectDownRight.height);

								if (isOneCell(sel) && isCellVisible(ss.getTable(), sel.getStartRow(), sel.getStartCol())) {

								} else {
									ss.getTable().scrollRectToVisible(selectionRect);
								}
							} catch (Exception e) {
							}
						}
					});
				}

				public void updateRedoAction() {
					redo.update();
				}

				public void updateUndoAction() {
					undo.update();
				}

				public void discardCache() {
				}

				public void discardCacheRange(CellRange range) {
				}

				public void discardCacheCell(int row, int col) {
				}

				public void discardColumnCount() {
				}

				public void discardRowCount() {
				}

				public void removeColumns(final int removeNum) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							for (int i = 0; i < removeNum; i++) {
								TableColumnModel tm = ss.getTable().getColumnModel();
								tm.removeColumn(tm.getColumn(tm.getColumnCount() - 1));
							}
						}
					});
				}

				public void insertColumn(final int insertNum, final int startCol) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							long t1 = System.currentTimeMillis();
							System.out.println("SpreadsheetPanel.insertColumn t1=" + t1);
							int lastCol = ss.getTable().getColumnCount() - 1;
							TableColumnModel tm = ss.getTable().getColumnModel();
							TableColumn column = tm.getColumn(startCol);

							for (int i = 0; i < insertNum; i++) {
								int curCol = lastCol + i + 1;

								TableColumn newcol = new TableColumn(curCol, column.getPreferredWidth());

								// TableColumn column =
								// tm.getColumn(tm.getColumnCount() - 1);
								// TableColumn newcol = new
								// TableColumn(tm.getColumnCount(),
								// column.getPreferredWidth());
								newcol.setHeaderValue(Node.translateColumn(curCol));
								tm.addColumn(newcol);
							}
							long t2 = System.currentTimeMillis();
							System.out.println("SpreadsheetPanel.insertColumn t2=" + t2 + " -> " + (t2 - t1));
						}
					});
				}

				public void removeRows(int removeNum) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							ss.refreshRowHeaders();
						}
					});

				}

				public void insertRow(int insertNum, int startRow) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							ss.refreshRowHeaders();
						}
					});
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		ss.getTable().addMouseListener(new WorkbenchApplet.PopupListener(new PopupMenu()));
		ss.getTable().addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
				if (((byte) e.getKeyChar()) == 3 && e.getModifiers() == 2) {
					if (copy.isEnabled())
						copy.actionPerformed(null);
				} else if (((byte) e.getKeyChar()) == 22 && e.getModifiers() == 2) {
					if (paste.isEnabled())
						paste.actionPerformed(null);
				} else if (((byte) e.getKeyChar()) == 24 && e.getModifiers() == 2) {
					if (cut.isEnabled())
						cut.actionPerformed(null);
				} else if (((byte) e.getKeyChar()) == 26 && e.getModifiers() == 2) {
					if (undo.isEnabled())
						undo.actionPerformed(null);
				} else if (((byte) e.getKeyChar()) == 25 && e.getModifiers() == 2) {
					if (redo.isEnabled())
						redo.actionPerformed(null);
				}
			}
		});

		JPanel main = new JPanel(new BorderLayout());
		main.add(ss, BorderLayout.CENTER);
		main.add(new ToolBarEdit(), BorderLayout.NORTH);

		rangeTextField = new JTextField();
		rangeTextField.setEditable(false);
		rangeTextField.setEditable(false);
		rangeTextField.setPreferredSize(new Dimension(70, rangeTextField.getHeight()));

		JButton rangeCopy = new JButton("C");
		rangeCopy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				StringSelection stringSelection = new StringSelection(rangeTextField.getText());
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(stringSelection, SpreadsheetPanel.this);
			}
		});

		JPanel twinPanel = new JPanel(new BorderLayout());
		twinPanel.add(rangeTextField, BorderLayout.WEST);
		twinPanel.add(rangeCopy, BorderLayout.EAST);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(twinPanel, BorderLayout.EAST);

		main.add(bottomPanel, BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(new ToolBarColRow(), BorderLayout.NORTH);
		add(main, BorderLayout.CENTER);
		
		
		ss.getTable().addKeyListener(new KeyListener() {


			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 67 && (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK ) {
					if (copy.isEnabled()) copy.actionPerformed(null); else Toolkit.getDefaultToolkit().beep();
				} else if (e.getKeyCode() == 'V' && (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK ) {
					if (paste.isEnabled()) paste.actionPerformed(null); else Toolkit.getDefaultToolkit().beep();
				} else if (e.getKeyCode() == 'X' && (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK ) {
					if (cut.isEnabled()) cut.actionPerformed(null); else Toolkit.getDefaultToolkit().beep();
				}else if (e.getKeyCode() == 90 && (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK) {
					if (undo.isEnabled()) undo.actionPerformed(null); else Toolkit.getDefaultToolkit().beep();					
				} else if (e.getKeyCode() == 89 && (e.getModifiers() & KeyEvent.CTRL_MASK) == KeyEvent.CTRL_MASK) {
					if (redo.isEnabled()) redo.actionPerformed(null); else Toolkit.getDefaultToolkit().beep();
				}

			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

		});
	}

	private void remove(boolean byRow) {
		CellRange range = ss.getSelectedRange();
		if (range != null) {
			if (byRow) {
				int start = range.getStartRow();
				int end = range.getEndRow();
				if ((end - start + 1) >= ss.getRowCount()) {
					tooMuchDeletion();
				} else {
					if (ss.isDeletionSafe(byRow, start, end) || unsafeDeletion()) {
						ss.remove(byRow, start, end);
					}
				}
			} else {
				int start = range.getStartCol();
				int end = range.getEndCol();
				if ((end - start + 1) >= ss.getColumnCount()) {
					tooMuchDeletion();
				} else {
					if (ss.isDeletionSafe(byRow, start, end) || unsafeDeletion()) {
						ss.remove(byRow, start, end);
					}
				}
			}
		}
	}

	private void sort(boolean byRow) {
		CellRange range = ss.getSelectedRange();
		if (range != null) {
			// create and show the sort dialog
			SortDialog sortDialog = new SortDialog(byRow, range);
			int rc = sortDialog.show(this, "Sort");
			if (rc == JOptionPane.OK_OPTION) {
				int first = sortDialog.getCriteriaA();
				first += (byRow ? range.getStartRow() : range.getStartCol());

				int second = sortDialog.getCriteriaB();
				if (second >= 0) {
					second += (byRow ? range.getStartRow() : range.getStartCol());
				}
				ss.sort(range, first, second, byRow, sortDialog.firstAscending(), sortDialog.secondAscending());
			}
		}
	}

	private void find(boolean newValue) {
		CellPoint start;

		// checks if anything is selected
		CellRange range = ss.getSelectedRange();

		if (range != null) {
			int x = range.getStartRow();
			int y = range.getStartCol();

			// start from the next cell
			if (!newValue) {
				if (y < ss.getColumnCount()) {
					y++;
				} else {
					y = 1;
					x++;
				}
			}

			start = new CellPoint(x, y);
		} else {
			// or start from the beginning
			start = new CellPoint(0, 0);
		}

		if (newValue) {
			// ask for new value
			FindDialog findDialog = new FindDialog(findValue, matchCase, matchCell);
			int rc = findDialog.show(this, "Find");
			if (rc != FindDialog.OK_OPTION) {
				return;
			}

			String inputValue = findDialog.getString();

			// if input is cancelled or nothing is entered then don't change
			// anything
			if ((inputValue == null) || (inputValue.length() == 0)) {
				return;
			} else {
				findValue = inputValue;
				matchCase = findDialog.isCaseSensitive();
				matchCell = findDialog.isCellMatching();
			}
		} else if (findValue == null) {
			findNext.update();
			return;
		}

		CellPoint found = ss.find(start, findValue, matchCase, matchCell);
		if (found != null) {
			ss.setSelectedRange(new CellRange(found.getRow(), found.getRow(), found.getCol(), found.getCol()));
		} else {
			JOptionPane.showMessageDialog(this, "Search complete and no more \"" + findValue + "\" were found.");
		}
		findNext.update();
	}

	private void fill() {
		CellRange range = ss.getSelectedRange();
		Cell first = ss.getCellAt(range.getStartRow(), range.getStartCol());
		String fillValue = first.toString();

		Icon fillIcon = getIcon("fill32.gif");
		String inputValue = (String) JOptionPane.showInputDialog(this, "Please enter a value to fill the range", "Fill", JOptionPane.INFORMATION_MESSAGE,
				fillIcon, null, fillValue);

		// if input is cancelled or nothing is entered
		// then don't change anything
		if ((inputValue != null) && (inputValue.length() != 0)) {
			ss.fill(range, inputValue);
		}
	}

	private void clear() {
		CellRange range = ss.getSelectedRange();
		if (range != null) {
			ss.clear(range);
		}
	}

	private void insert(boolean byRow) {
		CellRange range = ss.getSelectedRange();
		if (range != null) {
			if (byRow) {
				ss.insert(byRow, range.getStartRow(), range.getEndRow());
			} else {
				ss.insert(byRow, range.getStartCol(), range.getEndCol());
			}
		}
	}

	private void tooMuchDeletion() {
		JOptionPane.showMessageDialog(this, "You can not delete all the rows or columns!", "Delete", JOptionPane.ERROR_MESSAGE);
	}

	private boolean unsafeDeletion() {
		int choice = JOptionPane.showConfirmDialog(this, "The deletion may cause irriversible data loss in other cells.\n\n"
				+ "Do you really want to proceed?\n\n", "Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

		return choice == JOptionPane.YES_OPTION;
	}

	static Icon getIcon(String name) {
		try {
			return new ImageIcon(ImageIO.read(FindDialog.class.getResource("/org/kchine/r/workbench/spreadsheet/icons/" + name)));
		} catch (IOException x) {
			return null;
		}
	}

	private class ClearAction extends AbstractAction {
		ClearAction() {
			super("Clear");
			putValue(Action.SMALL_ICON, getIcon("Clear.png"));
			putValue(Action.SHORT_DESCRIPTION, "Clear");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			clear();
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class CopyAction extends AbstractAction {
		CopyAction() {
			super("Copy");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("Copy.png"));
			putValue(Action.SHORT_DESCRIPTION, "Copy");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			ss.copy();
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class EvalAction extends AbstractAction {
		EvalAction() {
			super("Eval");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('R', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("RunR.png"));
			putValue(Action.SHORT_DESCRIPTION, "Evaluate R expression on cells selection");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {


			if (!isEnabled())
				return;
			EvalDialog evalDialog = new EvalDialog();
			evalDialog.setVisible(true);
			if (evalDialog.getEvalInfo() != null) {

				try {
					CellRange range = ss.getSelectedRange();

					ExportInfo exportInfo = ExportInfo.getExportInfo(range, evalDialog.getEvalInfo().getDataType(),
							(SpreadsheetTableModelClipboardInterface) ss.getTable().getModel());
					RObject robj = exportInfo.getRObject();
					String[] conversionCommandHolder = new String[] { exportInfo.getConversionCommand() };

					if (robj == null)
						return;

					String tempVarName = "TEMP_____";
					try {
						_rgui.getRLock().lock();
						_rgui.getR().putAndAssign(robj, tempVarName);

						if (_rgui.getR().getStatus().toUpperCase().contains("ERROR")) {
							JOptionPane.showMessageDialog(ss, _rgui.getR().getStatus(), "R Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						String output = PoolUtils.replaceAll(evalDialog.getEvalInfo().getExpression(), "%%", tempVarName);

						_rgui.getR().consoleSubmit(replaceAll(conversionCommandHolder[0], "${VAR}", tempVarName) + tempVarName + "<-(" + output + ")");
						if (_rgui.getR().getStatus().toUpperCase().contains("ERROR")) {
							JOptionPane.showMessageDialog(ss, _rgui.getR().getStatus(), "R Error", JOptionPane.ERROR_MESSAGE);
							return;
						}

						String buffer = rToClipboard(tempVarName, false);
						if (buffer != null) {
							StringSelection stringSelection = new StringSelection(buffer);
							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
							clipboard.setContents(stringSelection, SpreadsheetPanel.this);
						}

						JOptionPane.showMessageDialog(ss, "Evaluation Done, The result is in the clipboard");

					} finally {
						try {
							_rgui.getR().evaluate("rm(" + tempVarName + ")");
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						_rgui.getRLock().unlock();
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(ss, ex.getMessage());
					return;

				}

			}
		}

		public boolean isEnabled() {
			return (ss.getSelectedRange() != null && _rgui.getR() != null && !_rgui.getRLock().isLocked());
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null && _rgui.getR() != null && !_rgui.getRLock().isLocked());
		}
	}

	String rToClipboard(String expr, boolean restToRStore) {

		try {
			_rgui.getRLock().lock();
			RObject robj = _rgui.getR().getObject(expr);
			if (_rgui.getR().getStatus().toUpperCase().contains("ERROR")) {
				JOptionPane.showMessageDialog(ss, _rgui.getR().getStatus(), "R Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}

			ImportInfo info = ImportInfo.getImportInfo(robj);

			int r0 = ss.getSelectedRange().getStartRow();
			int c0 = ss.getSelectedRange().getStartCol();
			int r1 = Math.min(ss.getRowCount(), r0 + info.getNrow() - 1);
			int c1 = Math.min(ss.getColumnCount(), c0 + info.getNcol() - 1);

			if (restToRStore) {
				toRDataStore.setAssignTo(expr);
				toRDataStore.setCellRange(Formula.getCellString(r0, c0) + ":" + Formula.getCellString(r1, c1));
				toRDataStore.setDatatype(info.getDtype());
				/*
				 * if (converter != null) {
				 * toRDataStore.setPostAssignCommand(toRDataStore.getAssignTo()
				 * + "<-" + converter + "(" + toRDataStore.getAssignTo() +
				 * ");"); if (robj instanceof RMatrix) {
				 * toRDataStore.setPostAssignCommand
				 * (toRDataStore.getPostAssignCommand() + "dim(" +
				 * toRDataStore.getAssignTo() + ")<-c(" + ((RMatrix)
				 * robj).getDim()[0] + "," + ((RMatrix) robj).getDim()[1] +
				 * ")"); } }
				 */
			}

			return info.getTabString();

		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			_rgui.getRLock().unlock();
		}

	}

	private class FromRAction extends AbstractAction implements ClipboardOwner {
		FromRAction() {
			super("Paste R Data");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("FromR.png"));
			putValue(Action.SHORT_DESCRIPTION, "Paste R Expression into Cells");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			if (!isEnabled())
				return;
			FromRDialog dialog = new FromRDialog();
			dialog.setVisible(true);
			if (dialog.getExpr() != null) {
				String buffer = rToClipboard(dialog.getExpr(), true);
				if (buffer != null) {
					StringSelection stringSelection = new StringSelection(buffer);
					Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					clipboard.setContents(stringSelection, SpreadsheetPanel.this);
					ss.paste();
				}
			}

		}

		public void update() {
			setEnabled(ss.getSelectedRange() != null && _rgui.getR() != null && !_rgui.getRLock().isLocked());
		}

		public boolean isEnabled() {
			return ss.getSelectedRange() != null && _rgui.getR() != null && !_rgui.getRLock().isLocked();
		}

		public void lostOwnership(Clipboard clipboard, Transferable contents) {

		}

	}

	private int getMessageType(String message) {
		if (message.toUpperCase().contains("ERROR"))
			return ERROR_MESSAGE;
		else if (message.toUpperCase().contains("WARNING"))
			return WARNING_MESSAGE;
		else
			return INFORMATION_MESSAGE;
	}

	private class ToRAction extends AbstractAction implements ClipboardOwner {
		ToRAction() {
			super("Export Region To R");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("ToR.png"));
			putValue(Action.SHORT_DESCRIPTION, "Export Cell Region To R");
			
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {

			if (!isEnabled())
				return;
			ToRDialog toRDialog = new ToRDialog(SpreadsheetPanel.this);
			toRDialog.setVisible(true);

			ToRData toRData = toRDialog.getToRData();
			if (toRData != null) {
				try {
					CellRange range = ImportInfo.getRange(toRData.getCellRange());

					ExportInfo exportInfo = ExportInfo.getExportInfo(range, toRData.getDatatype(), (SpreadsheetTableModelClipboardInterface) ss.getTable()
							.getModel());
					RObject result = exportInfo.getRObject();
					String[] conversionCommandHolder = new String[] { exportInfo.getConversionCommand() };

					if (result == null)
						return;
					String tempVarName = "TEMP_____";
					try {
						_rgui.getRLock().lock();
						_rgui.getR().putAndAssign(result, tempVarName);

						if (!_rgui.getR().getStatus().equals("")) {
							int messageType = getMessageType(_rgui.getR().getStatus());
							JOptionPane.showMessageDialog(SpreadsheetPanel.this, _rgui.getR().getStatus(), "R Message", messageType);
							if (messageType == ERROR_MESSAGE)
								return;
						}
						String log = _rgui.getR().consoleSubmit(toRData.getAssignTo() + "<-" + tempVarName);
						if (!log.equals("")) {
							int messageType = getMessageType(_rgui.getR().getStatus());
							JOptionPane.showMessageDialog(SpreadsheetPanel.this, _rgui.getR().getStatus(), "R Message", messageType);
							if (messageType == ERROR_MESSAGE)
								return;
						}

						System.out.println("--->" + PoolUtils.replaceAll(conversionCommandHolder[0], "${VAR}", toRData.getAssignTo()));
						log = _rgui.getR().consoleSubmit(
								PoolUtils.replaceAll(conversionCommandHolder[0], "${VAR}", toRData.getAssignTo()) + toRData.getPostAssignCommand());
						if (!log.equals("")) {
							int messageType = getMessageType(_rgui.getR().getStatus());
							JOptionPane.showMessageDialog(SpreadsheetPanel.this, _rgui.getR().getStatus(), "R Message", messageType);
							if (messageType == ERROR_MESSAGE)
								return;
						}

						_rgui.getConsoleLogger().printAsOutput(
								"\n" + toRData.getAssignTo() + " has been assigned a new value from the cell range " + toRData.getCellRange() + "\n");
					} finally {
						try {
							_rgui.getR().evaluate("rm(" + tempVarName + ")");
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						_rgui.getRLock().unlock();
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(ss, ex.getMessage());
					return;

				}
			}

			/*
			 * StringSelection stringSelection = new
			 * StringSelection("1\t2\t3\n6\t9\t2.5\n"); Clipboard clipboard =
			 * Toolkit.getDefaultToolkit().getSystemClipboard();
			 * clipboard.setContents(stringSelection, this); ss.paste();
			 */

		}

		public void update() {
			setEnabled(ss.getSelectedRange() != null && _rgui.getR() != null && !_rgui.getRLock().isLocked());
		}

		public boolean isEnabled() {
			return ss.getSelectedRange() != null && _rgui.getR() != null && !_rgui.getRLock().isLocked();
		}

		public void lostOwnership(Clipboard clipboard, Transferable contents) {

		}
	}

	private class CutAction extends AbstractAction {
		CutAction() {
			super("Cut");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("Cut.png"));
			putValue(Action.SHORT_DESCRIPTION, "Cut");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			ss.cut();
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class ToolBarEdit extends JToolBar {
		ToolBarEdit() {
			add(copy);
			add(paste);
			add(cut);
			add(clear);
			add(selectall);
			add(undo);
			add(redo);
			add(fromR);
			add(toR);
			add(eval);
			add(find);
			add(findNext);
			add(fill);
			
			
			
			// add(newView);
		}
	}

	private class ToolBarColRow extends JToolBar {
		ToolBarColRow() {
			add(insertColumn);
			add(removeColumn);
			add(insertRow);
			add(removeRow);
			add(sort);
		}
	}

	private class PopupMenu extends JPopupMenu {
		PopupMenu() {
			add(undo);
			add(redo);
			addSeparator();
			add(cut);
			add(copy);
			add(paste);
			addSeparator();
			add(fromR);
			add(toR);
			add(eval);
			addSeparator();
			add(clear);
			add(fill);
		}
	}

	private class FillAction extends AbstractAction {
		FillAction() {
			super("Fill...");
			putValue(Action.SMALL_ICON, getIcon("Fill.png"));
			putValue(Action.SHORT_DESCRIPTION, "Fill Cells");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			fill();
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class FindAction extends AbstractAction {
		FindAction() {
			super("Find...");
			putValue(Action.SMALL_ICON, getIcon("Find.png"));
			putValue(Action.SHORT_DESCRIPTION, "Find");
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {
			find(true);
		}
	}

	private class FindNextAction extends AbstractAction {
		FindNextAction() {
			super("Find Next");
			putValue(Action.SMALL_ICON, getIcon("FindAgain.png"));
			putValue(Action.SHORT_DESCRIPTION, "Find Next");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			find(false);
		}

		void update() {
			setEnabled(findValue != null && findValue.length() > 0);
		}
	}

	private class InsertColumnAction extends AbstractAction {
		InsertColumnAction() {
			super("Insert Column");
			putValue(Action.SMALL_ICON, getIcon("insertcolumn.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Insert Column");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			insert(false);
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class InsertRowAction extends AbstractAction {
		InsertRowAction() {
			super("Insert Row");
			putValue(Action.SMALL_ICON, getIcon("insertrow.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Insert Row");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			insert(true);
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class PasteAction extends AbstractAction {
		PasteAction() {
			super("Paste");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('V', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("Paste.png"));
			putValue(Action.SHORT_DESCRIPTION, "Paste");
		}

		public void actionPerformed(ActionEvent e) {
			ss.paste();
		}
	}

	private class RedoAction extends AbstractAction {
		RedoAction() {
			super("Redo");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('Y', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("Redo.png"));
			putValue(Action.SHORT_DESCRIPTION, "Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			ss.redo();
			undo.update();
			redo.update();
		}

		void update() {
			setEnabled(ss.canRedo());
		}
	}

	private class RemoveColumnAction extends AbstractAction {
		RemoveColumnAction() {
			super("Remove Column");
			putValue(Action.SMALL_ICON, getIcon("deletecolumn.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Remove Column");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			remove(false);
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class RemoveRowAction extends AbstractAction {
		RemoveRowAction() {
			super("Remove Row");
			putValue(Action.SMALL_ICON, getIcon("deleterow.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Remove Row");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			remove(true);
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class SelectAllAction extends AbstractAction {
		SelectAllAction() {
			super("Select All");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('A', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("SelectAll.png"));
			putValue(Action.SHORT_DESCRIPTION, "Select All");
		}

		public void actionPerformed(ActionEvent e) {
			int rows = ss.getRowCount();
			int cols = ss.getColumnCount();
			ss.setSelectedRange(new CellRange(0, rows - 1, 0, cols - 1));
		}
	}

	private class SortColumnAction extends AbstractAction {
		SortColumnAction() {
			super("Sort Column...");
			putValue(Action.SMALL_ICON, getIcon("sort.gif"));
			putValue(Action.SHORT_DESCRIPTION, "Sort Column");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			sort(false);
		}

		void update() {
			setEnabled(ss.getSelectedRange() != null);
		}
	}

	private class UndoAction extends AbstractAction {
		UndoAction() {
			super("Undo");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('Z', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("Undo.png"));
			putValue(Action.SHORT_DESCRIPTION, "Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			ss.undo();
		}

		void update() {
			setEnabled(ss.canUndo());
		}
	}

	public static class SortDialog extends JOptionPane {
		private JComboBox primary;
		private JComboBox tiebreaker;
		private JRadioButton ascending1;
		private JRadioButton ascending2;
		private JRadioButton descending1;
		private JRadioButton descending2;

		/** Creates a new instance of FindDialog */
		SortDialog(boolean byRow, CellRange range) {
			// gets parameters for combo box in dialog
			Vector first = new Vector();
			Vector second = new Vector();
			second.add("None");
			if (byRow) {
				for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
					first.add("Row " + JSpreadsheet.translateRow(i));
					second.add("Row " + JSpreadsheet.translateRow(i));
				}
			} else {
				for (int i = range.getStartCol(); i <= range.getEndCol(); i++) {
					first.add("Column " + JSpreadsheet.translateColumn(i));
					second.add("Column " + JSpreadsheet.translateColumn(i));
				}
			}

			primary = new JComboBox(first);
			primary.setSelectedIndex(0);

			tiebreaker = new JComboBox(second);
			tiebreaker.setSelectedIndex(0);

			JPanel box = new JPanel();

			ascending1 = new JRadioButton("Ascending");
			descending1 = new JRadioButton("Descending");
			ascending2 = new JRadioButton("Ascending");
			descending2 = new JRadioButton("Descending");

			ButtonGroup group = new ButtonGroup();
			ButtonGroup group2 = new ButtonGroup();

			ascending1.setSelected(true);
			group.add(ascending1);
			group.add(descending1);

			ascending2.setSelected(true);
			group2.add(ascending2);
			group2.add(descending2);

			box.setLayout(new GridLayout(0, 3, 10, 5));

			// define key shortcut
			JLabel sortLabel = new JLabel("Sort By:");
			sortLabel.setLabelFor(primary);
			sortLabel.setDisplayedMnemonic(KeyEvent.VK_S);
			ascending1.setMnemonic(KeyEvent.VK_A);
			descending1.setMnemonic(KeyEvent.VK_D);

			box.add(sortLabel);
			box.add(new JLabel(""));
			box.add(new JLabel(""));
			box.add(primary);
			box.add(ascending1);
			box.add(descending1);

			// define key shortcut
			sortLabel = new JLabel("Then By:");
			sortLabel.setLabelFor(tiebreaker);
			sortLabel.setDisplayedMnemonic(KeyEvent.VK_T);
			ascending2.setMnemonic(KeyEvent.VK_C);
			descending2.setMnemonic(KeyEvent.VK_E);

			box.add(sortLabel);
			box.add(new JLabel(""));
			box.add(new JLabel(""));
			box.add(tiebreaker);
			box.add(ascending2);
			box.add(descending2);

			// Border padding = BorderFactory.createEmptyBorder(20, 20, 20, 0);
			// box.setBorder(padding);
			setMessage(box);
			setIcon(SpreadsheetPanel.getIcon("sort32.gif"));
			setOptionType(OK_CANCEL_OPTION);
		}

		public int getCriteriaA() {
			return primary.getSelectedIndex();
		}

		public int getCriteriaB() {
			return tiebreaker.getSelectedIndex() - 1; // Subtract NONE
		}

		public boolean firstAscending() {
			return ascending1.isSelected();
		}

		public boolean secondAscending() {
			return ascending2.isSelected();
		}

		int show(Component parent, String title) {
			JDialog dlg = createDialog(parent, title);
			dlg.pack();
			dlg.setVisible(true);

			Object object = getValue();
			return (object instanceof Integer) ? ((Integer) object).intValue() : CLOSED_OPTION;
		}
	}

	public static class NewDialog extends JOptionPane {
		private JCheckBox saveAsDefault = new javax.swing.JCheckBox("Save as default");
		private SpinnerNumberModel rows;
		private SpinnerNumberModel columns;

		/** Creates a new instance of FindDialog */
		NewDialog(int r, int c) {
			rows = new SpinnerNumberModel(r, 1, 1000, 1);
			columns = new SpinnerNumberModel(c, 1, 1000, 1);
			JPanel box = new JPanel(new GridBagLayout());
			GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
			box.add(new JLabel("Rows:"), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			box.add(new JSpinner(rows), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
			box.add(new JLabel("Columns:"), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			box.add(new JSpinner(columns), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
			box.add(saveAsDefault, gridBagConstraints);
			setMessage(box);

			Icon icon = SpreadsheetPanel.getIcon("spread32.gif");
			setIcon(icon);
			setOptionType(OK_CANCEL_OPTION);
		}

		int getRows() {
			return rows.getNumber().intValue();
		}

		int getColumns() {
			return columns.getNumber().intValue();
		}

		boolean getSaveAsDefault() {
			return saveAsDefault.isSelected();
		}

		int show(Component parent, String title) {
			JDialog dlg = createDialog(parent, title);
			dlg.pack();
			dlg.setVisible(true);

			Object object = getValue();
			return (object instanceof Integer) ? ((Integer) object).intValue() : CLOSED_OPTION;
		}
	}

	public static class FindDialog extends JOptionPane {
		private JTextField textField;
		private JCheckBox caseSensitiveBox;
		private JCheckBox matchCellBox;

		/** Creates a new instance of FindDialog */
		FindDialog(String findValue, boolean mCase, boolean mCell) {
			textField = new JTextField(findValue);

			caseSensitiveBox = new JCheckBox("Match Case");
			caseSensitiveBox.setMnemonic(KeyEvent.VK_M);
			caseSensitiveBox.setSelected(mCase);

			matchCellBox = new JCheckBox("Match Entire Cell Only");
			matchCellBox.setMnemonic(KeyEvent.VK_E);
			matchCellBox.setSelected(mCell);

			JPanel box = new JPanel(new BorderLayout(0, 5));

			box.add(textField, BorderLayout.NORTH);
			box.add(caseSensitiveBox, BorderLayout.WEST);
			box.add(matchCellBox, BorderLayout.EAST);
			setMessage(box);
			setIcon(SpreadsheetPanel.getIcon("find32.gif"));
			setOptionType(OK_CANCEL_OPTION);
		}

		boolean isCaseSensitive() {
			return caseSensitiveBox.isSelected();
		}

		boolean isCellMatching() {
			return matchCellBox.isSelected();
		}

		String getString() {
			return textField.getText();
		}

		int show(Component parent, String title) {
			JDialog dlg = createDialog(parent, title);
			textField.selectAll();
			textField.requestFocus();
			dlg.pack();
			dlg.setVisible(true);
			Object object = getValue();
			return object instanceof Integer ? ((Integer) object).intValue() : CLOSED_OPTION;
		}
	}

	String expr_save = "";

	public class FromRDialog extends JDialog {
		String expr_str = null;

		private boolean _closedOnOK = false;
		final JTextField exprs;

		public String getExpr() {
			if (_closedOnOK)
				try {
					return expr_str;
				} catch (Exception e) {
					return null;
				}
			else
				return null;
		}

		public FromRDialog() {
			super((Frame) null, true);
			setLocationRelativeTo(WorkbenchApplet.getComponentParent(ss, JInternalFrame.class));
			getContentPane().setLayout(new GridLayout(1, 2));
			((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(0, 1));
			getContentPane().add(p1);
			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout(0, 1));
			getContentPane().add(p2);

			p1.add(new JLabel("  R Expression"));

			exprs = new JTextField();
			exprs.setText(expr_save);

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
			exprs.addKeyListener(keyListener);

			p2.add(exprs);

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

			setSize(new Dimension(340, 120));
			PoolUtils.locateInScreenCenter(this);

		}

		private void okMethod() {
			expr_str = exprs.getText();
			expr_save = expr_str;
			_closedOnOK = true;
			setVisible(false);
		}

		private void cancelMethod() {
			_closedOnOK = false;
			setVisible(false);
		}

	}

	public class ToRData {
		private String _cellRange;
		private int _datatype;
		private String _assignTo;
		private String _postAssignCommand;

		public ToRData(String cellRange, int datatype, String assignTo, String postAssignCommand) {
			super();
			this._cellRange = cellRange;
			this._datatype = datatype;
			this._assignTo = assignTo;
			this._postAssignCommand = postAssignCommand;
		}

		public String getCellRange() {
			return _cellRange;
		}

		public void setCellRange(String cellRange) {
			this._cellRange = cellRange;
		}

		public int getDatatype() {
			return _datatype;
		}

		public void setDatatype(int datatype) {
			this._datatype = datatype;
		}

		public String getAssignTo() {
			return _assignTo;
		}

		public void setAssignTo(String assignTo) {
			this._assignTo = assignTo;
		}

		public String getPostAssignCommand() {
			return _postAssignCommand;
		}

		public void setPostAssignCommand(String postAssignCommand) {
			this._postAssignCommand = postAssignCommand;
		}
	}

	ToRData toRDataStore = new ToRData("", 0, "", "");

	public class ToRDialog extends JDialog {

		private boolean _closedOnOK = false;

		private JTextField cellRange;
		private JButton setRange;
		private JComboBox dataType;
		private JTextField assignTo;
		private JTextField postAssignCommand;

		private JButton _ok;
		private JButton _cancel;

		public ToRData getToRData() {
			if (_closedOnOK)
				return new ToRData(cellRange.getText(), dataType.getSelectedIndex(), assignTo.getText(), postAssignCommand.getText());
			else
				return null;
		}

		private void update() {
			CellRange r = ss.getSelectedRange();
			String rect = Formula.getCellString(r.getStartRow(), r.getStartCol()) + ":" + Formula.getCellString(r.getEndRow(), r.getEndCol());
			if (cellRange.getText().trim().equals(rect)) {
				cellRange.setForeground(Color.blue);
				setRange.setEnabled(false);
			} else {
				cellRange.setForeground(Color.black);
				setRange.setEnabled(true);
			}
		}

		public ToRDialog(Component c) {
			super((Frame) null, true);

			setTitle("Send Data To R ");
			setLocationRelativeTo(c);

			getContentPane().setLayout(new GridLayout(1, 2));
			((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(0, 1));
			getContentPane().add(p1);
			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout(0, 1));
			getContentPane().add(p2);

			cellRange = new JTextField(toRDataStore.getCellRange());
			cellRange.getDocument().addDocumentListener(new DocumentListener() {
				public void changedUpdate(DocumentEvent e) {
					update();
				}

				public void insertUpdate(DocumentEvent e) {
					update();
				}

				public void removeUpdate(DocumentEvent e) {
					update();
				}
			});
			setRange = new JButton("Use current selection");
			setRange.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					CellRange r = ss.getSelectedRange();
					String rect = Formula.getCellString(r.getStartRow(), r.getStartCol()) + ":" + Formula.getCellString(r.getEndRow(), r.getEndCol());
					System.out.println("rect=" + rect);
					cellRange.setText(rect);

				}
			});

			update();
			dataType = new JComboBox(ImportInfo.R_TYPES_NAMES);
			dataType.setSelectedIndex(toRDataStore.getDatatype());
			assignTo = new JTextField(toRDataStore.getAssignTo());
			postAssignCommand = new JTextField(toRDataStore.getPostAssignCommand());

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
			cellRange.addKeyListener(keyListener);
			dataType.addKeyListener(keyListener);
			assignTo.addKeyListener(keyListener);
			postAssignCommand.addKeyListener(keyListener);

			JPanel cellRangePanel = new JPanel();
			cellRangePanel.setLayout(new BorderLayout());
			cellRangePanel.add(cellRange, BorderLayout.CENTER);
			cellRangePanel.add(setRange, BorderLayout.EAST);

			p2.add(cellRangePanel);
			p2.add(dataType);
			p2.add(assignTo);
			p2.add(postAssignCommand);

			p1.add(new JLabel("  Cell Range"));
			p1.add(new JLabel("  Data Type"));
			p1.add(new JLabel("  Assign To"));
			p1.add(new JLabel("  Post Assign Command"));

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

			p1.add(_ok);
			p2.add(_cancel);

			new Thread(new Runnable() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							cellRange.requestFocus();
						}
					});
				}
			}).start();
			setSize(new Dimension(500, 190));
			PoolUtils.locateInScreenCenter(this);

		}

		private void okMethod() {
			toRDataStore = new ToRData(cellRange.getText(), dataType.getSelectedIndex(), assignTo.getText(), postAssignCommand.getText());
			_closedOnOK = true;
			setVisible(false);
		}

		private void cancelMethod() {
			_closedOnOK = false;
			setVisible(false);
		}
	}

	public static class EvalInfo {
		private String expression;
		private int dataType;

		public String getExpression() {
			return expression;
		}

		public int getDataType() {
			return dataType;
		}

		public EvalInfo(String expression, int dataType) {
			super();
			this.expression = expression;
			this.dataType = dataType;
		}

	}

	static String eval_save = "";
	static int dataType_save = 0;

	public class EvalDialog extends JDialog {
		String expr_str = null;
		int dataType_int = 0;

		private boolean _closedOnOK = false;
		final JTextArea exprs;
		private JComboBox dataType;

		public EvalInfo getEvalInfo() {
			if (_closedOnOK)
				try {
					return new EvalInfo(expr_str, dataType_int);
				} catch (Exception e) {
					return null;
				}
			else
				return null;
		}

		public EvalDialog() {
			super((Frame) null, true);
			setLocationRelativeTo(WorkbenchApplet.getComponentParent(ss, JInternalFrame.class));
			getContentPane().setLayout(new GridLayout(1, 2));
			((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(0, 1));
			getContentPane().add(p1);
			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout(0, 1));
			getContentPane().add(p2);

			p1.add(new JLabel("  R Expression "));
			p1.add(new JLabel("  Selected Cells Data Type"));

			exprs = new JTextArea();
			exprs.setText(eval_save);

			dataType = new JComboBox(ImportInfo.R_TYPES_NAMES);
			dataType.setSelectedIndex(dataType_save);

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
			// exprs.addKeyListener(keyListener);

			p2.add(new JScrollPane(exprs));
			p2.add(dataType);

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

			setSize(new Dimension(540, 160));
			PoolUtils.locateInScreenCenter(this);

			setTitle("Evaluate an R expression ( you can refer to the selected cells with %% )");

		}

		private void okMethod() {
			expr_str = exprs.getText();
			dataType_int = dataType.getSelectedIndex();

			eval_save = expr_str;
			dataType_save = dataType_int;
			_closedOnOK = true;
			setVisible(false);
		}

		private void cancelMethod() {
			_closedOnOK = false;
			setVisible(false);
		}

	}

	public void refreshEmbeddedPanelsLayer() {
		ss.refreshEmbeddedPanelsLayer();
	}
	
	public void lostOwnership(Clipboard clipboard, Transferable contents) {

	}

}
