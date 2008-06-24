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

import graphics.pop.GDDevice;
import graphics.rmi.action.CopyFromCurrentDeviceAction;
import graphics.rmi.action.CopyToCurrentDeviceAction;
import graphics.rmi.action.CoupleToCurrentDeviceAction;
import graphics.rmi.action.FitDeviceAction;
import graphics.rmi.action.SaveDeviceAsJpgAction;
import graphics.rmi.action.SaveDeviceAsPdfAction;
import graphics.rmi.action.SaveDeviceAsPngAction;
import graphics.rmi.action.SaveDeviceAsSvgAction;
import graphics.rmi.action.SetCurrentDeviceAction;
import graphics.rmi.action.SnapshotDeviceAction;
import graphics.rmi.action.SnapshotDevicePdfAction;
import graphics.rmi.action.SnapshotDeviceSvgAction;
import graphics.rmi.dialogs.GetExprDialog;
import graphics.rmi.spreadsheet.DimensionsDialog;
import graphics.rmi.spreadsheet.SelectIdDialog;
import graphics.rmi.spreadsheet.SpreadsheetPanel;
import groovy.GroovyInterpreter;
import groovy.GroovyInterpreterSingleton;
import http.BadLoginPasswordException;
import http.ConnectionFailedException;
import http.FileLoad;
import http.HttpMarker;
import http.NoNodeManagerFound;
import http.NoRegistryAvailableException;
import http.NoServantAvailableException;
import http.NotLoggedInException;
import http.RHttpProxy;
import http.TunnelingException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowAdapter;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.FloatingWindow;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.TabWindow;
import net.infonode.docking.View;
import net.infonode.docking.ViewSerializer;
import net.infonode.docking.properties.RootWindowProperties;
import net.infonode.docking.theme.ShapedGradientDockingTheme;
import net.infonode.docking.util.DockingUtil;
import net.infonode.docking.util.MixedViewHandler;
import net.infonode.docking.util.ViewMap;
import net.infonode.util.Direction;
import net.java.dev.jspreadsheet.CellPoint;
import net.java.dev.jspreadsheet.SpreadsheetDefaultTableModel;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RObject;
import org.gjt.sp.jedit.gui.FloatingWindowContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.rosuda.ibase.RemoteUtil;
import org.rosuda.ibase.SMarkerInterface;
import org.rosuda.ibase.SMarkerInterfaceRemote;
import org.rosuda.ibase.SVarInterface;
import org.rosuda.ibase.SVarInterfaceRemote;
import org.rosuda.ibase.plots.BarCanvas;
import org.rosuda.ibase.plots.HamCanvas;
import org.rosuda.ibase.plots.HistCanvas;
import org.rosuda.ibase.plots.MapCanvas;
import org.rosuda.ibase.plots.MosaicCanvas;
import org.rosuda.ibase.plots.ParallelAxesCanvas;
import org.rosuda.ibase.plots.ScatterCanvas;

