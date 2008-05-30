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
package graphics.rmi.spreadsheet;

import model.ModelUtils;
import model.SpreadsheetAbstractTableModel;
import model.SpreadsheetListener;
import model.SpreadsheetTableModelRemote;
import model.SpreadsheetTableModelRemoteImpl;
import net.infonode.docking.View;
import net.java.dev.jspreadsheet.Cell;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.CellRange;
import net.java.dev.jspreadsheet.Formula;
import net.java.dev.jspreadsheet.JSpreadsheet;
import net.java.dev.jspreadsheet.Node;
import net.java.dev.jspreadsheet.SpreadsheetSelectionEvent;
import net.java.dev.jspreadsheet.SpreadsheetSelectionListener;
import net.java.dev.jspreadsheet.SpreadsheetTableModel;
import graphics.pop.GDDevice;
import graphics.rmi.ConsoleLogger;
import graphics.rmi.GDApplet;
import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;
import groovy.GroovyInterpreter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
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
import java.util.HashMap;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RComplex;
import org.bioconductor.packages.rservices.RDataFrame;
import org.bioconductor.packages.rservices.RFactor;
import org.bioconductor.packages.rservices.RInteger;
import org.bioconductor.packages.rservices.RList;
import org.bioconductor.packages.rservices.RLogical;
import org.bioconductor.packages.rservices.RMatrix;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.rservices.RObject;
import org.bioconductor.packages.rservices.RVector;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;
import static javax.swing.JOptionPane.*;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class SpreadsheetPanel extends JPanel implements ClipboardOwner {

	public static SpreadsheetTableModelRemoteImpl tmri;
	public static void main(String[] args) throws Exception {
		
		
		tmri=new SpreadsheetTableModelRemoteImpl(3,2, new HashMap<String, SpreadsheetTableModelRemoteImpl>());
		SpreadsheetTableModelRemote modelRemote=(SpreadsheetTableModelRemote)java.rmi.server.RemoteObject.toStub(tmri);
		
		
		//SpreadsheetTableModelRemote modelRemote=(SpreadsheetTableModelRemote)LocateRegistry.getRegistry().lookup("toto");
		
		SpreadsheetAbstractTableModel abstractTableModel1=ModelUtils.getSpreadsheetTableModelWrapper(modelRemote);
		SpreadsheetAbstractTableModel abstractTableModel2=ModelUtils.getSpreadsheetTableModelWrapper(modelRemote);
		
		JFrame f = new JFrame("F1");
		f.getContentPane().setLayout(new BorderLayout());
		f.getContentPane().add(new SpreadsheetPanel(abstractTableModel1, rgui ), BorderLayout.CENTER);
		f.setSize(new Dimension(800, 800));
		f.pack();
		f.setVisible(true);
		
		
		
		JFrame f2 = new JFrame("F2");
		f2.getContentPane().setLayout(new BorderLayout());
		f2.getContentPane().add(new SpreadsheetPanel(abstractTableModel2, rgui ), BorderLayout.CENTER);
		f2.setSize(new Dimension(800, 800));
		f2.pack();
		f2.setVisible(true);
		
		
		
		
		
	}
	
	static RGui rgui=new RGui() {

		public ConsoleLogger getConsoleLogger() {
			return null;
		}

		public View createView(Component panel, String title) {
			return null;
		}

		public RServices getR() {
			return null;
		}

		public ReentrantLock getRLock() {
			return null;
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
	};

	public static final int R_NUMERIC = 0;
	public static final int R_CHARACTER = 1;
	public static final int R_INTEGER = 2;
	public static final int R_LOGICAL = 3;
	public static final int R_COMPLEX = 4;
	public static final int R_FACTOR = 5;
	public static final int R_DATAFRAME = 6;
	public static final String[] R_TYPES_NAMES = { "numeric", "character", "integer", "logical", "complex", "factor", "data frame" };

	private JSpreadsheet ss;
	private CopyAction copy = new CopyAction();
	private CutAction cut = new CutAction();
	private PasteAction paste = new PasteAction();
	private FromRAction fromR = new FromRAction();
	private ToRAction toR = new ToRAction();
	private EvalAction eval = new EvalAction();
	//private NewViewAction newView = new NewViewAction();
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

	private SpreadsheetSelectionListener sl = new SpreadsheetSelectionListener() {
		public void selectionChanged(SpreadsheetSelectionEvent e) {
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
		}
	};

	private String findValue;

	private boolean matchCase;
	private boolean matchCell;

	public SpreadsheetPanel(AbstractTableModel m, RGui rgui) {
		super();
		_rgui = rgui;
		ss = new JSpreadsheet(m, rgui);

		ss.addSelectionListener(sl);
		try {
			ss.addSpreadsheetListener(new SpreadsheetListener(){
				
				public void setSelection(String origin, CellRange sel){
					
					if (origin!=null && origin.equals(ss.getId())) return ;
					
					System.out.println("selection should change :"+sel);
					// validate sel
					int maxRow = ss.getTable().getRowCount() - 1;
					int maxCol = ss.getTable().getColumnCount() - 1;
	
					int startRow = sel.getStartRow();
					int startCol = sel.getStartCol();
					int endRow = sel.getEndRow();
					int endCol = sel.getEndCol();
	
					ss.getTable().setColumnSelectionInterval(Math.min(startCol, maxCol), Math.min(endCol, maxCol));
					ss.getTable().setRowSelectionInterval(Math.min(startRow, maxRow), Math.min(endRow, maxRow));
					
				}
				
				public void updateRedoAction(){
					redo.update();			
				}
				
				public void updateUndoAction(){
					undo.update();			
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

		ss.getTable().addMouseListener(new GDApplet.PopupListener(new PopupMenu()));
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

		setLayout(new BorderLayout());
		add(new ToolBarColRow(), BorderLayout.NORTH);
		add(main, BorderLayout.CENTER);
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
			return new ImageIcon(ImageIO.read(FindDialog.class.getResource("/graphics/rmi/spreadsheet/icons/" + name)));
		} catch (IOException x) {
			return null;
		}
	}

	private class ClearAction extends AbstractAction {
		ClearAction() {
			super("Clear");
			putValue(Action.SMALL_ICON, getIcon("Clear.png"));
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
					String[] conversionCommandHolder = new String[1];
					RObject robj = getRObject(range, evalDialog.getEvalInfo().getDataType(), conversionCommandHolder);
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
		StringBuffer sb = new StringBuffer();

		try {
			_rgui.getRLock().lock();
			RObject robj = _rgui.getR().getObject(expr);
			if (_rgui.getR().getStatus().toUpperCase().contains("ERROR")) {
				JOptionPane.showMessageDialog(ss, _rgui.getR().getStatus(), "R Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}

			int nrow = 0;
			int ncol = 0;
			int dtype = 0;
			String converter = null;

			if (robj instanceof RNumeric) {
				RNumeric rnum = (RNumeric) robj;
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rnum.getIndexNA() != null)
					for (int i = 0; i < rnum.getIndexNA().length; ++i)
						NASet.add(rnum.getIndexNA()[i]);
				for (int i = 0; i < rnum.length(); ++i) {
					if (NASet.contains(i)) {
						sb.append("");
					} else {
						sb.append(rnum.getValue()[i]);
					}
					sb.append("\n");
				}

				nrow = rnum.length();
				ncol = 1;
				dtype = R_NUMERIC;

			} else if (robj instanceof RInteger) {
				RInteger rint = (RInteger) robj;
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rint.getIndexNA() != null)
					for (int i = 0; i < rint.getIndexNA().length; ++i)
						NASet.add(rint.getIndexNA()[i]);

				for (int i = 0; i < rint.length(); ++i) {
					if (NASet.contains(i)) {
						sb.append("");
					} else {
						sb.append(rint.getValue()[i]);
					}
					sb.append("\n");
				}
				nrow = rint.length();
				ncol = 1;
				dtype = R_INTEGER;
			} else if (robj instanceof RLogical) {
				RLogical rlogical = (RLogical) robj;
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rlogical.getIndexNA() != null)
					for (int i = 0; i < rlogical.getIndexNA().length; ++i)
						NASet.add(rlogical.getIndexNA()[i]);
				for (int i = 0; i < rlogical.length(); ++i) {
					if (NASet.contains(i)) {
						sb.append("");
					} else {
						sb.append(rlogical.getValue()[i]);
					}
					sb.append("\n");
				}
				nrow = rlogical.length();
				ncol = 1;
				dtype = R_LOGICAL;

			} else if (robj instanceof RChar) {
				RChar rchar = (RChar) robj;
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rchar.getIndexNA() != null)
					for (int i = 0; i < rchar.getIndexNA().length; ++i)
						NASet.add(rchar.getIndexNA()[i]);
				for (int i = 0; i < rchar.length(); ++i) {
					if (NASet.contains(i)) {
						sb.append("");
					} else {
						sb.append(rchar.getValue()[i]);
					}
					sb.append("\n");
				}
				nrow = rchar.length();
				ncol = 1;
				dtype = R_CHARACTER;
			} else if (robj instanceof RFactor) {
				String[] data = ((RFactor) robj).asData();
				HashSet<Integer> NASet = new HashSet<Integer>();
				for (int i = 0; i < data.length; ++i) {
					if (data[i].equals("NA")) {
						sb.append("");
					} else {
						sb.append(data[i]);
					}
					sb.append("\n");
				}
				nrow = data.length;
				ncol = 1;
				dtype = R_FACTOR;
			} else if (robj instanceof RComplex) {
				RComplex rcomplex = (RComplex) robj;
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rcomplex.getIndexNA() != null)
					for (int i = 0; i < rcomplex.getIndexNA().length; ++i)
						NASet.add(rcomplex.getIndexNA()[i]);
				for (int i = 0; i < rcomplex.length(); ++i) {
					if (NASet.contains(i)) {
						sb.append("");
					} else {
						sb.append(rcomplex.getReal()[i] + "+" + rcomplex.getImaginary()[i] + "i");
					}
					sb.append("\n");
				}
				nrow = rcomplex.length();
				ncol = 1;
				dtype = R_COMPLEX;
			} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RNumeric) {
				int[] dims = ((RMatrix) robj).getDim();
				RNumeric rnum = (RNumeric) ((RMatrix) robj).getValue();
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rnum.getIndexNA() != null)
					for (int i = 0; i < rnum.getIndexNA().length; ++i)
						NASet.add(rnum.getIndexNA()[i]);
				for (int i = 0; i < dims[0]; ++i) {
					for (int j = 0; j < dims[1]; ++j) {
						int offset = j * dims[0] + i;
						if (NASet.contains(offset)) {
							sb.append("");
						} else {
							sb.append(rnum.getValue()[offset]);
						}
						if (j == (dims[1] - 1)) {
						} else
							sb.append('\t');
					}
					sb.append('\n');
				}
				nrow = dims[0];
				ncol = dims[1];
				dtype = R_NUMERIC;
			} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RInteger) {
				int[] dims = ((RMatrix) robj).getDim();
				RInteger rint = (RInteger) ((RMatrix) robj).getValue();
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rint.getIndexNA() != null)
					for (int i = 0; i < rint.getIndexNA().length; ++i)
						NASet.add(rint.getIndexNA()[i]);
				for (int i = 0; i < dims[0]; ++i) {
					for (int j = 0; j < dims[1]; ++j) {
						int offset = j * dims[0] + i;
						if (NASet.contains(offset)) {
							sb.append("");
						} else {
							sb.append(rint.getValue()[offset]);
						}
						if (j == (dims[1] - 1)) {
						} else
							sb.append('\t');
					}
					sb.append('\n');
				}
				nrow = dims[0];
				ncol = dims[1];
				dtype = R_INTEGER;
			} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RChar) {
				int[] dims = ((RMatrix) robj).getDim();
				RChar rchar = (RChar) ((RMatrix) robj).getValue();
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rchar.getIndexNA() != null)
					for (int i = 0; i < rchar.getIndexNA().length; ++i)
						NASet.add(rchar.getIndexNA()[i]);
				for (int i = 0; i < dims[0]; ++i) {
					for (int j = 0; j < dims[1]; ++j) {
						int offset = j * dims[0] + i;
						if (NASet.contains(offset)) {
							sb.append("");
						} else {
							sb.append(rchar.getValue()[offset]);
						}
						if (j == (dims[1] - 1)) {
						} else
							sb.append('\t');
					}
					sb.append('\n');
				}
				nrow = dims[0];
				ncol = dims[1];
				dtype = R_CHARACTER;
			} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RLogical) {
				int[] dims = ((RMatrix) robj).getDim();
				RLogical rlogical = (RLogical) ((RMatrix) robj).getValue();
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rlogical.getIndexNA() != null)
					for (int i = 0; i < rlogical.getIndexNA().length; ++i)
						NASet.add(rlogical.getIndexNA()[i]);
				for (int i = 0; i < dims[0]; ++i) {
					for (int j = 0; j < dims[1]; ++j) {
						int offset = j * dims[0] + i;
						if (NASet.contains(offset)) {
							sb.append("");
						} else {
							sb.append(rlogical.getValue()[offset]);
						}
						if (j == (dims[1] - 1)) {
						} else
							sb.append('\t');
					}
					sb.append('\n');
				}
				nrow = dims[0];
				ncol = dims[1];
				dtype = R_LOGICAL;
			} else if (robj instanceof RMatrix && ((RMatrix) robj).getValue() instanceof RComplex) {
				int[] dims = ((RMatrix) robj).getDim();
				RComplex rcomplex = (RComplex) ((RMatrix) robj).getValue();
				HashSet<Integer> NASet = new HashSet<Integer>();
				if (rcomplex.getIndexNA() != null)
					for (int i = 0; i < rcomplex.getIndexNA().length; ++i)
						NASet.add(rcomplex.getIndexNA()[i]);
				for (int i = 0; i < dims[0]; ++i) {
					for (int j = 0; j < dims[1]; ++j) {
						int offset = j * dims[0] + i;
						if (NASet.contains(offset)) {
							sb.append("");
						} else {
							sb.append(rcomplex.getReal()[offset] + "+" + rcomplex.getImaginary()[offset] + "i");
						}
						if (j == (dims[1] - 1)) {
						} else
							sb.append('\t');
					}
					sb.append('\n');
				}
				nrow = dims[0];
				ncol = dims[1];
				dtype = R_COMPLEX;
			} else if (robj instanceof RDataFrame) {

				System.out.println(robj);

				RList list = ((RDataFrame) robj).getData();
				sb.append("\t");
				for (int i = 0; i < list.getValue().length; ++i) {
					if (list.getNames() != null) {

						sb.append("'" + list.getNames()[i] + "'");
						sb.append(" ");

						Class<?> elementClass = list.getValue()[i].getClass();
						if (elementClass == RNumeric.class) {
							sb.append("(numeric)");
						} else if (elementClass == RInteger.class) {
							sb.append("(integer)");
						} else if (elementClass == RChar.class) {
							sb.append("(character)");
						} else if (elementClass == RLogical.class) {
							sb.append("(logical)");
						} else if (elementClass == RComplex.class) {
							sb.append("(complex)");
						} else if (elementClass == RFactor.class) {
							sb.append("(factor)");
						}

						if (i == (list.getValue().length - 1)) {

						} else {
							sb.append("\t");
						}

					}
				}
				sb.append("\n");

				int nrow0 = -1;
				RObject robj0 = list.getValue()[0];

				if (robj0 instanceof RNumeric) {
					nrow0 = ((RNumeric) robj0).getValue().length;
				} else if (robj0 instanceof RInteger) {
					nrow0 = ((RInteger) robj0).getValue().length;
				} else if (robj0 instanceof RChar) {
					nrow0 = ((RChar) robj0).getValue().length;
				} else if (robj0 instanceof RLogical) {
					nrow0 = ((RLogical) robj0).getValue().length;
				} else if (robj0 instanceof RComplex) {
					nrow0 = ((RComplex) robj0).getReal().length;
				} else if (robj0 instanceof RFactor) {
					nrow0 = ((RFactor) robj0).asData().length;
				}

				String[] rownames = ((RDataFrame) robj).getRowNames();
				HashSet<Integer>[] NASet = new HashSet[list.getValue().length];
				for (int i = 0; i < list.getValue().length; ++i) {
					NASet[i] = new HashSet<Integer>();

					int[] indexNA = null;
					try {
						indexNA = (int[]) list.getValue()[i].getClass().getMethod("getIndexNA").invoke(list.getValue()[i]);
					} catch (Exception e) {
					}

					if (indexNA != null)
						for (int j = 0; j < indexNA.length; ++j)
							NASet[i].add(indexNA[j]);
				}

				for (int i = 0; i < nrow0; ++i) {
					sb.append("'" + rownames[i] + "'" + "\t");
					for (int j = 0; j < list.getValue().length; ++j) {

						if (NASet[j].contains(i)) {
							sb.append("");
						} else {
							if (list.getValue()[j] instanceof RFactor) {
								sb.append(((RFactor) list.getValue()[j]).asData()[i]);
							} else {
								RVector v = (RVector) list.getValue()[j];
								if (v instanceof RNumeric) {
									sb.append(((RNumeric) v).getValue()[i]);
								} else if (v instanceof RInteger) {
									sb.append(((RInteger) v).getValue()[i]);
								} else if (v instanceof RChar) {
									sb.append(((RChar) v).getValue()[i]);
								} else if (v instanceof RLogical) {
									sb.append(((RLogical) v).getValue()[i]);
								} else if (v instanceof RComplex) {
									sb.append(((RComplex) v).getReal()[i] + "+" + ((RComplex) v).getImaginary()[i] + "i");
								}
							}
						}

						if (j == (list.getValue().length - 1)) {
						} else {
							sb.append("\t");
						}
					}

					sb.append("\n");
				}

				nrow = nrow0 + 1;
				ncol = list.getValue().length + 1;

				dtype = R_DATAFRAME;
			}

			int r0 = ss.getSelectedRange().getStartRow();
			int c0 = ss.getSelectedRange().getStartCol();
			int r1 = Math.min(ss.getRowCount(), r0 + nrow - 1);
			int c1 = Math.min(ss.getColumnCount(), c0 + ncol - 1);

			if (restToRStore) {
				toRDataStore.setAssignTo(expr);
				toRDataStore.setCellRange(Formula.getCellString(r0, c0) + ":" + Formula.getCellString(r1, c1));
				toRDataStore.setDatatype(dtype);
				if (converter != null) {
					toRDataStore.setPostAssignCommand(toRDataStore.getAssignTo() + "<-" + converter + "(" + toRDataStore.getAssignTo() + ");");
					if (robj instanceof RMatrix) {
						toRDataStore.setPostAssignCommand(toRDataStore.getPostAssignCommand() + "dim(" + toRDataStore.getAssignTo() + ")<-c("
								+ ((RMatrix) robj).getDim()[0] + "," + ((RMatrix) robj).getDim()[1] + ")");
					}
				}
			}

			return sb.toString();

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

	private static int getCol(String c) throws Exception {
		int i = 0;
		while (Character.isLetter(c.charAt(i)))
			++i;
		return Node.translateColumn(c.substring(0, i));
	}

	private static int getRow(String c) throws Exception {
		int i = 0;
		while (Character.isLetter(c.charAt(i)))
			++i;
		return Node.translateRow(c.substring(i));
	}

	public static CellRange getRange(String s) throws Exception {
		try {
			int idx = s.indexOf(":");
			String c1 = s.substring(0, idx);
			String c2 = s.substring(idx + 1);
			return new CellRange(getRow(c1), getRow(c2), getCol(c1), getCol(c2));
		} catch (Exception e) {
			throw new Exception("Bad Cell Range");
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

	RObject getRObject(CellRange range, int dataType, String[] conversionCommandHolder) {
		SpreadsheetTableModel model = ((SpreadsheetTableModel) ss.getTable().getModel());
		conversionCommandHolder[0] = "";
		RObject result = null;
		if (dataType != R_DATAFRAME && range.getWidth() == 1) {
			result = new RChar();
			String[] value = new String[range.getHeight()];
			Vector<Integer> na = new Vector<Integer>();
			for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
				if (model.isEmptyCell(i, range.getStartCol())) {
					na.add(i - range.getStartRow());
				} else {
					value[i - range.getStartRow()] = model.getCellAt(i, range.getStartCol()).getValue().toString();
				}
			}

			int[] naTab = new int[na.size()];
			for (int k = 0; k < na.size(); ++k)
				naTab[k] = na.elementAt(k);
			((RChar) result).setIndexNA(naTab);
			((RChar) result).setValue(value);

			switch (dataType) {
			case R_NUMERIC:
				conversionCommandHolder[0] = "${VAR}" + "=as.numeric(" + "${VAR}" + ");";
				break;
			case R_CHARACTER:
				conversionCommandHolder[0] = "";
				break;
			case R_INTEGER:
				conversionCommandHolder[0] = "${VAR}" + "=as.integer(" + "${VAR}" + ");";
				break;
			case R_LOGICAL:
				for (int i = 0; i < value.length; ++i) {
					if (value[i] != null) {
						if (value[i].equals("true") || value[i].equals("TRUE")) {
							((RChar) result).getValue()[i] = "1";
						} else if (value[i].equals("false") || value[i].equals("FALSE")) {
							((RChar) result).getValue()[i] = "0";
						}
					}
				}
				conversionCommandHolder[0] = "${VAR}" + "=as.logical(as.numeric(" + "${VAR}" + "));";
				break;
			case R_COMPLEX:
				conversionCommandHolder[0] = "${VAR}" + "=as.complex(" + "${VAR}" + ");";
				break;
			case R_FACTOR:
				conversionCommandHolder[0] = "${VAR}" + "=as.factor(" + "${VAR}" + ");";
				break;
			default:
				break;
			}

		} else if (dataType != R_DATAFRAME && range.getWidth() > 1) {
			result = new RMatrix();
			int[] dims = new int[] { range.getHeight(), range.getWidth() };
			String[] value = new String[range.getHeight() * range.getWidth()];
			Vector<Integer> na = new Vector<Integer>();
			for (int i = range.getStartRow(); i <= range.getEndRow(); i++) {
				for (int j = range.getStartCol(); j <= range.getEndCol(); ++j) {
					int offset = (j - range.getStartCol()) * dims[0] + (i - range.getStartRow());
					if (model.isEmptyCell(i, j)) {
						na.add(offset);
					} else {
						value[offset] = model.getCellAt(i, j).getValue().toString();
					}
				}
			}
			int[] naTab = new int[na.size()];
			for (int k = 0; k < na.size(); ++k)
				naTab[k] = na.elementAt(k);
			((RMatrix) result).setValue(new RChar(value, naTab, null));
			((RMatrix) result).setDim(dims);

			switch (dataType) {
			case R_NUMERIC:
				conversionCommandHolder[0] = "${VAR}" + "=as.numeric(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				break;
			case R_CHARACTER:
				conversionCommandHolder[0] = "";
				break;
			case R_INTEGER:
				conversionCommandHolder[0] = "${VAR}" + "=as.integer(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				break;
			case R_LOGICAL:
				for (int i = 0; i < value.length; ++i) {
					if (value[i] != null) {
						if (value[i].equals("true") || value[i].equals("TRUE")) {
							((RChar) ((RMatrix) result).getValue()).getValue()[i] = "1";
						} else if (value[i].equals("false") || value[i].equals("FALSE")) {
							((RChar) ((RMatrix) result).getValue()).getValue()[i] = "0";
						}
					}
				}
				conversionCommandHolder[0] = "${VAR}" + "=as.logical(as.numeric(" + "${VAR}" + "));" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				break;
			case R_COMPLEX:
				conversionCommandHolder[0] = "${VAR}" + "=as.complex(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				break;
			case R_FACTOR:
				conversionCommandHolder[0] = "${VAR}" + "=as.factor(" + "${VAR}" + ");" + "dim(${VAR})<-c(" + dims[0] + "," + dims[1] + ");";
				break;
			default:
				break;
			}

		} else if (dataType == R_DATAFRAME) {
			String[] rownames = new String[range.getHeight() - 1];
			for (int i = range.getStartRow() + 1; i <= range.getEndRow(); ++i) {
				String name = model.getCellAt(i, range.getStartCol()).getValue().toString();
				if (name.startsWith("'") && name.endsWith("'")) {
					name = name.substring(1, name.length() - 1);
				}
				rownames[i - (range.getStartRow() + 1)] = name;
			}
			String[] colnames = new String[range.getWidth() - 1];
			String[] classnames = new String[range.getWidth() - 1];

			for (int j = range.getStartCol() + 1; j <= range.getEndCol(); ++j) {

				String name = model.getCellAt(range.getStartRow(), j).getValue().toString().trim();
				if (name.endsWith("(numeric)")) {
					classnames[j - (range.getStartCol() + 1)] = "numeric";
					name = name.substring(0, name.indexOf("(numeric)")).trim();
				} else if (name.endsWith("(logical)")) {
					classnames[j - (range.getStartCol() + 1)] = "logical";
					name = name.substring(0, name.indexOf("(logical)")).trim();
				} else if (name.endsWith("(integer)")) {
					classnames[j - (range.getStartCol() + 1)] = "integer";
					name = name.substring(0, name.indexOf("(integer)")).trim();
				} else if (name.endsWith("(complex)")) {
					classnames[j - (range.getStartCol() + 1)] = "complex";
					name = name.substring(0, name.indexOf("(complex)")).trim();
				} else if (name.endsWith("(factor)")) {
					classnames[j - (range.getStartCol() + 1)] = "factor";
					name = name.substring(0, name.indexOf("(factor)")).trim();
				} else if (name.endsWith("(character)")) {
					classnames[j - (range.getStartCol() + 1)] = "character";
					name = name.substring(0, name.indexOf("(character)")).trim();
				} else {
					classnames[j - (range.getStartCol() + 1)] = "character";
					name = name.trim();
				}

				if (name.startsWith("'") && name.endsWith("'")) {
					name = name.substring(1, name.length() - 1);
				}

				colnames[j - (range.getStartCol() + 1)] = name;
			}

			for (int j = 0; j < colnames.length; ++j) {
				if (colnames[j].equals("")) {
					JOptionPane.showMessageDialog(SpreadsheetPanel.this, "the data frame columns must be titeled", "Invalid Data", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}

			Vector<String> rowNamesVector = new Vector<String>();
			for (int i = 0; i < rownames.length; ++i) {
				if (rowNamesVector.contains(rownames[i])) {
					JOptionPane.showMessageDialog(SpreadsheetPanel.this, "the data frame row names must be unique", "Invalid Data", JOptionPane.ERROR_MESSAGE);
					return null;
				} else {

					rowNamesVector.add(rownames[i]);
				}
			}

			RObject[] elements = new RObject[range.getWidth() - 1];

			for (int j = range.getStartCol() + 1; j <= range.getEndCol(); ++j) {
				String[] value = new String[range.getHeight() - 1];
				Vector<Integer> na = new Vector<Integer>();
				for (int i = range.getStartRow() + 1; i <= range.getEndRow(); i++) {
					if (model.isEmptyCell(i, j)) {
						na.add(i - (range.getStartRow() + 1));
					} else {
						value[i - (range.getStartRow() + 1)] = model.getCellAt(i, j).getValue().toString();
					}
				}
				int[] naTab = new int[na.size()];
				for (int k = 0; k < na.size(); ++k)
					naTab[k] = na.elementAt(k);
				elements[j - (range.getStartCol() + 1)] = new RChar(value, naTab, null);
			}

			for (int i = 0; i < colnames.length; ++i) {
				if (!classnames[i].equals("character")) {
					if (classnames[i].equals("logical")) {
						for (int l = 0; l < rownames.length; l++) {
							String lstr = ((RChar) elements[i]).getValue()[l];
							if (lstr != null) {
								if (lstr.equals("false") || lstr.equals("FALSE")) {
									((RChar) elements[i]).getValue()[l] = "0";
								} else if (lstr.equals("true") || lstr.equals("TRUE")) {
									((RChar) elements[i]).getValue()[l] = "1";
								}
							}
						}
						conversionCommandHolder[0] += "${VAR}$" + colnames[i] + "=as.logical(as.numeric(" + "${VAR}$" + colnames[i] + "));";
					} else {
						conversionCommandHolder[0] += "${VAR}$" + colnames[i] + "=as." + classnames[i] + "(" + "${VAR}$" + colnames[i] + ");";
					}

				}
			}

			result = new RDataFrame(new RList(elements, colnames), rownames);
		} else {
			result = null;
		}
		return result;
	}

	private class ToRAction extends AbstractAction implements ClipboardOwner {
		ToRAction() {
			super("Export Region To R");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('C', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("ToR.png"));
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
					CellRange range = getRange(toRData.getCellRange());
					String[] conversionCommandHolder = new String[1];

					RObject result = getRObject(range, toRData.getDatatype(), conversionCommandHolder);
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

	/*
	private class NewViewAction extends AbstractAction {
		NewViewAction() {
			super("New Spreadsheet");
			putValue(Action.SMALL_ICON, getIcon("NewView.png"));
			setEnabled(true);
		}

		public void actionPerformed(ActionEvent e) {

			DimensionsDialog ddialog = new DimensionsDialog(ss);
			ddialog.setVisible(true);
			if (ddialog.getSpreadsheetDimension() != null) {
				try {
					_rgui.createView(new SpreadsheetPanel(new SpreadsheetDefaultTableModel((int) ddialog.getSpreadsheetDimension().getHeight(), (int) ddialog.getSpreadsheetDimension().getWidth()), _rgui),
							"Spreadsheet View");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}

		void update() {
			setEnabled(true);
		}
	}
	*/

	private class CutAction extends AbstractAction {
		CutAction() {
			super("Cut");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('X', InputEvent.CTRL_MASK));
			putValue(Action.SMALL_ICON, getIcon("Cut.png"));
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
			//add(newView);
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
			setLocationRelativeTo(GDApplet.getComponentParent(ss, JInternalFrame.class));
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
			dataType = new JComboBox(R_TYPES_NAMES);
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
		final JTextField exprs;
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
			setLocationRelativeTo(GDApplet.getComponentParent(ss, JInternalFrame.class));
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

			exprs = new JTextField();
			exprs.setText(eval_save);

			dataType = new JComboBox(R_TYPES_NAMES);
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
			exprs.addKeyListener(keyListener);

			p2.add(exprs);
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

			setSize(new Dimension(540, 140));
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

	public void lostOwnership(Clipboard clipboard, Transferable contents) {

	}
	

}
