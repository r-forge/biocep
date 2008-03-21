package graphics.rmi;

import graphics.pop.GDDevice;
import graphics.rmi.action.CopyFromCurrentDeviceAction;
import graphics.rmi.action.CopyToCurrentDeviceAction;
import graphics.rmi.action.CoupleToCurrentDeviceAction;
import graphics.rmi.action.FitDeviceAction;
import graphics.rmi.action.SaveDeviceAsJpgAction;
import graphics.rmi.action.SaveDeviceAsPngAction;
import graphics.rmi.action.SetCurrentDeviceAction;
import graphics.rmi.action.SnapshotDeviceAction;
import graphics.rmi.spreadsheet.SpreadsheetPanel;
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
import java.awt.Dimension;
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
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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

import org.apache.batik.swing.JSVGCanvas;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.bioconductor.packages.rservices.RChar;
import org.bioconductor.packages.rservices.RObject;
import org.gjt.sp.jedit.gui.FloatingWindowContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import remoting.FileDescription;
import remoting.RAction;
import remoting.RServices;
import server.NoMappingAvailable;
import splash.SplashWindow;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemoteLogListener;
import uk.ac.ebi.microarray.pools.SSHUtils;
import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;
import uk.ac.ebi.microarray.pools.db.monitor.ConsolePanel;
import uk.ac.ebi.microarray.pools.db.monitor.SubmitInterface;
import uk.ac.ebi.microarray.pools.db.monitor.SymbolPopDialog;
import uk.ac.ebi.microarray.pools.db.monitor.SymbolPushDialog;
import uk.ac.ebi.microarray.pools.http.LocalClassServlet;
import util.PropertiesGenerator;
import util.Utils;
import static graphics.rmi.JGDPanelPop.*;
import static uk.ac.ebi.microarray.pools.PoolUtils.redirectIO;