import remoting.FileDescription;
import remoting.RAction;
import remoting.RCollaborationListener;
import remoting.RConsoleAction;
import remoting.RConsoleActionListener;
import remoting.RServices;
import server.BadSshHostException;
import server.BadSshLoginPwdException;
import server.DirectJNI;
import server.LocalHttpServer;
import server.LocalRmiRegistry;
import server.NoMappingAvailable;
import server.ServerManager;
import splash.SplashWindow;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.PropertiesGenerator;
import uk.ac.ebi.microarray.pools.SSHUtils;
import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;
import uk.ac.ebi.microarray.pools.gui.ConsolePanel;
import uk.ac.ebi.microarray.pools.gui.SubmitInterface;
import uk.ac.ebi.microarray.pools.gui.SymbolPopDialog;
import uk.ac.ebi.microarray.pools.gui.SymbolPushDialog;
import util.Utils;
import views.*;
import static graphics.rmi.JGDPanelPop.*;
import static uk.ac.ebi.microarray.pools.PoolUtils.redirectIO;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class GDApplet extends GDAppletBase implements RGui {

	public static final int NEW_R_MODE = 0;
	public static final int RMI_MODE = 1;
	public static final int HTTP_MODE = 2;

	public static final int RMI_MODE_REGISTRY_MODE = 0;
	public static final int RMI_MODE_DB_MODE = 1;
	public static final int RMI_MODE_STUB_MODE = 2;

	private String _commandServletUrl = null;
	private String _defaultHelpUrl = null;
	private String _helpServletUrl = null;
	private String _sessionId = null;
	private RServices _rForConsole;
	private RServices _rForFiles;
	private RServices _rForPopCmd;
	private JPanel _graphicPanel;
	private JPanel _rootGraphicPanel;
	private Vector<FileDescription> _workDirFiles = new Vector<FileDescription>();
	private JTable _filesTable;
	private boolean _nopool;
	private boolean _save;
	private boolean _wait;
	private boolean _demo;
	private String _login;
	private Vector<String> _selectedFiles = new Vector<String>();
	private HashMap<String, AbstractAction> _actions = new HashMap<String, AbstractAction>();
	private JFileChooser _chooser = null;
	private SubmitInterface _submitInterface = null;
	private ConsolePanel _consolePanel = null;
	private int _mode = HTTP_MODE;
	private LookAndFeelInfo[] installedLFs = UIManager.getInstalledLookAndFeels();
	private int _lf;
	private boolean _isBiocLiteSourced = false;
	private GDDevice _currentDevice;

	private Boolean _keepAlive = null;
	private String[] _sshParameters = null;

	private String _rProcessId = null;
	Server _virtualizationServer = null;
	Identification ident = null;

	Stack<GDDevice> s = null;

	public static RGui _instance;
	private RCollaborationListenerImpl _collaborationListenerImpl;
	private RConsoleActionListenerImpl _rConsoleActionListenerImpl;

	private final ReentrantLock _protectR = new RGuiReentrantLock() {
		@Override
		public void lock() {
			super.lock();
			try {
				_currentDevice.setAsCurrentDevice();
				_rForConsole.setOrginatorUID(getUID());
			} catch (Exception e) {
				e.printStackTrace();
			}
			// _consolePanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		@Override
		public void unlock() {

			if (isCollaborativeMode()) {
				try {
					synchronizeCollaborators();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (_rForConsole instanceof HttpMarker) {
				((HttpMarker) _rForConsole).popActions();
			}

			super.unlock();

			// _consolePanel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		}

		@Override
		public void unlockNoBroadcast() {
			super.unlock();
		}

		public boolean isLocked() {
			try {
				if (_rForConsole.isBusy()) {
					return true;
				}
			} catch (Exception e) {
			}
			return super.isLocked();
		}
	};
	private String[] _packageNameSave = new String[] { "" };
	private String[] _expressionSave = new String[] { "" };
	private String[] _httpPortSave = new String[] { "8080" };
	private ConsoleLogger _consoleLogger = new ConsoleLogger() {

		public void printAsInput(String message) {
			_consolePanel.print(message, null);
			if (isCollaborativeMode()) {
				try {
					getR().consolePrint(_sessionId, message, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void printAsOutput(String message) {
			_consolePanel.print(null, message);
			if (isCollaborativeMode()) {
				try {
					getR().consolePrint(_sessionId, null, message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void print(String expression, String result) {
			_consolePanel.print(expression, result);
			if (isCollaborativeMode()) {
				try {
					getR().consolePrint(_sessionId, expression, result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	};

	private Icon _currentDeviceIcon = null;
	private Icon _inactiveDeviceIcon = null;

	private boolean _isGroovyEnabled = false;

	public GDApplet() throws HeadlessException {
		super();
	}

	public GDApplet(HashMap<String, String> customParams) {
		super(customParams);
	}

	public String getWebAppUrl() {
		String url = GDApplet.class.getResource("/graphics/rmi/GDApplet.class").toString();
		System.out.println("url=" + url);
		if (url.contains("http:")) {
			url = url.substring(4, url.indexOf("appletlibs"));
			return url;
		} else {
			return null;
		}
	}

	public void init() {
		super.init();

		PoolUtils.initLog4J();

		System.setErr(System.out);
		if (getParameter("debug") != null && getParameter("debug").equalsIgnoreCase("true")) {
			redirectIO();
		}

		System.out.println("INIT starts");

		if (getParameter("mode") == null) {
			_mode = NEW_R_MODE;
		} else {
			if (getParameter("mode").equalsIgnoreCase("local")) {
				_mode = NEW_R_MODE;
			} else if (getParameter("mode").equalsIgnoreCase("rmi")) {
				_mode = RMI_MODE;
			} else if (getParameter("mode").equalsIgnoreCase("http")) {
				_mode = HTTP_MODE;
			}
		}

		if (true) {

			LocalHttpServer.getRootContext().addServlet(new ServletHolder(new http.local.LocalHelpServlet(GDApplet.this)), "/rvirtual/helpme/*");
			LocalRmiRegistry.getLocalRmiRegistryPort();
		}

		if (getParameter("command_servlet_url") == null || getParameter("command_servlet_url").equals("")) {
			if (getWebAppUrl() != null) {
				// _commandServletUrl = getWebAppUrl() + "cmd";
				_commandServletUrl = "http://127.0.0.1:8080/rvirtual/cmd";
			} else {
				_commandServletUrl = "http://127.0.0.1:8080/rvirtual/cmd";
			}
		} else {
			_commandServletUrl = getParameter("command_servlet_url");
		}

		LoginDialog.mode_int = _mode;
		LoginDialog.url_str = _commandServletUrl;
		if (getParameter("stub") != null && !getParameter("stub").equals(""))
			LoginDialog.stub_str = getParameter("stub");
		if (getParameter("name") != null && !getParameter("name").equals(""))
			LoginDialog.servantName_str = getParameter("name");
		if (getParameter("registryhost") != null && !getParameter("registryhost").equals(""))
			LoginDialog.servantName_str = getParameter("registryhost");
		if (getParameter("registryport") != null && !getParameter("registryport").equals(""))
			LoginDialog.servantName_str = getParameter("registryport");
		if (getParameter("url") != null && !getParameter("url").equals(""))
			LoginDialog.url_str = getParameter("url");

		try {

			_currentDeviceIcon = new ImageIcon(ImageIO.read(GDApplet.class.getResource("/graphics/rmi/icons/" + "active_device.png")));
			_inactiveDeviceIcon = new ImageIcon(ImageIO.read(GDApplet.class.getResource("/graphics/rmi/icons/" + "inactive_device.png")));

			initActions();

			int lf = 0;
			try {
				lf = Integer.decode(getParameter("lf"));
			} catch (Exception e) {

			}
			installedLFs = UIManager.getInstalledLookAndFeels();
			if (lf >= installedLFs.length)
				lf = 0;

			_lf = lf;

			try {
				UIManager.setLookAndFeel(getLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}

			_rootGraphicPanel = new JPanel();
			_rootGraphicPanel.setLayout(new BorderLayout());
			_graphicPanel = new JPanel();
			_rootGraphicPanel.add(_graphicPanel, BorderLayout.CENTER);

			_submitInterface = new SubmitInterface() {

				public String submit(String expression) {

					if (expression.equals("logon") || expression.startsWith("logon ")) {

						if (getR() != null) {
							return "Already Logged On";
						}

						try {

							GDDevice d = null;

							boolean showLoginDialog = true;
							if (new File(GDApplet.NEW_R_STUB_FILE).exists()) {
								int n = JOptionPane.showConfirmDialog(GDApplet.this, "Would you like to connect to the last created and kept alive R Server ?",
										"", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null);
								if (n == JOptionPane.OK_OPTION) {
									showLoginDialog = false;

									BufferedReader pr = new BufferedReader(new FileReader(GDApplet.NEW_R_STUB_FILE));
									String stub = pr.readLine();
									pr.close();

									ident = new Identification(RMI_MODE, "", "", "", false, false, "", RMI_MODE_STUB_MODE, "", -1, "", "", "", -1, "", "", "",
											"", stub, -1, -1, false, false, "", -1, "", "", false, false);
								}
							}

							if (showLoginDialog) {
								LoginDialog loginDialog = new LoginDialog(GDApplet.this.getContentPane(), _mode);
								loginDialog.setVisible(true);
								ident = loginDialog.getIndentification();
							}

							if (ident == null)
								return "Logon cancelled\n";

							_mode = ident.getMode();

							if (getMode() == HTTP_MODE) {

								_commandServletUrl = ident.getUrl();
								_helpServletUrl = _commandServletUrl.substring(0, _commandServletUrl.lastIndexOf("cmd")) + "helpme";
								_defaultHelpUrl = _helpServletUrl + "/doc/html/index.html";

								_login = ident.getUser();
								_nopool = ident.isNopool();
								_wait = ident.isWaitForResource();
								_save = ident.isPersistentWorkspace();
								_demo = ident.isPlayDemo();
								String pwd = ident.getPwd();

								String oldSessionId = _sessionId;
								HashMap<String, Object> options = new HashMap<String, Object>();
								options.put("nopool", new Boolean(_nopool).toString());
								options.put("privatename", ident.getPrivateName());
								options.put("save", new Boolean(_save).toString());
								options.put("wait", new Boolean(_wait).toString());
								_sessionId = RHttpProxy.logOn(_commandServletUrl, _sessionId, _login, pwd, options);

								if (_sessionId.equals(oldSessionId)) {
									return "Already logged on\n";
								}

								if (_save && "guest".equals(_login) && _mode == HTTP_MODE) {
									JOptionPane.showMessageDialog(GDApplet.this, "The login <guest> is not allowed to have workspace persistency");
								}

								_rForConsole = RHttpProxy.getR(_commandServletUrl, _sessionId, true);
								_rForPopCmd = RHttpProxy.getR(_commandServletUrl, _sessionId, false);
								_rForFiles = RHttpProxy.getR(_commandServletUrl, _sessionId, false);

								if (new File(GDApplet.NEW_R_STUB_FILE).exists())
									new File(GDApplet.NEW_R_STUB_FILE).delete();

							} else {

								_helpServletUrl = "http://127.0.0.1:" + LocalHttpServer.getLocalHttpServerPort() + "/" + "rvirtual/helpme";
								_defaultHelpUrl = _helpServletUrl + "/doc/html/index.html";

								_save = ident.isPersistentWorkspace();
								_demo = ident.isPlayDemo();

								RServices r = null;

								if (getMode() == GDApplet.NEW_R_MODE) {

									/*
									  
									  DirectJNI.init(); r =
									  DirectJNI.getInstance().getRServices();
									  if (false) throw new
									  BadSshHostException(); if (false) throw
									  new BadSshLoginPwdException(); _keepAlive =			  ident.isKeepAlive();
									  
									 */

									_keepAlive = ident.isKeepAlive();
									if (ident.isUseSsh()) {
										r = ServerManager.createRSsh(ident.isKeepAlive(), PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),
												PoolUtils.getHostIp(), LocalRmiRegistry.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident.getMemoryMax(),
												ident.getSshHostIp(), ident.getSshPort(), ident.getSshLogin(), ident.getSshPwd(), "", false, null);
									} else {

										if (PoolUtils.isWindowsOs()) {
											if (_keepAlive) {
												r = ServerManager.createRLocal(_keepAlive, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),
														PoolUtils.getHostIp(), LocalRmiRegistry.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident
																.getMemoryMax(), "", false, null);
											} else {
												r = ServerManager.createR(_keepAlive, PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),
														PoolUtils.getHostIp(), LocalRmiRegistry.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident
																.getMemoryMax(), "", false, null);
											}
										} else {
											r = ServerManager.createR(ident.isKeepAlive(), PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(),
													PoolUtils.getHostIp(), LocalRmiRegistry.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident
															.getMemoryMax(), "", false, null);
										}
									}

									if (ident.isUseSsh()) {
										_sshParameters = new String[] { ident.getSshHostIp(), ident.getSshLogin(), ident.getSshPwd() };
									}

									_rProcessId = r.getProcessId();
									System.out.println("R Process Id :" + _rProcessId);

									new File(GDApplet.NEW_R_STUB_FILE).delete();
									if (_keepAlive) {
										PrintWriter pw = new PrintWriter(new FileWriter(GDApplet.NEW_R_STUB_FILE));
										pw.println(PoolUtils.stubToHex(r));
										pw.close();
									}

								} else {

									if (ident.getRmiMode() == RMI_MODE_STUB_MODE) {
										r = (RServices) PoolUtils.hexToStub(ident.getStub(), GDApplet.class.getClassLoader());
									} else if (ident.getRmiMode() == RMI_MODE_REGISTRY_MODE) {
										Registry registry = null;
										try {
											registry = LocateRegistry.getRegistry(ident.getRmiregistryIp(), ident.getRmiregistryPort());
											registry.list();
										} catch (Exception e) {
											e.printStackTrace();
											throw new NoRmiRegistryAvailableException();
										}

										try {
											r = (RServices) registry.lookup(ident.getServantName());
										} catch (Exception e) {
											e.printStackTrace();
											throw new BadServantNameException();
										}

										new File(GDApplet.NEW_R_STUB_FILE).delete();

									} else if (ident.getRmiMode() == RMI_MODE_DB_MODE) {
										Registry registry = null;
										try {
											final String[] dbDriverClass_dbUrl = LoginDialog.getDriverClassAndUrl(ident.getDbDriver(), ident.getDbHostIp(),
													ident.getDbHostPort(), ident.getDbName());
											Class.forName(dbDriverClass_dbUrl[0]);
											registry = DBLayer.getLayer(PoolUtils.getDBType(dbDriverClass_dbUrl[1]), new ConnectionProvider() {
												public Connection newConnection() throws java.sql.SQLException {
													return DriverManager.getConnection(dbDriverClass_dbUrl[1], ident.getDbUser(), ident.getDbPwd());
												};
											});
											registry.list();
										} catch (Exception e) {
											e.printStackTrace();
											throw new NoDbRegistryAvailableException();
										}

										try {
											r = (RServices) registry.lookup(ident.getDbServantName());
										} catch (Exception e) {
											e.printStackTrace();
											throw new BadServantNameException();
										}

										new File(GDApplet.NEW_R_STUB_FILE).delete();

									}

									try {
										r.ping();
									} catch (Exception e) {
										// e.printStackTrace();
										new File(GDApplet.NEW_R_STUB_FILE).delete();
										throw new PingRServerFailedException();
									}

								}

								if (r.isBusy()) {

									int n = JOptionPane.showConfirmDialog(GDApplet.this, "R is busy, can't login, would you like to stop it?", "",
											JOptionPane.OK_CANCEL_OPTION);
									if (n == JOptionPane.OK_OPTION) {
										r.stop();
									} else {
										throw new RBusyException();
									}
								}

								_rForConsole = r;
								_rForPopCmd = r;
								_rForFiles = r;

								_sessionId = RHttpProxy.FAKE_SESSION;

								restoreState();

							}

							s = new Stack<GDDevice>();

							if (_rForConsole instanceof HttpMarker || !_rForConsole.hasRCollaborationListeners()) {
								GDDevice[] ldevices = _rForConsole.listDevices();
								for (int i = ldevices.length - 1; i >= 0; --i)
									s.push(ldevices[i]);
							}

							if (s.empty()) {
								d = _rForConsole.newDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());
							} else {
								d = s.pop();
								d.fireSizeChangedEvent(_graphicPanel.getWidth(), _graphicPanel.getHeight());
							}

							_graphicPanel = new JGDPanelPop(d, true, true, new AbstractAction[] { new SetCurrentDeviceAction(GDApplet.this, d), null,
									new FitDeviceAction(GDApplet.this, d), null, new SnapshotDeviceAction(GDApplet.this),
									new SnapshotDeviceSvgAction(GDApplet.this), new SnapshotDevicePdfAction(GDApplet.this), null,
									new SaveDeviceAsPngAction(GDApplet.this), new SaveDeviceAsJpgAction(GDApplet.this),
									new SaveDeviceAsSvgAction(GDApplet.this), new SaveDeviceAsPdfAction(GDApplet.this), null,
									new CopyFromCurrentDeviceAction(GDApplet.this), new CopyToCurrentDeviceAction(GDApplet.this, d), null,
									new CoupleToCurrentDeviceAction(GDApplet.this)

							}, getRLock(), getConsoleLogger());
							_rootGraphicPanel.removeAll();
							_rootGraphicPanel.setLayout(new BorderLayout());
							_rootGraphicPanel.add(_graphicPanel, BorderLayout.CENTER);
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									_rootGraphicPanel.updateUI();
									_rootGraphicPanel.repaint();

								}
							});

							if (getOpenedServerLogView() != null) {
								getOpenedServerLogView().recreateRemoteLogListenerImpl();
								_rForConsole.addErrListener(getOpenedServerLogView().getRemoteLogListenerImpl());
								_rForConsole.addOutListener(getOpenedServerLogView().getRemoteLogListenerImpl());

							}

							Vector<DeviceView> deviceViews = getDeviceViews();
							for (int i = 0; i < deviceViews.size(); ++i) {
								final JPanel rootComponent = (JPanel) deviceViews.elementAt(i).getComponent();
								GDDevice newDevice = null;

								if (s.empty()) {
									newDevice = _rForConsole.newDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());
								} else {
									newDevice = s.pop();
									newDevice.fireSizeChangedEvent(rootComponent.getWidth(), rootComponent.getHeight());
								}

								JGDPanelPop gp = new JGDPanelPop(newDevice, true, true, new AbstractAction[] {
										new SetCurrentDeviceAction(GDApplet.this, newDevice), null, new FitDeviceAction(GDApplet.this, newDevice), null,
										new SnapshotDeviceAction(GDApplet.this), new SnapshotDeviceSvgAction(GDApplet.this),
										new SnapshotDevicePdfAction(GDApplet.this), null, new SaveDeviceAsPngAction(GDApplet.this),
										new SaveDeviceAsJpgAction(GDApplet.this), new SaveDeviceAsSvgAction(GDApplet.this),
										new SaveDeviceAsPdfAction(GDApplet.this), null, new CopyFromCurrentDeviceAction(GDApplet.this),
										new CopyToCurrentDeviceAction(GDApplet.this, newDevice), null, new CoupleToCurrentDeviceAction(GDApplet.this) },
										getRLock(), getConsoleLogger());

								rootComponent.removeAll();
								rootComponent.setLayout(new BorderLayout());
								rootComponent.add(gp, BorderLayout.CENTER);
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										rootComponent.updateUI();
										rootComponent.repaint();

									}
								});

								deviceViews.elementAt(i).setPanel((JGDPanelPop) gp);
							}

							_currentDevice = d;
							setCurrentDevice(d);
							d.setAsCurrentDevice();

							while (!s.empty()) {
								_actions.get("createdevice").actionPerformed(null);
							}

							_isGroovyEnabled = _rForConsole.isGroovyEnabled();

							_collaborationListenerImpl = new RCollaborationListenerImpl();
							_rForConsole.addRCollaborationListener(_collaborationListenerImpl);

							_rConsoleActionListenerImpl = new RConsoleActionListenerImpl();
							_rForConsole.addRConsoleActionListener(_rConsoleActionListenerImpl);

							if (_demo) {
								playDemo();
								LoginDialog.playDemo_bool = false;
							}

							if (_mode == HTTP_MODE) {
								return "Logged on as " + _login + "\n";
							} else {

								return "Logged on" + "\n";
							}

						} catch (NoServantAvailableException e) {
							return "No R servant available, can not log on\n";
						} catch (NoRegistryAvailableException nrae) {
							return "No Registry available, can not log on\n";
						} catch (NoNodeManagerFound nne) {
							return "No Node Manager Found, can not log on in <no pool> mode \n";
						} catch (ConnectionFailedException cfe) {
							return "Connection to HTTP Virtualization Server Failed \n";
						} catch (BadLoginPasswordException e) {
							return "Bad Login/Password \n";
						} catch (TunnelingException te) {
							return PoolUtils.getStackTraceAsString(te.getCause());
						}
						
						catch (NoRmiRegistryAvailableException normie) {
							return "No RMI Registry Available, can not log on\n";
						} catch (NoDbRegistryAvailableException nodbe) {
							return "No DB Registry Available, can not log on\n";
						} catch (BadServantNameException bsne) {
							return "Bad RMI Servant Name, can not log on\n";
						} catch (BadSshHostException bh_ssh_e) {
							return "Cannot connect to Remote SSH Host\n";
						} catch (BadSshLoginPwdException blp_ssh_e) {
							return "Bad SSH Login/Password\n";
						} catch (PingRServerFailedException prsf_e) {
							return "Ping R Server Failed\n";
						} catch (RBusyException rb_e) {
							return "Connection Failed, R is Busy\n";
						} catch (RemoteException re) {
							return PoolUtils.getStackTraceAsString(re.getCause());
						} catch (Exception unknow) {
							return "Unknown Error, can not log on -->" + PoolUtils.getStackTraceAsString(unknow) + "\n";
						}

					}

					if (_sessionId == null)
						return "Not Logged on, type 'logon' to connect\n";

					if (expression.equals("logoff") || expression.startsWith("logoff ")) {
						try {

							ServerLogView serverLogView = getOpenedServerLogView();
							if (getOpenedServerLogView() != null) {
								try {
									_rForConsole.removeErrListener(getOpenedServerLogView().getRemoteLogListenerImpl());
									_rForConsole.removeOutListener(getOpenedServerLogView().getRemoteLogListenerImpl());
									UnicastRemoteObject.unexportObject(getOpenedServerLogView().getRemoteLogListenerImpl(), false);
								} catch (Exception e) {
									e.printStackTrace();
								}
							}

							if (_mode == HTTP_MODE) {
								if (!getRLock().isLocked()) {
									disposeDevices();
								}
								RHttpProxy.logOff(_commandServletUrl, _sessionId);
							} else {

								if (!getRLock().isLocked()) {
									disposeDevices();
									persistState();
								}

							}

							noSession();
							return "Logged Off\n";
						} catch (NotLoggedInException nlie) {
							return "Not Logged In\n";
						} catch (TunnelingException te) {
							te.printStackTrace();
							return "Logoff Failed\n";
						}
					}

					Object result = null;
					if (getRLock().isLocked()) {
						result = "R is busy, please retry\n";
					} else {

						try {
							getRLock().lock();

							if (_rForConsole instanceof HttpMarker) {
								_rForConsole.asynchronousConsoleSubmit(expression);
								while (_rForConsole.isBusy()) {
									try {
										Thread.sleep(20);
									} catch (Exception e) {
									}
								}
								result = null;

							} else {
								result = _rForConsole.consoleSubmit(expression);
								if (isCollaborativeMode())
									_rForConsole.consolePrint(_sessionId, expression, (String) result);
							}

						} catch (NotLoggedInException nle) {
							noSession();
							result = "Not Logged on, type 'logon' to connect\n";
						} catch (NoServantAvailableException te) {
							result = "No Backend R Servant Available\n";
						} catch (NoRegistryAvailableException nrae) {
							return "No Registry available\n";
						} catch (Exception e) {
							e.printStackTrace();
							result = PoolUtils.getStackTraceAsString(e);
						} finally {
							getRLock().unlock();
						}
					}

					return (String) result;

				}
			};
			_consolePanel = new ConsolePanel(_submitInterface, new AbstractAction[] { _actions.get("logon"), _actions.get("logoff"), null,
					_actions.get("saveimage"), _actions.get("loadimage"), null, _actions.get("stopeval"), _actions.get("interrupteval"), null,
					_actions.get("playdemo"), null, new AbstractAction("Show R Info") {

						public void actionPerformed(ActionEvent e) {
							try {
								System.out.println("R is busy :" + _rForConsole.isBusy());
							} catch (Exception ex) {

							}

						}

					} });

			JPanel workingDirPanel = new JPanel();
			workingDirPanel.setLayout(new BorderLayout());

			_filesTable = new JTable();
			_filesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			_filesTable.setModel(new AbstractTableModel() {

				public int getColumnCount() {
					return 4;
				}

				public int getRowCount() {
					return _workDirFiles.size();
				}

				public Object getValueAt(int rowIndex, int columnIndex) {
					FileDescription fd = _workDirFiles.elementAt(rowIndex);
					if (columnIndex == 0) {
						return fd.getName();
					} else if (columnIndex == 1) {
						if (fd.isDir()) {
							return null;
						} else {
							return fd.getSize();
						}
					} else if (columnIndex == 2) {
						if (fd.isDir()) {
							return "File Folder";
						} else {
							return "";
						}

					} else if (columnIndex == 3) {
						return fd.getModifiedOn();
					} else {
						throw new RuntimeException(columnIndex + " : bad column index");
					}
				}

				public String getColumnName(int column) {
					if (column == 0) {
						return "Name";
					} else if (column == 1) {
						return "Size";
					} else if (column == 2) {
						return "Type";
					} else if (column == 3) {
						return "Last Modified";
					} else {
						throw new RuntimeException(column + " : bad column index");
					}
				}

				public Class<?> getColumnClass(int columnIndex) {
					if (columnIndex == 0) {
						return String.class;
					} else if (columnIndex == 1) {
						return Long.class;
					} else if (columnIndex == 2) {
						return String.class;
					} else if (columnIndex == 3) {
						return Date.class;
					} else {
						throw new RuntimeException(columnIndex + " : bad column index");
					}
				}

				@Override
				public boolean isCellEditable(int rowIndex, int columnIndex) {
					return false;
				}
			});

			TableCellRenderer renderer = new FileCellRenderer();
			for (int i = 0; i < _filesTable.getColumnCount(); ++i) {
				_filesTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
			}

			MouseListener ml = new FileMousePopupListener();
			_filesTable.addMouseListener(ml);

			JMenuBar menuBar = new JMenuBar();

			final JMenu sessionMenu = new JMenu("R-Session");
			sessionMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {
					sessionMenu.removeAll();
					sessionMenu.add(_actions.get("logon"));
					sessionMenu.add(_actions.get("logoff"));
					sessionMenu.addSeparator();
					sessionMenu.add(_actions.get("loadimage"));
					sessionMenu.add(_actions.get("saveimage"));
					sessionMenu.addSeparator();
					sessionMenu.add(_actions.get("stopeval"));
					sessionMenu.add(_actions.get("interrupteval"));

					sessionMenu.addSeparator();
					sessionMenu.add(_actions.get("playdemo"));
					sessionMenu.addSeparator();
					sessionMenu.add(_actions.get("runhttpserver"));
					sessionMenu.add(_actions.get("stophttpserver"));
					sessionMenu.addSeparator();
					sessionMenu.add(_actions.get("runhttpserverlocalhost"));
					sessionMenu.add(_actions.get("stophttpserverlocalhost"));
					sessionMenu.addSeparator();
					sessionMenu.add(_actions.get("serverlogview"));
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(sessionMenu);

			final JMenu filesMenu = new JMenu("File");
			filesMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					filesMenu.removeAll();
					filesMenu.add(_actions.get("import"));
					filesMenu.add(_actions.get("export"));
					filesMenu.add(_actions.get("delete"));
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(filesMenu);

			final JMenu toolsMenu = new JMenu("Tools");
			toolsMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {
					toolsMenu.removeAll();
					toolsMenu.add(_actions.get("editor"));
					toolsMenu.add(_actions.get("spreadsheet"));
					toolsMenu.addSeparator();
					toolsMenu.add(_actions.get("svgview"));
					toolsMenu.add(_actions.get("pdfview"));
					toolsMenu.addSeparator();
					toolsMenu.add(_actions.get("pythonconsole"));
					toolsMenu.add(_actions.get("clientpythonconsole"));
					toolsMenu.addSeparator();
					toolsMenu.add(_actions.get("groovyconsole"));
					toolsMenu.add(_actions.get("clientgroovyconsole"));
					toolsMenu.addSeparator();
					toolsMenu.add(_actions.get("logview"));
					toolsMenu.addSeparator();
					toolsMenu.add(_actions.get("sourcebioclite"));
					toolsMenu.add(_actions.get("installpackage"));
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(toolsMenu);

			final JMenu graphicsMenu = new JMenu("Graphics");
			graphicsMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					graphicsMenu.removeAll();
					graphicsMenu.add(_actions.get("createdevice"));
					graphicsMenu.addSeparator();

					/*
					 * graphicsMenu.add(new SnapshotDeviceAction(GDApplet.this,
					 * _sessionId == null ? null : getCurrentJGPanelPop()));
					 * graphicsMenu.add(new
					 * SaveDeviceAsPngAction(GDApplet.this));
					 * graphicsMenu.add(new
					 * SaveDeviceAsJpgAction(GDApplet.this));
					 * graphicsMenu.addSeparator();
					 */

					graphicsMenu.add(new AbstractAction("Fit Device to Panel") {

						public void actionPerformed(ActionEvent e) {
							getCurrentJGPanelPop().fit();
						}

						public boolean isEnabled() {
							return _sessionId != null;
						}
					});
					graphicsMenu.addSeparator();

					JRadioButtonMenuItem zoomSelect = new JRadioButtonMenuItem("Zoom In / Out on Region   [mouse click&drag / ctrl-mouse click&drag]",
							_sessionId != null && getCurrentJGPanelPop().getInteractor() == INTERACTOR_ZOOM_IN_OUT_SELECT);
					zoomSelect.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_ZOOM_IN_OUT_SELECT) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_ZOOM_IN_OUT_SELECT);
								}

							}
						}
					});
					zoomSelect.setEnabled(_sessionId != null);
					graphicsMenu.add(zoomSelect);

					JRadioButtonMenuItem zoom = new JRadioButtonMenuItem("Zoom In / Out   [mouse click / ctrl-mouse click]", _sessionId != null
							&& getCurrentJGPanelPop().getInteractor() == INTERACTOR_ZOOM_IN_OUT);
					zoom.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_ZOOM_IN_OUT) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_ZOOM_IN_OUT);
								}

							}
						}
					});
					zoom.setEnabled(_sessionId != null);
					graphicsMenu.add(zoom);

					JRadioButtonMenuItem scroll = new JRadioButtonMenuItem("Scroll   [mouse drag]", _sessionId != null
							&& getCurrentJGPanelPop().getInteractor() == INTERACTOR_SCROLL);
					scroll.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_SCROLL) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_SCROLL);
								}

							}
						}
					});
					scroll.setEnabled(_sessionId != null);
					graphicsMenu.add(scroll);

					graphicsMenu.addSeparator();

					JRadioButtonMenuItem zoomSelectX = new JRadioButtonMenuItem("Zoom X In / Out on Region   [mouse click&drag / ctrl-mouse click&drag]",
							_sessionId != null && getCurrentJGPanelPop().getInteractor() == INTERACTOR_ZOOM_IN_OUT_X_SELECT);
					zoomSelectX.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_ZOOM_IN_OUT_X_SELECT) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_ZOOM_IN_OUT_X_SELECT);
								}

							}
						}
					});
					zoomSelectX.setEnabled(_sessionId != null);
					graphicsMenu.add(zoomSelectX);

					JRadioButtonMenuItem zoomX = new JRadioButtonMenuItem("Zoom X In / Out   [mouse click / ctrl-mouse click]", _sessionId != null
							&& getCurrentJGPanelPop().getInteractor() == INTERACTOR_ZOOM_IN_OUT_X);
					zoomX.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_ZOOM_IN_OUT_X) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_ZOOM_IN_OUT_X);
								}

							}
						}
					});
					zoomX.setEnabled(_sessionId != null);
					graphicsMenu.add(zoomX);

					JRadioButtonMenuItem scrollX = new JRadioButtonMenuItem("Scroll X Left / Right   [mouse click / ctrl-mouse click]", _sessionId != null
							&& getCurrentJGPanelPop().getInteractor() == INTERACTOR_SCROLL_LEFT_RIGHT);
					scrollX.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_SCROLL_LEFT_RIGHT) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_SCROLL_LEFT_RIGHT);
								}

							}
						}
					});
					scrollX.setEnabled(_sessionId != null);
					graphicsMenu.add(scrollX);

					graphicsMenu.addSeparator();

					JRadioButtonMenuItem zoomSelectY = new JRadioButtonMenuItem("Zoom Y In / Out on Region   [mouse click&drag / ctrl-mouse click&drag]",
							_sessionId != null && getCurrentJGPanelPop().getInteractor() == INTERACTOR_ZOOM_IN_OUT_Y_SELECT);
					zoomSelectY.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_ZOOM_IN_OUT_Y_SELECT) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_ZOOM_IN_OUT_Y_SELECT);
								}

							}
						}
					});
					zoomSelectY.setEnabled(_sessionId != null);
					graphicsMenu.add(zoomSelectY);

					JRadioButtonMenuItem zoomY = new JRadioButtonMenuItem("Zoom Y In / Out   [mouse click / ctrl-mouse click]", _sessionId != null
							&& getCurrentJGPanelPop().getInteractor() == INTERACTOR_ZOOM_IN_OUT_Y);
					zoomY.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_ZOOM_IN_OUT_Y) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_ZOOM_IN_OUT_Y);
								}

							}
						}
					});
					zoomY.setEnabled(_sessionId != null);
					graphicsMenu.add(zoomY);

					JRadioButtonMenuItem scrollY = new JRadioButtonMenuItem("Scroll Y Up / Down   [mouse click / ctrl-mouse click]", _sessionId != null
							&& getCurrentJGPanelPop().getInteractor() == INTERACTOR_SCROLL_UP_DOWN);
					scrollY.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								if (p.getInteractor() == INTERACTOR_SCROLL_UP_DOWN) {
									setInteractor(INTERACTOR_NULL);
								} else {
									setInteractor(INTERACTOR_SCROLL_UP_DOWN);
								}

							}
						}
					});
					scrollY.setEnabled(_sessionId != null);
					graphicsMenu.add(scrollY);

					graphicsMenu.addSeparator();

					JRadioButtonMenuItem mouseTracker = new JRadioButtonMenuItem("Mouse Tracker   [mouse move]", _sessionId != null
							&& getCurrentJGPanelPop().isShowCoordinates());
					mouseTracker.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							if (_sessionId != null) {
								JGDPanelPop p = getCurrentJGPanelPop();
								p.setShowCoordinates(!p.isShowCoordinates());
							}
						}
					});
					mouseTracker.setEnabled(_sessionId != null);
					graphicsMenu.add(mouseTracker);

				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(graphicsMenu);

			final JMenu collaborationMenu = new JMenu("Collaboration");
			collaborationMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {
					collaborationMenu.removeAll();
					collaborationMenu.add(_actions.get("createbroadcasteddevice"));
					collaborationMenu.add(_actions.get("chatconsoleview"));
					collaborationMenu.add(_actions.get("newcollaborativespreadsheet"));
					collaborationMenu.add(_actions.get("connecttocollaborativespreadsheet"));
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(collaborationMenu);

			final JMenu dataMenu = new JMenu("Java");
			dataMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {
					dataMenu.removeAll();
					dataMenu.add(_actions.get("import_symbol"));
					dataMenu.add(_actions.get("push_symbol"));
					dataMenu.add(_actions.get("inspect"));
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(dataMenu);

			final JMenu lfMenu = new JMenu("Look & Feel");
			lfMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					lfMenu.removeAll();

					JRadioButtonMenuItem[] radioButtonsPool = new JRadioButtonMenuItem[installedLFs.length];
					for (int i = 0; i < installedLFs.length; ++i) {
						final int idx = i;
						radioButtonsPool[idx] = new JRadioButtonMenuItem(installedLFs[idx].getName(), idx == _lf);
						radioButtonsPool[idx].addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								_lf = idx;
								try {
									UIManager.setLookAndFeel(getLookAndFeelClassName());
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								SwingUtilities.updateComponentTreeUI(getContentPane());

							}
						});
						lfMenu.add(radioButtonsPool[idx]);
					}

				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(lfMenu);

			final JMenu demoMenu = new JMenu("Demos / Howtos");
			demoMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {
					demoMenu.removeAll();
					if (_sessionId != null) {
						try {
							final String[] demos = _rForConsole.listDemos();

							for (int i = 0; i < demos.length; ++i) {
								final int index = i;
								demoMenu.add(new AbstractAction(PoolUtils.replaceAll(demos[i], "_", " ")) {
									public void actionPerformed(ActionEvent e) {

										if (getRLock().isLocked()) {
											JOptionPane.showMessageDialog(null, "R is busy");
										} else {
											try {
												getRLock().lock();
												String log = _rForConsole.sourceFromBuffer(_rForConsole.getDemoSource(demos[index]));

												getConsoleLogger().print("sourcing demo " + PoolUtils.replaceAll(demos[index], "_", " "), log);

											} catch (Exception ex) {
												ex.printStackTrace();
											} finally {
												getRLock().unlock();
											}
										}

									}

									public boolean isEnabled() {
										return _sessionId != null && !getRLock().isLocked();
									}
								});

							}

							demoMenu.addSeparator();

							for (int i = 0; i < demos.length; ++i) {
								final int index = i;
								demoMenu.add(new AbstractAction("Copy to Clipboard - " + PoolUtils.replaceAll(demos[i], "_", " ")) {
									public void actionPerformed(ActionEvent e) {
										try {

											StringSelection stringSelection = new StringSelection(_rForConsole.getDemoSource(demos[index]).toString());
											Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
											clipboard.setContents(stringSelection, new ClipboardOwner() {
												public void lostOwnership(Clipboard clipboard, Transferable contents) {
												}
											});

										} catch (Exception ex) {
											ex.printStackTrace();
										}
									}

									@Override
									public boolean isEnabled() {
										return _sessionId != null;
									}
								});

							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(demoMenu);

			final JMenu pluginsMenu = new JMenu("Plugins");
			pluginsMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {
					pluginsMenu.removeAll();
					pluginsMenu.add(_actions.get("showpluginview"));
				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(pluginsMenu);

			final JMenu helpMenu = new JMenu("Help");
			helpMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					helpMenu.removeAll();
					helpMenu.add(_actions.get("help"));
					helpMenu.add(_actions.get("biocepmindmap"));
					helpMenu.addSeparator();
					helpMenu.add(_actions.get("rbiocmanual"));
					helpMenu.add(_actions.get("graphicstaskpage"));
					helpMenu.add(_actions.get("thergraphgallery"));
					helpMenu.add(_actions.get("rgraphicalmanual"));
					helpMenu.addSeparator();
					helpMenu.add(_actions.get("about"));

				}

				public void menuCanceled(MenuEvent e) {
				}

				public void menuDeselected(MenuEvent e) {
				}
			});
			menuBar.add(helpMenu);

			JScrollPane filesScrollPane = new JScrollPane(_filesTable);
			filesScrollPane.addMouseListener(ml);
			workingDirPanel.add(filesScrollPane, BorderLayout.CENTER);

			views[0] = new View("R Console", null, _consolePanel);
			views[1] = new View("Main Graphic Device", null, _rootGraphicPanel);
			views[2] = new View("Working Directory", null, workingDirPanel);
			ViewMap viewMap = new ViewMap();
			viewMap.addView(0, views[0]);
			viewMap.addView(1, views[1]);
			viewMap.addView(2, views[2]);

			MixedViewHandler handler = new MixedViewHandler(viewMap, new ViewSerializer() {
				public void writeView(View view, ObjectOutputStream out) throws IOException {
					out.writeInt(((DynamicView) view).getId());
				}

				public View readView(ObjectInputStream in) throws IOException {
					return dynamicViews.get(in.readInt());
				}
			});

			views[1].getViewProperties().setIcon(_currentDeviceIcon);
			final RootWindow rootWindow = DockingUtil.createRootWindow(viewMap, handler, true);
			rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);

			RootWindowProperties properties = new RootWindowProperties();
			properties.addSuperObject(new ShapedGradientDockingTheme().getRootWindowProperties());
			rootWindow.getRootWindowProperties().addSuperObject(properties);
			rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
			rootWindow.addListener(new DockingWindowListener() {

				public void viewFocusChanged(View arg0, View arg1) {
				}

				public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
					updateViews(addedWindow, true);
					if (addedWindow instanceof FloatingWindow)
						updateFloatingWindow((FloatingWindow) addedWindow);
				}

				public void windowClosed(DockingWindow arg0) {
				}

				public void windowClosing(DockingWindow window) throws OperationAbortedException {
					if (window == views[0] || window == views[1] || window == views[2])
						throw new OperationAbortedException("Window close was aborted!");

					if (window instanceof DeviceView) {
						try {
							GDDevice d = ((DeviceView) window).getPanel().getGdDevice();
							if (_currentDevice == d) {
								setCurrentDevice(((JGDPanelPop) _graphicPanel).getGdDevice());
								_currentDevice.setAsCurrentDevice();
							} else {
								getCurrentJGPanelPop().removeCoupledTo(((DeviceView) window).getPanel());
							}
							((DeviceView) window).getPanel().dispose();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (window instanceof TabWindow) {
						TabWindow tw = (TabWindow) window;
						for (int i = 0; i < tw.getChildWindowCount(); ++i) {
							DockingWindow w = tw.getChildWindow(i);
							if (w == views[0] || w == views[1] || w == views[2])
								throw new OperationAbortedException("Window close was aborted!");
						}
					}

					if (window instanceof TabWindow) {
						TabWindow tw = (TabWindow) window;
						for (int i = 0; i < tw.getChildWindowCount(); ++i) {
							DockingWindow w = tw.getChildWindow(i);
							if (w instanceof DeviceView) {
								try {

									GDDevice d = ((DeviceView) w).getPanel().getGdDevice();
									if (_currentDevice == d) {
										setCurrentDevice(((JGDPanelPop) _graphicPanel).getGdDevice());
										_currentDevice.setAsCurrentDevice();
									}
									((DeviceView) w).getPanel().dispose();

								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				}

				public void windowDocked(DockingWindow arg0) {
				}

				public void windowDocking(DockingWindow arg0) throws OperationAbortedException {
				}

				public void windowHidden(DockingWindow arg0) {
				}

				public void windowMaximized(DockingWindow arg0) {
				}

				public void windowMaximizing(DockingWindow window) throws OperationAbortedException {
				}

				public void windowMinimized(DockingWindow arg0) {
				}

				public void windowMinimizing(DockingWindow window) throws OperationAbortedException {
					if (window == views[2])
						throw new OperationAbortedException("Window minimize was aborted!");
					if (window instanceof TabWindow) {
						TabWindow tw = (TabWindow) window;
						for (int i = 0; i < tw.getChildWindowCount(); ++i) {
							DockingWindow w = tw.getChildWindow(i);
							if (w == views[2])
								throw new OperationAbortedException("Window minimize was aborted!");
						}
					}
				}

				public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
					updateViews(removedWindow, false);
				}

				public void windowRestored(DockingWindow arg0) {
				}

				public void windowRestoring(DockingWindow arg0) throws OperationAbortedException {
				}

				public void windowShown(DockingWindow arg0) {
				}

				public void windowUndocked(DockingWindow arg0) {
				}

				public void windowUndocking(DockingWindow arg0) throws OperationAbortedException {
				}

			});

			NewWindow._applet = this;
			getContentPane().setLayout(new BorderLayout());
			getContentPane().add(menuBar, BorderLayout.NORTH);
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(new JLabel("   "), BorderLayout.NORTH);
			mainPanel.add(rootWindow, BorderLayout.CENTER);
			((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.gray, 6));
			getContentPane().add(mainPanel, BorderLayout.CENTER);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					rootWindow.setWindow(new SplitWindow(true, 0.4f, views[0], new SplitWindow(false, 0.7f, new TabWindow(new DockingWindow[] { views[1] }),
							views[2])));
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		_nopool = getParameter("nopool") == null || getParameter("nopool").equals("") || !getParameter("nopool").equalsIgnoreCase("false");
		_save = (getParameter("save") != null && getParameter("save").equalsIgnoreCase("true"));
		_wait = (getParameter("wait") != null && getParameter("wait").equalsIgnoreCase("true"));
		_demo = (getParameter("demo") != null && getParameter("demo").equalsIgnoreCase("true"));
		_login = getParameter("login");

		LoginDialog.persistentWorkspace_bool = _save;
		LoginDialog.nopool_bool = _nopool;
		LoginDialog.waitForResource_bool = _wait;
		LoginDialog.playDemo_bool = _demo;
		LoginDialog.login_str = _login;

		if (getParameter("autologon") == null || getParameter("autologon").equalsIgnoreCase("true")) {
			new Thread(new Runnable() {
				public void run() {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							_consolePanel.play("logon", false);
						}
					});

				}
			}).start();

		}

		new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						if (_sessionId != null) {
							reload();
						}
					} catch (Exception e) {

					} finally {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
					}
				}
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				try {
					ServerManager.downloadBioceCore(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		_instance = this;
		System.out.println("INIT ends");

	}

	private GDHelpBrowser getOpenedBrowser() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof HelpView) {
				return ((HelpView) dv).getBrowser();
			}
		}
		return null;
	}

	private Vector<DeviceView> getDeviceViews() {
		Vector<DeviceView> result = new Vector<DeviceView>();
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof DeviceView) {
				result.add((DeviceView) dv);
			}
		}
		return result;
	}

	private Vector<views.CollaborativeSpreadsheetView> getCollaborativeSpreadsheetViews() {
		Vector<CollaborativeSpreadsheetView> result = new Vector<CollaborativeSpreadsheetView>();
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof CollaborativeSpreadsheetView) {
				result.add((CollaborativeSpreadsheetView) dv);
			}
		}
		return result;
	}

	private JTextArea getOpenedLogViewerArea() {
		LogView lv = getOpenedLogView();
		if (lv != null)
			return lv.getArea();
		else
			return null;
	}

	private LogView getOpenedLogView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof LogView) {
				return (LogView) dv;
			}
		}
		return null;
	}

	private ChatConsoleView getOpenedChatConsoleView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof ChatConsoleView) {
				return (ChatConsoleView) dv;
			}
		}
		return null;
	}

	private ServerLogView getOpenedServerLogView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof ServerLogView) {
				return (ServerLogView) dv;
			}
		}
		return null;
	}

	private ServerPythonConsoleView getOpenedServerPythonConsoleView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof ServerPythonConsoleView) {
				return (ServerPythonConsoleView) dv;
			}
		}
		return null;
	}

	private ClientPythonConsoleView getOpenedClientPythonConsoleView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof ClientPythonConsoleView) {
				return (ClientPythonConsoleView) dv;
			}
		}
		return null;
	}

	private ClientGroovyConsoleView getOpenedClientGroovyConsoleView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof ClientGroovyConsoleView) {
				return (ClientGroovyConsoleView) dv;
			}
		}
		return null;
	}

	private void setHelpBrowserURL(String url) {
		GDHelpBrowser openedBrowser = getOpenedBrowser();
		if (openedBrowser == null) {

			GDHelpBrowser _helpBrowser = new GDHelpBrowser(this);
			try {
				_helpBrowser.setURL(url);
			} catch (Exception e) {
				e.printStackTrace();
			}

			int id = getDynamicViewId();
			((TabWindow) views[2].getWindowParent()).addTab(new HelpView("Help View", null, _helpBrowser, id));

		} else {

			try {
				openedBrowser.setURL(url);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	class FileCellRenderer extends JLabel implements TableCellRenderer {
		protected Border m_noFocusBorder;
		SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss");

		public FileCellRenderer() {
			super();
			m_noFocusBorder = new EmptyBorder(1, 2, 1, 2);
			setOpaque(true);
			setBorder(m_noFocusBorder);
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

			if (value != null) {
				if (column == 3) {
					Date d = (Date) value;
					setText(sdf.format(d));
				} else {
					setText(value.toString());
				}
			} else
				setText("");

			setBackground(isSelected /* && !hasFocus */? table.getSelectionBackground() : table.getBackground());
			setForeground(isSelected /* && !hasFocus */? table.getSelectionForeground() : table.getForeground());

			if (_workDirFiles.elementAt(row).isDir()) {
				setFont(table.getFont().deriveFont(Font.BOLD));
			} else {
				setFont(table.getFont());
			}
			setBorder(false /* hasFocus */? UIManager.getBorder("Table.focusCellHighlightBorder") : m_noFocusBorder);

			return this;
		}

	};

	class FileMousePopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			checkPopup(e);
		}

		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2) {
				int row = _filesTable.rowAtPoint(e.getPoint());

				if (row >= 0) {

				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			checkPopup(e);
		}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu popupMenu = new JPopupMenu();

				popupMenu.add(_actions.get("import"));
				popupMenu.add(_actions.get("export"));
				popupMenu.add(_actions.get("delete"));
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	private void playDemo() {
		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(GDApplet.class.getResourceAsStream("demoscript.R")));
					String l;
					while ((l = br.readLine()) != null) {

						l = l.trim();
						if (l.equals(""))
							continue;

						if (l.equals("#SNAPSHOT")) {

							/*
							 * try { Thread.sleep(1400); } catch (Exception ex) { }
							 * _actions.get("clone").actionPerformed(new
							 * ActionEvent(_graphicPanel, 0, null));
							 */

						} else {
							_consolePanel.play(l, true);
						}
						try {
							Thread.sleep(300);
						} catch (Exception ex) {
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	synchronized private void reload() {

		if (_rForFiles == null)
			return;

		FileDescription[] descriptions = null;
		try {
			descriptions = _rForFiles.getWorkingDirectoryFileDescriptions();
		} catch (NotLoggedInException nle) {
			noSession();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Vector<FileDescription> temp = new Vector<FileDescription>();
		for (int i = 0; i < descriptions.length; ++i)
			temp.add(descriptions[i]);
		saveSelection();
		_workDirFiles = temp;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				((AbstractTableModel) _filesTable.getModel()).fireTableDataChanged();
				restoreSelection();
			}
		});

	}

	private void saveSelection() {

		_selectedFiles.removeAllElements();
		if (_workDirFiles.size() > 0) {
			int[] selRows = _filesTable.getSelectedRows();
			for (int i = 0; i < selRows.length; ++i) {
				_selectedFiles.add(_workDirFiles.elementAt(selRows[i]).getName());
			}
		}

	}

	void restoreSelection() {
		for (int i = 0; i < _workDirFiles.size(); ++i) {
			if (_selectedFiles.contains(_workDirFiles.elementAt(i).getName())) {
				_filesTable.getSelectionModel().addSelectionInterval(i, i);
			}
		}
	}

	private void restoreState() {
		File settings = new File(GDApplet.SETTINGS_FILE);
		if (settings.exists()) {
			try {
				Properties props = new Properties();
				props.loadFromXML(new FileInputStream(settings));
				if (props.get("working.dir.root") != null) {
					_rForConsole.consoleSubmit("setwd('" + props.get("working.dir.root") + "')");
				}

				if (props.get("command.history") != null) {
					_consolePanel.setCommandHistory((Vector<String>) PoolUtils.hexToObject((String) props.get("command.history")));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (_save) {
			try {
				_rForConsole.consoleSubmit("load('.RData')");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void persistState() {
		try {
			PropertiesGenerator.main(new String[] { GDApplet.SETTINGS_FILE, "working.dir.root=" + ((RChar) _rForConsole.getObject("getwd()")).getValue()[0],
					"command.history=" + PoolUtils.objectToHex(_consolePanel.getCommandHistory()) });
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (_save) {
			try {
				_rForConsole.consoleSubmit("save.image('.RData')");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void destroy() {
		System.out.println("destroy called on " + new Date());
		if (_sessionId != null) {
			try {
				if (_mode == HTTP_MODE) {
					disposeDevices();
					RHttpProxy.logOff(_commandServletUrl, _sessionId);
				} else {
					persistState();
				}
			} catch (TunnelingException e) {
				// e.printStackTrace();
			}
			noSession();
		}
	}

	private void initActions() {
		_actions.put("import", new AbstractAction("Import Selected File") {
			public void actionPerformed(ActionEvent ae) {

				if (_chooser == null) {
					try {
						_chooser = new JFileChooser();
					} catch (AccessControlException e) {
						e.printStackTrace();

						String instruction = "grant codeBase \"" + getWebAppUrl() + "-\" {permission java.security.AllPermission;};";
						System.out.println("add to java.policy : " + instruction);
						JOptionPane.showMessageDialog(GDApplet.this, "The Applet has no permissions to access your local disk.\n" + "please add : "
								+ instruction + " \n" + "to the java.policy file of your JRE \n", "Permissions Required", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				final String fileName = _workDirFiles.elementAt(_filesTable.getSelectedRows()[0]).getName();
				_chooser.setSelectedFile(new File(fileName));
				int returnVal = _chooser.showSaveDialog(GDApplet.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					new Thread(new Runnable() {
						public void run() {
							try {
								FileLoad.download(fileName, _chooser.getSelectedFile(), _rForFiles);
							} catch (NotLoggedInException nle) {
								noSession();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				}

			}

			@Override
			public boolean isEnabled() {
				return getR() != null && (_filesTable.getSelectedRows().length > 0 && !_workDirFiles.elementAt(_filesTable.getSelectedRows()[0]).isDir());
			}
		});

		_actions.put("export", new AbstractAction("Export Local File") {

			public void actionPerformed(ActionEvent ae) {
				if (_chooser == null) {
					try {
						_chooser = new JFileChooser();
					} catch (AccessControlException ace) {
						ace.printStackTrace();
						String instruction = "grant codeBase \"" + getWebAppUrl() + "-\" {permission java.security.AllPermission;};";
						System.out.println("add to java.policy : " + instruction);
						JOptionPane.showMessageDialog(GDApplet.this, "The Applet has no permissions to access your local disk.\n" + "please add : "
								+ instruction + " \n" + "to the java.policy file of your JRE \n", "Permissions Required", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				try {
					_chooser.setSelectedFile(new File(""));
					int returnVal = _chooser.showOpenDialog(GDApplet.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {

						final PushAsDialog paDialog = new PushAsDialog(GDApplet.this, _chooser.getSelectedFile().getName());
						paDialog.setVisible(true);

						if (paDialog.getFileName() != null) {

							new Thread(new Runnable() {
								public void run() {
									try {
										FileLoad.upload(_chooser.getSelectedFile(), paDialog.getFileName(), _rForFiles);
									} catch (NotLoggedInException nle) {
										noSession();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							}).start();

						}

					}

				} catch (Exception ex) {

				}

			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("stopeval", new AbstractAction("Stop R") {

			public void actionPerformed(ActionEvent ae) {
				try {
					_rForConsole.stop();
				} catch (NotLoggedInException nle) {
					noSession();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("interrupteval", new AbstractAction("Interrupt Server Call") {

			public void actionPerformed(ActionEvent ae) {
				try {
					if (getMode() == HTTP_MODE) {
						RHttpProxy.interrupt(_commandServletUrl, _sessionId);
					}
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("delete", new AbstractAction() {
			public Object getValue(String key) {

				if (key.equals(Action.NAME)) {
					if (_filesTable.getSelectedRows().length != 1) {
						putValue(Action.NAME, "Delete");
					} else {
						if (_workDirFiles.elementAt(_filesTable.getSelectedRows()[0]).isDir()) {
							putValue(Action.NAME, "Delete Selected Directory Recursively");
						} else {
							putValue(Action.NAME, "Delete Selected File");
						}
					}
				}
				return super.getValue(key);
			}

			public void actionPerformed(ActionEvent ae) {
				try {
					_rForFiles.removeWorkingDirectoryFile(_workDirFiles.elementAt(_filesTable.getSelectedRows()[0]).getName());
				} catch (NotLoggedInException nle) {
					noSession();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public boolean isEnabled() {
				return getR() != null && _filesTable.getSelectedRows().length > 0;
			}
		});

		_actions.put("import_symbol", new AbstractAction("Save R/Java Object To Local File") {
			public void actionPerformed(ActionEvent ae) {

				new Thread(new Runnable() {
					public void run() {
						try {

							final SymbolPopDialog sdialog = new SymbolPopDialog(GDApplet.this, null, _rForConsole.listSymbols(), true);

							SwingUtilities.invokeAndWait(new Runnable() {
								public void run() {
									sdialog.setVisible(true);
								};
							});

							if (sdialog.getSymbolName() != null) {

								try {
									((JGDPanelPop) _graphicPanel).setAutoModes(true, false);
									System.out.println("pop this : " + sdialog.getSymbolName());
									Object value = _rForConsole.pop(sdialog.getSymbolName());
									byte[] buffer = PoolUtils.objectToBytes(value);
									RandomAccessFile raf = new RandomAccessFile(sdialog.getFileName(), "rw");
									raf.setLength(0);
									raf.write(buffer);
									raf.close();
								} finally {
									((JGDPanelPop) _graphicPanel).setAutoModes(true, true);
								}

							}

						} catch (NotLoggedInException nle) {
							noSession();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return getR() != null && !getRLock().isLocked();
			}
		});

		_actions.put("push_symbol", new AbstractAction("Load R/Java Object From Local File") {
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {
					public void run() {
						try {

							final SymbolPushDialog sdialog = new SymbolPushDialog(GDApplet.this.getContentPane(), null, null, true);
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
								Object value = PoolUtils.bytesToObject(buffer);

								try {
									((JGDPanelPop) _graphicPanel).setAutoModes(true, false);
									_rForConsole.push(sdialog.getPushAs(), (Serializable) value);
								} finally {
									((JGDPanelPop) _graphicPanel).setAutoModes(true, true);
								}
							}

						} catch (NotLoggedInException nle) {
							noSession();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

			}

			@Override
			public boolean isEnabled() {
				return getR() != null && !getRLock().isLocked();
			}
		});

		_actions.put("help", new AbstractAction("Help Contents") {
			public void actionPerformed(ActionEvent e) {
				setHelpBrowserURL(_defaultHelpUrl);
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("rbiocmanual", new AbstractAction("R/Bioconductor Manual") {
			public void actionPerformed(ActionEvent e) {
				String link = "http://faculty.ucr.edu/~tgirke/Documents/R_BioCond/R_BioCondManual.html";
				try {
					if (Utils.isWebBrowserSupported()) {
						Utils.showDocument(new URL(link));
					} else {
						setHelpBrowserURL(link);
					}
				} catch (Exception ex) {
					setHelpBrowserURL(link);
				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("graphicstaskpage", new AbstractAction("Graphics Task Page") {
			public void actionPerformed(ActionEvent e) {
				String link = "http://cran.r-project.org/src/contrib/Views/Graphics.html";
				try {

					if (Utils.isWebBrowserSupported()) {
						Utils.showDocument(new URL(link));
					} else {
						setHelpBrowserURL(link);
					}
				} catch (Exception ex) {
					setHelpBrowserURL(link);
				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("thergraphgallery", new AbstractAction("The R Graph Gallery") {
			public void actionPerformed(ActionEvent e) {
				String link = "http://addictedtor.free.fr/graphiques/allgraph.php";
				try {
					if (Utils.isWebBrowserSupported()) {
						Utils.showDocument(new URL(link));
					} else {
						setHelpBrowserURL(link);
					}
				} catch (Exception ex) {
					setHelpBrowserURL(link);
				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("rgraphicalmanual", new AbstractAction("R Graphical Manual") {
			public void actionPerformed(ActionEvent e) {
				String link = "http://cged.genes.nig.ac.jp/RGM2/index.php";
				try {

					if (Utils.isWebBrowserSupported()) {
						Utils.showDocument(new URL(link));
					} else {
						setHelpBrowserURL(link);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("about", new AbstractAction("About Virtualized R") {
			public void actionPerformed(ActionEvent e) {
				new SplashWindow(new JFrame(), Toolkit.getDefaultToolkit().createImage(GDAppletLauncher.class.getResource("/splash/splashscreen.png")))
						.setVisible(true);
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("logon", new AbstractAction("Log On") {
			public void actionPerformed(ActionEvent e) {
				_consolePanel.play("logon", false);
			}

			@Override
			public boolean isEnabled() {
				return getR() == null;
			}
		});

		_actions.put("logoff", new AbstractAction("Log Off") {
			public void actionPerformed(ActionEvent e) {
				_consolePanel.play("logoff", false);
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("playdemo", new AbstractAction("Play Demo") {
			public void actionPerformed(ActionEvent e) {
				playDemo();
			}

			@Override
			public boolean isEnabled() {
				return getR() != null && !getRLock().isLocked();
			}
		});

		_actions.put("saveimage", new AbstractAction("Save Workspace") {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						try {

							if (getMode() == HTTP_MODE) {
								RHttpProxy.saveimage(_commandServletUrl, _sessionId);
							} else {
								_consolePanel.play("save.image('.RData')", false);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("loadimage", new AbstractAction("Load Workspace") {
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						try {
							if (getMode() == HTTP_MODE) {
								RHttpProxy.loadimage(_commandServletUrl, _sessionId);
							} else {
								_consolePanel.play("load('.RData')", false);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("editor", new AbstractAction("New Script Editor") {
			private boolean firstCall = true;

			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						try {

							try {
								UIManager.setLookAndFeel(getLookAndFeelClassName());
							} catch (Exception e) {
								e.printStackTrace();
							}

							if (firstCall) {
								firstCall = false;
								ClassLoader cl = GDApplet.class.getClassLoader();
								try {
									File jEditDir = new File(System.getProperty("user.dir") + "/jEdit");
									if (jEditDir.exists()) {
										PoolUtils.deleteDirectory(jEditDir);
									}
									new File(System.getProperty("user.dir") + "/jEdit").mkdirs();
								} catch (Exception ex) {
									ex.printStackTrace();
								}
								System.setProperty("jedit.home", System.getProperty("user.dir") + "/jEdit");
								System.setProperty("jedit.newwindow.class", "graphics.rmi.NewWindow");
								System.setProperty("jedit.save.to.r.class", "graphics.rmi.SaveToR");
								cl.loadClass("org.gjt.sp.jedit.jEdit").getMethod("main", new Class<?>[] { String[].class, RGui.class }).invoke(null,
										new Object[] { new String[] { "-noserver", "-noplugins", "-nogui", "-nosettings" }, GDApplet.this });
							}

							GDApplet.class.getClassLoader().loadClass("org.gjt.sp.jedit.jEdit").getMethod("newView", new Class<?>[0]).invoke((Object) null,
									(Object[]) null);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("spreadsheet", new AbstractAction("New Spreadsheet") {
			public void actionPerformed(final ActionEvent ae) {
				final DimensionsDialog ddialog = new DimensionsDialog(GDApplet.this);
				ddialog.setVisible(true);
				if (ddialog.getSpreadsheetDimension() != null) {
					NewWindow.create(new SpreadsheetPanel(new SpreadsheetDefaultTableModel((int) ddialog.getSpreadsheetDimension().getWidth(), (int) ddialog
							.getSpreadsheetDimension().getHeight()), GDApplet.this), "Spreadsheet View");
				}
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("newcollaborativespreadsheet", new AbstractAction("New Collaborative Spreadsheet") {
			public void actionPerformed(final ActionEvent ae) {
				new Thread(new Runnable() {
					public void run() {

						final DimensionsDialog ddialog = new DimensionsDialog(GDApplet.this);
						ddialog.setVisible(true);
						if (ddialog.getSpreadsheetDimension() != null) {
							int id = getDynamicViewId();
							final CollaborativeSpreadsheetView lv = new CollaborativeSpreadsheetView(id, (int) ddialog.getSpreadsheetDimension().getHeight(),
									(int) ddialog.getSpreadsheetDimension().getWidth(), GDApplet.this);
							((TabWindow) views[2].getWindowParent()).addTab(lv);
						}
					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("connecttocollaborativespreadsheet", new AbstractAction("Connect to Collaborative Spreadsheet") {
			public void actionPerformed(final ActionEvent ae) {
				new Thread(new Runnable() {
					public void run() {

						try {
							final SelectIdDialog ddialog = new SelectIdDialog(GDApplet.this, "Connect to Collaborative Spreadsheet", "Spreadsheet Id", getR()
									.listSpreadsheetTableModelRemoteId());
							ddialog.setVisible(true);
							if (ddialog.getId() != null) {

								int id = getDynamicViewId();
								final CollaborativeSpreadsheetView lv = new CollaborativeSpreadsheetView(id, ddialog.getId(), GDApplet.this);
								((TabWindow) views[2].getWindowParent()).addTab(lv);

							}
						} catch (Exception e) {
							e.printStackTrace();
						} finally {

						}

					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("createdevice", new AbstractAction("New Device") {
			public void actionPerformed(final ActionEvent e) {

				JPanel rootGraphicPanel = new JPanel();
				rootGraphicPanel.setLayout(new BorderLayout());
				JPanel graphicPanel = new JPanel();
				rootGraphicPanel.add(graphicPanel, BorderLayout.CENTER);
				int id = getDynamicViewId();
				DeviceView deviceView = new DeviceView("Graphic Device", null, rootGraphicPanel, id);
				((TabWindow) views[2].getWindowParent()).addTab(deviceView);

				try {

					GDDevice newDevice = null;

					_protectR.lock();

					try {

						if (s == null || s.empty()) {
							newDevice = _rForConsole.newDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());
						} else {
							newDevice = s.pop();
							newDevice.fireSizeChangedEvent(rootGraphicPanel.getWidth(), rootGraphicPanel.getHeight());
						}

					} finally {
						_protectR.unlock();
					}

					graphicPanel = new JGDPanelPop(newDevice, true, true, new AbstractAction[] { new SetCurrentDeviceAction(GDApplet.this, newDevice), null,
							new FitDeviceAction(GDApplet.this, newDevice), null, new SnapshotDeviceAction(GDApplet.this),
							new SnapshotDeviceSvgAction(GDApplet.this), new SnapshotDevicePdfAction(GDApplet.this), null,
							new SaveDeviceAsPngAction(GDApplet.this), new SaveDeviceAsJpgAction(GDApplet.this), new SaveDeviceAsSvgAction(GDApplet.this),
							new SaveDeviceAsPdfAction(GDApplet.this), null, new CopyFromCurrentDeviceAction(GDApplet.this),
							new CopyToCurrentDeviceAction(GDApplet.this, newDevice), null, new CoupleToCurrentDeviceAction(GDApplet.this) }, getRLock(),
							getConsoleLogger());

					rootGraphicPanel.removeAll();
					rootGraphicPanel.setLayout(new BorderLayout());
					rootGraphicPanel.add(graphicPanel, BorderLayout.CENTER);

					deviceView.setPanel((JGDPanelPop) graphicPanel);

					setCurrentDevice(newDevice);

					final JGDPanelPop gp = (JGDPanelPop) graphicPanel;
					new Thread(new Runnable() {

						public void run() {
							SwingUtilities.invokeLater(new Runnable() {

								public void run() {
									gp.fit();
								}
							});

						}
					}).start();

				} catch (TunnelingException te) {
					te.printStackTrace();
				} catch (RemoteException re) {
					re.printStackTrace();
				}

			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("createbroadcasteddevice", new AbstractAction("New Broadcasted Device") {
			public void actionPerformed(final ActionEvent e) {

				JPanel rootGraphicPanel = new JPanel();
				rootGraphicPanel.setLayout(new BorderLayout());
				JPanel graphicPanel = new JPanel();
				rootGraphicPanel.add(graphicPanel, BorderLayout.CENTER);
				int id = getDynamicViewId();
				DeviceView deviceView = new DeviceView("Broadcasted Graphic Device", null, rootGraphicPanel, id);
				((TabWindow) views[2].getWindowParent()).addTab(deviceView);

				try {

					GDDevice newDevice = null;

					_protectR.lock();

					try {

						newDevice = _rForConsole.newBroadcastedDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());

					} finally {
						_protectR.unlock();
					}

					graphicPanel = new JGDPanelPop(newDevice, true, true, new AbstractAction[] { new SetCurrentDeviceAction(GDApplet.this, newDevice), null,
							new FitDeviceAction(GDApplet.this, newDevice), null, new SnapshotDeviceAction(GDApplet.this),
							new SnapshotDeviceSvgAction(GDApplet.this), new SnapshotDevicePdfAction(GDApplet.this), null,
							new SaveDeviceAsPngAction(GDApplet.this), new SaveDeviceAsJpgAction(GDApplet.this), new SaveDeviceAsSvgAction(GDApplet.this),
							new SaveDeviceAsPdfAction(GDApplet.this), null, new CopyFromCurrentDeviceAction(GDApplet.this),
							new CopyToCurrentDeviceAction(GDApplet.this, newDevice), null, new CoupleToCurrentDeviceAction(GDApplet.this) }, getRLock(),
							getConsoleLogger());

					rootGraphicPanel.removeAll();
					rootGraphicPanel.setLayout(new BorderLayout());
					rootGraphicPanel.add(graphicPanel, BorderLayout.CENTER);

					deviceView.setPanel((JGDPanelPop) graphicPanel);

					setCurrentDevice(newDevice);

					final JGDPanelPop gp = (JGDPanelPop) graphicPanel;
					new Thread(new Runnable() {

						public void run() {
							SwingUtilities.invokeLater(new Runnable() {

								public void run() {
									gp.fit();
								}
							});

						}
					}).start();

				} catch (TunnelingException te) {
					te.printStackTrace();
				} catch (RemoteException re) {
					re.printStackTrace();
				}

			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("logview", new AbstractAction("Console Log Viewer") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedLogView() == null) {

					try {
						_rForConsole.setProgressiveConsoleLogEnabled(true);
					} catch (Exception ex) {
						ex.printStackTrace();
					}

					int id = getDynamicViewId();
					DockingWindow lv = new LogView("Console Log Viewer", null, id);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {

						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
								_rForConsole.setProgressiveConsoleLogEnabled(false);
							} catch (Exception e) {
							}
						}

					});
				} else {
					getOpenedLogView().restoreFocus();
				}
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("serverlogview", new AbstractAction("R Server Log Viewer") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerLogView() == null) {
					int id = getDynamicViewId();
					final ServerLogView lv = new ServerLogView("R Server Log Viewer", null, id);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {

						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
								_rForConsole.removeErrListener(((ServerLogView) arg0).getRemoteLogListenerImpl());
								_rForConsole.removeOutListener(((ServerLogView) arg0).getRemoteLogListenerImpl());
								UnicastRemoteObject.unexportObject(((ServerLogView) arg0).getRemoteLogListenerImpl(), false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					});

					lv.setLayout(new BorderLayout());
					JScrollPane _scrollPane = new JScrollPane(lv.getArea());

					lv.add(_scrollPane);

					lv.getArea().setEditable(false);
					lv.getArea().addMouseListener(new MouseAdapter() {
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
								popupMenu.add(new AbstractAction("Clean") {
									public void actionPerformed(ActionEvent e) {
										lv.getArea().setText("");
									}

									public boolean isEnabled() {
										return !lv.getArea().getText().equals("");
									}
								});

								popupMenu.show(lv.getArea(), e.getX(), e.getY());
							}
						}
					});

					try {

						_rForConsole.addOutListener(lv.getRemoteLogListenerImpl());
						_rForConsole.addErrListener(lv.getRemoteLogListenerImpl());
					} catch (RemoteException re) {
						re.printStackTrace();
					}
				}
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("chatconsoleview", new AbstractAction("Chat Console") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedChatConsoleView() == null) {
					int id = getDynamicViewId();

					final ChatConsoleView lv = new ChatConsoleView("Chat Console", null, id, GDApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("svgview", new AbstractAction("New SVG Viewer") {
			public void actionPerformed(final ActionEvent e) {

				int id = getDynamicViewId();

				final SvgView lv = new SvgView("SVG Viewer", null, id, GDApplet.this);
				((TabWindow) views[2].getWindowParent()).addTab(lv);
				lv.addListener(new AbstractDockingWindowListener() {
					public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
						try {
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("pdfview", new AbstractAction("New PDF Viewer") {
			public void actionPerformed(final ActionEvent e) {

				int id = getDynamicViewId();
				final PdfView lv = new PdfView("PDF Viewer", null, id, GDApplet.this);
				((TabWindow) views[2].getWindowParent()).addTab(lv);
				lv.addListener(new AbstractDockingWindowListener() {
					public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
						try {
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("pythonconsole", new AbstractAction("Python Console") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerPythonConsoleView() == null) {
					int id = getDynamicViewId();

					final ServerPythonConsoleView lv = new ServerPythonConsoleView("Python Console", null, id, GDApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {

						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

					});

				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("clientpythonconsole", new AbstractAction("Local Python Console") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedClientPythonConsoleView() == null) {
					int id = getDynamicViewId();

					final ClientPythonConsoleView lv = new ClientPythonConsoleView("Local Python Console", null, id, GDApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("groovyconsole", new AbstractAction("Groovy Console") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerPythonConsoleView() == null) {
					int id = getDynamicViewId();

					final ServerGroovyConsoleView lv = new ServerGroovyConsoleView("Groovy Console", null, id, GDApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

				}
			}

			public boolean isEnabled() {
				return getR() != null && _isGroovyEnabled;
			}
		});

		_actions.put("clientgroovyconsole", new AbstractAction("Local Groovy Console") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerPythonConsoleView() == null) {
					int id = getDynamicViewId();

					final ClientGroovyConsoleView lv = new ClientGroovyConsoleView("Local Groovy Console", null, id, GDApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

				}
			}

			public boolean isEnabled() {
				return getR() != null && GroovyInterpreterSingleton.getInstance() != null;
			}
		});

		_actions.put("biocepmindmap", new AbstractAction("Biocep Mind Map & Doc") {
			public void actionPerformed(final ActionEvent e) {
				{
					int id = getDynamicViewId();

					final BiocepMindMapView lv = new BiocepMindMapView("Biocep Mind Map & Doc", null, id);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
								lv.getFreeMindApplet().destroy();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("inspect", new AbstractAction("Inspect") {
			public void actionPerformed(final ActionEvent e) {

				new Thread(new Runnable() {
					public void run() {
						try {
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									try {
										GetExprDialog dialog = new GetExprDialog(GDApplet.this, "  R Expression", _expressionSave);
										dialog.setVisible(true);
										if (dialog.getExpr() != null) {
											RObject robj = null;
											try {
												((JGDPanelPop) _graphicPanel).setAutoModes(true, false);
												robj = _rForConsole.getObject(dialog.getExpr());
											} catch (NoMappingAvailable re) {
												JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), re.getMessage(), "R Error",
														JOptionPane.ERROR_MESSAGE);
												return;
											} catch (Exception e) {
											} finally {
												((JGDPanelPop) _graphicPanel).setAutoModes(true, true);
											}

											if (_rForConsole.getStatus().toUpperCase().contains("ERROR")) {
												JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), _rForConsole.getStatus(), "R Error",
														JOptionPane.ERROR_MESSAGE);
												return;
											}

											ClassLoader cl = GDApplet.class.getClassLoader();
											System.setProperty("joi.newwindow.class", "graphics.rmi.NewWindow");
											cl.loadClass("org.pf.joi.Inspector").getMethod("inspect", new Class<?>[] { String.class, Object.class }).invoke(
													null, new Object[] { dialog.getExpr(), robj });
										}

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

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("sourcebioclite", new AbstractAction("biocLite") {
			public void actionPerformed(final ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						try {
							final String cmd = "source(\"http://bioconductor.org/biocLite.R\")";
							safeConsoleSubmit(cmd);
							_isBiocLiteSourced = true;

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("installpackage", new AbstractAction("Install Package") {
			public void actionPerformed(final ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {

						try {

							if (!_isBiocLiteSourced) {
								final String cmd = "source(\"http://bioconductor.org/biocLite.R\")";
								safeConsoleSubmit(cmd);
								_isBiocLiteSourced = true;
							}

							GetExprDialog dialog = new GetExprDialog(GDApplet.this, "  R package", _packageNameSave);
							dialog.setVisible(true);
							if (dialog.getExpr() != null) {
								_actions.get("logview").actionPerformed(null);
								final String cmd = "biocLite('" + dialog.getExpr() + "')";
								safeConsoleSubmit(cmd);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			@Override
			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("runhttpserverlocalhost", new AbstractAction("Run Http Virtualization Engine On Local Host") {
			public void actionPerformed(final ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {

						try {

							while (true) {
								GetExprDialog dialog = new GetExprDialog(GDApplet.this, " Run Http Virtualization Engine On Port : ", _httpPortSave);
								dialog.setVisible(true);
								if (dialog.getExpr() != null) {
									try {
										final int port = Integer.decode(dialog.getExpr());
										if (ServerManager.isPortInUse("127.0.0.1", port)) {
											JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), "Port already in use", "", JOptionPane.ERROR_MESSAGE);
										} else {

											_virtualizationServer = new Server(port);
											Context root = new Context(_virtualizationServer, "/", Context.SESSIONS);
											root.addServlet(new ServletHolder(new http.local.LocalGraphicsServlet(GDApplet.this)), "/rvirtual/graphics/*");
											root.addServlet(new ServletHolder(new http.CommandServlet(GDApplet.this)), "/rvirtual/cmd/*");
											root.addServlet(new ServletHolder(new http.local.LocalHelpServlet(GDApplet.this)), "/rvirtual/helpme/*");
											System.out.println("+++++++++++++++++++ going to start virtualization http server port : " + port);
											_virtualizationServer.start();

											JOptionPane
													.showMessageDialog(
															GDApplet.this.getContentPane(),
															"A Virtualization HTTP Engine is now running on port "
																	+ port
																	+ "\n You can control the current R session from anywhere via the Workench\n log on in HTTP mode to the following URL : http://"
																	+ PoolUtils.getHostIp() + ":" + port + "/rvirtual/cmd", "", JOptionPane.INFORMATION_MESSAGE);
											break;

										}
									} catch (Exception e) {
										JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), "Bad Port", "", JOptionPane.ERROR_MESSAGE);
										continue;
									}

								} else {
									break;
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

			public boolean isEnabled() {
				return getR() != null && _virtualizationServer == null;
			}
		});

		_actions.put("stophttpserverlocalhost", new AbstractAction("Stop Http Server On Local Host") {
			public void actionPerformed(final ActionEvent e) {
				try {
					_virtualizationServer.stop();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				_virtualizationServer = null;
			}

			public boolean isEnabled() {
				return getR() != null && _virtualizationServer != null;
			}
		});

		_actions.put("runhttpserver", new AbstractAction("Run Http Virtualization Engine") {
			public void actionPerformed(final ActionEvent ae) {
				new Thread(new Runnable() {
					public void run() {

						while (true) {
							GetExprDialog dialog = new GetExprDialog(GDApplet.this, " Run Http Virtualization Engine On Port : ", _httpPortSave);
							dialog.setVisible(true);
							if (dialog.getExpr() != null) {
								try {
									final int port = Integer.decode(dialog.getExpr());
									if (_rForConsole.isPortInUse(port)) {
										JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), "Port already in use", "", JOptionPane.ERROR_MESSAGE);
									} else {

										_rForConsole.startHttpServer(port);
										JOptionPane
												.showMessageDialog(
														GDApplet.this.getContentPane(),
														"A Virtualization HTTP Engine is now running on port "
																+ port
																+ "\n You can control the current R session from anywhere via the Workench\n log on in HTTP mode to the following URL : http://"
																+ _rForConsole.getHostIp() + ":" + port + "/rvirtual/cmd", "", JOptionPane.INFORMATION_MESSAGE);

										break;
									}
								} catch (Exception e) {
									e.printStackTrace();
									JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), "Bad Port", "", JOptionPane.ERROR_MESSAGE);
									continue;
								}

							} else {
								break;
							}
						}
					}
				}).start();
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("stophttpserver", new AbstractAction("Stop Http Server") {
			public void actionPerformed(final ActionEvent e) {
				try {
					_rForConsole.stopHttpServer();
					JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), "The Virtualization HTTP Engine was stopped successfully", "",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("showpluginview", new AbstractAction("Open Plugin View") {
			public void actionPerformed(final ActionEvent e) {
				PluginDialog pdialog = new PluginDialog(GDApplet.this);
				pdialog.setVisible(true);
				String[] viewDetail = pdialog.getPluginViewDetail();
				if (viewDetail != null) {
					try {
						URLClassLoader cl = new URLClassLoader(new URL[] { new File(viewDetail[0]).toURL() }, GDApplet.class.getClassLoader());
						Class<?> c_ = cl.loadClass(viewDetail[1]);
						JPanel panel = (JPanel) c_.getConstructor(RGui.class).newInstance(GDApplet.this);
						createView(panel, "plugin view");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

	}

	private void disposeDevices() {

		try {
			_rForConsole.removeRConsoleActionListener(_rConsoleActionListenerImpl);
			UnicastRemoteObject.unexportObject(_rConsoleActionListenerImpl, false);
			_rConsoleActionListenerImpl = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			_rForConsole.removeRCollaborationListener(_collaborationListenerImpl);
			UnicastRemoteObject.unexportObject(_collaborationListenerImpl, false);
			_collaborationListenerImpl = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		((JGDPanelPop) _graphicPanel).stopThreads();
		Vector<DeviceView> deviceViews = getDeviceViews();
		for (int i = 0; i < deviceViews.size(); ++i)
			deviceViews.elementAt(i).getPanel().stopThreads();

		Vector<CollaborativeSpreadsheetView> collaborativeSpreadsheetViews = getCollaborativeSpreadsheetViews();
		for (int i = 0; i < collaborativeSpreadsheetViews.size(); ++i) {
			collaborativeSpreadsheetViews.elementAt(i).close();
		}

		if (getR() instanceof HttpMarker) {
			((HttpMarker) getR()).stopThreads();
		} else {
			try {
				if (_rForConsole.hasRCollaborationListeners()) {
					((JGDPanelPop) _graphicPanel).dispose();
					for (int i = 0; i < deviceViews.size(); ++i)
						deviceViews.elementAt(i).getPanel().dispose();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	private void setInteractor(int interactor) {
		((JGDPanelPop) _graphicPanel).setInteractor(interactor);
		Vector<DeviceView> deviceViews = getDeviceViews();
		for (int i = 0; i < deviceViews.size(); ++i)
			deviceViews.elementAt(i).getPanel().setInteractor(interactor);
	}

	public JGDPanelPop getCurrentJGPanelPop() {
		if (_currentDevice == ((JGDPanelPop) _graphicPanel).getGdDevice())
			return (JGDPanelPop) _graphicPanel;
		Vector<DeviceView> deviceViews = getDeviceViews();
		for (int i = 0; i < deviceViews.size(); ++i) {
			if (deviceViews.elementAt(i).getPanel().getGdDevice() == _currentDevice) {
				return deviceViews.elementAt(i).getPanel();
			}
		}
		throw new RuntimeException("No current device !!!");
	}

	private void noSession() {

		if (_rProcessId != null && !_keepAlive) {

			try {
				_rForConsole.reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (_sshParameters == null) {
				try {
					if (PoolUtils.isWindowsOs()) {
						PoolUtils.killLocalWinProcess(_rProcessId, true);
					} else {
						PoolUtils.killLocalUnixProcess(_rProcessId, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("---> SSH Kill process ID:" + _rProcessId);
				try {
					SSHUtils.killSshProcess(_rProcessId, _sshParameters[0], _sshParameters[1], _sshParameters[2], true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (_virtualizationServer != null) {
			try {
				_virtualizationServer.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		_sessionId = null;
		_rForConsole = null;
		_rForPopCmd = null;
		_rForFiles = null;
		_isBiocLiteSourced = false;
		_keepAlive = null;
		_sshParameters = null;
		_rProcessId = null;
		_virtualizationServer = null;

	}

	static JPanel newPanel(JTextArea a) {
		JPanel result = new JPanel(new BorderLayout());
		result.add(new JScrollPane(a), BorderLayout.CENTER);
		return result;
	}

	public static class PopupListener extends MouseAdapter {
		private JPopupMenu popup;

		public PopupListener(JPopupMenu popup) {
			this.popup = popup;
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		protected void maybeShowPopup(MouseEvent e) {
			if (popup.isPopupTrigger(e)) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	HashMap<Integer, DynamicView> dynamicViews = new HashMap<Integer, DynamicView>();
	View[] views = new View[3];
	public static String NEW_R_STUB_FILE = ServerManager.INSTALL_DIR + "new_R_stub.txt";
	public static String SETTINGS_FILE = ServerManager.INSTALL_DIR + "settings.xml";

	int getDynamicViewId() {
		int id = 0;
		while (dynamicViews.containsKey(new Integer(id)))
			id++;
		return id;
	}

	private void updateViews(DockingWindow window, boolean added) {
		if (window instanceof View) {
			if (window instanceof DynamicView) {
				if (added)
					dynamicViews.put(new Integer(((DynamicView) window).getId()), (DynamicView) window);
				else
					dynamicViews.remove(new Integer(((DynamicView) window).getId()));
			} else {
			}
		} else {
			for (int i = 0; i < window.getChildWindowCount(); i++)
				updateViews(window.getChildWindow(i), added);
		}
	}

	private void updateFloatingWindow(FloatingWindow fw) {
		System.out.println("updateFloatingWindow");
		fw.addListener(new DockingWindowAdapter() {
			public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
			}

			public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
			}

			public void windowClosing(DockingWindow window) throws OperationAbortedException {
				throw new OperationAbortedException();
			}

			public void windowDocking(DockingWindow window) throws OperationAbortedException {
			}

			public void windowUndocking(DockingWindow window) throws OperationAbortedException {
			}
		});
	}

	int getMode() {
		return _mode;
	}

	String getLookAndFeelClassName() {
		return installedLFs[_lf].getClassName();
	}

	String getSessionId() {
		return _sessionId;
	}

	String getHelpServletUrl() {
		return _helpServletUrl;
	}

	String getDefaultHelpUrl() {
		return _defaultHelpUrl;
	}

	public RServices getR() {
		return _rForConsole;
	}

	public ReentrantLock getRLock() {
		return _protectR;
	}

	public ConsoleLogger getConsoleLogger() {
		return _consoleLogger;
	}

	public void synchronizeCollaborators() throws RemoteException {

		new Thread(new Runnable() {
			public void run() {
				try {
					getRLock().lock();
					getCurrentDevice().broadcast();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					((RGuiReentrantLock) getRLock()).unlockNoBroadcast();
				}
			}
		}).start();

	}

	public boolean isCollaborativeMode() {
		// return _mode == HTTP_MODE;
		return true;
	}

	public GDDevice getCurrentDevice() {
		return _currentDevice;
	}

	public void setCurrentDevice(GDDevice device) {

		JGDPanelPop lastCurrentPanel = getCurrentJGPanelPop();
		int interactor = lastCurrentPanel.getInteractor();
		boolean showCoordinates = lastCurrentPanel.isShowCoordinates();
		lastCurrentPanel.setInteractor(INTERACTOR_NULL);
		lastCurrentPanel.setShowCoordinates(false);
		lastCurrentPanel.removeAllCoupledTo();

		try {
			if (_currentDevice.hasLocations())
				safeConsoleSubmit("locator()");
		} catch (Exception e) {
			e.printStackTrace();
		}

		_currentDevice = device;
		views[1].getViewProperties().setIcon(_inactiveDeviceIcon);
		Vector<DeviceView> deviceViews = getDeviceViews();
		for (int i = 0; i < deviceViews.size(); ++i)
			deviceViews.elementAt(i).getViewProperties().setIcon(_inactiveDeviceIcon);

		if (_currentDevice == ((JGDPanelPop) _graphicPanel).getGdDevice()) {
			views[1].getViewProperties().setIcon(_currentDeviceIcon);
			((JGDPanelPop) _graphicPanel).setInteractor(interactor);
			((JGDPanelPop) _graphicPanel).setShowCoordinates(showCoordinates);
		} else {
			for (int i = 0; i < deviceViews.size(); ++i) {
				DeviceView dv = deviceViews.elementAt(i);
				if (dv.getPanel().getGdDevice() == _currentDevice) {
					dv.getViewProperties().setIcon(_currentDeviceIcon);
					dv.getPanel().setInteractor(interactor);
					dv.getPanel().setShowCoordinates(showCoordinates);
					break;
				}
			}
		}
	}

	public Component getRootComponent() {
		return getContentPane();
	}

	String safeConsoleSubmit(final String cmd) throws RemoteException {
		if (getRLock().isLocked()) {
			return "R is busy, please retry\n";
		}
		try {
			getRLock().lock();

			final String log = _rForConsole.consoleSubmit(cmd);

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					getConsoleLogger().print(cmd, log);
				}
			});
			return log;

		} finally {
			getRLock().unlock();
		}

	}

	public View createView(final Component panel, final String title) {
		final View[] result = new View[1];
		Runnable createRunnable = new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(getLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				SwingUtilities.updateComponentTreeUI(panel);

				int id = getDynamicViewId();
				DynamicView v = new DynamicView(title, null, panel, id);
				((TabWindow) views[2].getWindowParent()).addTab(v);
				result[0] = v;
			}
		};

		if (SwingUtilities.isEventDispatchThread()) {
			createRunnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(createRunnable);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return result[0];

	}

	static public CellPoint getSize(String input, char delim) {
		BufferedReader in = new BufferedReader(new StringReader(input));
		String line;
		int rowcount = 0;
		int colcount = 0;

		try {
			while ((line = in.readLine()) != null) {
				rowcount++;

				// initialize new tokenizer on line with tab delimiter.
				// tokenizer = new StringTokenizer(line, "\t");
				int index;
				int prev = 0;

				// set col to 1 before each loop
				int col = 0;

				while (true) {
					index = line.indexOf(delim, prev);
					prev = index + 1;

					// increment column number
					col++;

					if (index == -1) {
						break;
					}
				}

				if (colcount < col) {
					colcount = col;
				}
			}
		} catch (Exception e) {
			return null;
		}

		return new CellPoint(rowcount, colcount);
	}

	public static Component getComponentParent(Component comp, Class<?> clazz) {
		for (;;) {
			if (comp == null)
				break;

			if (comp instanceof JComponent) {
				Component real = (Component) ((JComponent) comp).getClientProperty("KORTE_REAL_FRAME");
				if (real != null)
					comp = real;
			}

			if (clazz.isAssignableFrom(comp.getClass()))
				return comp;
			else if (comp instanceof JPopupMenu)
				comp = ((JPopupMenu) comp).getInvoker();
			else if (comp instanceof FloatingWindowContainer) {
				comp = ((FloatingWindowContainer) comp).getDockableWindowManager();
			} else
				comp = comp.getParent();
		}
		return null;

	}

	public void upload(File localFile, String fileName) throws Exception {
		FileLoad.upload(localFile, fileName, getR());
	}

	public GroovyInterpreter getGroovyInterpreter() {
		return GroovyInterpreterSingleton.getInstance();
	}

	private String uid = null;

	public String getUID() {
		if (uid == null) {
			uid = UUID.randomUUID().toString();
		}
		return uid;
	}

	class RCollaborationListenerImpl extends UnicastRemoteObject implements RCollaborationListener {
		public RCollaborationListenerImpl() throws RemoteException {
			super();
		}

		public void chat(String sourceSession, String message) throws RemoteException {
			if (_sessionId != null && !_sessionId.equals(sourceSession)) {
				ChatConsoleView chatConsoleView = getOpenedChatConsoleView();
				if (chatConsoleView == null) {
					_actions.get("chatconsoleview").actionPerformed(null);
					chatConsoleView = getOpenedChatConsoleView();
				}

				chatConsoleView.getConsolePanel().print("[" + sourceSession + "] - " + message, null);

			}
		}

		public void consolePrint(String sourceSession, String expression, String result) throws RemoteException {
			if (_sessionId != null && !_sessionId.equals(sourceSession)) {
				_consolePanel.print("[" + sourceSession + "] - " + expression, result);
			}
		}
	}

	public String getUserName() {
		return System.getProperty("user.name");
	}

	class RConsoleActionListenerImpl extends UnicastRemoteObject implements RConsoleActionListener {

		public RConsoleActionListenerImpl() throws RemoteException {
			super();
		}

		public void rConsoleActionPerformed(final RConsoleAction action) throws RemoteException {

			System.out.println("console action:" + action);

			if (getUID().equals(action.getAttributes().get("originatorUID"))) {
				if (action.getActionName().equals("help")) {
					new Thread(new Runnable() {
						public void run() {
							try {
								getRLock().lock();
								String topic = (String) action.getAttributes().get("topic");
								String pack = (String) action.getAttributes().get("package");
								String helpUri = _rForPopCmd.getRHelpFileUri(topic, pack);
								if (helpUri == null) {
									setHelpBrowserURL(_defaultHelpUrl);
								} else {
									setHelpBrowserURL(_helpServletUrl + helpUri);
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								getRLock().unlock();
							}
						}
					}).start();
				} else if (action.getActionName().equals("ASYNCHRONOUS_SUBMIT_LOG")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getConsoleLogger().print((String) action.getAttributes().get("command"), (String) action.getAttributes().get("result"));
						}
					});
				} else if (action.getActionName().equals("newHistogram")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								int vsId = (Integer) action.getAttributes().get("vs");
								int vId = (Integer) action.getAttributes().get("v");
								SVarInterface varProxy = RemoteUtil.getSVarWrapper(getR().getVar(vsId, vId));
								SMarkerInterface markerProxy = RemoteUtil.getSMarkerWrapper(getR().getSet(vsId).getMarker());
								String title = (String) action.getAttributes().get("title");
								HistCanvas histCanvas = new HistCanvas((Integer) action.getAttributes().get("gd"), new JFrame(), varProxy, markerProxy);
								createView(histCanvas.getComponent(), title);
								histCanvas.updateObjects();
								markerProxy.addDepend(histCanvas);
								histCanvas.setTitle(title);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (action.getActionName().equals("newScatterplot")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								int vsId = (Integer) action.getAttributes().get("vs");
								int v1Id = (Integer) action.getAttributes().get("v1");
								int v2Id = (Integer) action.getAttributes().get("v2");
								SVarInterface var1Proxy = RemoteUtil.getSVarWrapper(getR().getVar(vsId, v1Id));
								SVarInterface var2Proxy = RemoteUtil.getSVarWrapper(getR().getVar(vsId, v2Id));
								SMarkerInterface markerProxy = RemoteUtil.getSMarkerWrapper(getR().getSet(vsId).getMarker());
								String title = (String) action.getAttributes().get("title");
								int gd = (Integer) action.getAttributes().get("gd");
								ScatterCanvas scatterCanvas = new ScatterCanvas(gd, new JFrame(), var1Proxy, var2Proxy, markerProxy);
								createView(scatterCanvas.getComponent(), title);
								scatterCanvas.updateObjects();
								markerProxy.addDepend(scatterCanvas);
								scatterCanvas.setTitle(title);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (action.getActionName().equals("newMosaic")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								int vsId = (Integer) action.getAttributes().get("vs");
								int[] vIds = (int[]) action.getAttributes().get("v");
								SVarInterface[] v = new SVarInterface[vIds.length];
								for (int i = 0; i < vIds.length; ++i)
									v[i] = RemoteUtil.getSVarWrapper(getR().getVar(vsId, vIds[i]));
								SMarkerInterface markerProxy = RemoteUtil.getSMarkerWrapper(getR().getSet(vsId).getMarker());
								String title = (String) action.getAttributes().get("title");
								int gd = (Integer) action.getAttributes().get("gd");
								MosaicCanvas mosaicCanvas = new MosaicCanvas(gd, new JFrame(), v, markerProxy);
								createView(mosaicCanvas.getComponent(), title);
								mosaicCanvas.updateObjects();
								markerProxy.addDepend(mosaicCanvas);
								mosaicCanvas.setTitle(title);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (action.getActionName().equals("newMap")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								int vsId = (Integer) action.getAttributes().get("vs");
								int vId = (Integer) action.getAttributes().get("v");
								SVarInterface var1Proxy = RemoteUtil.getSVarWrapper(getR().getVar(vsId, vId));
								SMarkerInterface markerProxy = RemoteUtil.getSMarkerWrapper(getR().getSet(vsId).getMarker());
								String title = (String) action.getAttributes().get("title");
								int gd = (Integer) action.getAttributes().get("gd");
								MapCanvas mapCanvas = new MapCanvas(gd, new JFrame(), var1Proxy, markerProxy);
								createView(mapCanvas.getComponent(), title);
								mapCanvas.updateObjects();
								markerProxy.addDepend(mapCanvas);
								mapCanvas.setTitle(title);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (action.getActionName().equals("newBarchart")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								int vsId = (Integer) action.getAttributes().get("vs");
								int vId = (Integer) action.getAttributes().get("v");
								int wgtId = (Integer) action.getAttributes().get("wgt");
								SVarInterface varProxy = RemoteUtil.getSVarWrapper(getR().getVar(vsId, vId));
								SVarInterface wgtProxy = wgtId < 0 ? null : RemoteUtil.getSVarWrapper(getR().getVar(vsId, wgtId));
								SMarkerInterface markerProxy = RemoteUtil.getSMarkerWrapper(getR().getSet(vsId).getMarker());
								String title = (String) action.getAttributes().get("title");
								int gd = (Integer) action.getAttributes().get("gd");
								BarCanvas barCanvas = new BarCanvas(gd, new JFrame(), varProxy, markerProxy, wgtProxy);
								createView(barCanvas.getComponent(), title);
								barCanvas.updateObjects();
								markerProxy.addDepend(barCanvas);
								barCanvas.setTitle(title);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (action.getActionName().equals("newHammock")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								int vsId = (Integer) action.getAttributes().get("vs");
								int[] vIds = (int[]) action.getAttributes().get("v");
								SVarInterface[] v = new SVarInterface[vIds.length];
								for (int i = 0; i < vIds.length; ++i)
									v[i] = RemoteUtil.getSVarWrapper(getR().getVar(vsId, vIds[i]));
								SMarkerInterface markerProxy = RemoteUtil.getSMarkerWrapper(getR().getSet(vsId).getMarker());
								String title = (String) action.getAttributes().get("title");
								int gd = (Integer) action.getAttributes().get("gd");
								HamCanvas mosaicCanvas = new HamCanvas(gd, new JFrame(), v, markerProxy);
								createView(mosaicCanvas.getComponent(), title);
								mosaicCanvas.updateObjects();
								markerProxy.addDepend(mosaicCanvas);
								mosaicCanvas.setTitle(title);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (action.getActionName().equals("newBoxplot")) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {

								int vsId = (Integer) action.getAttributes().get("vs");
								int[] iIds = (int[]) action.getAttributes().get("i");
								int icId = (Integer) action.getAttributes().get("ic");
								SVarInterface[] vl = new SVarInterface[iIds.length];
								for (int i = 0; i < iIds.length; ++i)
									vl[i] = RemoteUtil.getSVarWrapper(getR().getVar(vsId, iIds[i]));

								SVarInterface catVar = RemoteUtil.getSVarWrapper((icId < 0) ? null : getR().getVar(vsId, icId));

								System.out.println("-->catVar:" + catVar);
								SMarkerInterface marker = RemoteUtil.getSMarkerWrapper(getR().getSet(vsId).getMarker());
								String title = (String) action.getAttributes().get("title");
								int gd = (Integer) action.getAttributes().get("gd");
								ParallelAxesCanvas bc = (catVar == null) ? new ParallelAxesCanvas(gd, new JFrame(), vl, marker, ParallelAxesCanvas.TYPE_BOX)
										: new ParallelAxesCanvas(gd, new JFrame(), vl[0], catVar, marker, ParallelAxesCanvas.TYPE_BOX);
								createView(bc.getComponent(), title);
								bc.updateObjects();
								marker.addDepend(bc);
								bc.setTitle(title);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			if (action.getActionName().equals("RESET_CONSOLE_LOG")) {
				final JTextArea area = getOpenedLogViewerArea();
				if (area != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							area.setText("");
							area.repaint();
						}
					});
				}
			} else if (action.getActionName().equals("APPEND_CONSOLE_LOG")) {
				final JTextArea area = getOpenedLogViewerArea();
				if (area != null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							area.setText(area.getText() + action.getAttributes().get("log"));
						}
					});
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							area.setCaretPosition(area.getText().length());
							area.repaint();
						}
					});

				}
			}

		}

	}

}
