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
package org.kchine.r.workbench;

import genericnaming.httpregistryClass;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.StringReader;
import java.net.ServerSocket;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
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
import javax.swing.JToggleButton;
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
import org.kchine.r.RObject;
import org.kchine.r.server.ExtendedReentrantLock;
import org.kchine.r.server.FileDescription;
import org.kchine.r.server.NoMappingAvailable;
import org.kchine.r.server.RCollaborationListener;
import org.kchine.r.server.RConsoleAction;
import org.kchine.r.server.RConsoleActionListener;
import org.kchine.r.server.RServices;
import org.kchine.r.server.UserStatus;
import org.kchine.r.server.Utils;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.http.FileLoad;
import org.kchine.r.server.http.HttpMarker;
import org.kchine.r.server.http.RHttpProxy;
import org.kchine.r.server.http.frontend.BadLoginPasswordException;
import org.kchine.r.server.http.frontend.ConnectionFailedException;
import org.kchine.r.server.http.frontend.NoNodeManagerFound;
import org.kchine.r.server.http.frontend.NoRegistryAvailableException;
import org.kchine.r.server.http.frontend.NoServantAvailableException;
import org.kchine.r.server.http.frontend.NotLoggedInException;
import org.kchine.r.server.http.frontend.TunnelingException;
import org.kchine.r.server.http.local.LocalHttpServer;
import org.kchine.r.server.manager.BadSshHostException;
import org.kchine.r.server.manager.BadSshLoginPwdException;
import org.kchine.r.server.manager.ServantCreationFailed;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.r.server.scripting.GroovyInterpreter;
import org.kchine.r.workbench.scripting.GroovyInterpreterSingleton;
import org.kchine.r.server.spreadsheet.CellPoint;
import org.kchine.r.server.spreadsheet.CellRange;
import org.kchine.r.server.spreadsheet.SpreadsheetDefaultTableModel;
import org.kchine.r.workbench.actions.CopyFromCurrentDeviceAction;
import org.kchine.r.workbench.actions.CopyToCurrentDeviceAction;
import org.kchine.r.workbench.actions.CoupleToCurrentDeviceAction;
import org.kchine.r.workbench.actions.FitDeviceAction;
import org.kchine.r.workbench.actions.SaveDeviceAsBmpAction;
import org.kchine.r.workbench.actions.SaveDeviceAsEmfAction;
import org.kchine.r.workbench.actions.SaveDeviceAsJavaBmpAction;
import org.kchine.r.workbench.actions.SaveDeviceAsJavaJpgAction;
import org.kchine.r.workbench.actions.SaveDeviceAsJavaPngAction;
import org.kchine.r.workbench.actions.SaveDeviceAsJavaGifAction;
import org.kchine.r.workbench.actions.SaveDeviceAsJpgAction;
import org.kchine.r.workbench.actions.SaveDeviceAsOdgAction;
import org.kchine.r.workbench.actions.SaveDeviceAsPdfAction;
import org.kchine.r.workbench.actions.SaveDeviceAsPdfAppletAction;
import org.kchine.r.workbench.actions.SaveDeviceAsPictexAction;
import org.kchine.r.workbench.actions.SaveDeviceAsPngAction;
import org.kchine.r.workbench.actions.SaveDeviceAsPsAction;
import org.kchine.r.workbench.actions.SaveDeviceAsSvgAction;
import org.kchine.r.workbench.actions.SaveDeviceAsTiffAction;
import org.kchine.r.workbench.actions.SaveDeviceAsWmfAction;
import org.kchine.r.workbench.actions.SaveDeviceAsXfigAction;
import org.kchine.r.workbench.actions.SetCurrentDeviceAction;
import org.kchine.r.workbench.actions.SnapshotDeviceAction;
import org.kchine.r.workbench.actions.SnapshotDevicePdfAction;
import org.kchine.r.workbench.actions.SnapshotDeviceSvgAction;
import org.kchine.r.workbench.dialogs.DbInfo;
import org.kchine.r.workbench.dialogs.GetDbDialog;
import org.kchine.r.workbench.dialogs.GetExprDialog;
import org.kchine.r.workbench.dialogs.GetUrlLoginPwdDialog;
import org.kchine.r.workbench.dialogs.Identification;
import org.kchine.r.workbench.dialogs.LoginDialog;
import org.kchine.r.workbench.dialogs.OpenPluginViewDialog;
import org.kchine.r.workbench.dialogs.PushAsDialog;
import org.kchine.r.workbench.dialogs.UrlLoginPwd;
import org.kchine.r.workbench.exceptions.BadServantNameException;
import org.kchine.r.workbench.exceptions.NoDbRegistryAvailableException;
import org.kchine.r.workbench.exceptions.NoRmiRegistryAvailableException;
import org.kchine.r.workbench.exceptions.PingRServerFailedException;
import org.kchine.r.workbench.exceptions.RBusyException;
import org.kchine.r.workbench.graphics.JGDPanelPop;
import org.kchine.r.workbench.macros.Macro;
import org.kchine.r.workbench.macros.MacroInterface;
import org.kchine.r.workbench.plugins.PluginViewDescriptor;
import org.kchine.r.workbench.splashscreen.SplashWindow;
import org.kchine.r.workbench.spreadsheet.DimensionsDialog;
import org.kchine.r.workbench.spreadsheet.SelectIdDialog;
import org.kchine.r.workbench.spreadsheet.SpreadsheetPanel;
import org.kchine.r.workbench.utils.AbstractDockingWindowListener;
import org.kchine.r.workbench.utils.AppletBase;
import org.kchine.r.workbench.views.BiocepMindMapView;
import org.kchine.r.workbench.views.BroadcastedDeviceView;
import org.kchine.r.workbench.views.ChatConsoleView;
import org.kchine.r.workbench.views.ClientGroovyConsoleView;
import org.kchine.r.workbench.views.ClientPythonConsoleView;
import org.kchine.r.workbench.views.CollaborativeSpreadsheetView;
import org.kchine.r.workbench.views.DeviceView;
import org.kchine.r.workbench.views.DynamicView;
import org.kchine.r.workbench.views.HelpBrowserPanel;
import org.kchine.r.workbench.views.HelpView;
import org.kchine.r.workbench.views.PagerView;
import org.kchine.r.workbench.views.PdfView;
import org.kchine.r.workbench.views.ScilabConsoleView;
import org.kchine.r.workbench.views.ServerGroovyConsoleView;
import org.kchine.r.workbench.views.ServerLogView;
import org.kchine.r.workbench.views.ServerPythonConsoleView;
import org.kchine.r.workbench.views.SliderView;
import org.kchine.r.workbench.views.SvgView;
import org.kchine.r.workbench.views.UnsafeEvaluatorView;
import org.kchine.r.workbench.views.UsersView;
import org.kchine.rpf.LocalRmiRegistry;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.PropertiesGenerator;
import org.kchine.rpf.SSHUtils;
import org.kchine.rpf.ServerDefaults;
import org.kchine.rpf.YesSecurityManager;
import org.kchine.rpf.db.ConnectionProvider;
import org.kchine.rpf.db.DBLayer;
import org.kchine.rpf.db.DBLayerInterface;
import org.kchine.rpf.db.SupervisorInterface;
import org.kchine.rpf.db.monitor.Supervisor;
import org.kchine.rpf.db.monitor.SupervisorUtils;
import org.kchine.rpf.gui.ConsolePanel;
import org.kchine.rpf.gui.InDialog;
import org.kchine.rpf.gui.SubmitInterface;
import org.kchine.rpf.gui.SymbolPopDialog;
import org.kchine.rpf.gui.SymbolPushDialog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.rosuda.ibase.RemoteUtil;
import org.rosuda.ibase.SMarkerInterface;
import org.rosuda.ibase.SVarInterface;
import org.rosuda.ibase.plots.BarCanvas;
import org.rosuda.ibase.plots.HamCanvas;
import org.rosuda.ibase.plots.HistCanvas;
import org.rosuda.ibase.plots.MapCanvas;
import org.rosuda.ibase.plots.MosaicCanvas;
import org.rosuda.ibase.plots.ParallelAxesCanvas;
import org.rosuda.ibase.plots.ScatterCanvas;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import static org.kchine.r.workbench.graphics.JGDPanelPop.*;
import static org.kchine.rpf.PoolUtils.redirectIO;
import static org.kchine.rpf.PoolUtils.unzip;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class WorkbenchApplet extends AppletBase implements RGui {

	public static final int NEW_R_MODE = 0;
	public static final int RMI_MODE = 1;
	public static final int HTTP_MODE = 2;

	public static final int RMI_MODE_REGISTRY_MODE = 0;
	public static final int RMI_MODE_DB_MODE = 1;
	public static final int RMI_MODE_STUB_MODE = 2;

	private String _commandServletUrl = null;
	private String _helpRootUrl = null;
	private String _sessionId = null;
	private RServices _rForConsole;
	private RServices _rForFiles;
	private RServices _rForPopCmd;
	private JPanel _graphicPanel;
	private JPanel _rootGraphicPanel;
	private Vector<FileDescription> _workDirFiles = new Vector<FileDescription>();
	private JTable _filesTable;
	private boolean _nopool;
	private boolean _wait;
	private String _login;
	private String _pwd;
	private Vector<String> _selectedFiles = new Vector<String>();
	private HashMap<String, AbstractAction> _actions = new HashMap<String, AbstractAction>();
	private JFileChooser _chooser = null;
	private SubmitInterface _submitInterface = null;
	private ConsolePanel _consolePanel = null;
	private int _mode = -1;
	private LookAndFeelInfo[] installedLFs = UIManager.getInstalledLookAndFeels();
	private int _lf;
	private boolean _isBiocLiteSourced = false;
	private GDDevice _currentDevice;

	private Boolean _keepAlive = null;
	private String[] _sshParameters = null;

	private String _rProcessId = null;
	Server _virtualizationServer = null;
	Server _pluginServer = null;
	int _pluginServerPort = -1;

	Identification ident = null;

	Stack<GDDevice> s = null;

	boolean _selfish = false;

	int maxNbrRactionsOnPop = 50;

	private static RGui _instance;

	public static RGui getInstance() {
		return _instance;
	}

	private RCollaborationListenerImpl _collaborationListenerImpl;
	private RConsoleActionListenerImpl _rConsoleActionListenerImpl;

	private String[] _demos;

	private boolean logonWithoutConfirmation = false;

	private ClassLoader jeditcl = null;

	private static int LOCAL_SPREADSHEET_COUNTER = 0;

	HashMap<String, Vector<PluginViewDescriptor>> pluginViewsHash = new HashMap<String, Vector<PluginViewDescriptor>>();
	Vector<MacroInterface> macrosVector = new Vector<MacroInterface>();

	Vector<Runnable> _tasks = new Vector<Runnable>();

	String _clientIP = null;
	HashSet<String> _availableExtensions = null;

	boolean _macrosEnabled = true;

	String gui_url = null;

	private final ReentrantLock _protectR = new ExtendedReentrantLock() {

		@Override
		public void lock() {
			super.lock();
			try {
				_currentDevice.setAsCurrentDevice();
				_rForConsole.setOrginatorUID(getUID());
			} catch (Exception e) {
				e.printStackTrace();
			}
			updateConsoleIcon(_busyIcon);
			// _consolePanel.setCursor(Cursor.getPredefinedCursor(Cursor.
			// WAIT_CURSOR));
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

			updateConsoleIcon(null);
			super.unlock();

			// _consolePanel.setCursor(Cursor.getPredefinedCursor(Cursor.
			// DEFAULT_CURSOR));
		}

		@Override
		public void rawUnlock() {
			super.unlock();
		}

		@Override
		public void rawLock() {
			super.lock();
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
					getR().consolePrint(getUID(), getUserName(), message, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void printAsOutput(String message) {
			_consolePanel.print(null, message);
			if (isCollaborativeMode()) {
				try {
					getR().consolePrint(getUID(), getUserName(), null, message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void print(String expression, String result) {
			_consolePanel.print(expression, result);
			if (isCollaborativeMode()) {
				try {
					getR().consolePrint(getUID(), getUserName(), expression, result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		public void pasteToConsoleEditor() {
			_consolePanel.pasteToConsoleEditor();
		}

	};

	private Icon _currentDeviceIcon = null;
	private Icon _inactiveDeviceIcon = null;

	private Icon _connectedIcon = null;
	private Icon _disconnectedIcon = null;
	private Icon _busyIcon = null;

	public WorkbenchApplet() throws HeadlessException {
		super();
	}

	public WorkbenchApplet(HashMap<String, String> customParams) {
		super(customParams);
	}

	public String getWebAppUrl() {
		String url = WorkbenchApplet.class.getResource("/graphics/rmi/GDApplet.class").toString();
		System.out.println("url=" + url);
		if (url.contains("http:")) {
			url = url.substring(4, url.indexOf("appletlibs"));
			return url;
		} else {
			return null;
		}
	}

	private boolean isDesktopApplication() {
		return getParameter("desktopapplication") != null && getParameter("desktopapplication").equalsIgnoreCase("true");
	}


	public void init() {
		super.init();

		// _clientIP=PoolUtils.whatIsMyIp();

		if (_clientIP == null) _clientIP = PoolUtils.getHostIp();
		if (System.getProperty("java.rmi.server.hostname") == null || System.getProperty("java.rmi.server.hostname").equals("")) {
			System.setProperty("java.rmi.server.hostname", _clientIP);
		}
		System.out.println("client IP:" + _clientIP);
		PoolUtils.initLog4J();
		PoolUtils.initRmiSocketFactory();
		System.setErr(System.out);
		if (getParameter("debug") != null && getParameter("debug").equalsIgnoreCase("true")) {
			redirectIO();
		}

		if (getParameter("proxy_host")!=null && !getParameter("proxy_host").equals("")) {
			System.setProperty("proxy_host", getParameter("proxy_host"));
		}
		
		if (getParameter("proxy_port")!=null && !getParameter("proxy_port").equals("")) {
			System.setProperty("proxy_port", getParameter("proxy_port"));
		}
		
		System.out.println("INIT starts");


		LocalHttpServer.getRootContext().addServlet(new ServletHolder(new org.kchine.r.server.http.local.LocalHelpServlet(WorkbenchApplet.this)),"/rvirtual/helpme/*");
		LocalRmiRegistry.getLocalRmiRegistryPort();

		try {
			jeditcl = new URLClassLoader(new URL[] { new URL("http://127.0.0.1:" + LocalHttpServer.getLocalHttpServerPort()
					+ "/classes/org/kchine/r/workbench/plugins/embedded/basiceditor.jar") }, WorkbenchApplet.class.getClassLoader());

		} catch (Exception e) {
			e.printStackTrace();
		}

		restoreState();

		if (getParameter("mode") == null || getParameter("mode").equals("")) {
			_mode = LoginDialog.mode_int;
		} else {
			if (getParameter("mode").equalsIgnoreCase("local")) {
				_mode = NEW_R_MODE;
			} else if (getParameter("mode").equalsIgnoreCase("rmi")) {
				_mode = RMI_MODE;
			} else if (getParameter("mode").equalsIgnoreCase("http")) {
				_mode = HTTP_MODE;
			}
		}

		if (getParameter("url") == null || getParameter("url").equals("")) {

			if (LoginDialog.url_str == null || LoginDialog.url_str.equals("")) {
				if (getWebAppUrl() != null) {
					_commandServletUrl = getWebAppUrl() + "cmd";
					System.out.println("1:1: " + _commandServletUrl);
				} else {
					_commandServletUrl = "http://127.0.0.1:8080/rvirtual/cmd";
					System.out.println("1:2: " + _commandServletUrl);
				}
			}

		} else {
			_commandServletUrl = getParameter("url");
			LoginDialog.url_str = _commandServletUrl;
		}

		LoginDialog.mode_int = _mode;

		if (getParameter("stub") != null && !getParameter("stub").equals(""))
			LoginDialog.stub_str = getParameter("stub");
		if (getParameter("name") != null && !getParameter("name").equals(""))
			LoginDialog.servantName_str = getParameter("name");
		if (getParameter("registry_host") != null && !getParameter("registry_host").equals(""))
			LoginDialog.servantName_str = getParameter("registry_host");
		if (getParameter("registry_port") != null && !getParameter("registry_port").equals(""))
			LoginDialog.servantName_str = getParameter("registry_port");
		if (getParameter("privatename") != null && !getParameter("privatename").equals(""))
			LoginDialog.privateName_str = getParameter("privatename");

		if (getParameter("rmi_mode") != null && !getParameter("rmi_mode").equals("")) {
			if (getParameter("rmi_mode").equals("registry")) {
				LoginDialog.rmiMode_int = RMI_MODE_REGISTRY_MODE;
			} else if (getParameter("rmi_mode").equals("db")) {
				LoginDialog.rmiMode_int = RMI_MODE_DB_MODE;
			} else if (getParameter("rmi_mode").equals("stub")) {
				LoginDialog.rmiMode_int = RMI_MODE_STUB_MODE;
			}
		}

		if (getParameter("noconfirmation") != null && getParameter("noconfirmation").equals("true")) {
			logonWithoutConfirmation = true;
			System.out.println("------> NO Confirmation");
		}

		if (getParameter("selfish") != null && getParameter("selfish").equals("true")) {
			_selfish = true;
		}

		_nopool = getParameter("nopool") == null || getParameter("nopool").equals("") || !getParameter("nopool").equalsIgnoreCase("false");
		_wait = (getParameter("wait") != null && getParameter("wait").equalsIgnoreCase("true"));
		_login = getParameter("login");
		_pwd = getParameter("password");

		LoginDialog.nopool_bool = _nopool;
		LoginDialog.waitForResource_bool = _wait;
		LoginDialog.login_str = _login;
		LoginDialog.pwd_str = _pwd;

		try {

			_currentDeviceIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/icons/" + "active_device.gif")));
			_inactiveDeviceIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/icons/" + "inactive_device.png")));
			_connectedIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/icons/" + "connected.gif")));
			_disconnectedIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/icons/" + "disconnected.png")));
			_busyIcon = new ImageIcon(ImageIO.read(WorkbenchApplet.class.getResource("/org/kchine/r/workbench/icons/" + "busy.gif")));

			initActions();

			installedLFs = UIManager.getInstalledLookAndFeels();
			
			int lf = 0;
			try {
				lf = Integer.decode(getParameter("lf"));
			} catch (Exception e) {
				if (PoolUtils.isMacOs()) {
					for (int i=0;i<installedLFs.length;++i) {
						if (installedLFs[i].getName().toLowerCase().indexOf("mac")!=-1) {
							lf=i; break;
						}
					}					
				}
			}
			
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
							LoginDialog loginDialog = new LoginDialog(WorkbenchApplet.this.getContentPane());

							String aliveStub = null;

							if (new File(WorkbenchApplet.NEW_R_STUB_FILE).exists()) {
								BufferedReader pr = new BufferedReader(new FileReader(WorkbenchApplet.NEW_R_STUB_FILE));
								aliveStub = pr.readLine();
								pr.close();
								try {
									PoolUtils.ping((RServices) PoolUtils.hexToStub(aliveStub, WorkbenchApplet.class.getClassLoader()), 1000);
								} catch (Exception e) {
									e.printStackTrace();
									try {
										new File(WorkbenchApplet.NEW_R_STUB_FILE).delete();
									} catch (Exception ex) {
									}
									aliveStub = null;
								}
							}

							if (aliveStub != null) {
								int n = JOptionPane.showConfirmDialog(WorkbenchApplet.this,
										"Would you like to connect to the last created and kept alive R Server ?", "", JOptionPane.OK_CANCEL_OPTION,
										JOptionPane.QUESTION_MESSAGE, null);
								if (n == JOptionPane.OK_OPTION) {

									ident = new Identification(RMI_MODE, "", "", "", false, false, "", RMI_MODE_STUB_MODE, "", -1, "", "", "", -1, "", "", "",
											"", aliveStub, -1, -1, false, false, true, "", "", -1, "", "",

											false, "", -1, "", "");

									showLoginDialog = false;
								}
							} else if (logonWithoutConfirmation) {
								loginDialog.okMethod();
								ident = loginDialog.getIndentification();
								showLoginDialog = false;
								logonWithoutConfirmation = false;
							}

							if (showLoginDialog) {
								loginDialog.setVisible(true);
								ident = loginDialog.getIndentification();
							}

							persistState();

							if (ident == null)
								return "Logon cancelled\n";

							_mode = ident.getMode();

							if (getMode() == HTTP_MODE) {

								if (!ident.isUseSshTunnel()) {
									_commandServletUrl = ident.getUrl();
								} else {

									ch.ethz.ssh2.Connection conn = null;
									try {
										conn = new ch.ethz.ssh2.Connection(ident.getSshTunnelHostIp(), ident.getSshTunnelPort());
										conn.connect();
										boolean isAuthenticated = conn.authenticateWithPassword(ident.getSshTunnelLogin(), ident.getSshTunnelPwd());
										if (isAuthenticated == false)
											throw new BadSshLoginPwdException();
									} catch (Exception e) {
										throw new BadSshLoginPwdException();
									} finally {
										try {
											if (conn != null)
												conn.close();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									System.out.println("Ping SSH Succeeded");

									JSch jsch = new JSch();
									Session session = jsch.getSession(ident.getSshTunnelLogin(), ident.getSshTunnelHostIp(), ident.getSshTunnelPort());
									session.setPassword(ident.getSshTunnelPwd());
									UserInfo lui = new UserInfo() {
										String passwd;

										public String getPassword() {
											return passwd;
										}

										public boolean promptYesNo(String str) {
											return true;
										}

										public String getPassphrase() {
											return null;
										}

										public boolean promptPassphrase(String message) {
											return true;
										}

										public boolean promptPassword(String message) {
											return true;
										}

										public void showMessage(String message) {
										}
									};

									session.setUserInfo(lui);
									session.connect();

									ServerSocket ss = new ServerSocket(0);
									int tunnelLocalPort = ss.getLocalPort();
									ss.close();

									URL httpModeUrl = null;
									try {
										httpModeUrl = new URL(ident.getUrl());
									} catch (Exception e) {
										e.printStackTrace();
										throw new ConnectionFailedException();
									}

									String tunnelRemoteHost = httpModeUrl.getHost();
									int tunnelRemotePort = httpModeUrl.getPort();
									if (tunnelRemotePort <= 0)
										tunnelRemotePort = 80;

									session.setPortForwardingL(tunnelLocalPort, tunnelRemoteHost, tunnelRemotePort);

									_commandServletUrl = "http://" + "127.0.0.1" + ":" + tunnelLocalPort + httpModeUrl.toURI().getPath();
									System.out.println("_commandServletUrl:" + _commandServletUrl);

								}

								_keepAlive = true;
								try {
									// _helpRootUrl =
									// _commandServletUrl.substring(0,
									// _commandServletUrl.lastIndexOf("cmd")) +
									// "helpme";
									_helpRootUrl = "http://127.0.0.1:" + LocalHttpServer.getLocalHttpServerPort() + "/" + "rvirtual/helpme";
								} catch (Exception e) {
									e.printStackTrace();
								}

								_login = ident.getUser();
								_nopool = ident.isNopool();
								_wait = ident.isWaitForResource();
								String pwd = ident.getPwd();

								String oldSessionId = _sessionId;
								HashMap<String, Object> options = new HashMap<String, Object>();
								options.put("nopool", new Boolean(_nopool).toString());
								options.put("privatename", ident.getPrivateName());
								options.put("wait", new Boolean(_wait).toString());
								options.put("memorymin", new Integer(ident.getMemoryMin()).toString());
								options.put("memorymax", new Integer(ident.getMemoryMax()).toString());
								if (_selfish)
									options.put("selfish", "true");

								_sessionId = RHttpProxy.logOn(_commandServletUrl, _sessionId, _login, pwd, options);

								if (_sessionId.equals(oldSessionId)) {
									return "Already logged on\n";
								}

								_rForConsole = RHttpProxy.getR(_commandServletUrl, _sessionId, true, maxNbrRactionsOnPop);
								_rForPopCmd = RHttpProxy.getR(_commandServletUrl, _sessionId, false, maxNbrRactionsOnPop);
								_rForFiles = RHttpProxy.getR(_commandServletUrl, _sessionId, false, maxNbrRactionsOnPop);

								if (new File(WorkbenchApplet.NEW_R_STUB_FILE).exists())
									new File(WorkbenchApplet.NEW_R_STUB_FILE).delete();

							} else {

								_helpRootUrl = "http://127.0.0.1:" + LocalHttpServer.getLocalHttpServerPort() + "/" + "rvirtual/helpme";

								RServices r = null;

								if (getMode() == WorkbenchApplet.NEW_R_MODE) {

									/*
									 * 
									 * DirectJNI.init(); r =
									 * DirectJNI.getInstance().getRServices();
									 * if (false) throw new
									 * BadSshHostException(); if (false) throw
									 * new BadSshLoginPwdException(); _keepAlive
									 * = ident.isKeepAlive();
									 */

									_keepAlive = ident.isKeepAlive();
									if (ident.isUseSsh()) {
										r = ServerManager.createRSsh(ident.isKeepAlive(), _clientIP, LocalHttpServer.getLocalHttpServerPort(), ServerManager
												.getRegistryNamingInfo(_clientIP, LocalRmiRegistry.getLocalRmiRegistryPort()), ident.getMemoryMin(), ident
												.getMemoryMax(), ident.getSshHostIp(), ident.getSshPort(), ident.getSshLogin(), ident.getSshPwd(), "", false,
												null, null);
									} else {

										r = ServerManager.createR(ident.isDefaultR() ? System.getProperty("r.binary") : ident.getDefaultRBin(), ident
												.isKeepAlive(), PoolUtils.getHostIp(), LocalHttpServer.getLocalHttpServerPort(), ServerManager
												.getRegistryNamingInfo(PoolUtils.getHostIp(), LocalRmiRegistry.getLocalRmiRegistryPort()),
												ident.getMemoryMin(), ident.getMemoryMax(), "", true, null, null, true, null);
									}

									if (ident.isUseSsh()) {
										_sshParameters = new String[] { ident.getSshHostIp(), ident.getSshLogin(), ident.getSshPwd() };
									}

									_rProcessId = r.getProcessId();
									System.out.println("R Process Id :" + _rProcessId);

									new File(WorkbenchApplet.NEW_R_STUB_FILE).delete();
									if (_keepAlive) {
										PrintWriter pw = new PrintWriter(new FileWriter(WorkbenchApplet.NEW_R_STUB_FILE));
										pw.println(PoolUtils.stubToHex(r));
										pw.close();
									}

								} else {

									_keepAlive = true;
									if (ident.getRmiMode() == RMI_MODE_STUB_MODE) {
										r = (RServices) PoolUtils.hexToStub(ident.getStub(), WorkbenchApplet.class.getClassLoader());
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

										new File(WorkbenchApplet.NEW_R_STUB_FILE).delete();

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

										new File(WorkbenchApplet.NEW_R_STUB_FILE).delete();

									}

									try {
										r.ping();
									} catch (Exception e) {
										// e.printStackTrace();
										new File(WorkbenchApplet.NEW_R_STUB_FILE).delete();
										throw new PingRServerFailedException();
									}

								}

								if (r.isBusy()) {

									int n = JOptionPane.showConfirmDialog(WorkbenchApplet.this, "R is busy, can't login, would you like to stop it?", "",
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

							}

							if (gui_url == null || gui_url.equals("")) {

								s = new Stack<GDDevice>();

								if (_rForConsole instanceof HttpMarker || !_rForConsole.hasRCollaborationListeners() || _selfish) {
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

								_graphicPanel = new JGDPanelPop(d, true, true, new AbstractAction[] { new SetCurrentDeviceAction(WorkbenchApplet.this, d),
										null, new FitDeviceAction(WorkbenchApplet.this, d), null, new SnapshotDeviceAction(WorkbenchApplet.this),
										new SnapshotDeviceSvgAction(WorkbenchApplet.this), new SnapshotDevicePdfAction(WorkbenchApplet.this), 
										
										null,
										new SaveDeviceAsJpgAction(WorkbenchApplet.this), new SaveDeviceAsPngAction(WorkbenchApplet.this),
										new SaveDeviceAsBmpAction(WorkbenchApplet.this), new SaveDeviceAsTiffAction(WorkbenchApplet.this),
										
										null,
										new SaveDeviceAsSvgAction(WorkbenchApplet.this), new SaveDeviceAsPdfAction(WorkbenchApplet.this),
										new SaveDeviceAsPsAction(WorkbenchApplet.this), new SaveDeviceAsXfigAction(WorkbenchApplet.this),
										new SaveDeviceAsPictexAction(WorkbenchApplet.this), new SaveDeviceAsPdfAppletAction(WorkbenchApplet.this),

										null,
										new SaveDeviceAsJavaJpgAction(WorkbenchApplet.this), new SaveDeviceAsJavaPngAction(WorkbenchApplet.this),
										new SaveDeviceAsJavaBmpAction(WorkbenchApplet.this), new SaveDeviceAsJavaGifAction(WorkbenchApplet.this),
										
										null, new SaveDeviceAsWmfAction(WorkbenchApplet.this), new SaveDeviceAsEmfAction(WorkbenchApplet.this),
										new SaveDeviceAsOdgAction(WorkbenchApplet.this), null, new CopyFromCurrentDeviceAction(WorkbenchApplet.this),
										new CopyToCurrentDeviceAction(WorkbenchApplet.this, d), null, new CoupleToCurrentDeviceAction(WorkbenchApplet.this)

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
											new SetCurrentDeviceAction(WorkbenchApplet.this, newDevice), null,
											new FitDeviceAction(WorkbenchApplet.this, newDevice), null, new SnapshotDeviceAction(WorkbenchApplet.this),
											new SnapshotDeviceSvgAction(WorkbenchApplet.this), new SnapshotDevicePdfAction(WorkbenchApplet.this), 
											null,
											new SaveDeviceAsJpgAction(WorkbenchApplet.this), new SaveDeviceAsPngAction(WorkbenchApplet.this),
											new SaveDeviceAsBmpAction(WorkbenchApplet.this), new SaveDeviceAsTiffAction(WorkbenchApplet.this),
											
											null,
											new SaveDeviceAsSvgAction(WorkbenchApplet.this), new SaveDeviceAsPdfAction(WorkbenchApplet.this),
											new SaveDeviceAsPsAction(WorkbenchApplet.this), new SaveDeviceAsXfigAction(WorkbenchApplet.this),
											new SaveDeviceAsPictexAction(WorkbenchApplet.this), new SaveDeviceAsPdfAppletAction(WorkbenchApplet.this),

											null,
											new SaveDeviceAsJavaJpgAction(WorkbenchApplet.this), new SaveDeviceAsJavaPngAction(WorkbenchApplet.this),
											new SaveDeviceAsJavaBmpAction(WorkbenchApplet.this), new SaveDeviceAsJavaGifAction(WorkbenchApplet.this),
											
											null, new SaveDeviceAsWmfAction(WorkbenchApplet.this), new SaveDeviceAsEmfAction(WorkbenchApplet.this),
											new SaveDeviceAsOdgAction(WorkbenchApplet.this), null,

											new CopyFromCurrentDeviceAction(WorkbenchApplet.this),
											new CopyToCurrentDeviceAction(WorkbenchApplet.this, newDevice), null,
											new CoupleToCurrentDeviceAction(WorkbenchApplet.this) }, getRLock(), getConsoleLogger());

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

							}

							_demos = _rForConsole.listDemos();

							_macrosEnabled = !_rForConsole.hasRCollaborationListeners();

							_collaborationListenerImpl = new RCollaborationListenerImpl();

							_rForConsole.addRCollaborationListener(_collaborationListenerImpl);

							_rConsoleActionListenerImpl = new RConsoleActionListenerImpl();

							_rForConsole.addRConsoleActionListener(_rConsoleActionListenerImpl);

							_rForConsole.registerUser(getUID(), getUserName());

							_availableExtensions = new HashSet<String>();
							String[] extensions = _rForConsole.listExtensions();
							System.out.println("extensions:" + Arrays.toString(extensions));
							for (int i = 0; i < extensions.length; ++i) {
								_availableExtensions.add(extensions[i]);
							}

							if (_macrosEnabled) {
								for (MacroInterface m : macrosVector)
									_rForConsole.addProbeOnVariables(m.getProbes());
							}

							new Thread(new Runnable() {
								public void run() {
									try {
										refreshMacros();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}).start();

							new Thread(new Runnable() {
								public void run() {
									try {
										GroovyInterpreterSingleton.getInstance().exec("import org.kchine.r.workbench.R;");
										GroovyInterpreterSingleton.getInstance().exec("R=org.kchine.r.workbench.R.getInstance();");
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

							}).start();

							if (_mode == HTTP_MODE) {

								connected();

								return "Logged on as " + _login + "\n";
							} else {

								connected();

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
						} catch (ServantCreationFailed scf) {
							return "R Server Creation Failed\n";
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
								}
							}
							return "Logged Off\n";
						} catch (NotLoggedInException nlie) {
							return "Not Logged In\n";
						} catch (TunnelingException te) {
							te.printStackTrace();
							return "Logoff Failed\n";
						} finally {
							noSession();
						}
					}

					Object result = null;

					if (getRLock().isLocked()) {
						Toolkit.getDefaultToolkit().beep();
						result = "R is busy, please retry\n";
					} else {

						try {
							getRLock().lock();

							if (true || _rForConsole instanceof HttpMarker) {
								getConsoleLogger().printAsInput(expression);
								System.out.println("before asynchronousConsoleSubmit");
								_rForConsole.asynchronousConsoleSubmit(expression);
								System.out.println("after asynchronousConsoleSubmit");
								/*
								 * while (_rForConsole.isBusy()) { try {
								 * Thread.sleep(20); } catch (Exception e) { } }
								 */
								result = null;

							} else {
								getConsoleLogger().printAsInput(expression);
								_rForConsole.consoleSubmit(expression);
								result = null;
							}

							// persistState();

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
			_consolePanel = new ConsolePanel(_submitInterface, "Evaluate", new Color(0x00, 0x80, 0x80), "R", true, new AbstractAction[] {
					_actions.get("logon"), _actions.get("logoff"), null, _actions.get("saveimage"), _actions.get("loadimage"), null, _actions.get("stopeval"),
					null, _actions.get("playdemo") });

			_consolePanel.getCommandInputField().addKeyListener(new KeyListener() {

				public void keyPressed(KeyEvent e) {
				}

				public void keyReleased(KeyEvent e) {
				}

				public void keyTyped(KeyEvent e) {
				}

			});

			gui_url = getParameter("gui_url");

			if (gui_url == null || gui_url.equals("")) {
				
				
				

				try {
					if (stateProperties.get("command.history") != null) {
						_consolePanel.setCommandHistory((Vector<String>) PoolUtils.hexToObject((String) stateProperties.get("command.history")));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

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
						// sessionMenu.add(_actions.get("interrupteval"));

						sessionMenu.addSeparator();
						sessionMenu.add(_actions.get("playdemo"));
						sessionMenu.addSeparator();
						sessionMenu.add(_actions.get("runhttpserver"));
						sessionMenu.add(_actions.get("stophttpserver"));
						sessionMenu.addSeparator();
						sessionMenu.add(_actions.get("runhttpserverlocalhost"));
						sessionMenu.add(_actions.get("stophttpserverlocalhost"));
						sessionMenu.addSeparator();
						sessionMenu.add(_actions.get("showsessioninfo"));
						sessionMenu.add(_actions.get("showworkbenchinfo"));
						sessionMenu.addSeparator();
						sessionMenu.add(_actions.get("quit"));
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

				final JMenu graphicsMenu = new JMenu("Graphics");
				graphicsMenu.addMenuListener(new MenuListener() {
					public void menuSelected(MenuEvent e) {

						graphicsMenu.removeAll();
						graphicsMenu.add(_actions.get("createdevice"));
						graphicsMenu.addSeparator();

						/*
						 * graphicsMenu.add(new
						 * SnapshotDeviceAction(GDApplet.this, _sessionId ==
						 * null ? null : getCurrentJGPanelPop()));
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
								return getR() != null;
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

				final JMenu spreadsheetMenu = new JMenu("Spreadsheet");
				spreadsheetMenu.addMenuListener(new MenuListener() {
					public void menuSelected(MenuEvent e) {
						spreadsheetMenu.removeAll();
						spreadsheetMenu.add(_actions.get("spreadsheet"));
						spreadsheetMenu.addSeparator();
						spreadsheetMenu.add(_actions.get("newserversidespreadsheet"));
						spreadsheetMenu.add(_actions.get("connecttoserversidespreadsheet"));
					}

					public void menuCanceled(MenuEvent e) {
					}

					public void menuDeselected(MenuEvent e) {
					}
				});
				menuBar.add(spreadsheetMenu);

				final JMenu toolsMenu = new JMenu("Tools");
				toolsMenu.addMenuListener(new MenuListener() {
					public void menuSelected(MenuEvent e) {
						toolsMenu.removeAll();
						toolsMenu.add(_actions.get("editor"));
						toolsMenu.addSeparator();
						toolsMenu.add(_actions.get("svgview"));
						toolsMenu.add(_actions.get("pdfview"));
						toolsMenu.add(_actions.get("slider"));
						toolsMenu.addSeparator();
						toolsMenu.add(_actions.get("pythonconsole"));
						toolsMenu.add(_actions.get("clientpythonconsole"));
						toolsMenu.addSeparator();
						toolsMenu.add(_actions.get("groovyconsole"));
						toolsMenu.add(_actions.get("clientgroovyconsole"));
						toolsMenu.add(_actions.get("unsafeevaluator"));
						toolsMenu.addSeparator();
						toolsMenu.add(_actions.get("scilabconsole"));
						toolsMenu.addSeparator();
						toolsMenu.add(_actions.get("sourcebioclite"));
						toolsMenu.add(_actions.get("installpackage"));
						toolsMenu.addSeparator();
						toolsMenu.add(_actions.get("supervisor"));
						toolsMenu.add(_actions.get("httpsupervisor"));

					}

					public void menuCanceled(MenuEvent e) {
					}

					public void menuDeselected(MenuEvent e) {
					}
				});
				menuBar.add(toolsMenu);

				final JMenu collaborationMenu = new JMenu("Collaboration");
				collaborationMenu.addMenuListener(new MenuListener() {
					public void menuSelected(MenuEvent e) {
						collaborationMenu.removeAll();
						collaborationMenu.add(_actions.get("createbroadcasteddevice"));
						collaborationMenu.add(_actions.get("chatconsoleview"));
						collaborationMenu.addSeparator();
						collaborationMenu.add(_actions.get("newcollaborativespreadsheet"));
						collaborationMenu.add(_actions.get("connecttocollaborativespreadsheet"));
						collaborationMenu.addSeparator();
						collaborationMenu.add(_actions.get("usersview"));
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

				final JMenu demoMenu = new JMenu("Demos");
				demoMenu.addMenuListener(new MenuListener() {
					public void menuSelected(MenuEvent e) {
						demoMenu.removeAll();
						if (_sessionId != null) {
							try {

								for (int i = 0; i < _demos.length; ++i) {
									final int index = i;
									demoMenu.add(new AbstractAction(PoolUtils.replaceAll(_demos[i], "_", " ")) {
										public void actionPerformed(ActionEvent e) {

											if (getRLock().isLocked()) {
												JOptionPane.showMessageDialog(null, "R is busy");
											} else {
												try {
													getRLock().lock();
													getConsoleLogger().print("sourcing demo " + PoolUtils.replaceAll(_demos[index], "_", " "), null);
													String log = _rForConsole.sourceFromBuffer(_rForConsole.getDemoSource(_demos[index]));

												} catch (Exception ex) {
													ex.printStackTrace();
												} finally {
													getRLock().unlock();
												}
											}

										}

										public boolean isEnabled() {
											return getR() != null;
										}
									});

								}

								demoMenu.addSeparator();

								for (int i = 0; i < _demos.length; ++i) {
									final int index = i;
									demoMenu.add(new AbstractAction("Copy to Clipboard - " + PoolUtils.replaceAll(_demos[i], "_", " ")) {
										public void actionPerformed(ActionEvent e) {
											try {

												StringSelection stringSelection = new StringSelection(_rForConsole.getDemoSource(_demos[index]).toString());
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
											return getR() != null;
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

				final JMenu macrosMenu = new JMenu("Macros");
				macrosMenu.addMenuListener(new MenuListener() {
					public void menuSelected(MenuEvent e) {
						macrosMenu.removeAll();
						int count = 0;
						for (MacroInterface m : macrosVector) {
							if (m.isShow()) {
								++count;
								final MacroInterface finalMacro = m;
								macrosMenu.add(new AbstractAction(m.getLabel()) {
									public void actionPerformed(ActionEvent e) {
										finalMacro.sourceAll(WorkbenchApplet.this, null);
									}
								});
							}
						}
						macrosMenu.addSeparator();

						JMenu macrosCopyMenu = new JMenu("Copy Example to Clipboard");
						macrosCopyMenu.add(new AbstractAction("Hello World Action Macro") {
							public void actionPerformed(ActionEvent e) {
								StringSelection stringSelection = new StringSelection(Macro.getHelloWorldAction());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(stringSelection, new ClipboardOwner() {
									public void lostOwnership(Clipboard clipboard, Transferable contents) {
									}
								});
							}
						});

						macrosCopyMenu.add(new AbstractAction("Hello World Macro With Variables Listeners") {
							public void actionPerformed(ActionEvent e) {
								StringSelection stringSelection = new StringSelection(Macro.getHelloWorldVars());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(stringSelection, new ClipboardOwner() {
									public void lostOwnership(Clipboard clipboard, Transferable contents) {
									}
								});
							}
						});

						macrosCopyMenu.add(new AbstractAction("Hello World Macro With Cells Listeners") {
							public void actionPerformed(ActionEvent e) {
								StringSelection stringSelection = new StringSelection(Macro.getHelloWorldCells());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(stringSelection, new ClipboardOwner() {
									public void lostOwnership(Clipboard clipboard, Transferable contents) {
									}
								});
							}
						});

						macrosCopyMenu.add(new AbstractAction("Hello World Data Link") {
							public void actionPerformed(ActionEvent e) {
								StringSelection stringSelection = new StringSelection(Macro.getHelloWorldDataLink());
								Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
								clipboard.setContents(stringSelection, new ClipboardOwner() {
									public void lostOwnership(Clipboard clipboard, Transferable contents) {
									}
								});
							}
						});
						macrosMenu.add(macrosCopyMenu);
						macrosMenu.addSeparator();
						macrosMenu.add(_actions.get("macroseditor"));
						macrosMenu.addSeparator();
						macrosMenu.add(new AbstractAction("Refresh") {
							public void actionPerformed(ActionEvent e) {
								new Thread(new Runnable() {
									public void run() {
										try {
											refreshMacros();
											for (CollaborativeSpreadsheetView v : getCollaborativeSpreadsheetViews()) {
												v.repaint();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}).start();
							}
						});
					}

					public void menuCanceled(MenuEvent e) {
					}

					public void menuDeselected(MenuEvent e) {
					}
				});
				menuBar.add(macrosMenu);

				final JMenu pluginsMenu = new JMenu("Plugins");
				pluginsMenu.addMenuListener(new MenuListener() {
					public void menuSelected(MenuEvent e) {
						pluginsMenu.removeAll();
						if (pluginViewsHash.keySet().size() > 0) {
							for (String p : pluginViewsHash.keySet()) {

								JMenu viewsMenu = new JMenu(p);
								for (PluginViewDescriptor pvd : pluginViewsHash.get(p)) {

									final PluginViewDescriptor pvdFinal = pvd;
									viewsMenu.add(new AbstractAction(pvd.getName()) {
										public void actionPerformed(ActionEvent e) {
											System.setSecurityManager(new YesSecurityManager());
											try {
												Class<?> c_ = pvdFinal.getPluginClassLoader().loadClass(pvdFinal.getClassName());
												Object o_ = c_.getConstructor(RGui.class).newInstance(WorkbenchApplet.this);
												if (JPanel.class.isAssignableFrom(c_)) {
													View v = createView((JPanel) o_, pvdFinal.getName());
												}
											} catch (Exception ex) {
												ex.printStackTrace();
											}
										}
									});
								}
								pluginsMenu.add(viewsMenu);
							}
							pluginsMenu.addSeparator();
						}

						pluginsMenu.add(_actions.get("installpluginjarfile"));
						pluginsMenu.add(_actions.get("installpluginjarurl"));
						pluginsMenu.add(_actions.get("installpluginzipfile"));
						pluginsMenu.add(_actions.get("installpluginzipurl"));

						pluginsMenu.addSeparator();
						pluginsMenu.add(_actions.get("openpluginviewjarfile"));
						pluginsMenu.add(_actions.get("openpluginviewjarurl"));
						pluginsMenu.add(_actions.get("openpluginviewclasses"));
						pluginsMenu.addSeparator();
						pluginsMenu.add(new AbstractAction("Refresh") {
							public void actionPerformed(ActionEvent e) {
								new Thread(new Runnable() {
									public void run() {
										try {
											refreshPluginViewsHash();
										} catch (Exception ex) {
											ex.printStackTrace();
										}
									}

								}).start();

							}
						});
						pluginsMenu.addSeparator();
						pluginsMenu.add(_actions.get("browsepluginsrepository"));
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
						rootWindow.setWindow(new SplitWindow(true, 0.4f, views[0], new SplitWindow(false, 0.7f,
								new TabWindow(new DockingWindow[] { views[1] }), views[2])));
					}
				});

				new Thread(new Runnable() {
					public void run() {
						try {
							refreshPluginViewsHash();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

				new Thread(new Runnable() {
					public void run() {
						try {
							refreshMacros();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
				
				new Thread(new Runnable() {
					public void run() {
						while (true) {

							updateConsoleIcon(null);
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
						while (true) {

							if (_tasks.size() > 0) {
								Vector<Runnable> tasks = popAllTasks(-1);
								if (getR() != null) {
									if (tasks != null) {
										for (Runnable t : tasks) {
											try {
												t.run();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								}
							}

							try {
								Thread.sleep(200);
							} catch (Exception e) {
							}

						}
					}
				}).start();

			} else {
				final JPanel mainPanel = new JPanel();
				mainPanel.setLayout(new BorderLayout());
				mainPanel.add(new JLabel("Connecting..."), BorderLayout.CENTER);
				((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.gray, 6));
				getContentPane().add(mainPanel, BorderLayout.CENTER);

				String fileName = PoolUtils.cacheJar(new URL(getParameter("gui_url")), ServerManager.DOWNLOAD_DIR, PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT, false);
				String pname = new File(fileName).getName();
				pname = pname.substring(0, pname.lastIndexOf('.'));
				final File pfile = new File(ServerManager.DOWNLOAD_DIR + "/" + pname);
				if (!pfile.exists() || pfile.lastModified() < new File(fileName).lastModified()) {
					if (pfile.exists()) {
						PoolUtils.deleteDirectory(pfile);
					}
					InputStream is = new FileInputStream(fileName);
					unzip(is, ServerManager.DOWNLOAD_DIR, null, PoolUtils.BUFFER_SIZE, true, "Unzipping Plugin..", 10000);
				}

				addRConnectionListener(new RConnectionListener() {
					
					Vector<PluginViewDescriptor> views;
					Vector<JPanel> perspectives=new Vector<JPanel>();
					Vector<JToggleButton> buttons=new Vector<JToggleButton>();
					
					public void showPerspective(int i) {
						mainPanel.removeAll();

						if (getParameter("gui_selector")!=null && getParameter("gui_selector").equalsIgnoreCase("true")) {
							JPanel p=new JPanel(new GridLayout(1,buttons.size()));
							for (int k=0;k<buttons.size();++k) p.add(buttons.elementAt(k));
							mainPanel.add(p, BorderLayout.NORTH);
						}
						
						mainPanel.add(perspectives.elementAt(i), BorderLayout.CENTER);
						
						mainPanel.updateUI();
						mainPanel.repaint();
						
					}
					
					public void connected() {
						try {

							views = OpenPluginViewDialog.getPluginViews(pfile.getAbsolutePath() + "/");

							System.out.println("####  " + views);
							PluginViewDescriptor pvd = null;

							int index=-1;
							
							if (getParameter("gui_name") == null || getParameter("gui_name").equals("")) {
								index=0;
							} else {
								for (int i = 0; i < views.size(); ++i) {
									if (views.elementAt(i).getName().equals(getParameter("gui_name"))) {
										index=i;
										break;
									}
								}
							}
							
							System.setSecurityManager(new YesSecurityManager());
							ButtonGroup group=new ButtonGroup();
							for (int i=0; i<views.size();++i) {
								try {
									Class<?> c_ = views.elementAt(i).getPluginClassLoader().loadClass(views.elementAt(i).getClassName());
									Object o_ = c_.getConstructor(RGui.class).newInstance(WorkbenchApplet.this);
									if (JPanel.class.isAssignableFrom(c_)) {								
										perspectives.add((JPanel) o_);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
									perspectives.add(new JPanel());
								}
								JToggleButton tb=new JToggleButton(views.elementAt(i).getName());
								tb.addActionListener(new ActionListener(){
									public void actionPerformed(ActionEvent e) {
										showPerspective(buttons.indexOf(e.getSource()));										
									}
								});
								buttons.add(tb);
								group.add(tb);
							}
							
							
							buttons.elementAt(index).setSelected(true);
							showPerspective(index);
							

						} catch (Exception e) {
							// TODO: handle exception
						}

					}

					public void connecting() {}

					public void disconnected() {}

					public void disconnecting() {}

				});

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		/*
		 * new Thread(new Runnable() { public void run() { loadJEditClasses(); }
		 * }).start();
		 */

		/*
		 * new Thread(new Runnable() { public void run() { try {
		 * ServerManager.downloadBioceCore(0); } catch (Exception e) {
		 * e.printStackTrace(); } } }).start();
		 */

		_instance = this;

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

		System.out.println("INIT ends");

	}

	private boolean firstCall = true;

	private void updateConsoleIcon(final Icon icon) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (icon == null) {
					if (getR() == null) {
						views[0].getViewProperties().setIcon(_disconnectedIcon);
					} else {
						try {
							if (_rForFiles.isBusy()) {
								views[0].getViewProperties().setIcon(_busyIcon);
							} else {
								views[0].getViewProperties().setIcon(_connectedIcon);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					views[0].getViewProperties().setIcon(icon);
				}
			}
		});
	}

	private synchronized void loadJEditClasses() {
		/*
		 * try { UIManager.setLookAndFeel(getLookAndFeelClassName()); } catch
		 * (Exception e) { e.printStackTrace(); }
		 */
		if (firstCall) {
			firstCall = false;
			try {
				System.setSecurityManager(new YesSecurityManager());

				try {
					File jEditDir = new File(ServerManager.INSTALL_DIR + "/jEdit");
					if (!jEditDir.exists()) {
						new File(ServerManager.INSTALL_DIR + "/jEdit").mkdirs();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				System.setProperty("jedit.home", ServerManager.INSTALL_DIR + "/jEdit");

				jeditcl.loadClass("org.gjt.sp.jedit.jEdit").getMethod("main", new Class<?>[] { String[].class, RGui.class }).invoke(null,
						new Object[] { new String[] { "-noserver", "-noplugins", "-nogui", "-nosettings" }, WorkbenchApplet.this });
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private HelpBrowserPanel getOpenedBrowser() {
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

	private Vector<org.kchine.r.workbench.views.CollaborativeSpreadsheetView> getCollaborativeSpreadsheetViews() {
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

	private UsersView getOpenedUsersView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof UsersView) {
				return (UsersView) dv;
			}
		}
		return null;
	}

	private Vector<BroadcastedDeviceView> getOpenedBroadcastedDeviceViews() {
		Vector<BroadcastedDeviceView> result = new Vector<BroadcastedDeviceView>();
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof BroadcastedDeviceView) {
				result.add((BroadcastedDeviceView) dv);
			}
		}
		return result;
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

	private ServerGroovyConsoleView getOpenedServerGroovyConsoleView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof ServerGroovyConsoleView) {
				return (ServerGroovyConsoleView) dv;
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

	private UnsafeEvaluatorView getOpenedUnsafeEvaluatorView() {
		Iterator<DynamicView> iter = dynamicViews.values().iterator();
		while (iter.hasNext()) {
			DynamicView dv = iter.next();
			if (dv instanceof UnsafeEvaluatorView) {
				return (UnsafeEvaluatorView) dv;
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
		HelpBrowserPanel openedBrowser = getOpenedBrowser();
		if (openedBrowser == null) {

			HelpBrowserPanel _helpBrowser = new HelpBrowserPanel(this);
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
					BufferedReader br = new BufferedReader(new InputStreamReader(WorkbenchApplet.class
							.getResourceAsStream("/org/kchine/r/workbench/demos/demoscript.R")));
					String l;
					while ((l = br.readLine()) != null) {

						l = l.trim();
						if (l.equals(""))
							continue;

						if (l.equals("#SNAPSHOT")) {

							/*
							 * try { Thread.sleep(1400); } catch (Exception ex)
							 * { } _actions.get("clone").actionPerformed(new
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

	int failureCounter = 0;

	synchronized private void reload() {

		if (_rForFiles == null)
			return;

		FileDescription[] descriptions = null;
		try {
			descriptions = _rForFiles.getWorkingDirectoryFileDescriptions();
			failureCounter = 0;
		} catch (NotLoggedInException nle) {
			noSession();
		} catch (Exception e) {
			e.printStackTrace();

			++failureCounter;
			System.out.println("///// failure counter :" + failureCounter);
			/*
			 * if (failureCounter == 1) manageServerFailure();
			 */
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

	private Properties stateProperties = new Properties();

	private void restoreState() {

		File settings = new File(WorkbenchApplet.SETTINGS_FILE);
		if (settings.exists()) {

			System.out.println("--restoreState");

			try {

				stateProperties.loadFromXML(new FileInputStream(settings));

				if (getMode() == NEW_R_MODE && getR() != null && !_keepAlive) {
					if (stateProperties.get("working.dir.root") != null) {
						getR().consoleSubmit("setwd('" + stateProperties.get("working.dir.root") + "')");
					}
				}

				if (stateProperties.get("command.history") != null) {
					try {
						if (_consolePanel != null)
							_consolePanel.setCommandHistory((Vector<String>) PoolUtils.hexToObject((String) stateProperties.get("command.history")));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("mode") != null) {
					try {
						LoginDialog.mode_int = Integer.decode((String) stateProperties.get("mode"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("url") != null) {
					try {
						LoginDialog.url_str = (String) stateProperties.get("url");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("default.r.bin") != null) {
					try {
						LoginDialog.defaultRBin_str = (String) stateProperties.get("default.r.bin");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("default.r") != null) {
					try {
						LoginDialog.defaultR_bool = new Boolean((String) stateProperties.get("default.r"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("memorymin") != null) {
					try {
						LoginDialog.memoryMin_int = Integer.decode((String) stateProperties.get("memorymin"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("memorymax") != null) {
					try {
						LoginDialog.memoryMax_int = Integer.decode((String) stateProperties.get("memorymax"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("privatename") != null) {
					try {
						LoginDialog.privateName_str = (String) stateProperties.get("privatename");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("rmi.mode") != null) {
					try {
						LoginDialog.rmiMode_int = Integer.decode((String) stateProperties.get("rmi.mode"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("registry.host") != null) {
					try {
						LoginDialog.rmiregistryIp_str = (String) stateProperties.get("registry.host");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("registry.port") != null) {
					try {
						LoginDialog.rmiregistryPort_int = Integer.decode((String) stateProperties.get("registry.port"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("registry.servant.name") != null) {
					try {
						LoginDialog.servantName_str = (String) stateProperties.get("registry.servant.name");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("db.driver") != null) {
					try {
						LoginDialog.dbDriver_str = (String) stateProperties.get("db.driver");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("db.host") != null) {
					try {
						LoginDialog.dbHostIp_str = (String) stateProperties.get("db.host");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("db.port") != null) {
					try {
						LoginDialog.dbHostPort_int = Integer.decode((String) stateProperties.get("db.port"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("db.name") != null) {
					try {
						LoginDialog.dbName_str = (String) stateProperties.get("db.name");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("db.user") != null) {
					try {
						LoginDialog.dbUser_str = (String) stateProperties.get("db.user");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("db.servant.name") != null) {
					try {
						LoginDialog.dbServantName_str = (String) stateProperties.get("db.servant.name");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("stub") != null) {
					try {
						LoginDialog.stub_str = (String) stateProperties.get("stub");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("keepalive") != null) {
					try {
						LoginDialog.keepAlive_bool = new Boolean((String) stateProperties.get("keepalive"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("usessh") != null) {
					try {
						LoginDialog.useSsh_bool = new Boolean((String) stateProperties.get("usessh"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("ssh.host") != null) {
					try {
						LoginDialog.sshHost_str = (String) stateProperties.get("ssh.host");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("ssh.port") != null) {
					try {
						LoginDialog.sshPort_int = Integer.decode((String) stateProperties.get("ssh.port"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("ssh.login") != null) {
					try {
						LoginDialog.sshLogin_str = (String) stateProperties.get("ssh.login");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("usesshtunnel") != null) {
					try {
						LoginDialog.useSshTunnel_bool = new Boolean((String) stateProperties.get("usesshtunnel"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("ssh.tunnel.host") != null) {
					try {
						LoginDialog.sshTunnelHost_str = (String) stateProperties.get("ssh.tunnel.host");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("ssh.tunnel.port") != null) {
					try {
						LoginDialog.sshTunnelPort_int = Integer.decode((String) stateProperties.get("ssh.tunnel.port"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (stateProperties.get("ssh.tunnel.login") != null) {
					try {
						LoginDialog.sshTunnelLogin_str = (String) stateProperties.get("ssh.tunnel.login");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/*
		 * if (getMode()==NEW_R_MODE && getR() != null && !_keepAlive && _save)
		 * { try { _rForConsole.consoleSubmit("load('.RData')"); } catch
		 * (Exception e) { e.printStackTrace(); } }
		 */
	}

	synchronized private void persistState() {
		try {
			Vector<String> generatorParams = new Vector<String>();
			generatorParams.add(WorkbenchApplet.SETTINGS_FILE);
			if (getMode() == NEW_R_MODE && getR() != null && !_keepAlive) {
				generatorParams.add("working.dir.root=" + getR().getObjectConverted("getwd()"));
				System.out.println("--working.dir.root=" + getR().getObjectConverted("getwd()"));
			}

			generatorParams.add("command.history=" + PoolUtils.objectToHex(_consolePanel.getCommandHistory()));
			generatorParams.add("mode=" + LoginDialog.mode_int);
			generatorParams.add("url=" + LoginDialog.url_str);
			generatorParams.add("default.r=" + LoginDialog.defaultR_bool);
			generatorParams.add("default.r.bin=" + LoginDialog.defaultRBin_str);

			generatorParams.add("default.r.bin=" + LoginDialog.defaultRBin_str);
			generatorParams.add("default.r.bin=" + LoginDialog.defaultRBin_str);

			generatorParams.add("memorymin=" + LoginDialog.memoryMin_int);
			generatorParams.add("memorymax=" + LoginDialog.memoryMax_int);

			generatorParams.add("privatename=" + LoginDialog.privateName_str);
			generatorParams.add("rmi.mode=" + LoginDialog.rmiMode_int);
			generatorParams.add("registry.host=" + LoginDialog.rmiregistryIp_str);
			generatorParams.add("registry.port=" + LoginDialog.rmiregistryPort_int);
			generatorParams.add("registry.servant.name=" + LoginDialog.servantName_str);

			generatorParams.add("db.driver=" + LoginDialog.dbDriver_str);
			generatorParams.add("db.host=" + LoginDialog.dbHostIp_str);
			generatorParams.add("db.port=" + LoginDialog.dbHostPort_int);
			generatorParams.add("db.name=" + LoginDialog.dbName_str);
			generatorParams.add("db.user=" + LoginDialog.dbUser_str);
			generatorParams.add("db.servant.name=" + LoginDialog.dbServantName_str);

			generatorParams.add("stub=" + LoginDialog.stub_str);
			generatorParams.add("keepalive=" + LoginDialog.keepAlive_bool);
			generatorParams.add("usessh=" + LoginDialog.useSsh_bool);

			generatorParams.add("ssh.host=" + LoginDialog.sshHost_str);
			generatorParams.add("ssh.port=" + LoginDialog.sshPort_int);
			generatorParams.add("ssh.login=" + LoginDialog.sshLogin_str);

			generatorParams.add("usesshtunnel=" + LoginDialog.useSshTunnel_bool);
			generatorParams.add("ssh.tunnel.host=" + LoginDialog.sshTunnelHost_str);
			generatorParams.add("ssh.tunnel.port=" + LoginDialog.sshTunnelPort_int);
			generatorParams.add("ssh.tunnel.login=" + LoginDialog.sshTunnelLogin_str);

			PropertiesGenerator.main((String[]) generatorParams.toArray(new String[generatorParams.size()]));
		} catch (Exception e) {
			e.printStackTrace();
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
				}
			} catch (TunnelingException e) {
				// e.printStackTrace();
			}
			noSession();
		} else {

			persistState();

			/*
			 * if (getR() != null && !_keepAlive && _save) { try {
			 * _rForConsole.consoleSubmit("save.image('.RData')"); } catch
			 * (Exception e) { e.printStackTrace(); } }
			 */
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
						JOptionPane.showMessageDialog(WorkbenchApplet.this, "The Applet has no permissions to access your local disk.\n" + "please add : "
								+ instruction + " \n" + "to the java.policy file of your JRE \n", "Permissions Required", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				final String fileName = _workDirFiles.elementAt(_filesTable.getSelectedRows()[0]).getName();
				_chooser.setSelectedFile(new File(fileName));
				int returnVal = _chooser.showSaveDialog(WorkbenchApplet.this);

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
						JOptionPane.showMessageDialog(WorkbenchApplet.this, "The Applet has no permissions to access your local disk.\n" + "please add : "
								+ instruction + " \n" + "to the java.policy file of your JRE \n", "Permissions Required", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				try {
					_chooser.setSelectedFile(new File(""));
					int returnVal = _chooser.showOpenDialog(WorkbenchApplet.this);

					if (returnVal == JFileChooser.APPROVE_OPTION) {

						final PushAsDialog paDialog = new PushAsDialog(WorkbenchApplet.this, _chooser.getSelectedFile().getName());
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

							final SymbolPopDialog sdialog = new SymbolPopDialog(WorkbenchApplet.this, null, _rForConsole.listSymbols(), true);

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
				return getR() != null;
			}
		});

		_actions.put("push_symbol", new AbstractAction("Load R/Java Object From Local File") {
			public void actionPerformed(ActionEvent e) {

				new Thread(new Runnable() {
					public void run() {
						try {

							final SymbolPushDialog sdialog = new SymbolPushDialog(WorkbenchApplet.this.getContentPane(), null, null, true);
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
				return getR() != null;
			}
		});

		_actions.put("help", new AbstractAction("Help Contents") {
			public void actionPerformed(ActionEvent e) {
				setHelpBrowserURL(getHelpRootUrl() + "/doc/html/index.html");
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

		_actions.put("about", new AbstractAction("About Virtual R") {
			public void actionPerformed(ActionEvent e) {
				new SplashWindow(new JFrame(), Toolkit.getDefaultToolkit().createImage(SplashWindow.getSplashPng())).setVisible(true);
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
				return getR() != null;
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

			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						try {
							try {
								UIManager.setLookAndFeel(getLookAndFeelClassName());
							} catch (Exception e) {
								e.printStackTrace();
							}
							loadJEditClasses();
							jeditcl.loadClass("org.gjt.sp.jedit.jEdit").getMethod("newView", new Class<?>[0]).invoke((Object) null, (Object[]) null);
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

		_actions.put("macroseditor", new AbstractAction("Edit Macros") {

			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {
						try {
							try {
								UIManager.setLookAndFeel(getLookAndFeelClassName());
							} catch (Exception e) {
								e.printStackTrace();
							}
							loadJEditClasses();
							Object view = jeditcl.loadClass("org.gjt.sp.jedit.jEdit").getMethod("newView", new Class<?>[0]).invoke((Object) null,
									(Object[]) null);
							jeditcl.loadClass("org.gjt.sp.jedit.jEdit").getMethod("openFile",
									new Class<?>[] { jeditcl.loadClass("org.gjt.sp.jedit.View"), String.class }).invoke((Object) null,
									new Object[] { view, new File(getInstallDir() + "/macros.xml").getAbsolutePath() });
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

		// public static Buffer openFile(View view, String parent, String path,
		// boolean newFile, Hashtable props) {

		_actions.put("spreadsheet", new AbstractAction("New Local Spreadsheet") {
			public void actionPerformed(final ActionEvent ae) {
				final DimensionsDialog ddialog = new DimensionsDialog(WorkbenchApplet.this);
				ddialog.setVisible(true);
				if (ddialog.getSpreadsheetDimension() != null) {
					createView(new SpreadsheetPanel(new SpreadsheetDefaultTableModel((int) ddialog.getSpreadsheetDimension().getHeight(), (int) ddialog
							.getSpreadsheetDimension().getWidth()), WorkbenchApplet.this), "Local Spreadsheet  (" + (++LOCAL_SPREADSHEET_COUNTER) + ")");
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

						final DimensionsDialog ddialog = new DimensionsDialog(WorkbenchApplet.this);
						ddialog.setVisible(true);
						if (ddialog.getSpreadsheetDimension() != null) {
							int id = getDynamicViewId();
							final CollaborativeSpreadsheetView lv = new CollaborativeSpreadsheetView(id, (int) ddialog.getSpreadsheetDimension().getHeight(),
									(int) ddialog.getSpreadsheetDimension().getWidth(), WorkbenchApplet.this);
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
							final SelectIdDialog ddialog = new SelectIdDialog(WorkbenchApplet.this, "Connect to Collaborative Spreadsheet", "Spreadsheet Id",
									getR().listSpreadsheetTableModelRemoteId());
							ddialog.setVisible(true);
							if (ddialog.getId() != null) {

								int id = getDynamicViewId();
								final CollaborativeSpreadsheetView lv = new CollaborativeSpreadsheetView(id, ddialog.getId(), WorkbenchApplet.this);
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

		_actions.put("newserversidespreadsheet", new AbstractAction("New Server-side Spreadsheet") {
			public void actionPerformed(final ActionEvent ae) {
				new Thread(new Runnable() {
					public void run() {

						final DimensionsDialog ddialog = new DimensionsDialog(WorkbenchApplet.this);
						ddialog.setVisible(true);
						if (ddialog.getSpreadsheetDimension() != null) {
							int id = getDynamicViewId();
							final CollaborativeSpreadsheetView lv = new CollaborativeSpreadsheetView(id, (int) ddialog.getSpreadsheetDimension().getHeight(),
									(int) ddialog.getSpreadsheetDimension().getWidth(), WorkbenchApplet.this);
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

		_actions.put("connecttoserversidespreadsheet", new AbstractAction("Connect to Server-side Spreadsheet") {
			public void actionPerformed(final ActionEvent ae) {
				new Thread(new Runnable() {
					public void run() {

						try {
							final SelectIdDialog ddialog = new SelectIdDialog(WorkbenchApplet.this, "Connect to Server-side Spreadsheet", "Spreadsheet Id",
									getR().listSpreadsheetTableModelRemoteId());
							ddialog.setVisible(true);
							if (ddialog.getId() != null) {

								int id = getDynamicViewId();
								final CollaborativeSpreadsheetView lv = new CollaborativeSpreadsheetView(id, ddialog.getId(), WorkbenchApplet.this);
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

					graphicPanel = new JGDPanelPop(newDevice, true, true, new AbstractAction[] { new SetCurrentDeviceAction(WorkbenchApplet.this, newDevice),
							null, new FitDeviceAction(WorkbenchApplet.this, newDevice), null, new SnapshotDeviceAction(WorkbenchApplet.this),
							new SnapshotDeviceSvgAction(WorkbenchApplet.this), new SnapshotDevicePdfAction(WorkbenchApplet.this), 
							null,
							new SaveDeviceAsJpgAction(WorkbenchApplet.this), new SaveDeviceAsPngAction(WorkbenchApplet.this),
							new SaveDeviceAsBmpAction(WorkbenchApplet.this), new SaveDeviceAsTiffAction(WorkbenchApplet.this),
							null,
							new SaveDeviceAsSvgAction(WorkbenchApplet.this), new SaveDeviceAsPdfAction(WorkbenchApplet.this),
							new SaveDeviceAsPsAction(WorkbenchApplet.this), new SaveDeviceAsXfigAction(WorkbenchApplet.this),
							new SaveDeviceAsPictexAction(WorkbenchApplet.this), new SaveDeviceAsPdfAppletAction(WorkbenchApplet.this),
							
							null,
							new SaveDeviceAsJavaJpgAction(WorkbenchApplet.this), new SaveDeviceAsJavaPngAction(WorkbenchApplet.this),
							new SaveDeviceAsJavaBmpAction(WorkbenchApplet.this), new SaveDeviceAsJavaGifAction(WorkbenchApplet.this),
							
							null, 
							new SaveDeviceAsWmfAction(WorkbenchApplet.this), new SaveDeviceAsEmfAction(WorkbenchApplet.this),
							new SaveDeviceAsOdgAction(WorkbenchApplet.this), null, new CopyFromCurrentDeviceAction(WorkbenchApplet.this),
							new CopyToCurrentDeviceAction(WorkbenchApplet.this, newDevice), null, new CoupleToCurrentDeviceAction(WorkbenchApplet.this) },
							getRLock(), getConsoleLogger());

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
				DeviceView deviceView = new BroadcastedDeviceView("Broadcasted Graphic Device", null, rootGraphicPanel, id);

				((TabWindow) views[2].getWindowParent()).addTab(deviceView);

				try {

					GDDevice newDevice = null;

					_protectR.lock();

					try {

						newDevice = _rForConsole.newBroadcastedDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());

					} finally {
						_protectR.unlock();
					}

					graphicPanel = new JGDPanelPop(newDevice, true, true, new AbstractAction[] { new SetCurrentDeviceAction(WorkbenchApplet.this, newDevice),
							null, new FitDeviceAction(WorkbenchApplet.this, newDevice), null, new SnapshotDeviceAction(WorkbenchApplet.this),
							new SnapshotDeviceSvgAction(WorkbenchApplet.this), new SnapshotDevicePdfAction(WorkbenchApplet.this), null,
							
							
							new SaveDeviceAsJpgAction(WorkbenchApplet.this), new SaveDeviceAsPngAction(WorkbenchApplet.this),
							new SaveDeviceAsBmpAction(WorkbenchApplet.this), new SaveDeviceAsTiffAction(WorkbenchApplet.this),
							
							null,							
							new SaveDeviceAsSvgAction(WorkbenchApplet.this), new SaveDeviceAsPdfAction(WorkbenchApplet.this),
							new SaveDeviceAsPsAction(WorkbenchApplet.this), new SaveDeviceAsXfigAction(WorkbenchApplet.this),
							new SaveDeviceAsPictexAction(WorkbenchApplet.this), new SaveDeviceAsPdfAppletAction(WorkbenchApplet.this),

							null,
							new SaveDeviceAsJavaJpgAction(WorkbenchApplet.this), new SaveDeviceAsJavaPngAction(WorkbenchApplet.this),
							new SaveDeviceAsJavaBmpAction(WorkbenchApplet.this), new SaveDeviceAsJavaGifAction(WorkbenchApplet.this),
							
							null, 
							new SaveDeviceAsWmfAction(WorkbenchApplet.this), new SaveDeviceAsEmfAction(WorkbenchApplet.this),
							new SaveDeviceAsOdgAction(WorkbenchApplet.this), null, new CopyFromCurrentDeviceAction(WorkbenchApplet.this),
							new CopyToCurrentDeviceAction(WorkbenchApplet.this, newDevice), null, new CoupleToCurrentDeviceAction(WorkbenchApplet.this) },
							getRLock(), getConsoleLogger());

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

					final ChatConsoleView lv = new ChatConsoleView("Chat Console", null, id, WorkbenchApplet.this);
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

		_actions.put("usersview", new AbstractAction("Connected Users") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedUsersView() == null) {
					int id = getDynamicViewId();

					final UsersView lv = new UsersView("Connected Users", null, id);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					new Thread(new Runnable() {
						public void run() {
							updateUsers();
						}
					}).start();
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

				final SvgView lv = new SvgView("SVG Viewer", null, id, WorkbenchApplet.this);
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
				final PdfView lv = new PdfView("PDF Viewer", null, id, WorkbenchApplet.this);
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

					final ServerPythonConsoleView lv = new ServerPythonConsoleView("Python Console", null, id, WorkbenchApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							lv.getConsolePanel().stopLogThread();
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

					final ClientPythonConsoleView lv = new ClientPythonConsoleView("Local Python Console", null, id, WorkbenchApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							lv.getConsolePanel().stopLogThread();
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
				if (getOpenedServerGroovyConsoleView() == null) {
					int id = getDynamicViewId();

					final ServerGroovyConsoleView lv = new ServerGroovyConsoleView("Groovy Console", null, id, WorkbenchApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							lv.getConsolePanel().stopLogThread();
						}
					});

				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("scilabconsole", new AbstractAction("Scilab Console") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerGroovyConsoleView() == null) {
					int id = getDynamicViewId();

					final ScilabConsoleView lv = new ScilabConsoleView("Scilab Console", null, id, WorkbenchApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);

				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});
		
		_actions.put("slider", new AbstractAction("Variable Slider") {
			public void actionPerformed(final ActionEvent e) {
				int id = getDynamicViewId();
				final SliderView lv = new SliderView("Slider", null, id, WorkbenchApplet.this, 0, 100, 10);
				((TabWindow) views[2].getWindowParent()).addTab(lv);

				lv.addListener(new AbstractDockingWindowListener() {
					@Override
					public void windowClosed(DockingWindow arg0) {
						try {
							lv.destroy();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});

			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("clientgroovyconsole", new AbstractAction("Local Groovy Console") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedClientGroovyConsoleView() == null) {
					int id = getDynamicViewId();

					final ClientGroovyConsoleView lv = new ClientGroovyConsoleView("Local Groovy Console", null, id, WorkbenchApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							lv.getConsolePanel().stopLogThread();
						}
					});

				}
			}

			public boolean isEnabled() {
				return getR() != null && GroovyInterpreterSingleton.getInstance() != null;
			}
		});

		_actions.put("unsafeevaluator", new AbstractAction("Unsafe Evaluator") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedUnsafeEvaluatorView() == null) {
					int id = getDynamicViewId();

					final UnsafeEvaluatorView lv = new UnsafeEvaluatorView("Unsafe Evaluator", null, id, WorkbenchApplet.this);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new AbstractDockingWindowListener() {
						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							lv.getConsolePanel().stopLogThread();
						}
					});

				}
			}

			public boolean isEnabled() {
				return getR() != null;
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
										GetExprDialog dialog = new GetExprDialog(WorkbenchApplet.this, "  R Expression", _expressionSave);
										dialog.setVisible(true);
										if (dialog.getExpr() != null) {
											RObject robj = null;
											try {
												((JGDPanelPop) _graphicPanel).setAutoModes(true, false);
												robj = _rForConsole.getObject(dialog.getExpr());
											} catch (NoMappingAvailable re) {
												JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), re.getMessage(), "R Error",
														JOptionPane.ERROR_MESSAGE);
												return;
											} catch (Exception e) {
											} finally {
												((JGDPanelPop) _graphicPanel).setAutoModes(true, true);
											}

											if (_rForConsole.getStatus().toUpperCase().contains("ERROR")) {
												JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), _rForConsole.getStatus(), "R Error",
														JOptionPane.ERROR_MESSAGE);
												return;
											}

											ClassLoader cl = WorkbenchApplet.class.getClassLoader();
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

		_actions.put("quit", new AbstractAction("Quit") {
			public void actionPerformed(final ActionEvent e) {

				if (isDesktopApplication()) {
					System.exit(0);
				} else {
					_consolePanel.play("logoff", false);
				}

			}

			@Override
			public boolean isEnabled() {
				return true;
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

							GetExprDialog dialog = new GetExprDialog(WorkbenchApplet.this, "  R package", _packageNameSave);
							dialog.setVisible(true);
							if (dialog.getExpr() != null) {
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

		_actions.put("runhttpserverlocalhost", new AbstractAction("Start HTTP Relay On Local Host") {
			public void actionPerformed(final ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {

						try {

							while (true) {
								GetExprDialog dialog = new GetExprDialog(WorkbenchApplet.this, " Run HTTP Relay On Port : ", _httpPortSave);
								dialog.setVisible(true);
								if (dialog.getExpr() != null) {
									try {
										final int port = Integer.decode(dialog.getExpr());
										if (ServerManager.isPortInUse("127.0.0.1", port)) {
											JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), "Port already in use", "",
													JOptionPane.ERROR_MESSAGE);
										} else {

											_virtualizationServer = new Server(port);
											Context root = new Context(_virtualizationServer, "/rvirtual", Context.SESSIONS);
											root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.GraphicsServlet(WorkbenchApplet.this)),
													"/graphics/*");
											root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.RESTServlet(WorkbenchApplet.this)),
													"/rest/*");
											root.addServlet(
													new ServletHolder(new org.kchine.r.server.http.frontend.CommandServlet(WorkbenchApplet.this, true)),
													"/cmd/*");
											root.addServlet(new ServletHolder(new org.kchine.r.server.http.local.LocalHelpServlet(WorkbenchApplet.this)),													
													"/helpme/*");											
											root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.WWWDirectoryServlet(ServerManager.WWW_DIR,"/www")), "/www/*");											
											root.addServlet(new ServletHolder(new org.kchine.r.server.http.frontend.WWWDirectoryServlet(ServerManager.WWW_DIR,"/appletlibs")), "/appletlibs/*");		

											

											System.out.println("+++++++++++++++++++ going to start virtualization http server port : " + port);
											_virtualizationServer.start();

											JTextArea a = new JTextArea();
											a
													.setText(" An HTTP Relay has been created on port "
															+ port
															+ "\n You can control the current R session from anywhere via the Workench\n log on in HTTP mode to the following URL : http://"
															+ _clientIP + ":" + port + "/rvirtual/cmd");
											a.setEditable(false);
											a.setBackground(new JLabel().getBackground());

											JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), a, "", JOptionPane.INFORMATION_MESSAGE);

											break;

										}
									} catch (Exception e) {
										JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), "Bad Port", "", JOptionPane.ERROR_MESSAGE);
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

		_actions.put("stophttpserverlocalhost", new AbstractAction("Stop HTTP Relay") {
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

		_actions.put("runhttpserver", new AbstractAction("Create HTTP Listener on Server") {
			public void actionPerformed(final ActionEvent ae) {
				new Thread(new Runnable() {
					public void run() {

						while (true) {
							GetExprDialog dialog = new GetExprDialog(WorkbenchApplet.this, " Create HTTP Listener on Port : ", _httpPortSave);
							dialog.setVisible(true);
							if (dialog.getExpr() != null) {
								try {
									final int port = Integer.decode(dialog.getExpr());
									if (_rForConsole.isPortInUse(port)) {
										JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), "Port already in use", "",
												JOptionPane.ERROR_MESSAGE);
									} else {

										_rForConsole.startHttpServer(port);

										JTextArea a = new JTextArea();
										a
												.setText(" An HTTP Listener has been created on port "
														+ port
														+ "\n You can control the current R session via the Workench\n log on in HTTP mode to the following URL : http://"
														+ _rForConsole.getHostIp() + ":" + port + "/rvirtual/cmd");
										a.setEditable(false);
										a.setBackground(new JLabel().getBackground());

										JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), a, "", JOptionPane.INFORMATION_MESSAGE);

										break;
									}
								} catch (Exception e) {
									e.printStackTrace();
									JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), "Bad Port", "", JOptionPane.ERROR_MESSAGE);
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

		_actions.put("stophttpserver", new AbstractAction("Remove HTTP Listener") {
			public void actionPerformed(final ActionEvent e) {
				try {
					_rForConsole.stopHttpServer();
					JOptionPane.showMessageDialog(WorkbenchApplet.this.getContentPane(), "HTTP Listener Removed Successfully", "",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});


		_actions.put("installpluginjarfile", new AbstractAction("Install Plugin From Jar File") {
			public void actionPerformed(final ActionEvent e) {

				final JFileChooser chooser = new JFileChooser("Install Plugin From Jar File");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(WorkbenchApplet.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					final File[] files = chooser.getSelectedFiles();

					new Thread(new Runnable() {
						public void run() {
							for (int i = 0; i < files.length; ++i) {

								try {

									PoolUtils.cacheJar(files[i].toURI().toURL(), ServerManager.PLUGINS_DIR, PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT, true);
									JOptionPane.showMessageDialog(WorkbenchApplet.this, "Plugin "
											+ files[i].getName().substring(0, files[i].getName().lastIndexOf('.')) + " Installed Successfully");
								} catch (Exception ex) {
									ex.printStackTrace();
								}

								try {
									refreshPluginViewsHash();
								} catch (Exception e) {
									e.printStackTrace();
								}

							}

						}
					}).start();

				}
			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("installpluginjarurl", new AbstractAction("Install Plugin From Jar URL") {
			public void actionPerformed(final ActionEvent e) {

				final String jarUrl = JOptionPane.showInputDialog("Please enter Jar URL");
				if (jarUrl != null) {
					new Thread(new Runnable() {
						public void run() {
							try {
								PoolUtils.cacheJar(new URL(jarUrl), ServerManager.PLUGINS_DIR, PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT, true);
								JOptionPane.showMessageDialog(WorkbenchApplet.this, "Plugin Installed Successfully");
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							try {
								refreshPluginViewsHash();
							} catch (Exception ex) {
								ex.printStackTrace();
							}

						}
					}).start();

				}

			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("installpluginzipfile", new AbstractAction("Install Plugin From Zip File") {
			public void actionPerformed(final ActionEvent e) {

				final JFileChooser chooser = new JFileChooser("Install Plugin From Zip File");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(true);
				int returnVal = chooser.showOpenDialog(WorkbenchApplet.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					final File[] files = chooser.getSelectedFiles();
					new Thread(new Runnable() {
						public void run() {

							for (int i = 0; i < files.length; ++i) {

								try {
									File pf = new File(ServerManager.PLUGINS_DIR + "/"
											+ files[i].getName().substring(0, files[i].getName().lastIndexOf('.')));
									if (pf.exists()) {
										PoolUtils.deleteDirectory(pf);
									}
									// pf.mkdirs();

									URL rUrl = files[i].toURI().toURL();
									InputStream is = rUrl.openConnection().getInputStream();
									unzip(is, ServerManager.PLUGINS_DIR, null, PoolUtils.BUFFER_SIZE, true, "Unzipping Plugin..", 10000);

									JOptionPane.showMessageDialog(null, "Plugin " + files[i].getName().substring(0, files[i].getName().lastIndexOf('.'))
											+ " Installed Successfully");
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}

							try {
								refreshPluginViewsHash();
							} catch (Exception ex) {
								ex.printStackTrace();
							}

						}
					}).start();

				}

			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("installpluginzipurl", new AbstractAction("Install Plugin From Zip URL") {
			public void actionPerformed(final ActionEvent e) {

				final String jarUrl = JOptionPane.showInputDialog("Please enter Zip URL");
				if (jarUrl != null) {

					new Thread(new Runnable() {
						public void run() {

							try {

								String pluginname = jarUrl.substring(jarUrl.lastIndexOf("/") + 1, jarUrl.lastIndexOf("."));

								File pf = new File(ServerManager.PLUGINS_DIR + "/" + pluginname);
								if (pf.exists()) {
									PoolUtils.deleteDirectory(pf);
								}
								// pf.mkdirs();

								URL rUrl = new URL(jarUrl);
								InputStream is = rUrl.openConnection().getInputStream();
								unzip(is, ServerManager.PLUGINS_DIR, null, PoolUtils.BUFFER_SIZE, true, "Unzipping Plugin..", 10000);

								JOptionPane.showMessageDialog(null, "Plugin " + pluginname + " Installed Successfully");
							} catch (Exception ex) {
								ex.printStackTrace();
							}

							try {
								refreshPluginViewsHash();
							} catch (Exception ex) {
								ex.printStackTrace();
							}

						}
					}).start();

				}

			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("openpluginviewjarfile", new AbstractAction("Open Plugin View From Jar File") {
			public void actionPerformed(final ActionEvent e) {

				OpenPluginViewDialog pdialog = new OpenPluginViewDialog(WorkbenchApplet.this, OpenPluginViewDialog.JAR_MODE, false);
				pdialog.setVisible(true);
				PluginViewDescriptor pvd = pdialog.getPluginViewDetail();
				if (pvd != null) {

					System.setSecurityManager(new YesSecurityManager());
					try {
						Class<?> c_ = pvd.getPluginClassLoader().loadClass(pvd.getClassName());
						Object o_ = c_.getConstructor(RGui.class).newInstance(WorkbenchApplet.this);
						if (JPanel.class.isAssignableFrom(c_)) {
							View v = createView((JPanel) o_, pvd.getName());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("openpluginviewjarurl", new AbstractAction("Open Plugin View From Jar URL") {
			public void actionPerformed(final ActionEvent e) {

				OpenPluginViewDialog pdialog = new OpenPluginViewDialog(WorkbenchApplet.this, OpenPluginViewDialog.URL_MODE, false);
				pdialog.setVisible(true);
				PluginViewDescriptor pvd = pdialog.getPluginViewDetail();
				if (pvd != null) {

					System.setSecurityManager(new YesSecurityManager());
					try {
						Class<?> c_ = pvd.getPluginClassLoader().loadClass(pvd.getClassName());
						Object o_ = c_.getConstructor(RGui.class).newInstance(WorkbenchApplet.this);
						if (JPanel.class.isAssignableFrom(c_)) {
							View v = createView((JPanel) o_, pvd.getName());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("openpluginviewclasses", new AbstractAction("Open Plugin View From Classes Directory") {
			public void actionPerformed(final ActionEvent e) {

				OpenPluginViewDialog pdialog = new OpenPluginViewDialog(WorkbenchApplet.this, OpenPluginViewDialog.CLASSES_MODE, false);
				pdialog.setVisible(true);
				PluginViewDescriptor pvd = pdialog.getPluginViewDetail();
				if (pvd != null) {

					System.setSecurityManager(new YesSecurityManager());
					try {
						Class<?> c_ = pvd.getPluginClassLoader().loadClass(pvd.getClassName());
						Object o_ = c_.getConstructor(RGui.class).newInstance(WorkbenchApplet.this);
						if (JPanel.class.isAssignableFrom(c_)) {
							View v = createView((JPanel) o_, pvd.getName());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("browsepluginsrepository", new AbstractAction("Browse Plugins Repository") {
			public void actionPerformed(final ActionEvent e) {
				JOptionPane.showMessageDialog(WorkbenchApplet.this, "Not Yet Implemented");
			}

			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("showpluginview", new AbstractAction("Open Plugin View") {
			public void actionPerformed(final ActionEvent e) {
				OpenPluginViewDialog pdialog = new OpenPluginViewDialog(WorkbenchApplet.this, OpenPluginViewDialog.JAR_MODE, false);
				pdialog.setVisible(true);
				PluginViewDescriptor pvd = pdialog.getPluginViewDetail();
				if (pvd != null) {

					System.setSecurityManager(new YesSecurityManager());
					try {
						Class<?> c_ = pvd.getPluginClassLoader().loadClass(pvd.getClassName());
						Object o_ = c_.getConstructor(RGui.class).newInstance(WorkbenchApplet.this);
						if (JPanel.class.isAssignableFrom(c_)) {
							View v = createView((JPanel) o_, pvd.getName());
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}

			public boolean isEnabled() {
				return getR() != null;
			}
		});

		_actions.put("showsessioninfo", new AbstractAction("Show R Server Info") {
			public void actionPerformed(ActionEvent e) {
				try {
					String sessionMode = null;
					if (getMode() == HTTP_MODE)
						sessionMode = "CONNECT TO HTTP";
					else if (getMode() == RMI_MODE)
						sessionMode = "CONNECT TO RMI";
					else if (getMode() == NEW_R_MODE)
						sessionMode = "NEW R";
					getConsoleLogger().printAsOutput("\nR Server Information :" + "\n");
					getConsoleLogger().printAsOutput("Session Mode :" + sessionMode + "\n");
					getConsoleLogger().printAsOutput("Server Name :" + getR().getServantName() + "\n");
					getConsoleLogger().printAsOutput("Working Directory :" + getR().getWorkingDirectory() + "\n");
					getConsoleLogger().printAsOutput("Installation Directory :" + getR().getInstallDirectory() + "\n");
					getConsoleLogger().printAsOutput("Extensions Directory :" + getR().getExtensionsDirectory() + "\n");
					// getConsoleLogger().printAsOutput("System Environment Variables :"
					// + getR().getSystemEnv() + "\n");
					// getConsoleLogger().printAsOutput("System Properties :" +
					// getR().getSystemProperties() + "\n");
					getConsoleLogger().printAsOutput("Server Process ID :" + getR().getProcessId() + "\n");
					getConsoleLogger().printAsOutput("Server Host IP :" + getR().getHostIp() + "\n");
					getConsoleLogger().printAsOutput("Server Host Name :" + getR().getHostName() + "\n");
					getConsoleLogger().printAsOutput("STUB : \n" + getR().getStub() + "\n");

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			public boolean isEnabled() {
				return getR() != null;
			}

		});

		_actions.put("showworkbenchinfo", new AbstractAction("Show Workbench Info") {
			public void actionPerformed(ActionEvent e) {
				try {
					getConsoleLogger().printAsOutput("\nWorkbench Information :" + "\n");
					getConsoleLogger().printAsOutput("Installation Directory :" + ServerManager.INSTALL_DIR + "\n");
					getConsoleLogger().printAsOutput("Plugins Directory :" + ServerManager.PLUGINS_DIR + "\n");
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			public boolean isEnabled() {
				return true;
			}

		});

		_actions.put("supervisor", new AbstractAction("Supervisor") {
			public void actionPerformed(ActionEvent e) {
				try {

					GetDbDialog getDbDialog = new GetDbDialog(WorkbenchApplet.this);
					getDbDialog.setVisible(true);
					DbInfo dbInfo = getDbDialog.getDbInfo();

					if (dbInfo != null) {

						Properties props = new Properties();

						props.put("naming.mode", "db");
						props.put("db.type", dbInfo.getDbDriver());
						props.put("db.host", dbInfo.getDbHostIp());
						props.put("db.port", dbInfo.getDbHostPort().toString());
						props.put("db.name", dbInfo.getDbName());
						props.put("db.user", dbInfo.getDbUser());
						props.put("db.password", dbInfo.getDbPwd());

						DBLayerInterface db = null;
						try {
							db = (DBLayerInterface) ServerDefaults.getRegistry(props);
						} catch (ConnectionFailedException ex) {
							JOptionPane.showMessageDialog(WorkbenchApplet.this, "Connection to Server Failed", "", JOptionPane.ERROR_MESSAGE);
							return;
						}

						int id = getDynamicViewId();
						final Supervisor supervisor = new Supervisor(db, new SupervisorUtils(db));
						final DynamicView lv = new DynamicView("Supervisor: " + dbInfo.getDbHostIp() + ":" + dbInfo.getDbHostPort(), null, supervisor
								.getPanel(), id);
						((TabWindow) views[2].getWindowParent()).addTab(lv);
						lv.addListener(new AbstractDockingWindowListener() {
							public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
								supervisor.stopThreads();
							}
						});
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			public boolean isEnabled() {
				return true;
			}

		});

		_actions.put("httpsupervisor", new AbstractAction("Supervisor (HTTP) ") {
			public void actionPerformed(ActionEvent e) {
				try {
					GetUrlLoginPwdDialog getUrlLoginPwdDialog = new GetUrlLoginPwdDialog(WorkbenchApplet.this);
					getUrlLoginPwdDialog.setVisible(true);
					UrlLoginPwd ulp = getUrlLoginPwdDialog.getUrlLoginPwd();
					if (ulp != null) {
						Properties props = new Properties();
						props.put("httpregistry.url", ulp.getUrl());
						props.put("httpregistry.login", ulp.getLogin());
						props.put("httpregistry.password", ulp.getPwd());

						httpregistryClass hr = new genericnaming.httpregistryClass();
						DBLayerInterface db = null;
						try {
							db = (DBLayerInterface) hr.getRegistry(props);
						} catch (ConnectionFailedException ex) {
							JOptionPane.showMessageDialog(WorkbenchApplet.this, "Connection to Server Failed", "", JOptionPane.ERROR_MESSAGE);
							return;
						}

						SupervisorInterface supervisorInterface = hr.getSupervisorInterface();
						int id = getDynamicViewId();
						final Supervisor supervisor = new Supervisor(db, supervisorInterface);
						final DynamicView lv = new DynamicView("Supervisor HTTP: " + ulp.getUrl(), null, supervisor.getPanel(), id);
						((TabWindow) views[2].getWindowParent()).addTab(lv);
						lv.addListener(new AbstractDockingWindowListener() {
							public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
								supervisor.stopThreads();
							}
						});
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}

			public boolean isEnabled() {
				return true;
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

		try {
			getR().unregisterUser(getUID());
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

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (getOpenedUsersView() != null)
					getOpenedUsersView().close();
			}
		});

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
		try {
			persistState();

			/*
			 * if (getR() != null && !_keepAlive && _save) { try {
			 * _rForConsole.consoleSubmit("save.image('.RData')"); } catch
			 * (Exception e) { e.printStackTrace(); } }
			 */

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
		} finally {

			_sessionId = null;
			_rForConsole = null;
			_rForPopCmd = null;
			_rForFiles = null;
			_isBiocLiteSourced = false;
			_keepAlive = null;
			_sshParameters = null;
			_rProcessId = null;
			_virtualizationServer = null;

			disconnected();

		}

	}

	private void manageServerFailure() {

		try {
			((JGDPanelPop) _graphicPanel).stopThreads();
			Vector<DeviceView> deviceViews = getDeviceViews();
			for (int i = 0; i < deviceViews.size(); ++i)
				deviceViews.elementAt(i).getPanel().stopThreads();

			Vector<CollaborativeSpreadsheetView> collaborativeSpreadsheetViews = getCollaborativeSpreadsheetViews();
			for (int i = 0; i < collaborativeSpreadsheetViews.size(); ++i) {
				collaborativeSpreadsheetViews.elementAt(i).close();
			}

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (getOpenedUsersView() != null)
						getOpenedUsersView().close();
				}
			});

			if (getR() instanceof HttpMarker) {
				((HttpMarker) getR()).stopThreads();
			}

			persistState();

			if (_virtualizationServer != null) {
				try {
					_virtualizationServer.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} finally {
			_sessionId = null;
			_rForConsole = null;
			_rForPopCmd = null;
			_rForFiles = null;
			_isBiocLiteSourced = false;
			_keepAlive = null;
			_sshParameters = null;
			_rProcessId = null;
			_virtualizationServer = null;
			getConsoleLogger().printAsInput("Server failure, you have been disconnected, please relogon");
		}

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

	public String getLookAndFeelClassName() {
		return installedLFs[_lf].getClassName();
	}

	public String getSessionId() {
		return _sessionId;
	}

	public String getHelpRootUrl() {
		return _helpRootUrl;
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

	public String getInstallDir() {
		return ServerManager.INSTALL_DIR;
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
					((ExtendedReentrantLock) getRLock()).rawUnlock();
				}
			}
		}).start();

	}

	public boolean isCollaborativeMode() {
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

	private void updateUsers() {
		final UsersView uv = getOpenedUsersView();
		if (uv != null) {
			try {
				final UserStatus[] status = getR().getUserStatusTable();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						AbstractListModel model = new AbstractListModel() {
							public Object getElementAt(int index) {
								return " - " + status[index].getUserName() + (status[index].isTyping() ? " [T] " : "");
							}

							public int getSize() {
								return status.length;
							}
						};
						uv.getList().setModel(model);
						uv.getList().repaint();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
			else if (comp instanceof JPopupMenu) {
				comp = ((JPopupMenu) comp).getInvoker();
			}

			// cut dependency on jEdit
			/*
			 * else if (comp instanceof FloatingWindowContainer) { comp =
			 * ((FloatingWindowContainer) comp).getDockableWindowManager(); }
			 */else
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

		public void chat(String sourceUID, String user, String message) throws RemoteException {
			if (!getUID().equals(sourceUID)) {
				ChatConsoleView chatConsoleView = getOpenedChatConsoleView();
				if (chatConsoleView == null) {
					_actions.get("chatconsoleview").actionPerformed(null);
					chatConsoleView = getOpenedChatConsoleView();
				}
				chatConsoleView.getConsolePanel().print("[" + user + "] - " + message, null);
			}
		}

		public void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException {
			if (!getUID().equals(sourceUID)) {
				_consolePanel.print(expression == null ? null : "[" + user + "] - " + expression, result);
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

			// System.out.println("Action:" + action);
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
									setHelpBrowserURL(getHelpRootUrl() + "/doc/html/index.html");
								} else {
									setHelpBrowserURL(getHelpRootUrl() + helpUri);
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								getRLock().unlock();
							}
						}
					}).start();
				}
				if (action.getActionName().equals("q")) {
					_actions.get("quit").actionPerformed(null);
				} else if (action.getActionName().equals("ASYNCHRONOUS_SUBMIT_LOG")) {

					/*
					 * SwingUtilities.invokeLater(new Runnable() { public void
					 * run() { _consolePanel.print(null, (String)
					 * action.getAttributes().get("result")); } });
					 */

				} else if (action.getActionName().equals("GET_USER_INPUT")) {

					System.out.println("IIIIs Event dispatch thread ::" + SwingUtilities.isEventDispatchThread());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							try {
								InDialog dialog = new InDialog(null, "  R Console Input  ", new String[] { "" });
								dialog.setVisible(true);
								if (dialog.getExpr() != null)
									getR().setUserInput(dialog.getExpr());
								else
									getR().setUserInput("");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
				} else if (action.getActionName().equals("PAGER")) {

					String fileName = (String) action.getAttributes().get("fileName");
					byte[] content = (byte[]) action.getAttributes().get("content");
					String header = (String) action.getAttributes().get("header");
					String title = (String) action.getAttributes().get("title");
					boolean deleteFile = (Boolean) action.getAttributes().get("deleteFile");
					int id = getDynamicViewId();

					final PagerView lv = new PagerView(title, null, id, WorkbenchApplet.this, fileName, content, header, deleteFile);
					((TabWindow) views[2].getWindowParent()).addTab(lv);

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

			if (!getUID().equals(action.getAttributes().get("originatorUID"))) {
				if (action.getActionName().equals("OPEN_BROADCASTED_DEVICE")) {
					if (getOpenedBroadcastedDeviceViews().size() == 0) {
						_actions.get("createbroadcasteddevice").actionPerformed(null);
					}
				}
			}

			if (action.getActionName().equals("USER_JOINED")) {
				if (!action.getAttributes().get("sourceUID").equals(getUID())) {
					_consolePanel.print(null, "User [ " + (String) action.getAttributes().get("user") + " ] joined \n");
				}
			} else if (action.getActionName().equals("USER_LEFT")) {
				if (!action.getAttributes().get("sourceUID").equals(getUID())) {
					_consolePanel.print(null, "User [ " + (String) action.getAttributes().get("user") + " ] left \n");
				}
			} else if (action.getActionName().equals("USER_UPDATED")) {
				if (!action.getAttributes().get("sourceUID").equals(getUID())) {
					_consolePanel.print(null, "User [ " + (String) action.getAttributes().get("user") + " ] updated \n");
				}

			}

			if (action.getActionName().equals("APPEND_CONSOLE_LOG")) {
				_consolePanel.print(null, (String) action.getAttributes().get("log"));
			} else if (action.getActionName().equals("APPEND_CONSOLE_CONTINUE")) {
				_consolePanel.print(null, (String) action.getAttributes().get("log") + "\n", ConsolePanel.RED);
			} else if (action.getActionName().equals("UPDATE_USERS")) {
				updateUsers();
			} else if (action.getActionName().equals("CELLS_CHANGE")) {
				System.out.println(action);

				if (cellsListners.size() > 0) {
					CellsChangeEvent event = new CellsChangeEvent((String) action.getAttributes().get("name"), (CellRange) action.getAttributes().get("range"),
							(String) action.getAttributes().get("originatorUID"), WorkbenchApplet.this);
					for (int i = 0; i < cellsListners.size(); ++i) {
						try {
							cellsListners.elementAt(i).cellsChanged(event);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} else if (action.getActionName().equals("VARIABLES_CHANGE")) {
				if (varsListners.size() > 0) {
					VariablesChangeEvent event = new VariablesChangeEvent((HashSet<String>) action.getAttributes().get("variables"), (String) action
							.getAttributes().get("originatorUID"), action.getClientProperties(), WorkbenchApplet.this);
					for (int i = 0; i < varsListners.size(); ++i) {
						try {
							varsListners.elementAt(i).variablesChanged(event);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void refreshPluginViewsHash() throws Exception {
		HashMap<String, Vector<PluginViewDescriptor>> tempPluginViewsHash = new HashMap<String, Vector<PluginViewDescriptor>>();
		File[] list = new File(ServerManager.PLUGINS_DIR).listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.getName().endsWith(".jar");
			}
		});
		for (int i = 0; i < list.length; ++i) {
			Vector<PluginViewDescriptor> views = null;
			System.out.println("Plugin Candidate:" + list[i].getAbsolutePath());
			if (list[i].isDirectory())
				try {
					views = OpenPluginViewDialog.getPluginViews(list[i].getAbsolutePath() + "/");
				} catch (Exception e) {
					e.printStackTrace();
				}
			else {
				try {
					views = OpenPluginViewDialog.getPluginViews(list[i].getAbsolutePath());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (views != null && views.size() > 0) {
				tempPluginViewsHash.put(views.elementAt(0).getPluginName(), views);
			}
		}
		pluginViewsHash = tempPluginViewsHash;
	}

	public void refreshMacros() throws Exception {

		Vector<MacroInterface> tempMacrosVector = null;
		try {
			tempMacrosVector = Macro.getMacros(getInstallDir());

			for (MacroInterface m : macrosVector) {
				for (VariablesChangeListener v : m.getVarsListeners())
					removeVariablesChangeListener(v);
				for (CellsChangeListener c : m.getCellsListeners())
					removeCellsChangeListener(c);
			}

			if (_macrosEnabled) {
				macrosVector = tempMacrosVector;
				for (MacroInterface m : macrosVector) {
					if (getR() != null) {
						getR().addProbeOnVariables(m.getProbes());
					}
					for (VariablesChangeListener v : m.getVarsListeners()) {
						addVariablesChangeListener(v);
					}
					for (CellsChangeListener c : m.getCellsListeners())
						addCellsChangeListener(c);
				}
			} else {
				macrosVector = new Vector<MacroInterface>();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	Vector<VariablesChangeListener> varsListners = new Vector<VariablesChangeListener>();

	public void removeAllVariablesChangeListeners() {
		varsListners.removeAllElements();
	}

	public void removeVariablesChangeListener(VariablesChangeListener listener) {
		varsListners.remove(listener);
	}

	public void addVariablesChangeListener(VariablesChangeListener listener) {
		varsListners.add(listener);
	}

	Vector<CellsChangeListener> cellsListners = new Vector<CellsChangeListener>();

	public void removeAllCellsChangeListeners() {
		cellsListners.removeAllElements();
	}

	public void removeCellsChangeListener(CellsChangeListener listener) {
		cellsListners.remove(listener);
	}

	public void addCellsChangeListener(CellsChangeListener listener) {
		cellsListners.add(listener);
	}

	Vector<RConnectionListener> rconnectionListners = new Vector<RConnectionListener>();

	public void removeAllRConnectionListeners() {
		rconnectionListners.removeAllElements();
	}

	public void removeRConnectionListener(RConnectionListener listener) {
		rconnectionListners.remove(listener);
	}

	public void addRConnectionListener(RConnectionListener listener) {
		rconnectionListners.add(listener);
	}

	void disconnected() {
		for (int i = 0; i < rconnectionListners.size(); ++i) {
			try {
				rconnectionListners.elementAt(i).disconnected();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void connected() {
		for (int i = 0; i < rconnectionListners.size(); ++i) {
			try {
				rconnectionListners.elementAt(i).connected();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Vector<MacroInterface> getMacros() {
		return macrosVector;
	}

	public HashSet<String> getAvailableExtensions() {
		return _availableExtensions;
	}

	synchronized public Vector<Runnable> popAllTasks(int maxNbrLogActions) {
		if (_tasks.size() == 0)
			return null;
		Vector<Runnable> result = (Vector<Runnable>) _tasks.clone();
		if (maxNbrLogActions != -1 && result.size() > maxNbrLogActions) {
			int delta = result.size() - maxNbrLogActions;
			for (int i = 0; i < delta; ++i) {
				result.remove(result.size() - 1);
			}
		}
		for (int i = 0; i < result.size(); ++i)
			_tasks.remove(0);
		return result;
	}

	synchronized public void pushTask(Runnable task) {
		_tasks.add(task);
	}

	public String getPluginsDir() {
		return ServerManager.PLUGINS_DIR;
	}

	static public void main(String[] args) throws Exception {

		// views = OpenPluginViewDialog.getPluginViews(list[i].getAbsolutePath()
		// + "/");
		/*
		 * File pf = new File(pluginsDir.getAbsolutePath() + "/" +
		 * files[i].getName().substring(0,
		 * files[i].getName().lastIndexOf('.'))); if (pf.exists()) {
		 * PoolUtils.deleteDirectory(pf); } // pf.mkdirs();
		 * 
		 * URL rUrl = files[i].toURI().toURL(); InputStream is =
		 * rUrl.openConnection().getInputStream(); unzip(is,
		 * pluginsDir.getAbsolutePath(), null, PoolUtils.BUFFER_SIZE, true,
		 * "Unzipping Plugin..", 10000);
		 */
	}
}