/**
 * @author Karim Chine kchine@ebi.ac.uk
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

	private final ReentrantLock _protectR = new ReentrantLock() {
		@Override
		public void lock() {
			super.lock();
			if (_mode == HTTP_MODE) {
				try {
					_currentDevice.setAsCurrentDevice();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void unlock() {
			if (isCollaborativeMode()) {
				try {
					if (((JGDPanelPop) _graphicPanel).getGdDevice().hasGraphicObjects()) {
						synchronizeCollaborators();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
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
		}

		public void printAsOutput(String message) {
			_consolePanel.print(null, message);
		}
	};

	private Icon _currentDeviceIcon = null;
	private Icon _inactiveDeviceIcon = null;

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

		System.setErr(System.out);
		if (getParameter("debug") != null && getParameter("debug").equalsIgnoreCase("true")) {
			redirectIO();
		}

		System.out.println("INIT starts");

		if (getParameter("mode") == null) {
			_mode = HTTP_MODE;
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

			final Server server = new Server(PoolUtils.getLocalTomcatPort());
			// server.setStopAtShutdown(true);
			Context root = new Context(server, "/", Context.SESSIONS);
			root.addServlet(new ServletHolder(new LocalClassServlet()), "/classes/*");
			root.addServlet(new ServletHolder(new http.local.LocalHelpServlet(GDApplet.this)), "/helpme/*");
			System.out.println("+++++++++++++++++++ going to start local http server port : " + PoolUtils.getLocalTomcatPort());
			try {
				server.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (!server.isStarted()) {
				try {
					Thread.sleep(20);
				} catch (Exception e) {
				}
			}

			new Thread(new Runnable() {
				public void run() {
					System.out.println("local rmiregistry port : " + PoolUtils.getLocalTomcatPort());
					try {
						LocateRegistry.createRegistry(PoolUtils.getLocalRmiRegistryPort());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

		}

		if (getParameter("command_servlet_url") == null || getParameter("command_servlet_url").equals("")) {
			if (getWebAppUrl() != null) {
				_commandServletUrl = getWebAppUrl() + "cmd";
			} else {
				_commandServletUrl = "http://127.0.0.1:8080/cmd";
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

									ident = new Identification(RMI_MODE, "", "", "", false, false, RMI_MODE_STUB_MODE, "", -1, "", "", "", -1, "", "", "", "",
											stub, -1, -1, false, false, "", "", "", false, false);
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
								options.put("save", new Boolean(_save).toString());
								options.put("wait", new Boolean(_wait).toString());
								_sessionId = RHttpProxy.logOn(_commandServletUrl, _sessionId, _login, pwd, options);

								if (_sessionId.equals(oldSessionId)) {
									return "Already logged on\n";
								}

								if (_save && "guest".equals(_login) && _mode == HTTP_MODE) {
									JOptionPane.showMessageDialog(GDApplet.this, "The login <guest> is not allowed to have workspace persistency");
								}

								_rForConsole = (RServices) RHttpProxy.getDynamicProxy(_commandServletUrl, _sessionId, "R", new Class<?>[] { RServices.class,
										HttpMarker.class }, new HttpClient(new MultiThreadedHttpConnectionManager()));
								_rForPopCmd = (RServices) RHttpProxy.getDynamicProxy(_commandServletUrl, _sessionId, "R", new Class<?>[] { RServices.class,
										HttpMarker.class }, new HttpClient(new MultiThreadedHttpConnectionManager()));
								_rForFiles = (RServices) RHttpProxy.getDynamicProxy(_commandServletUrl, _sessionId, "R", new Class<?>[] { RServices.class,
										HttpMarker.class }, new HttpClient(new MultiThreadedHttpConnectionManager()));

								d = RHttpProxy.newDevice(_commandServletUrl, _sessionId, _graphicPanel.getWidth(), _graphicPanel.getHeight());
								System.out.println("device id:" + d.getDeviceNumber());

								if (new File(GDApplet.NEW_R_STUB_FILE).exists())
									new File(GDApplet.NEW_R_STUB_FILE).delete();

							} else {

								_helpServletUrl = "http://127.0.0.1:" + PoolUtils.getLocalTomcatPort() + "/" + "helpme";
								_defaultHelpUrl = _helpServletUrl + "/doc/html/index.html";

								_save = ident.isPersistentWorkspace();
								_demo = ident.isPlayDemo();

								RServices r = null;

								if (getMode() == GDApplet.NEW_R_MODE) {

									/*
									 * DirectJNI.init(); r =
									 * DirectJNI.getInstance().getRServices();
									 */

									_keepAlive = ident.isKeepAlive();
									
									if (ident.isUseSsh()) {
										r = ServerLauncher.createRSsh(ident.isKeepAlive(), PoolUtils.getHostIp(), PoolUtils.getLocalTomcatPort(), PoolUtils
												.getHostIp(), PoolUtils.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident.getMemoryMax(), ident
												.getSshHostIp(), ident.getSshLogin(), ident.getSshPwd(), false);
									} else {

										if (PoolUtils.isWindowsOs()) {
											if (_keepAlive) {
												r = ServerLauncher.createRLocal(ident.isKeepAlive(), PoolUtils.getHostIp(), PoolUtils.getLocalTomcatPort(),
													PoolUtils.getHostIp(), PoolUtils.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident.getMemoryMax(),
													false);
											} else {
												r = ServerLauncher.createR(ident.isKeepAlive(), PoolUtils.getHostIp(), PoolUtils.getLocalTomcatPort(),
														PoolUtils.getHostIp(), PoolUtils.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident.getMemoryMax(),
														false);												
											}
										} else {
											r = ServerLauncher.createR(ident.isKeepAlive(), PoolUtils.getHostIp(), PoolUtils.getLocalTomcatPort(), PoolUtils
													.getHostIp(), PoolUtils.getLocalRmiRegistryPort(), ident.getMemoryMin(), ident.getMemoryMax(), false);
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
										try {
											r.ping();
										} catch (Exception e) {
											e.printStackTrace();
											new File(GDApplet.NEW_R_STUB_FILE).delete();
											throw new PingRServerFailedException();
										}

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

								d = r.newDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());

								_rForConsole = r;
								_rForPopCmd = r;
								_rForFiles = r;

								_sessionId = RHttpProxy.FAKE_SESSION;

								restoreState();

							}

							_graphicPanel = new JGDPanelPop(d, true, true, new AbstractAction[] {

							new SetCurrentDeviceAction(GDApplet.this, d), null, new FitDeviceAction(GDApplet.this, d), null,
									new SnapshotDeviceAction(GDApplet.this), new SaveDeviceAsPngAction(GDApplet.this),
									new SaveDeviceAsJpgAction(GDApplet.this), null, new CopyFromCurrentDeviceAction(GDApplet.this),
									new CopyToCurrentDeviceAction(GDApplet.this, d), null, new CoupleToCurrentDeviceAction(GDApplet.this)

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
								if (_mode == HTTP_MODE) {
									newDevice = RHttpProxy.newDevice(_commandServletUrl, _sessionId, rootComponent.getWidth(), rootComponent.getHeight());
								} else {
									newDevice = _rForConsole.newDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());
								}
								JGDPanelPop gp = new JGDPanelPop(newDevice, true, true, new AbstractAction[] {
										new SetCurrentDeviceAction(GDApplet.this, newDevice), null, new FitDeviceAction(GDApplet.this, newDevice), null,
										new SnapshotDeviceAction(GDApplet.this), new SaveDeviceAsPngAction(GDApplet.this),
										new SaveDeviceAsJpgAction(GDApplet.this), null, new CopyFromCurrentDeviceAction(GDApplet.this),
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
								System.out.println(_rForConsole.isBusy());
								while (_rForConsole.isBusy()) {
									try {
										Thread.sleep(20);
									} catch (Exception e) {
									}
								}
								result = "";

							} else {
								result = _rForConsole.consoleSubmit(expression);
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
					toolsMenu.add(_actions.get("svgview"));
					toolsMenu.add(_actions.get("logview"));
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

					graphicsMenu.add(new SnapshotDeviceAction(GDApplet.this, _sessionId == null ? null : getCurrentJGPanelPop()));
					graphicsMenu.add(new SaveDeviceAsPngAction(GDApplet.this));
					graphicsMenu.add(new SaveDeviceAsJpgAction(GDApplet.this));

					graphicsMenu.addSeparator();

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

												_consolePanel.print("sourcing demo " + PoolUtils.replaceAll(demos[index], "_", " "), log);

											} catch (Exception ex) {
												ex.printStackTrace();
											} finally {
												getRLock().unlock();
											}
										}

									}

									@Override
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

			final JMenu helpMenu = new JMenu("Help");
			helpMenu.addMenuListener(new MenuListener() {
				public void menuSelected(MenuEvent e) {

					helpMenu.removeAll();
					helpMenu.add(_actions.get("help"));
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
			SaveToR.applet = this;
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

		_nopool = (getParameter("nopool") != null && getParameter("nopool").equalsIgnoreCase("true"));
		_save = (getParameter("save") != null && getParameter("save").equalsIgnoreCase("true"));
		_wait = (getParameter("wait") != null && getParameter("wait").equalsIgnoreCase("true"));
		_demo = (getParameter("demo") != null && getParameter("demo").equalsIgnoreCase("true"));
		_login = getParameter("login");

		LoginDialog.persistentWorkspace_bool = _save;
		LoginDialog.nopool_bool = _nopool;
		LoginDialog.waitForResource_bool = _wait;
		LoginDialog.playDemo_bool = _demo;
		LoginDialog.login_str = _login;

		if (getParameter("autologon") != null && getParameter("autologon").equalsIgnoreCase("true")) {
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
				while (true) {
					try {
						if (_sessionId != null && _rForPopCmd != null) {
							Vector<RAction> ractions = _rForPopCmd.popRActions();

							if (ractions != null) {

								for (int i = 0; i < ractions.size(); ++i) {
									final RAction action = ractions.elementAt(i);
									if (action.getActionName().equals("help")) {
										String topic = (String) action.getAttributes().get("topic");
										String pack = (String) action.getAttributes().get("package");

										String helpUri = _rForPopCmd.getRHelpFileUri(topic, pack);

										// System.out.println("<" + topic + "><"
										// + pack + "><" + helpUri + ">");

										if (helpUri == null) {
											setHelpBrowserURL(_defaultHelpUrl);
										} else {
											setHelpBrowserURL(_helpServletUrl + helpUri);
										}

									} else if (action.getActionName().equals("RESET_CONSOLE_LOG")) {
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
									} else if (action.getActionName().equals("ASYNCHRONOUS_SUBMIT_LOG")) {
										SwingUtilities.invokeLater(new Runnable() {
											public void run() {
												_consolePanel.print(null, (String) action.getAttributes().get("result"));
											}
										});
									}

								}

							}

						}
					} catch (NotLoggedInException nle) {
						noSession();
						nle.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
					}
				}
			}
		}).start();

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
			((TabWindow) views[2].getWindowParent()).addTab(new graphics.rmi.GDApplet.HelpView("Help View", null, _helpBrowser, id));

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
			PropertiesGenerator.main(new String[] { GDApplet.SETTINGS_FILE,
					"working.dir.root=" + ((RChar) _rForConsole.evalAndGetObject("getwd()")).getValue()[0],
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
				return _sessionId != null && (_filesTable.getSelectedRows().length > 0 && !_workDirFiles.elementAt(_filesTable.getSelectedRows()[0]).isDir());
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
				return _sessionId != null;
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
				return _sessionId != null;
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
				return _sessionId != null;
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
				return _sessionId != null && _filesTable.getSelectedRows().length > 0;
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
				return _sessionId != null && !getRLock().isLocked();
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
				return _sessionId != null && !getRLock().isLocked();
			}
		});

		_actions.put("help", new AbstractAction("Help Contents") {
			public void actionPerformed(ActionEvent e) {
				setHelpBrowserURL(_defaultHelpUrl);
			}

			public boolean isEnabled() {
				return _sessionId != null;
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
				return _sessionId == null;
			}
		});

		_actions.put("logoff", new AbstractAction("Log Off") {
			public void actionPerformed(ActionEvent e) {
				_consolePanel.play("logoff", false);
			}

			@Override
			public boolean isEnabled() {
				return _sessionId != null;
			}
		});

		_actions.put("playdemo", new AbstractAction("Play Demo") {
			public void actionPerformed(ActionEvent e) {
				playDemo();
			}

			@Override
			public boolean isEnabled() {
				return _sessionId != null && !getRLock().isLocked();
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
				return _sessionId != null;
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
				return _sessionId != null;
			}
		});

		_actions.put("editor", new AbstractAction("Script Editor") {
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
								cl.loadClass("org.gjt.sp.jedit.jEdit").getMethod("main", new Class<?>[] { String[].class }).invoke(null,
										new Object[] { new String[] { "-noserver", "-noplugins", "-nogui", "-nosettings" } });
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

		_actions.put("spreadsheet", new AbstractAction("Spreadsheet Editor") {
			public void actionPerformed(final ActionEvent ae) {

				NewWindow.create(new SpreadsheetPanel(300, 40, GDApplet.this), "Spreadsheet View");

			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("createdevice", new AbstractAction("New Device") {
			public void actionPerformed(final ActionEvent e) {

				JPanel rootGraphicPanel = new JPanel();
				rootGraphicPanel.setLayout(new BorderLayout());
				JPanel graphicPanel = new JPanel();
				rootGraphicPanel.add(graphicPanel, BorderLayout.CENTER);
				int id = getDynamicViewId();
				DeviceView deviceView = new graphics.rmi.GDApplet.DeviceView("Graphic Device", null, rootGraphicPanel, id);
				((TabWindow) views[2].getWindowParent()).addTab(deviceView);

				try {

					GDDevice newDevice = null;

					_protectR.lock();

					try {
						if (_mode == HTTP_MODE) {
							newDevice = RHttpProxy.newDevice(_commandServletUrl, _sessionId, _graphicPanel.getWidth(), _graphicPanel.getHeight());
						} else {
							newDevice = _rForConsole.newDevice(_graphicPanel.getWidth(), _graphicPanel.getHeight());
						}
						getR().evaluate("plot.new()");
					} finally {
						_protectR.unlock();
					}

					graphicPanel = new JGDPanelPop(newDevice, true, true, new AbstractAction[] { new SetCurrentDeviceAction(GDApplet.this, newDevice), null,
							new FitDeviceAction(GDApplet.this, newDevice), null, new SnapshotDeviceAction(GDApplet.this),
							new SaveDeviceAsPngAction(GDApplet.this), new SaveDeviceAsJpgAction(GDApplet.this), null,
							new CopyFromCurrentDeviceAction(GDApplet.this), new CopyToCurrentDeviceAction(GDApplet.this, newDevice), null,
							new CoupleToCurrentDeviceAction(GDApplet.this) }, getRLock(), getConsoleLogger());

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
					DockingWindow lv = new graphics.rmi.GDApplet.LogView("Console Log Viewer", null, id);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new DockingWindowListener() {
						public void viewFocusChanged(View arg0, View arg1) {
						}

						public void windowAdded(DockingWindow arg0, DockingWindow arg1) {
						}

						public void windowClosed(DockingWindow arg0) {
						}

						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
								_rForConsole.setProgressiveConsoleLogEnabled(false);
							} catch (Exception e) {
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

						public void windowMaximizing(DockingWindow arg0) throws OperationAbortedException {
						}

						public void windowMinimized(DockingWindow arg0) {
						}

						public void windowMinimizing(DockingWindow arg0) throws OperationAbortedException {
						}

						public void windowRemoved(DockingWindow arg0, DockingWindow arg1) {
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
				} else {
					getOpenedLogView().restoreFocus();
				}
			}

			@Override
			public boolean isEnabled() {
				return true;
			}
		});

		_actions.put("serverlogview", new AbstractAction("R Server Log Viewer") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerLogView() == null) {
					int id = getDynamicViewId();
					final ServerLogView lv = new ServerLogView("R Server Log Viewer", null, id);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new DockingWindowListener() {
						public void viewFocusChanged(View arg0, View arg1) {
						}

						public void windowAdded(DockingWindow arg0, DockingWindow arg1) {
						}

						public void windowClosed(DockingWindow arg0) {
						}

						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
								_rForConsole.removeErrListener(((ServerLogView) arg0).getRemoteLogListenerImpl());
								_rForConsole.removeOutListener(((ServerLogView) arg0).getRemoteLogListenerImpl());
								UnicastRemoteObject.unexportObject(((ServerLogView) arg0).getRemoteLogListenerImpl(), false);
							} catch (Exception e) {
								e.printStackTrace();
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

						public void windowMaximizing(DockingWindow arg0) throws OperationAbortedException {
						}

						public void windowMinimized(DockingWindow arg0) {
						}

						public void windowMinimizing(DockingWindow arg0) throws OperationAbortedException {
						}

						public void windowRemoved(DockingWindow arg0, DockingWindow arg1) {
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
				return true;
			}
		});


		_actions.put("svgview", new AbstractAction("SVG Viewer") {
			public void actionPerformed(final ActionEvent e) {
				if (getOpenedServerLogView() == null) {
					int id = getDynamicViewId();
					
					final SvgView lv = new SvgView("SVG Viewer", null, id);
					((TabWindow) views[2].getWindowParent()).addTab(lv);
					lv.addListener(new DockingWindowListener() {
						public void viewFocusChanged(View arg0, View arg1) {
						}

						public void windowAdded(DockingWindow arg0, DockingWindow arg1) {
						}

						public void windowClosed(DockingWindow arg0) {
						}

						public void windowClosing(DockingWindow arg0) throws OperationAbortedException {
							try {
							} catch (Exception e) {
								e.printStackTrace();
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

						public void windowMaximizing(DockingWindow arg0) throws OperationAbortedException {
						}

						public void windowMinimized(DockingWindow arg0) {
						}

						public void windowMinimizing(DockingWindow arg0) throws OperationAbortedException {
						}

						public void windowRemoved(DockingWindow arg0, DockingWindow arg1) {
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

				}
			}

			@Override
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
										GetExprDialog dialog = new GetExprDialog("  R Expression", _expressionSave);
										dialog.setVisible(true);
										if (dialog.getExpr() != null) {
											RObject robj = null;
											try {
												((JGDPanelPop) _graphicPanel).setAutoModes(true, false);
												robj = _rForConsole.evalAndGetObject(dialog.getExpr());
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
				return true;
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
				return _sessionId != null;
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

							GetExprDialog dialog = new GetExprDialog("  R package", _packageNameSave);
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
				return _sessionId != null;
			}
		});

		_actions.put("runhttpserverlocalhost", new AbstractAction("Run Http Virtualization Engine On Local Host") {
			public void actionPerformed(final ActionEvent e) {
				new Thread(new Runnable() {
					public void run() {

						try {

							while (true) {
								GetExprDialog dialog = new GetExprDialog(" Run Http Virtualization Engine On Port : ", _httpPortSave);
								dialog.setVisible(true);
								if (dialog.getExpr() != null) {
									try {
										final int port = Integer.decode(dialog.getExpr());
										if (PoolUtils.isPortInUse("127.0.0.1", port)) {
											JOptionPane.showMessageDialog(GDApplet.this.getContentPane(), "Port already in use", "", JOptionPane.ERROR_MESSAGE);
										} else {

											_virtualizationServer = new Server(port);
											Context root = new Context(_virtualizationServer, "/", Context.SESSIONS);
											root.addServlet(new ServletHolder(new http.local.LocalGraphicsServlet(GDApplet.this)), "/graphics/*");
											root.addServlet(new ServletHolder(new http.CommandServlet(GDApplet.this)), "/cmd/*");
											root.addServlet(new ServletHolder(new http.local.LocalHelpServlet(GDApplet.this)), "/helpme/*");
											System.out.println("+++++++++++++++++++ going to start virtualization http server port : " + port);
											_virtualizationServer.start();

											JOptionPane
													.showMessageDialog(
															GDApplet.this.getContentPane(),
															"A Virtualization HTTP Engine is now running on port "
																	+ port
																	+ "\n You can control the current R session from anywhere via the Workench\n log on in HTTP mode to the following URL : http://"
																	+ PoolUtils.getHostIp() + ":" + port + "/cmd", "", JOptionPane.INFORMATION_MESSAGE);
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
				return _sessionId != null && _virtualizationServer == null;
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
				return _sessionId != null && _virtualizationServer != null;
			}
		});

		_actions.put("runhttpserver", new AbstractAction("Run Http Virtualization Engine") {
			public void actionPerformed(final ActionEvent ae) {
				new Thread(new Runnable() {
					public void run() {

						while (true) {
							GetExprDialog dialog = new GetExprDialog(" Run Http Virtualization Engine On Port : ", _httpPortSave);
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
																+ _rForConsole.getHostIp() + ":" + port + "/cmd", "", JOptionPane.INFORMATION_MESSAGE);

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
				return _sessionId != null;
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
				return _sessionId != null;
			}
		});

	}

	public static class RemoteLogListenerImpl extends UnicastRemoteObject implements RemoteLogListener {
		private ServerLogView _serverLogView;

		public RemoteLogListenerImpl(ServerLogView serverLogView) throws RemoteException {
			super();
			_serverLogView = serverLogView;
		}

		public void scrollToEnd() {
			_serverLogView.getScrollPane().getVerticalScrollBar().setValue(_serverLogView.getScrollPane().getVerticalScrollBar().getMaximum());
		}

		public void flush() throws RemoteException {
		}

		public void write(final byte[] b) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_serverLogView.getArea().append(new String(b));
					scrollToEnd();
					_serverLogView.getArea().repaint();
				}
			});
		}

		public void write(final byte[] b, final int off, final int len) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_serverLogView.getArea().append(new String(b, off, len));
					scrollToEnd();
					_serverLogView.getArea().repaint();
				}
			});
		}

		public void write(final int b) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_serverLogView.getArea().append(new String(new byte[] { (byte) b, (byte) (b >> 8) }));
					scrollToEnd();
					_serverLogView.getArea().repaint();
				}
			});
		}

	}

	private void disposeDevices() {
		((JGDPanelPop) _graphicPanel).dispose();
		Vector<DeviceView> deviceViews = getDeviceViews();
		for (int i = 0; i < deviceViews.size(); ++i)
			deviceViews.elementAt(i).getPanel().dispose();
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
				System.out.println("333333333333333333333333333");
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

	class GetExprDialog extends JDialog {
		String[] save;
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

		public GetExprDialog(String label, String[] expr_save) {
			super(new JFrame(), true);
			save = expr_save;
			setLocationRelativeTo(GDApplet.this);
			getContentPane().setLayout(new GridLayout(1, 2));
			((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JPanel p1 = new JPanel();
			p1.setLayout(new GridLayout(0, 1));
			getContentPane().add(p1);
			JPanel p2 = new JPanel();
			p2.setLayout(new GridLayout(0, 1));
			getContentPane().add(p2);

			p1.add(new JLabel(label));

			exprs = new JTextField();
			exprs.setText(save[0]);

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

			setSize(new Dimension(320, 100));

			PoolUtils.locateInScreenCenter(this);

		}

		private void okMethod() {
			expr_str = exprs.getText();
			save[0] = expr_str;
			_closedOnOK = true;
			setVisible(false);
		}

		private void cancelMethod() {
			_closedOnOK = false;
			setVisible(false);
		}

	}

	static class DynamicView extends View {
		private int id;

		DynamicView(String title, Icon icon, Component component, int id) {
			super(title, icon, component);
			this.id = id;
		}

		public int getId() {
			return id;
		}
	}

	static class HelpView extends DynamicView {
		GDHelpBrowser _browser;

		HelpView(String title, Icon icon, GDHelpBrowser browser, int id) {
			super(title, icon, browser, id);
			_browser = browser;
		}

		public GDHelpBrowser getBrowser() {
			return _browser;
		}
	}

	static class DeviceView extends DynamicView {
		JGDPanelPop _panel;

		DeviceView(String title, Icon icon, Component component, int id) {
			super(title, icon, component, id);
		}

		public JGDPanelPop getPanel() {
			return _panel;
		}

		public void setPanel(JGDPanelPop panel) {
			this._panel = panel;
		}

	}

	static JPanel newPanel(JTextArea a) {
		JPanel result = new JPanel(new BorderLayout());
		result.add(new JScrollPane(a), BorderLayout.CENTER);
		return result;
	}

	static class LogView extends DynamicView {
		JTextArea _area;

		LogView(String title, Icon icon, int id) {
			super(title, icon, new JPanel(), id);
			((JPanel) getComponent()).setLayout(new BorderLayout());
			_area = new JTextArea();
			((JPanel) getComponent()).add(_area);
		}

		public JTextArea getArea() {
			return _area;
		}
	}

	public static class ServerLogView extends DynamicView {
		JTextArea _area;
		JScrollPane _scrollPane;
		RemoteLogListenerImpl _rll;

		ServerLogView(String title, Icon icon, int id) {
			super(title, icon, new JPanel(), id);
			_area = new JTextArea();
			_scrollPane = new JScrollPane(_area);

			((JPanel) getComponent()).setLayout(new BorderLayout());
			((JPanel) getComponent()).add(_scrollPane);
			try {
				_rll = new RemoteLogListenerImpl(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public JTextArea getArea() {
			return _area;
		}

		public JScrollPane getScrollPane() {
			return _scrollPane;
		}

		public RemoteLogListenerImpl getRemoteLogListenerImpl() {
			return _rll;
		}

		public void recreateRemoteLogListenerImpl() {
			try {
				_rll = new RemoteLogListenerImpl(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public class SvgView extends DynamicView {
		JTextArea _area;
		JScrollPane _scrollPane;
		JSVGCanvas _svgCanvas = new JSVGCanvas();

		SvgView(String title, Icon icon, int id) {
			super(title, icon, new JPanel(), id);

			_area = new JTextArea();
			_scrollPane = new JScrollPane(_area);
			_svgCanvas = new JSVGCanvas();
			_svgCanvas.setEnableZoomInteractor(true);

			JButton submit = new JButton("Submit");
			submit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					new Thread(new Runnable() {
						public void run() {
							if (GDApplet.this.getRLock().isLocked()) {
								JOptionPane.showMessageDialog(null, "R is busy");
							} else {
								GDApplet.this.getRLock().lock();
								try {
									SwingUtilities.invokeLater(new Runnable(){
										public void run() {
											_area.setEnabled(false);
										}
									});
									Vector<String> result=GDApplet.this.getR().evalAndGetSvg(_area.getText());
									if (result==null) {
										JOptionPane.showMessageDialog(null, GDApplet.this.getR().getStatus(), "R Error", JOptionPane.ERROR_MESSAGE);
										return ;
									}
									//System.out.println("SVG RESULT:"+result);
									final String tempFile=System.getProperty("java.io.tmpdir")+"/svgview"+System.currentTimeMillis()+".svg";
									PrintWriter pw=new PrintWriter(new FileWriter(tempFile));
									for (int i=0; i<result.size(); ++i) pw.println(result.elementAt(i));
									pw.close();
									
									
									SwingUtilities.invokeLater(new Runnable(){
										public void run() {
											try {
											_svgCanvas.setURI(new File(tempFile).toURL().toString());
											repaint();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									});
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									GDApplet.this.getRLock().unlock();
									SwingUtilities.invokeLater(new Runnable(){
										public void run() {
											_area.setEnabled(true);
										}
									});
								}
							}
						}
					}).start();
				}
			});

			JPanel topPanel = new JPanel(new BorderLayout());
			topPanel.add(_scrollPane, BorderLayout.CENTER);
			topPanel.add(submit, BorderLayout.SOUTH);

			final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, _svgCanvas);
			splitPane.setDividerSize(3);
			
			((JPanel) getComponent()).setLayout(new BorderLayout());
			((JPanel) getComponent()).setBorder(BorderFactory.createLineBorder(Color.gray, 2));
			((JPanel) getComponent()).add(splitPane);

			
			
			new Thread(new Runnable(){
				public void run() {
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							splitPane.setDividerLocation((int)60);
						}
					});
				}
			}).start();
			

		}

		public JTextArea getArea() {
			return _area;
		}

		public JScrollPane getScrollPane() {
			return _scrollPane;
		}

		JSVGCanvas getSvgCanvas() {
			return _svgCanvas;
		}

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
	public static String NEW_R_STUB_FILE = GUtils.INSTALL_DIR + "new_R_stub.txt";
	public static String SETTINGS_FILE = GUtils.INSTALL_DIR + "settings.xml";

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
		getR().evaluate(".PrivateEnv$dev.broadcast()");
	}

	public boolean isCollaborativeMode() {
		return _mode == HTTP_MODE && _login.indexOf("@@") != -1;
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
					_consolePanel.print(cmd, log);
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
				graphics.rmi.GDApplet.DynamicView v = new graphics.rmi.GDApplet.DynamicView(title, null, panel, id);
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

}
