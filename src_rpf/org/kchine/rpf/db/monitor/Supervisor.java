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
package org.kchine.rpf.db.monitor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.URLClassLoader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import static org.kchine.rpf.PoolUtils.*;

import javax.crypto.*;
import javax.crypto.spec.*;

import org.kchine.rpf.InitializingException;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.TimeoutException;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.NodeDataDB;
import org.kchine.rpf.db.PoolDataDB;
import org.kchine.rpf.db.SupervisorInterface;
import org.kchine.rpf.gui.Symbol;
import org.kchine.rpf.gui.SymbolPopDialog;
import org.kchine.rpf.gui.SymbolPushDialog;

/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class Supervisor {
	
	public Supervisor(DBLayerInterface regsitry, SupervisorInterface supervisorInterface) {
		_registry=regsitry;
		_supervisorInterface=supervisorInterface;
	}
	
	private static final String[] servantTableColumns = new String[] { "NAME", "#POOL_NAME", "IN_USE", "PING_FAILURES", "NODE_NAME", "REGISTER_TIME",
			"PROCESS_ID", "HOST_NAME", "HOST_IP", "OS", "BORROW_TIME", "BORROW_PROCESS_ID", "BORROW_HOST_NAME",
			"BORROW_HOST_IP", "RETURN_TIME", "RETURN_PROCESS_ID", "RETURN_HOST_NAME", "RETURN_HOST_IP", "BORROW_SESSION_INFO_HEX", "ATTRIBUTES_HEX",
			"CODEBASE", "STUB_HEX", "JOB_ID", "JOB_NAME" , "NOTIFY_EMAIL", "NOTIFIED"

	};

	private static final String[] servantTableLabels = new String[] { "Servant Name", "Pool", "Used", "Ping F.", "Node Name", "Reg Time", "Proc",
			"Host", "IP", "OS", "Borrow Time", "Borrow Proc", "Borrow Host", "Borrow IP",
			"Return Time", "Return Proc", "Return Host", "Return IP",
			"Borrow Session Info", "Attributes", "Codebase", "Stub Hex", "Job Id","Job Name", "Notify Email", "Notified" };

	private static final int[] servantTableWidths = new int[] { 160, // NAME
			80, // POOL NAME
			50, // IN_USE
			50, // PING_FAILURES
			100, // NODE_NAME

			130, // REGISTER_TIME
			100, // PROCESS_ID
			100, // HOST_NAME
			100, // HOST_IP
			100, // OS

			130, // BORROW_TIME
			100, // BORROW_PROCESS_ID
			100, // BORROW_HOST_NAME
			100, // BORROW_HOST_IP

			130, // RETURN_TIME
			100, // RETURN_PROCESS_ID
			100, // RETURN_HOST_NAME
			100, // RETURN_HOST_IP

			300, // BORROW_SESSION_INFO_HEX
			200, // ATTRIBUTES_HEX
			200, // CODEBASE
			300, // STUB_HEX
			100, // JOB_ID
			100, // JOB_NAME
			100, // NOTIFY_EMAIL 
			100  // NOTIFIED
	};

	private static final String[] poolTableLabels = new String[] { "Pool Name", "Pool Prefixes", "Timeout" };

	private static final String[] nodeTableLabels = new String[] { "Node Name", "Pool Prefix", "Host Ip", "Host Name", "Login", "Pwd", "Install Dir",
			"Create Servant Command", "Kill Servant Command", "OS", "Servants Number Min", "Servants Number Max", "Process Counter" };
	private static final int[] nodeTableWidths = new int[] { 140, 240, 120, 160, 100, 100, 160, 200, 120, 120, 100, 100, 100 };

	private static final String[] reportTableLabels = new String[] { "Action", "Status" };
	private static final int[] reportTableWidths = new int[] { 80, 300 };
	private Vector<Symbol> _symbols = new Vector<Symbol>();
	private Vector<HashMap<String, Object>> _V = new Vector<HashMap<String, Object>>();
	private Vector<PoolDataDB> _PDATA = new Vector<PoolDataDB>();
	private Vector<NodeDataDB> _NDATA = new Vector<NodeDataDB>();
	private HashMap<String, String> _prefixToName = new HashMap<String, String>();
	private Vector<HashMap<String, Object>> _RDATA = new Vector<HashMap<String, Object>>();
	private JTable _servantTable = null;
	private JTable _poolTable = null;
	private JTable _nodeTable = null;
	private JTable _reportTable = null;
	private int _periodMilliSec = 500;
	private boolean _autoRefresh = true;
	private DBLayerInterface _registry = null;
	private Vector<String> _selectedServants = new Vector<String>();
	private Vector<String> _selectedPools = new Vector<String>();
	Vector<String> _selectedNodes = new Vector<String>();
	static private JFrame _frame;
	private boolean _disconnected = false;
	private Color[] _colors = new Color[] { Color.red, Color.white };
	private long _colorCounter = 0;
	private JMenu _debugMenu;
	private SupervisorInterface _supervisorInterface;
	

	public static String generateKey() {
		try {
			KeyGenerator desEdeGen = KeyGenerator.getInstance("DESede");
			SecretKey desEdeKey = desEdeGen.generateKey();
			SecretKeyFactory desEdeFactory = SecretKeyFactory.getInstance("DESede");
			DESedeKeySpec desEdeSpec = (DESedeKeySpec) desEdeFactory.getKeySpec(desEdeKey, javax.crypto.spec.DESedeKeySpec.class);
			byte[] rawDesEdeKey = desEdeSpec.getKey();
			return PoolUtils.bytesToHex(rawDesEdeKey);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
						
					DBLayerInterface db = (DBLayerInterface) ServerDefaults.getRmiRegistry();

					_frame = new JFrame("Supervisor");
					_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					_frame.getContentPane().add(new Supervisor(db,new SupervisorUtils()).getPanel());					
					_frame.pack();
					_frame.setVisible(true);
					_frame.setPreferredSize(new Dimension(800, 700));
										
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	int colIndex(String colName) {
		for (int i = 0; i < servantTableColumns.length; ++i)
			if (servantTableColumns[i].equals(colName))
				return i;
		throw new RuntimeException("bad col name");
	}

	synchronized void reload() {

		saveSelection();

		try {
			_V = _registry.getTableData("SERVANTS");
			_PDATA = _registry.getPoolData();
			_NDATA = _registry.getNodeData("");
			_prefixToName.clear();
			for (PoolDataDB pdata : _PDATA) {
				for (int i = 0; i < pdata.getPrefixes().length; ++i)
					_prefixToName.put(pdata.getPrefixes()[i], pdata.getPoolName());
			}
			_disconnected = false;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					((AbstractTableModel) _servantTable.getModel()).fireTableDataChanged();
					((AbstractTableModel) _poolTable.getModel()).fireTableDataChanged();
					((AbstractTableModel) _nodeTable.getModel()).fireTableDataChanged();
					restoreSelection();

					_servantTable.getTableHeader().repaint();
					_nodeTable.getTableHeader().repaint();
					_poolTable.getTableHeader().repaint();

				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_disconnected = true;
					++_colorCounter;
					_servantTable.getTableHeader().repaint();
					_nodeTable.getTableHeader().repaint();
					_poolTable.getTableHeader().repaint();
				}
			});

		}
	}

	private void saveSelection() {
		try {
			_selectedServants.removeAllElements();
			if (_V.size() > 0) {
				int[] selRows = _servantTable.getSelectedRows();
				for (int i = 0; i < selRows.length; ++i) {
					_selectedServants.add((String) _V.elementAt(selRows[i]).get("NAME"));
				}
			}

			_selectedPools.removeAllElements();
			if (_PDATA.size() > 0) {
				int[] selRows = _poolTable.getSelectedRows();
				for (int i = 0; i < selRows.length; ++i) {
					_selectedPools.add(_PDATA.elementAt(selRows[i]).getPoolName());
				}
			}

			_selectedNodes.removeAllElements();
			if (_NDATA.size() > 0) {
				int[] selRows = _nodeTable.getSelectedRows();
				for (int i = 0; i < selRows.length; ++i) {
					_selectedNodes.add(_NDATA.elementAt(selRows[i]).getNodeName());
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

	}

	void restoreSelection() {
		try {
			for (int i = 0; i < _V.size(); ++i) {
				if (_selectedServants.contains(_V.elementAt(i).get("NAME"))) {
					_servantTable.getSelectionModel().addSelectionInterval(i, i);
				}
			}

			for (int i = 0; i < _PDATA.size(); ++i) {
				if (_selectedPools.contains(_PDATA.elementAt(i).getPoolName())) {
					_poolTable.getSelectionModel().addSelectionInterval(i, i);
				}
			}

			for (int i = 0; i < _NDATA.size(); ++i) {
				if (_selectedNodes.contains(_NDATA.elementAt(i).getNodeName())) {
					_nodeTable.getSelectionModel().addSelectionInterval(i, i);
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
		}

	}

	boolean isServantLocked(String servantName) {
		if (PoolUtils.getProcessId().equals(PoolUtils.UNKOWN))
			return false;

		HashMap<String, Object> servantRow = null;
		for (int i = 0; i < _V.size(); ++i) {
			if (_V.elementAt(i).get("NAME").equals(servantName)) {
				servantRow = _V.elementAt(i);
				break;
			}
		}

		if (servantRow == null)
			return false;

		String BORROW_HOST_IP = (String) servantRow.get("BORROW_HOST_IP");
		String BORROW_PROCESS_ID = (String) servantRow.get("BORROW_PROCESS_ID");
		return BORROW_HOST_IP != null && !BORROW_HOST_IP.equals("") && BORROW_PROCESS_ID != null && !BORROW_PROCESS_ID.equals("")
				&& BORROW_HOST_IP.equals(PoolUtils.getHostIp()) && BORROW_PROCESS_ID.equals(PoolUtils.getProcessId());

	}

	class ServantCellRenderer extends JLabel implements TableCellRenderer {
		protected Border m_noFocusBorder;

		public ServantCellRenderer() {
			super();
			m_noFocusBorder = new EmptyBorder(1, 2, 1, 2);
			setOpaque(true);
			setBorder(m_noFocusBorder);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			if (value != null) {
				if (column == colIndex("BORROW_SESSION_INFO_HEX")) {
					setText(hexToObject((String) value).toString());
				} else if (column == colIndex("ATTRIBUTES_HEX")) {
					setText(hexToObject((String) value).toString());
				} else {
					setText(value.toString());
				}

			} else {
				setText("");
			}

			setBackground(isSelected /* && !hasFocus */? table.getSelectionBackground() : table.getBackground());
			setForeground(isSelected /* && !hasFocus */? table.getSelectionForeground() : table.getForeground());

			if (isServantLocked((String) _V.elementAt(row).get("NAME"))) {
				setFont(table.getFont().deriveFont(Font.BOLD));
			} else {
				setFont(table.getFont());
			}
			setBorder(false /* hasFocus */? UIManager.getBorder("Table.focusCellHighlightBorder") : m_noFocusBorder);

			return this;
		}

	};

	class StatusHeaderRenderer extends JLabel implements TableCellRenderer {

		public StatusHeaderRenderer() {
			super();
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			if (value != null)
				setText(value.toString());
			else {
				setText(value.toString());
			}
			setBackground((_disconnected ? _colors[(int) _colorCounter % _colors.length] : (_autoRefresh ? table.getTableHeader().getBackground() : Color.blue)));
			setForeground(_autoRefresh ? table.getTableHeader().getForeground() : Color.white);
			setFont(table.getTableHeader().getFont());
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(JLabel.CENTER);
			return this;
		}

	};

	TableCellRenderer renderer = new ServantCellRenderer();
	TableCellRenderer headerRenderer = new StatusHeaderRenderer();

	HashMap<String, AbstractAction> actions = new HashMap<String, AbstractAction>();

	class ServantMousePopupListener extends MouseAdapter {
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

				popupMenu.add(actions.get("kill_servants"));
				popupMenu.add(actions.get("kill_processes"));
				popupMenu.add(actions.get("kill_processes_forced"));
				popupMenu.add(actions.get("unbind_servants"));

				popupMenu.addSeparator();
				popupMenu.add(actions.get("lock_servants"));
				popupMenu.add(actions.get("unlock_servants"));
				popupMenu.addSeparator();
				popupMenu.add(actions.get("select_servants"));

				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	class NodeMousePopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			checkPopup(e);
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int row = _nodeTable.rowAtPoint(e.getPoint());
				System.out.println(row);
				if (row >= 0) {
					_nodeTable.getSelectionModel().addSelectionInterval(row, row);
					actions.get("edit_node").actionPerformed(null);
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			checkPopup(e);
		}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popupMenu = new JPopupMenu();

				popupMenu.add(actions.get("add_node"));
				popupMenu.add(actions.get("edit_node"));
				popupMenu.add(actions.get("delete_node"));

				popupMenu.addSeparator();

				popupMenu.add(actions.get("new_servant_without_log_console"));
				popupMenu.add(actions.get("new_servant_with_log_console"));

				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	class PoolMousePopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			checkPopup(e);
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int row = _poolTable.rowAtPoint(e.getPoint());
				if (row >= 0) {
					_poolTable.getSelectionModel().addSelectionInterval(row, row);
					actions.get("edit_pool").actionPerformed(null);
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			checkPopup(e);
		}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popupMenu = new JPopupMenu();

				popupMenu.add(actions.get("add_pool"));
				popupMenu.add(actions.get("edit_pool"));
				popupMenu.add(actions.get("delete_pool"));

				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	class ReportMousePopupListener extends MouseAdapter {
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

				popupMenu.add(actions.get("clear_report"));

				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public JPanel getPanel() throws Exception {

		try {
			
			_servantTable = new JTable();

			_servantTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

			_servantTable.setModel(new AbstractTableModel() {

				public int getColumnCount() {
					return servantTableColumns.length;
				}

				public int getRowCount() {
					return _V.size();
				}

				public Object getValueAt(int rowIndex, int columnIndex) {
					String cn = servantTableColumns[columnIndex];
					if (cn.startsWith("#")) {
						if (cn.equals("#POOL_NAME")) {
							String name = (String) _V.elementAt(rowIndex).get("NAME");
							for (String p : _prefixToName.keySet()) {
								if (name.startsWith(p)) {
									return _prefixToName.get(p);
								}
							}
						}
						return null;
					} else {
						return _V.elementAt(rowIndex).get(cn);
					}

				}

				public String getColumnName(int column) {
					return servantTableLabels[column];
				}

				public Class<?> getColumnClass(int columnIndex) {
					if (_V.size() > 0 && _V.elementAt(0).get(servantTableColumns[columnIndex]) != null) {
						return _V.elementAt(0).get(servantTableColumns[columnIndex]).getClass();
					} else {
						return String.class;
					}
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}
			});

			for (int i = 0; i < servantTableColumns.length; ++i) {
				_servantTable.getColumnModel().getColumn(i).setPreferredWidth(servantTableWidths[i]);

				_servantTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
				_servantTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);

			}

			_servantTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			_servantTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					_debugMenu.setEnabled(_servantTable.getSelectedRowCount() == 1);
					_debugMenu.repaint();
				}
			});

			_poolTable = new JTable();
			_poolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			_poolTable.setModel(new AbstractTableModel() {

				public int getColumnCount() {
					return 3;
				}

				public int getRowCount() {
					return _PDATA.size();
				}

				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 0)
						return _PDATA.elementAt(rowIndex).getPoolName();
					else if (columnIndex == 1) {
						String[] prefixes = _PDATA.elementAt(rowIndex).getPrefixes();
						String ps = prefixes[0];
						for (int i = 1; i < prefixes.length; ++i)
							ps += "," + prefixes[i];
						return ps;
					} else if (columnIndex == 2)
						return _PDATA.elementAt(rowIndex).getBorrowTimeout();
					return null;
				}

				public String getColumnName(int column) {
					return poolTableLabels[column];
				}

				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 2)
						return Integer.class;
					else
						return String.class;
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}
			});

			_poolTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

			for (int i = 0; i < _poolTable.getColumnCount(); ++i) {
				_poolTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
			}

			_poolTable.addMouseListener(new PoolMousePopupListener());

			_nodeTable = new JTable();
			_nodeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			_nodeTable.setModel(new AbstractTableModel() {

				public int getColumnCount() {
					return 13;
				}

				public int getRowCount() {
					return _NDATA.size();
				}

				public Object getValueAt(int rowIndex, int columnIndex) {
					if (columnIndex == 0) {
						return _NDATA.elementAt(rowIndex).getNodeName();
					}
					if (columnIndex == 1) {
						return _NDATA.elementAt(rowIndex).getPoolPrefix() + " ( pool " + _prefixToName.get(_NDATA.elementAt(rowIndex).getPoolPrefix()) + " )";
					} else if (columnIndex == 2) {
						return _NDATA.elementAt(rowIndex).getHostIp();
					} else if (columnIndex == 3) {
						return _NDATA.elementAt(rowIndex).getHostName();
					} else if (columnIndex == 4) {
						return _NDATA.elementAt(rowIndex).getLogin();
					} else if (columnIndex == 5) {
						String pwd = _NDATA.elementAt(rowIndex).getPwd();
						return pwd.equals("") ? "" : PoolUtils.charRepeat('*', pwd.length());
					} else if (columnIndex == 6) {
						return _NDATA.elementAt(rowIndex).getInstallDir();
					} else if (columnIndex == 7) {
						return _NDATA.elementAt(rowIndex).getCreateServantCommand();
					} else if (columnIndex == 8) {
						return _NDATA.elementAt(rowIndex).getKillServantCommand();
					} else if (columnIndex == 9) {
						return _NDATA.elementAt(rowIndex).getOS();
					} else if (columnIndex == 10) {
						return _NDATA.elementAt(rowIndex).getServantNbrMin();
					} else if (columnIndex == 11) {
						return _NDATA.elementAt(rowIndex).getServantNbrMax();
					} else if (columnIndex == 12) {
						return _NDATA.elementAt(rowIndex).getProcessCounter();
					} else
						return null;
				}

				public String getColumnName(int column) {
					return nodeTableLabels[column];
				}

				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 8)
						return Integer.class;
					else
						return String.class;
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}
			});

			_nodeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			for (int i = 0; i < _nodeTable.getColumnCount(); ++i) {
				_nodeTable.getColumnModel().getColumn(i).setPreferredWidth(nodeTableWidths[i]);
				_nodeTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
			}

			_nodeTable.addMouseListener(new NodeMousePopupListener());

			_reportTable = new JTable();
			_reportTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			_reportTable.setModel(new AbstractTableModel() {

				public int getColumnCount() {
					return reportTableLabels.length;
				}

				public int getRowCount() {
					return _RDATA.size();
				}

				public Object getValueAt(int rowIndex, int columnIndex) {

					if (columnIndex == 0) {
						return _RDATA.elementAt(rowIndex).get("ACTION");
					} else if (columnIndex == 1) {
						return _RDATA.elementAt(rowIndex).get("STATUS");
					} else
						return null;

				}

				public String getColumnName(int column) {
					return reportTableLabels[column];
				}

				public Class<?> getColumnClass(int columnIndex) {
					return String.class;
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}
			});

			_reportTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
			for (int i = 0; i < _reportTable.getColumnCount(); ++i) {
				_reportTable.getColumnModel().getColumn(i).setPreferredWidth(reportTableWidths[i]);
			}

			_reportTable.addMouseListener(new ReportMousePopupListener());

			initActions();
			_servantTable.addMouseListener(new ServantMousePopupListener());
			

			JScrollPane servantSP = new JScrollPane();
			servantSP.addMouseListener(new ServantMousePopupListener());
			servantSP.getViewport().add(_servantTable);
			JTabbedPane servantTabbebPane = new JTabbedPane();
			servantTabbebPane.add("Servants", servantSP);
			servantTabbebPane.setBorder(BorderFactory.createLineBorder(servantSP.getBackground(), 10));

			JScrollPane poolSP = new JScrollPane(_poolTable);
			JTabbedPane poolTabbedPane = new JTabbedPane();
			poolTabbedPane.add("Pools", poolSP);
			poolTabbedPane.setBorder(BorderFactory.createLineBorder(poolSP.getBackground(), 10));

			JScrollPane nodeSP = new JScrollPane(_nodeTable);
			nodeSP.addMouseListener(new NodeMousePopupListener());
			JTabbedPane nodeTabbedPane = new JTabbedPane();
			nodeTabbedPane.add("Nodes", nodeSP);
			nodeTabbedPane.setBorder(BorderFactory.createLineBorder(nodeSP.getBackground(), 10));

			JSplitPane dataPane1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			dataPane1.setLeftComponent(poolTabbedPane);
			dataPane1.setRightComponent(nodeTabbedPane);
			dataPane1.setDividerLocation((int) 360);
			dataPane1.setDividerSize(10);

			JScrollPane reportSP = new JScrollPane(_reportTable);
			reportSP.addMouseListener(new ReportMousePopupListener());
			JTabbedPane dataPane2 = new JTabbedPane();
			dataPane2.add("Reports", reportSP);
			dataPane2.setBorder(BorderFactory.createLineBorder(reportSP.getBackground(), 10));

			JSplitPane dataPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			dataPane.setLeftComponent(dataPane1);
			dataPane.setRightComponent(dataPane2);
			dataPane.setDividerLocation((int) 200);
			dataPane.setDividerSize(10);

			JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			mainPane.setLeftComponent(servantTabbebPane);
			;
			mainPane.setRightComponent(dataPane);
			mainPane.setDividerLocation((int) 280);
			mainPane.setDividerSize(10);

			JMenuBar menuBar = new JMenuBar();
			final JMenu manageMenu = new JMenu("Manage");
			manageMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					manageMenu.removeAll();

					manageMenu.add(actions.get("kill_servants"));
					manageMenu.add(actions.get("kill_processes"));
					manageMenu.add(actions.get("kill_processes_forced"));
					manageMenu.add(actions.get("unbind_servants"));
					manageMenu.addSeparator();
					manageMenu.add(actions.get("lock_servants"));
					manageMenu.add(actions.get("unlock_servants"));
					manageMenu.addSeparator();
					manageMenu.add(actions.get("select_servants"));

				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(manageMenu);

			_debugMenu = new JMenu("Debug");
			_debugMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					if (_servantTable.getSelectedRowCount() != 1)
						return;
					else {
						try {
							((ManagedServant) _registry.lookup((String) _V.elementAt(_servantTable.getSelectedRows()[0]).get("NAME"))).ping();
						} catch (Exception re) {
							return;
						}
					}

					_debugMenu.removeAll();
					_debugMenu.add(actions.get("show_log"));
					_debugMenu.add(actions.get("say_hello"));
					_debugMenu.add(actions.get("open_console"));
					_debugMenu.add(actions.get("open_device"));
					_debugMenu.addSeparator();
					_debugMenu.add(actions.get("enable_reset"));
					_debugMenu.addSeparator();
					_debugMenu.add(actions.get("pop"));
					_debugMenu.add(actions.get("push"));

					_debugMenu.add(actions.get("pop_to_file"));
					_debugMenu.add(actions.get("push_from_file"));
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(_debugMenu);
			_debugMenu.setEnabled(false);

			final JMenu monitorMenu = new JMenu("Monitor");
			monitorMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					monitorMenu.removeAll();
					monitorMenu.add(actions.get("auto_refresh"));

				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(monitorMenu);

			
			
			

			new Thread(new Runnable() {
				public void run() {

					while (true) {

						if (_autoRefresh) {

							reload();
						}

						try {
							Thread.sleep(_periodMilliSec);
						} catch (Exception e) {
						}
					}

				}
			}).start();

			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
				public void run() {
					String[] namesTab = _reservedServantNames.toArray(new String[0]);
					for (int i = 0; i < namesTab.length; ++i) {
						unlockServant(namesTab[i]);
					}
				}
			}));
			
			
			JPanel contentPane=new JPanel(new BorderLayout());
			contentPane.add(menuBar, BorderLayout.NORTH);
			contentPane.add(mainPane, BorderLayout.CENTER);
			return contentPane;
			

		} finally {

		}

	}

	private void initActions() {

		actions.put("auto_refresh", new AbstractAction() {
			@Override
			public Object getValue(String key) {
				putValue(Action.NAME, (_autoRefresh ? "Stop Auto Refresh" : "Start Auto Refresh"));
				return super.getValue(key);
			}

			public void actionPerformed(ActionEvent e) {
				_autoRefresh = !_autoRefresh;
				_servantTable.getTableHeader().repaint();
				_poolTable.getTableHeader().repaint();
				_nodeTable.getTableHeader().repaint();
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		actions.put("kill_servants", new AbstractAction("Kill Servants") {
			public void actionPerformed(ActionEvent e) {
				HashMap<String, Object>[] rows = new HashMap[_servantTable.getSelectedRows().length];
				for (int i = 0; i < _servantTable.getSelectedRows().length; ++i)
					rows[i] = _V.elementAt(_servantTable.getSelectedRows()[i]);

				for (int i = 0; i < rows.length; ++i) {
					final HashMap<String, Object> row = rows[i];
					new Thread(new Runnable() {
						public void run() {

							System.out.println("Killing Servant:" + row.get("NAME"));
							try {
								((ManagedServant) _registry.lookup((String) row.get("NAME"))).die();
								reload();
							} catch (NotBoundException nbe) {
								nbe.printStackTrace();
							} catch (RemoteException re) {
								// re.printStackTrace();
							}
						}

					}).start();
				}
			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("kill_processes", new AbstractAction("Kill Processes") {
			public void actionPerformed(ActionEvent e) {
				final HashMap<String, Object>[] rows = new HashMap[_servantTable.getSelectedRows().length];
				for (int i = 0; i < _servantTable.getSelectedRows().length; ++i)
					rows[i] = _V.elementAt(_servantTable.getSelectedRows()[i]);
				new Thread(new Runnable() {
					public void run() {
						for (int i = 0; i < rows.length; ++i) {
							final HashMap<String, Object> row = rows[i];
							System.out.println("Killing Process :" + row.get("NAME"));
							try {
								_supervisorInterface.killProcess((String) row.get("NAME"), false, _frame);
								_registry.unbind((String) row.get("NAME"));
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
						reload();
					}
				}).start();

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("kill_processes_forced", new AbstractAction("Kill Processes (Forced)") {
			public void actionPerformed(ActionEvent e) {
				final HashMap<String, Object>[] rows = new HashMap[_servantTable.getSelectedRows().length];
				for (int i = 0; i < _servantTable.getSelectedRows().length; ++i)
					rows[i] = _V.elementAt(_servantTable.getSelectedRows()[i]);

				new Thread(new Runnable() {
					public void run() {
						for (int i = 0; i < rows.length; ++i) {
							final HashMap<String, Object> row = rows[i];
							System.out.println("Killing Process :" + row.get("NAME"));
							try {
								_supervisorInterface.killProcess((String) row.get("NAME"), true, _frame);
								_registry.unbind((String) row.get("NAME"));

							} catch (Exception ex) {
								ex.printStackTrace();

							}
						}
						reload();
					}
				}).start();

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("unbind_servants", new AbstractAction("Unbind Servants") {
			public void actionPerformed(ActionEvent e) {

				int[] rows = _servantTable.getSelectedRows();
				String[] names = new String[rows.length];
				for (int i = 0; i < rows.length; ++i)
					names[i] = (String) _V.elementAt(rows[i]).get("NAME");

				for (int i = 0; i < names.length; ++i) {

					final String name = names[i];

					new Thread(new Runnable() {
						public void run() {

							System.out.println("Unbinding :" + name);
							try {
								_registry.unbind(name);
								reload();
							} catch (NotBoundException nbe) {
								nbe.printStackTrace();
							} catch (RemoteException re) {
								re.printStackTrace();
							}
						}
					}).start();

				}

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("select_servants", new AbstractAction("Select All Servants") {
			public void actionPerformed(ActionEvent e) {
				_servantTable.getSelectionModel().addSelectionInterval(0, _servantTable.getRowCount() - 1);
			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRowCount() < _servantTable.getRowCount();
			}
		});

		actions.put("show_log", new AbstractAction("Show Log") {
			public void actionPerformed(ActionEvent e) {
				int[] rows = _servantTable.getSelectedRows();
				String[] names = new String[rows.length];
				for (int i = 0; i < rows.length; ++i)
					names[i] = (String) _V.elementAt(rows[i]).get("NAME");

				for (int i = 0; i < names.length; ++i) {
					final String name = names[i];
					new Thread(new Runnable() {
						public void run() {

							try {
								((ManagedServant) _registry.lookup(name)).ping();
							} catch (InitializingException ie) {

							} catch (Exception ex) {
								System.out.println("!! ping failed on :" + name);
								return;
							}

							final JFrame logDialog = new LogDialog(_frame, name, _registry);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									logDialog.setVisible(true);
								}
							});

						}

					}).start();

				}

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("say_hello", new AbstractAction("Say Hello") {
			public void actionPerformed(ActionEvent e) {
				int[] rows = _servantTable.getSelectedRows();
				String[] names = new String[rows.length];
				for (int i = 0; i < rows.length; ++i)
					names[i] = (String) _V.elementAt(rows[i]).get("NAME");

				for (int i = 0; i < names.length; ++i) {
					final String name = names[i];
					new Thread(new Runnable() {
						public void run() {
							try {
								((ManagedServant) _registry.lookup(name)).logInfo("Hello");
							} catch (NotBoundException nbe) {
								nbe.printStackTrace();
							} catch (RemoteException re) {
								// re.printStackTrace();
							}
						}
					}).start();
				}

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("enable_reset", new AbstractAction() {
			@Override
			public Object getValue(String key) {
				if (key.equals(Action.NAME)) {
					if (_servantTable.getSelectedRows().length != 1) {
						putValue(Action.NAME, "Enable Reset Unavailable");
					} else {
						try {
							boolean isResetEnabled = ((ManagedServant) _registry.lookup((String) _V.elementAt(_servantTable.getSelectedRows()[0]).get("NAME")))
									.isResetEnabled();
							putValue(Action.NAME, (isResetEnabled ? "Disable Reset" : "Enable Reset"));
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				return super.getValue(key);
			}

			public void actionPerformed(ActionEvent e) {
				try {
					ManagedServant servant = ((ManagedServant) _registry.lookup((String) _V.elementAt(_servantTable.getSelectedRows()[0]).get("NAME")));
					servant.setResetEnabled(!servant.isResetEnabled());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				try {

					if (_servantTable.getSelectedRows().length != 1)
						return false;
					((ManagedServant) _registry.lookup((String) _V.elementAt(_servantTable.getSelectedRows()[0]).get("NAME"))).ping();
					return true;

				} catch (Exception e) {
					return false;
				}
			}
		});

		actions.put("open_console", new AbstractAction("Open Console") {
			public void actionPerformed(ActionEvent e) {
				int[] rows = _servantTable.getSelectedRows();
				String[] names = new String[rows.length];
				for (int i = 0; i < rows.length; ++i)
					names[i] = (String) _V.elementAt(rows[i]).get("NAME");

				for (int i = 0; i < names.length; ++i) {
					final String name = names[i];
					new Thread(new Runnable() {
						public void run() {
							try {
								ManagedServant servant = (ManagedServant) _registry.lookup(name);
								if (servant.hasConsoleMode()) {
									new ConsoleDialog(_frame, servant, new ServantStatus() {
										public boolean isLocked() {
											return isServantLocked(name);
										}
									}).setVisible(true);
								} else {
									System.out.println("servant " + servant.getServantName() + " doesn't support console mode");
								}

							} catch (NotBoundException nbe) {
								nbe.printStackTrace();
							} catch (RemoteException re) {
								re.printStackTrace();
							}
						}
					}).start();
				}

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("open_device", new AbstractAction("Open Graphics Device") {
			public void actionPerformed(ActionEvent e) {
				int[] rows = _servantTable.getSelectedRows();
				String[] names = new String[rows.length];
				for (int i = 0; i < rows.length; ++i)
					names[i] = (String) _V.elementAt(rows[i]).get("NAME");

				for (int i = 0; i < names.length; ++i) {
					final String name = names[i];
					new Thread(new Runnable() {
						public void run() {
							try {
								ManagedServant servant = (ManagedServant) _registry.lookup(name);
								if (servant.hasGraphicMode()) {
									new DeviceDialog(_frame, servant).setVisible(true);
								} else {
									System.out.println("servant " + servant.getServantName() + " doesn't have Graphics mode");
								}

							} catch (NotBoundException nbe) {
								nbe.printStackTrace();
							} catch (RemoteException re) {
								re.printStackTrace();
							}
						}
					}).start();
				}

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("pop", new AbstractAction("Pop Symbol") {
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {
					public void run() {
						try {

							String name = (String) _V.elementAt(_servantTable.getSelectedRows()[0]).get("NAME");
							ManagedServant servant = (ManagedServant) _registry.lookup(name);
							if (!servant.hasPushPopMode()) {
								System.out.println("servant " + servant.getServantName() + " doesn't support push/pop mode");
								return;
							}

							final SymbolPopDialog sdialog = new SymbolPopDialog(_frame, servant.getServantName(), servant.listSymbols(), false);
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									sdialog.setVisible(true);
								};
							});

							if (sdialog.getSymbolName() != null) {
								Serializable value = servant.pop(sdialog.getSymbolName());
								System.out.println("popped value for " + sdialog.getSymbolName() + " : " + value);
								_symbols.add(new Symbol(sdialog.getSymbolName(), value, servant.getServantName()));
							}

						} catch (NotBoundException nbe) {
							nbe.printStackTrace();
						} catch (RemoteException re) {
							re.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length == 1;
			}
		});

		actions.put("pop_to_file", new AbstractAction("Pop Symbol To File") {
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {
					public void run() {
						try {

							HashMap<String, Object> m = _V.elementAt(_servantTable.getSelectedRows()[0]);
							String name = (String) m.get("NAME");
							ManagedServant servant = (ManagedServant) _registry.lookup(name);
							if (!servant.hasPushPopMode()) {
								System.out.println("servant " + servant.getServantName() + " doesn't support push/pop mode");
								return;
							}

							final SymbolPopDialog sdialog = new SymbolPopDialog(_frame, servant.getServantName(), servant.listSymbols(), true);
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									sdialog.setVisible(true);
								};
							});

							if (sdialog.getSymbolName() != null) {
								Object value = (Object) servant.pop(sdialog.getSymbolName());
								byte[] buffer = PoolUtils.objectToBytes(value);

								System.out.println("popped value for " + sdialog.getSymbolName() + " : " + value);
								System.out.println("writing to file : " + sdialog.getFileName());
								RandomAccessFile raf = new RandomAccessFile(sdialog.getFileName(), "rw");
								raf.setLength(0);
								raf.write(buffer);
								raf.close();

							}

						} catch (NotBoundException nbe) {
							nbe.printStackTrace();
						} catch (RemoteException re) {
							re.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}

					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length == 1;
			}
		});

		actions.put("push", new AbstractAction("Push Symbol") {
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {
					public void run() {
						try {

							String name = (String) _V.elementAt(_servantTable.getSelectedRows()[0]).get("NAME");
							ManagedServant servant = (ManagedServant) _registry.lookup(name);
							if (!servant.hasPushPopMode()) {
								System.out.println("servant " + servant.getServantName() + " doesn't support push/pop mode");
								return;
							}

							final SymbolPushDialog sdialog = new SymbolPushDialog(_frame, servant.getServantName(), (Symbol[]) _symbols.toArray(new Symbol[0]),
									false);
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									sdialog.setVisible(true);
								};
							});

							if (sdialog.getSymbol() != null) {
								System.out.println("going to push symbol " + sdialog.getSymbol().getName() + " as " + sdialog.getPushAs() + " --> value:"
										+ sdialog.getSymbol().getValue());
								servant.push(sdialog.getPushAs(), sdialog.getSymbol().getValue());
							}

						} catch (NotBoundException nbe) {
							nbe.printStackTrace();
						} catch (RemoteException re) {
							re.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						}
					}
				}).start();

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length == 1;
			}
		});

		actions.put("push_from_file", new AbstractAction("Push Symbol From File") {
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {
					public void run() {
						try {
							HashMap<String, Object> m = _V.elementAt(_servantTable.getSelectedRows()[0]);

							String name = (String) m.get("NAME");
							ManagedServant servant = (ManagedServant) _registry.lookup(name);
							if (!servant.hasPushPopMode()) {
								System.out.println("servant " + servant.getServantName() + " doesn't support push/pop mode");
								return;
							}

							final SymbolPushDialog sdialog = new SymbolPushDialog(_frame, servant.getServantName(), (Symbol[]) _symbols.toArray(new Symbol[0]),
									true);
							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									sdialog.setVisible(true);
								};
							});

							if (sdialog.getFileName() != null) {
								byte[] buffer = null;

								RandomAccessFile raf = new RandomAccessFile(sdialog.getFileName(), "r");
								buffer = new byte[(int) raf.length()];
								raf.readFully(buffer);
								raf.close();

								Object value = PoolUtils.bytesToObject(buffer, new URLClassLoader(PoolUtils.getURLS((String) m.get("CODEBASE")),
										Supervisor.class.getClassLoader()));
								System.out.println("going to push symbol  as " + sdialog.getPushAs() + " --> value:" + value);

								servant.push(sdialog.getPushAs(), (Serializable) value);
							}

						} catch (NotBoundException nbe) {
							nbe.printStackTrace();
						} catch (RemoteException re) {
							re.printStackTrace();
						} catch (InvocationTargetException e) {
							e.printStackTrace();
						} catch (InterruptedException ie) {
							ie.printStackTrace();
						} catch (FileNotFoundException fne) {
							fne.printStackTrace();
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
					}
				}).start();

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length == 1;
			}
		});

		actions.put("unlock_servants", new AbstractAction("Unlock Servants") {
			public void actionPerformed(ActionEvent e) {
				int[] rows = _servantTable.getSelectedRows();
				String[] names = new String[rows.length];
				for (int i = 0; i < rows.length; ++i)
					names[i] = (String) _V.elementAt(rows[i]).get("NAME");

				for (int i = 0; i < names.length; ++i) {
					final String name = names[i];
					new Thread(new Runnable() {
						public void run() {
							unlockServant(name);

						}
					}).start();
				}

			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length > 0;
			}
		});

		actions.put("lock_servants", new AbstractAction("Lock Servant") {
			public void actionPerformed(ActionEvent e) {
				if (PoolUtils.getProcessId().equals(PoolUtils.UNKOWN)) {
					System.out.println("pslist.exe couldn't be found, set PSTOOLS_HOME to the install dir of pslist tool");
					return;
				}

				int[] rows = _servantTable.getSelectedRows();
				String[] names = new String[rows.length];
				for (int i = 0; i < rows.length; ++i)
					names[i] = (String) _V.elementAt(rows[i]).get("NAME");

				for (int i = 0; i < names.length; ++i) {
					final String name = names[i];
					new Thread(new Runnable() {
						public void run() {
							try {
								lockServant(name);
								reload();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}
			}

			@Override
			public boolean isEnabled() {
				return _servantTable.getSelectedRows().length == 1;
			}
		});

		actions.put("new_servant_without_log_console", new AbstractAction("New Servant") {
			public void actionPerformed(ActionEvent e) {
				try {
					_supervisorInterface.launch(_NDATA.elementAt(_nodeTable.getSelectedRows()[0]).getNodeName(), "", false);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				return _nodeTable.getSelectedRows().length > 0;
			}
		});

		actions.put("new_servant_with_log_console", new AbstractAction("New Servant (Log Console)") {
			public void actionPerformed(ActionEvent e) {
				try {
					_supervisorInterface.launch(_NDATA.elementAt(_nodeTable.getSelectedRows()[0]).getNodeName(), "", true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				return _nodeTable.getSelectedRows().length > 0;
			}
		});

		actions.put("add_node", new AbstractAction("Add Node") {
			public void actionPerformed(ActionEvent e) {

				NodeDialog ld = new NodeDialog(_frame, true);
				ld.setVisible(true);
				NodeDataDB info = ld.getLaunchInfo();
				if (info != null) {
					try {
						_registry.addNode(info);
						reload();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		actions.put("edit_node", new AbstractAction("Edit Node") {
			public void actionPerformed(ActionEvent e) {
				NodeDataDB nodeToEdit = _NDATA.elementAt(_nodeTable.getSelectedRows()[0]);

				NodeDialog._nodeName = nodeToEdit.getNodeName();
				NodeDialog._host_ip_str = nodeToEdit.getHostIp();
				NodeDialog._host_name_str = nodeToEdit.getHostName();
				NodeDialog._login_str = nodeToEdit.getLogin();
				NodeDialog._pwd_str = nodeToEdit.getPwd();
				NodeDialog._homeDir_str = nodeToEdit.getInstallDir();
				NodeDialog._command_str = nodeToEdit.getCreateServantCommand();
				NodeDialog._kill_command_str = nodeToEdit.getKillServantCommand();
				NodeDialog._os_str = nodeToEdit.getOS();
				NodeDialog._servant_nbr_min = new Integer(nodeToEdit.getServantNbrMin()).toString();
				NodeDialog._servant_nbr_max = new Integer(nodeToEdit.getServantNbrMax()).toString();
				NodeDialog._prefix = nodeToEdit.getPoolPrefix();
				NodeDialog._processCounter = new Integer(nodeToEdit.getProcessCounter()).toString();

				NodeDialog ld = new NodeDialog(_frame, false);

				ld.setVisible(true);

				NodeDataDB info = ld.getLaunchInfo();
				if (info != null) {
					try {
						_registry.updateNode(info);
						reload();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			@Override
			public boolean isEnabled() {
				return _nodeTable.getSelectedRows().length > 0;
			}
		});

		actions.put("delete_node", new AbstractAction("Delete Node") {
			public void actionPerformed(ActionEvent e) {
				NodeDataDB nodeToRemove = _NDATA.elementAt(_nodeTable.getSelectedRows()[0]);
				try {
					_registry.removeNode(nodeToRemove.getNodeName());
					reload();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				return _nodeTable.getSelectedRows().length > 0;
			}
		});

		actions.put("add_pool", new AbstractAction("Add Pool") {
			public void actionPerformed(ActionEvent e) {

				PoolDialog ld = new PoolDialog(_frame, false);
				ld.setVisible(true);
				PoolDataDB info = ld.getPoolInfo();
				if (info != null) {
					try {
						_registry.addPool(info);
						reload();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		actions.put("edit_pool", new AbstractAction("Edit Pool") {
			public void actionPerformed(ActionEvent e) {
				PoolDataDB poolToEdit = _PDATA.elementAt(_poolTable.getSelectedRows()[0]);
				String prefixes = "";
				for (int i = 0; i < poolToEdit.getPrefixes().length; ++i)
					prefixes += poolToEdit.getPrefixes()[i] + (i == poolToEdit.getPrefixes().length - 1 ? "" : ",");

				PoolDialog._poolName = poolToEdit.getPoolName();
				PoolDialog._timeOut = new Integer(poolToEdit.getBorrowTimeout()).toString();
				PoolDialog._prefix = prefixes;

				PoolDialog ld = new PoolDialog(_frame, true);
				ld.setVisible(true);

				PoolDataDB info = ld.getPoolInfo();
				if (info != null) {
					try {
						_registry.updatePool(info);
						reload();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			@Override
			public boolean isEnabled() {
				return _poolTable.getSelectedRows().length > 0;
			}
		});

		actions.put("delete_pool", new AbstractAction("Delete Pool") {
			public void actionPerformed(ActionEvent e) {
				PoolDataDB poolToRemove = _PDATA.elementAt(_poolTable.getSelectedRows()[0]);
				try {
					_registry.removePool(poolToRemove.getPoolName());
					reload();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				return _poolTable.getSelectedRows().length > 0;
			}
		});

		actions.put("clear_report", new AbstractAction("Clear Report") {
			public void actionPerformed(ActionEvent e) {
				_RDATA.removeAllElements();
				reload();
			}

			@Override
			public boolean isEnabled() {
				return _RDATA.size() > 0;
			}
		});

	}

	private static int LOCK_TIMEOUT = 15000;
	private static Vector<String> _reservedServantNames = new Vector<String>();

	void lockServant(String servantName) throws Exception {
		String prefix = null;
		for (String p : _prefixToName.keySet()) {
			if (servantName.startsWith(p)) {
				prefix = _prefixToName.get(p);
				break;
			}
		}
		long tstart = System.currentTimeMillis();
		do {

			if (System.currentTimeMillis() - tstart > LOCK_TIMEOUT)
				throw new TimeoutException();
			try {

				_registry.lock();
				if (!_registry.list(new String[] { prefix }).contains(servantName))
					continue;

				ManagedServant servant = (ManagedServant) _registry.lookup(servantName);
				servant.ping();

				_registry.reserve(servantName);
				_reservedServantNames.add(servantName);

				return;

			} catch (Exception e) {

			} finally {
				_registry.unlock();
				_registry.commit();
			}

			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}

		} while (true);
	}

	void unlockServant(String name) {
		try {

			_registry.lock();

			try {
				_registry.unlockServant(name);
			} catch (RemoteException re) {
				re.printStackTrace();
			} finally {
				_registry.unlock();
				_registry.commit();
			}

			((ManagedServant) _registry.lookup(name)).reset();

			if (_reservedServantNames.contains(name)) {
				_reservedServantNames.remove(name);
			}

			reload();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
