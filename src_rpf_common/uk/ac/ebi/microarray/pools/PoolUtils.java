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
package uk.ac.ebi.microarray.pools;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.PropertyConfigurator;
import org.neilja.net.interruptiblermi.InterruptibleRMISocketFactory;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class PoolUtils {

	public static final String DEFAULT_PREFIX = "RSERVANT_";
	public static final String DEFAULT_REGISTRY_HOST = "localhost";
	public static final int DEFAULT_REGISTRY_PORT = 1099;
	public static final int DEFAULT_MEMORY_MIN = 256;
	public static final int DEFAULT_MEMORY_MAX = 256;

	public static final int DEFAULT_TIMEOUT = 40000;

	public static final int PING_FAILURES_NBR_MAX = 1;
	public static final long LOOKUP_TIMEOUT_MILLISEC = 8000;
	public static final long PING_TIMEOUT_MILLISEC = 8000;
	public static final long RESET_TIMEOUT_MILLISEC = 10000;
	public static final long DIE_TIMEOUT_MILLISEC = 20000;

	public static final String TEMP_DIR = ".";
	public static final String UNKOWN = "Unknown";
	private static String _hostName = null;
	private static String _hostIp = null;
	private static String _processId = null;
	private static final Integer PING_DONE = new Integer(0);
	private static final Integer RESET_DONE = new Integer(0);
	private static final Integer DIE_DONE = new Integer(0);
	private static final int GET_PROCESS_ID_RETRY_MAX = 3;

	private static boolean _propertiesInjected = false;
	private static boolean _rmiSocketFactoryInitialized = false;

	public static final int LOG_PRGRESS_TO_SYSTEM_OUT=1;
	public static final int LOG_PRGRESS_TO_LOGGER=2;
	public static final int LOG_PRGRESS_TO_DIALOG=4;
	
	public static int BUFFER_SIZE = 1024 * 32;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(PoolUtils.class);

	public static String getHostName() {
		if (_hostName == null) {
			try {
				_hostName = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				_hostName = UNKOWN;
			}
		}
		return _hostName;
	}

	public static String getHostIp() {
		if (_hostIp == null) {
			try {
				_hostIp = InetAddress.getLocalHost().getHostAddress();
			} catch (Exception e) {
				_hostIp = UNKOWN;
			}
		}
		return _hostIp;
	}

	public static String getProcessId() {
		if (_processId == null) {
			try {
				_processId = PoolUtils.currentProcessID();
			} catch (Exception e) {
				_processId = UNKOWN;
			}
		}
		return _processId;
	}

	public static String getOs() {
		return System.getProperty("os.name");
	}

	public static boolean isWindowsOs() {
		return System.getProperty("os.name").toLowerCase().contains("windows");
	}

	public static boolean isMacOs() {
		return System.getProperty("os.name").toLowerCase().contains("mac");
	}

	public static String shortRmiName(String fullRmiName) {
		return fullRmiName.substring(fullRmiName.lastIndexOf('/') + 1);
	}

	public static String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}

	public static Vector<Integer> getRandomOrder(int size) {
		Vector<Integer> randomIndexes = new Vector<Integer>();
		Vector<Integer> availableIndexes = new Vector<Integer>();
		for (int i = 0; i < size; ++i)
			availableIndexes.add(i);
		for (int i = 0; i < size - 1; ++i) {
			int randomIndex = (int) Math.round((availableIndexes.size() - 1) * Math.random());
			randomIndexes.add(availableIndexes.elementAt(randomIndex));
			availableIndexes.remove(randomIndex);
		}
		randomIndexes.add(availableIndexes.elementAt(0));
		return randomIndexes;
	}

	private static void injectSystemProperties(InputStream is) {
		if (is != null) {
			try {
				Properties props = new Properties();

				props.loadFromXML(is);

				System.out.println("Properties : " + props);

				Enumeration<Object> keys = props.keys();
				while (keys.hasMoreElements()) {
					String key = (String) keys.nextElement();
					System.setProperty(key, props.getProperty(key));
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void injectSystemProperties(boolean onlyOnce) {

		if (onlyOnce && _propertiesInjected)
			return;
		injectSystemProperties(ServerDefaults.class.getResourceAsStream("/globals.properties"));

		if (System.getProperty("properties.extension") != null && !System.getProperty("properties.extension").equals("")) {

			if (new File(System.getProperty("properties.extension")).exists()) {
				try {
					injectSystemProperties(new FileInputStream(System.getProperty("properties.extension")));
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Invalid File Name in 'properties.extension' <" + System.getProperty("properties.extension") + ">");
			}
		}

		_propertiesInjected = true;

	}

	public static void initRmiSocketFactory() {
		if (!_rmiSocketFactoryInitialized) {
			_rmiSocketFactoryInitialized = true;
			try {
				RMISocketFactory.setSocketFactory(new InterruptibleRMISocketFactory());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static String bytesToHex(byte in[]) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0)
			return null;
		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		StringBuffer out = new StringBuffer(in.length * 2);
		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0);
			ch = (byte) (ch >>> 4);
			ch = (byte) (ch & 0x0F);
			out.append(pseudo[(int) ch]);
			ch = (byte) (in[i] & 0x0F);
			out.append(pseudo[(int) ch]);
			i++;
		}
		String rslt = new String(out);
		return rslt;
	}

	public static final byte[] hexToBytes(String s) throws NumberFormatException, IndexOutOfBoundsException {
		int slen = s.length();
		if ((slen % 2) != 0) {
			s = '0' + s;
		}

		byte[] out = new byte[slen / 2];

		byte b1, b2;
		for (int i = 0; i < slen; i += 2) {
			b1 = (byte) Character.digit(s.charAt(i), 16);
			b2 = (byte) Character.digit(s.charAt(i + 1), 16);
			if ((b1 < 0) || (b2 < 0)) {
				throw new NumberFormatException();
			}
			out[i / 2] = (byte) (b1 << 4 | b2);
		}
		return out;
	}

	public static String flatArray(Object[] array) {
		String result = "{";
		if (array != null) {
			for (int i = 0; i < array.length; ++i) {
				result += array[i].toString() + (i == (array.length - 1) ? "" : ",");
			}
		} else {
			result += "null";
		}
		result += "}";
		return result;
	}

	public static String currentUnixProcessID() throws Exception {

		String outputFileName = TEMP_DIR + "/echoPPID_" + System.currentTimeMillis() + ".txt";
		String[] command = new String[] { "/bin/sh", "-c", "echo $PPID > " + outputFileName };

		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);
		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception(Arrays.toString(command) + " exit code : " + exitVal);

		BufferedReader br = new BufferedReader(new FileReader(outputFileName));
		String result = br.readLine();
		br.close();

		new File(outputFileName).delete();
		return result;
	}

	public static String currentWinProcessID() throws Exception {

		String pslistpath = System.getProperty("pstools.home") + "/pslist.exe";
		if (!new File(pslistpath).exists()) {
			String psToolsHome = System.getProperty("user.home") + "/RWorkbench/" + "PsTools";
			pslistpath = psToolsHome + "/pslist.exe";
			if (!new File(pslistpath).exists()) {
				new File(psToolsHome).mkdirs();
				MainPsToolsDownload.main(new String[] { psToolsHome });
			}
		}

		String[] command = new String[] { pslistpath, "-t" };
		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);

		final Vector<String> pslistPrint = new Vector<String>();
		final Vector<String> errorPrint = new Vector<String>();

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					String line = null;
					while ((line = br.readLine()) != null)
						errorPrint.add(line);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						pslistPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		int exitVal = proc.waitFor();
		/*
		 * System.out.println(">"+exitVal+"<"); for (int i=0; i<pslistPrint.size();
		 * ++i) System.out.println(">>"+pslistPrint.elementAt(i)+"<<");
		 */

		if (exitVal != 0)
			throw new Exception("pslist exit code : " + exitVal);
		int i = 0;
		while (!pslistPrint.elementAt(i).trim().startsWith("pslist "))
			++i;
		// System.out.println(">>>>>>>>>>>"+pslistPrint.elementAt(i -
		// 1)+"<<<<<<<<<<<<<");
		StringTokenizer st = new StringTokenizer(pslistPrint.elementAt(i - 1), " ");
		st.nextElement();
		return (String) st.nextElement();
	}

	public static String currentProcessID() throws Exception {
		for (int i = 0; i < GET_PROCESS_ID_RETRY_MAX; ++i) {
			try {
				String result = isWindowsOs() ? currentWinProcessID() : currentUnixProcessID();
				if (result != null) {
					return result;
				}
			} catch (Exception e) {
			}

		}
		throw new Exception("Couldn't retrieve Process ID");
	}

	public static String stubToHex(Remote obj) throws NoSuchObjectException {
		if (obj instanceof UnicastRemoteObject) {
			obj = java.rmi.server.RemoteObject.toStub(obj);
		}
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baoStream).writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String stub_hex = PoolUtils.bytesToHex(baoStream.toByteArray());
		return stub_hex;
	}

	public static Remote hexToStub(String stubHex, ClassLoader cl) {
		try {
			return (Remote) new ObjectInputStreamCL(new ByteArrayInputStream(PoolUtils.hexToBytes(stubHex)), cl).readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String objectToHex(Object obj) throws NoSuchObjectException {

		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		try {
			new ObjectOutputStream(baoStream).writeObject(obj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String hex = PoolUtils.bytesToHex(baoStream.toByteArray());
		return hex;
	}

	public static Object hexToObject(String hex) {
		if (hex == null || hex.equals(""))
			return null;
		try {
			return (Object) new ObjectInputStream(new ByteArrayInputStream(PoolUtils.hexToBytes(hex))).readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object hexToObject(String hex, ClassLoader cl) {
		try {
			return (Object) new ObjectInputStreamCL(new ByteArrayInputStream(PoolUtils.hexToBytes(hex)), cl).readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static byte[] objectToBytes(Object obj) throws NoSuchObjectException {
		ByteArrayOutputStream baoStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(baoStream);
			oos.writeObject(obj);
			oos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baoStream.toByteArray();
	}

	public static Object bytesToObject(byte[] b) {
		try {
			return (Object) new ObjectInputStream(new ByteArrayInputStream(b)).readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object bytesToObject(byte[] buffer, ClassLoader cl) {
		try {
			return new ObjectInputStreamCL(new ByteArrayInputStream(buffer), cl).readObject();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String charRepeat(char c, int rep) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < rep; ++i)
			sb.append(c);
		return sb.toString();
	}

	public static String getDBType(String jdbcUrl) {
		int p1 = jdbcUrl.indexOf(':');
		int p2 = jdbcUrl.indexOf(':', p1 + 1);
		return jdbcUrl.substring(p1 + 1, p2);
	}

	public static URL[] getURLS(String urlsStr) {
		StringTokenizer st = new StringTokenizer(urlsStr, " ");
		Vector<URL> result = new Vector<URL>();
		while (st.hasMoreElements()) {
			try {
				result.add(new URL((String) st.nextElement()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return (URL[]) result.toArray(new URL[0]);
	}

	public static Point deriveLocation(Point origin, double radius) {
		return new Point((int) ((origin.getX() - radius) + (Math.random() * 2 * radius)), (int) ((origin.getY() - radius) + (Math.random() * 2 * radius)));
	}

	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	public static void ping(final ManagedServant servant) throws RemoteException {

		final Object[] resultHolder = new Object[1];
		Runnable pingRunnable = new Runnable() {
			public void run() {
				try {
					servant.ping();
					resultHolder[0] = PING_DONE;
				} catch (Exception e) {
					final boolean wasInterrupted = Thread.interrupted();
					if (wasInterrupted) {
						resultHolder[0] = new PingInterrupted();
					} else {
						resultHolder[0] = e;
					}
				}
			}
		};

		Thread pingThread = InterruptibleRMIThreadFactory.getInstance().newThread(pingRunnable);
		pingThread.start();

		long t1 = System.currentTimeMillis();
		while (resultHolder[0] == null) {
			if ((System.currentTimeMillis() - t1) > PING_TIMEOUT_MILLISEC) {
				pingThread.interrupt();
				resultHolder[0] = new PingTimeout();
				break;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}

		if (resultHolder[0] instanceof Throwable) {
			throw (RemoteException) resultHolder[0];
		}

	}

	public static void reset(final ManagedServant servant) throws RemoteException {
		final Object[] resultHolder = new Object[1];
		Runnable resetRunnable = new Runnable() {
			public void run() {
				try {
					servant.reset();
					resultHolder[0] = RESET_DONE;
				} catch (Exception e) {
					final boolean wasInterrupted = Thread.interrupted();
					if (wasInterrupted) {
						resultHolder[0] = new ResetInterrupted();
					} else {
						resultHolder[0] = e;
					}
				}
			}
		};

		Thread resetThread = InterruptibleRMIThreadFactory.getInstance().newThread(resetRunnable);
		resetThread.start();

		long t1 = System.currentTimeMillis();
		while (resultHolder[0] == null) {
			if ((System.currentTimeMillis() - t1) > RESET_TIMEOUT_MILLISEC) {
				resetThread.interrupt();
				resultHolder[0] = new ResetTimeout();
				break;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}

		if (resultHolder[0] instanceof Throwable) {
			throw (RemoteException) resultHolder[0];
		}
	}

	public static void die(final ManagedServant servant) throws RemoteException {
		final Object[] resultHolder = new Object[1];
		Runnable dieRunnable = new Runnable() {
			public void run() {
				try {
					servant.die();
					resultHolder[0] = DIE_DONE;
				} catch (UnmarshalException ue) {
					resultHolder[0] = DIE_DONE;
				} catch (Exception e) {
					final boolean wasInterrupted = Thread.interrupted();
					if (wasInterrupted) {
						resultHolder[0] = new DieInterrupted();
					} else {
						resultHolder[0] = e;
					}
				}
			}
		};

		Thread dieThread = InterruptibleRMIThreadFactory.getInstance().newThread(dieRunnable);
		dieThread.start();

		long t1 = System.currentTimeMillis();
		while (resultHolder[0] == null) {
			if ((System.currentTimeMillis() - t1) > DIE_TIMEOUT_MILLISEC) {
				dieThread.interrupt();
				resultHolder[0] = new DieTimeout();
				break;
			}
			try {
				Thread.sleep(10);
			} catch (Exception e) {
			}
		}

		if (resultHolder[0] instanceof Throwable) {
			throw (RemoteException) resultHolder[0];
		}
	}

	public static String replaceAll(String input, String replaceWhat, String replaceWith) throws Exception {
		int p;
		int bindex = 0;
		while ((p = input.indexOf(replaceWhat, bindex)) != -1) {
			input = input.substring(0, p) + replaceWith + input.substring(p + replaceWhat.length());
			bindex = p + replaceWith.length();
		}
		return input;
	}

	public static void locateInScreenCenter(Component c) {
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
		c.setLocation((screenDim.width - c.getWidth()) / 2, (screenDim.height - c.getHeight()) / 2);
	}

	public static void unzip(InputStream is, String destination, NameFilter nameFilter, int bufferSize, boolean showProgress, String taskName,
			int estimatedFilesNumber) {

		final JTextArea area = new JTextArea();
		final JProgressBar jpb = new JProgressBar(0, 100);
		final JFrame f = new JFrame(taskName);
		if (showProgress) {
			Runnable runnable = new Runnable() {
				public void run() {
					area.setFocusable(false);
					jpb.setIndeterminate(true);
					JPanel p = new JPanel(new BorderLayout());
					p.add(jpb, BorderLayout.SOUTH);
					p.add(new JScrollPane(area), BorderLayout.CENTER);
					f.add(p);
					f.pack();
					f.setSize(300, 90);
					f.setVisible(true);
					locateInScreenCenter(f);
				}
			};

			if (SwingUtilities.isEventDispatchThread())
				runnable.run();
			else {
				SwingUtilities.invokeLater(runnable);
			}
		}

		try {

			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
			int entriesNumber = 0;
			int currentPercentage = 0;
			int count;
			byte data[] = new byte[bufferSize];
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				if (!entry.isDirectory() && (nameFilter == null || nameFilter.accept(entry.getName()))) {
					String entryName = entry.getName();
					prepareFileDirectories(destination, entryName);
					String destFN = destination + File.separator + entry.getName();

					FileOutputStream fos = new FileOutputStream(destFN);
					BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
					while ((count = zis.read(data, 0, bufferSize)) != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();

					if (showProgress) {
						++entriesNumber;
						final int p = (int) (100 * entriesNumber / estimatedFilesNumber);
						if (p > currentPercentage) {
							currentPercentage = p;
							final JTextArea fa = area;
							final JProgressBar fjpb = jpb;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fjpb.setIndeterminate(false);
									fjpb.setValue(p);
									fa.setText("\n" + p + "%" + " Done ");
								}
							});

							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fa.setCaretPosition(fa.getText().length());
									fa.repaint();
									fjpb.repaint();
								}
							});

						}
					}
				}
			}
			zis.close();
			if (showProgress) {
				f.setVisible(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void cacheJar(URL url, String location, int logInfo) {
		String jarName = url.toString().substring(url.toString().lastIndexOf("/") + 1);
		if (!location.endsWith("/") && !location.endsWith("\\"))
			location += "/";
		String fileName = location + jarName;
		new File(location).mkdirs();

		final JTextArea area = new JTextArea();
		final JProgressBar jpb = new JProgressBar(0, 100);
		final JFrame f = new JFrame("copying " + jarName + " ...");

		try {
			URLConnection urlC = url.openConnection();
			InputStream is = url.openStream();
			File file = new File(fileName);
			if (file.exists() && file.length() == urlC.getContentLength() && file.lastModified() > urlC.getLastModified()) {
				return;
			}

			if ((logInfo & LOG_PRGRESS_TO_DIALOG)!=0) {

				Runnable runnable = new Runnable() {
					public void run() {
						area.setFocusable(false);
						jpb.setIndeterminate(true);
						JPanel p = new JPanel(new BorderLayout());
						p.add(jpb, BorderLayout.SOUTH);
						p.add(new JScrollPane(area), BorderLayout.CENTER);
						f.add(p);
						f.pack();
						f.setSize(300, 90);
						locateInScreenCenter(f);
						f.setVisible(true);
					}
				};

				if (SwingUtilities.isEventDispatchThread())
					runnable.run();
				else {
					SwingUtilities.invokeLater(runnable);
				}
			}

			if ((logInfo & LOG_PRGRESS_TO_SYSTEM_OUT)!=0) {
				System.out.println("Downloading " + jarName + ":");
				System.out.println("expected:==================================================\ndone    :");
			} 
			
			if ((logInfo & LOG_PRGRESS_TO_LOGGER)!=0) {
				log.info("Downloading " + jarName + ":");
			}
			

			int jarSize = urlC.getContentLength();
			int currentPercentage = 0;

			FileOutputStream fos = null;

			fos = new FileOutputStream(fileName);
			int count = 0;
			int printcounter = 0;

			byte data[] = new byte[BUFFER_SIZE];
			int co = 0;
			while ((co = is.read(data, 0, BUFFER_SIZE)) != -1) {
				fos.write(data, 0, co);
				count = count + co;				
				int expected=(50*count / jarSize);
				while (printcounter<expected) {
					if ((logInfo & LOG_PRGRESS_TO_SYSTEM_OUT)!=0) {
						System.out.print("=");
					} 
					if ((logInfo & LOG_PRGRESS_TO_LOGGER)!=0) {
						log.info((int) (100 * count / jarSize)+"% done.");
					}					
					
					++printcounter;
				}
										
				if ((logInfo & LOG_PRGRESS_TO_DIALOG)!=0) {
					final int p = (int) (100 * count / jarSize);
					if (p > currentPercentage) {
						currentPercentage = p;

						final JTextArea fa = area;
						final JProgressBar fjpb = jpb;
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								fjpb.setIndeterminate(false);
								fjpb.setValue(p);
								fa.setText("\n" + p + "%" + " Done. ");
							}
						});

						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								fa.setCaretPosition(fa.getText().length());
								fa.repaint();
								fjpb.repaint();
							}
						});

					}
				}
				
				
			}

			/*
			while ((oneChar = is.read()) != -1) {
				fos.write(oneChar);
				count++;

				final int p = (int) (100 * count / jarSize);
				if (p > currentPercentage) {
					System.out.print(p+" % ");
					currentPercentage = p;
					if (showProgress) {							
							final JTextArea fa = area;
							final JProgressBar fjpb = jpb;
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fjpb.setIndeterminate(false);
									fjpb.setValue(p);
									fa.setText("\n" + p + "%" + " Done ");
								}
							});
			
							SwingUtilities.invokeLater(new Runnable() {
								public void run() {
									fa.setCaretPosition(fa.getText().length());
									fa.repaint();
									fjpb.repaint();
								}
							});
					} else {
						if (p%2==0) System.out.print("=");
					}
				}

			}
			
			 */
			is.close();
			fos.close();

		} catch (MalformedURLException e) {
			System.err.println(e.toString());
		} catch (IOException e) {
			System.err.println(e.toString());
		} finally {
			if ((logInfo & LOG_PRGRESS_TO_DIALOG)!=0) {
				f.setVisible(false);
			} 
			if ((logInfo & LOG_PRGRESS_TO_SYSTEM_OUT)!=0) {
				System.out.println(" 100% of " + jarName + " has been downloaded \n");
			} 
			if ((logInfo & LOG_PRGRESS_TO_LOGGER)!=0) {
				log.info(" 100% of " + jarName + " has been downloaded");
			}					
		}
	}

	public static void prepareFileDirectories(String destination, String entryName) {
		destination = destination.replace('\\', '/');
		String outputFileName = destination + entryName;
		new File(outputFileName.substring(0, outputFileName.lastIndexOf("/"))).mkdirs();
	}

	public static void redirectIO() {
		final JTextArea area = new JTextArea();
		JFrame f = new JFrame("out/err");
		f.add(new JScrollPane(area), BorderLayout.CENTER);
		f.pack();
		f.setVisible(true);
		f.setSize(500, 500);
		f.setLocation(100, 100);

		PrintStream ps = new PrintStream(new OutputStream() {
			public void write(final int b) throws IOException {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						area.setText(area.getText() + new String(new byte[] { (byte) b }));
					}
				});
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						area.setCaretPosition(area.getText().length());
						area.repaint();
					}
				});
			}

			public void write(final byte[] b) throws IOException {

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						area.setText(area.getText() + new String(b));
					}
				});
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						area.setCaretPosition(area.getText().length());
						area.repaint();
					}
				});
			}

			public void write(byte[] b, int off, int len) throws IOException {
				final byte[] r = new byte[len];
				for (int i = 0; i < len; ++i)
					r[i] = b[off + i];

				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						area.setText(area.getText() + new String(r));
					}
				});
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						area.setCaretPosition(area.getText().length());
						area.repaint();
					}
				});
			}
		});
		System.setOut(ps);
		System.setErr(ps);
	}

	public interface NameFilter {
		public boolean accept(String name);
	}

	public static class EqualNameFilter implements NameFilter {
		HashSet<String> restrictToHashMap = new HashSet<String>();

		public EqualNameFilter(String... names) {
			if (names != null)
				for (int i = 0; i < names.length; ++i)
					restrictToHashMap.add(names[i]);
		}

		public boolean accept(String name) {
			return restrictToHashMap.contains(name);
		}
	}

	public static class StartsWithNameFilter implements NameFilter {
		String _startsWith;

		public StartsWithNameFilter(String startsWith) {
			_startsWith = startsWith;
		}

		public boolean accept(String name) {
			return name.startsWith(_startsWith);
		}
	}

	private static Vector<String> orderP(String[] keys) {
		Arrays.sort(keys);
		Vector<String> result = new Vector<String>();
		for (int i = 0; i < keys.length; ++i)
			result.add((String) keys[i]);
		return result;
	}

	public static Vector<String> orderO(Collection<Object> c) {
		String[] keys = new String[c.size()];
		int i = 0;
		for (Object k : c)
			keys[i++] = (String) k;
		return orderP(keys);
	}

	public static Vector<String> orderS(Collection<String> c) {
		String[] keys = new String[c.size()];
		int i = 0;
		for (String k : c)
			keys[i++] = k;
		return orderP(keys);
	}

	public static void main(String[] args) throws Exception {

	}

	public static void killLocalUnixProcess(String processId, boolean isKILLSIG) throws Exception {
		String[] command = isKILLSIG ? new String[] { "kill", "-9", processId } : new String[] { "kill", processId };
		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);
		final Vector<String> killPrint = new Vector<String>();
		final Vector<String> errorPrint = new Vector<String>();

		System.out.println("Kill command : " + Arrays.toString(command));

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						errorPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						killPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception("kill exit code : " + exitVal + "\n" + errorPrint);
	}

	public static void killLocalWinProcess(String processId, boolean isKILLSIG) throws Exception {
		// String[] command = isKILLSIG ? new String[] { "taskkill", "/F",
		// "/PID", processId } : new String[] {"taskkill", "/PID", processId };

		String pskillCommand = System.getProperty("pstools.home") + "/pskill.exe";
		if (!new File(pskillCommand).exists()) {
			String psToolsHome = System.getProperty("user.home") + "/RWorkbench/" + "PsTools";
			pskillCommand = psToolsHome + "/pskill.exe";
			if (!new File(pskillCommand).exists()) {
				new File(psToolsHome).mkdirs();
				MainPsToolsDownload.main(new String[] { psToolsHome });
			}
		}
		String[] command = new String[] { pskillCommand, processId };

		Runtime rt = Runtime.getRuntime();
		final Process proc = rt.exec(command);
		final Vector<String> killPrint = new Vector<String>();
		final Vector<String> errorPrint = new Vector<String>();

		System.out.println("Kill command : " + Arrays.toString(command));

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						errorPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		new Thread(new Runnable() {
			public void run() {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
					String line = null;
					while ((line = br.readLine()) != null) {
						System.out.println(line);
						killPrint.add(line);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

		int exitVal = proc.waitFor();
		if (exitVal != 0)
			throw new Exception("kill exit code : " + exitVal + "\n" + errorPrint);
	}

	public static void callBack(final ServantCreationListener servantCreationListener, final ManagedServant servant, final RemoteException exception) {
		try {

			final Object[] resultHolder = new Object[1];
			Runnable setServantStubRunnable = new Runnable() {
				public void run() {
					try {
						if (servant != null) {
							servantCreationListener.setServantStub(servant);
						} else {
							servantCreationListener.setRemoteException(exception);
						}
						resultHolder[0] = PoolUtils.SET_SERVANT_STUB_DONE;
					} catch (Exception e) {
						final boolean wasInterrupted = Thread.interrupted();
						if (wasInterrupted) {
							resultHolder[0] = new RmiCallInterrupted();
						} else {
							resultHolder[0] = e;
						}
					}
				}
			};

			Thread setServantStubThread = InterruptibleRMIThreadFactory.getInstance().newThread(setServantStubRunnable);
			setServantStubThread.start();

			long t1 = System.currentTimeMillis();
			while (resultHolder[0] == null) {
				if ((System.currentTimeMillis() - t1) > PoolUtils.SET_SERVANT_STUB_TIMEOUT_MILLISEC) {
					setServantStubThread.interrupt();
					resultHolder[0] = new RmiCallTimeout();
					break;
				}
				try {
					Thread.sleep(10);
				} catch (Exception e) {
				}
			}

			if (resultHolder[0] instanceof Throwable) {
				throw (RemoteException) resultHolder[0];
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static final Integer SET_SERVANT_STUB_DONE = new Integer(0);
	public static final int SET_SERVANT_STUB_TIMEOUT_MILLISEC = 2000;

	public static int countLines(String fileName) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String line = null;
		int counter = 0;
		while ((line = br.readLine()) != null)
			++counter;
		return counter;
	}

	public static void getFileNames(File path, Vector<String> result) throws Exception {
		File[] files = path.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.toString().toLowerCase().endsWith(".java") || pathname.toString().toLowerCase().endsWith(".r")
						|| pathname.toString().toLowerCase().endsWith("build.xml");
			}
		});
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getFileNames(files[i], result);
			} else {
				String name = files[i].getAbsolutePath();
				System.out.println(name + " --> " + countLines(name));
				result.add(name);
			}
		}
	}

	public static void getClasses(File root, File path, Vector<String> result) throws Exception {
		if (path == null)
			path = root;
		File[] files = path.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.toString().toLowerCase().endsWith(".class");
			}
		});
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getClasses(root, files[i], result);
			} else {
				String absolutepath = files[i].getAbsolutePath();
				String name = absolutepath.substring(root.getAbsolutePath().length() + 1, absolutepath.length() - ".class".length()).replace('/', '.').replace(
						'\\', '.');
				result.add(name);
			}
		}
	}

	public static void getResources(File root, File path, Vector<String> result) throws Exception {
		if (path == null)
			path = root;
		File[] files = path.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory() || pathname.toString().toLowerCase().endsWith(".xml") || pathname.toString().toLowerCase().endsWith(".props")
						|| pathname.toString().toLowerCase().endsWith(".properties") || pathname.toString().toLowerCase().endsWith(".r")
						|| pathname.toString().toLowerCase().endsWith(".png") || pathname.toString().toLowerCase().endsWith(".jpg")
						|| pathname.toString().toLowerCase().endsWith(".gif") || pathname.toString().toLowerCase().endsWith(".html")
						|| pathname.toString().toLowerCase().endsWith(".sql");
			}
		});
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getResources(root, files[i], result);
			} else {
				String absolutepath = files[i].getAbsolutePath();
				String name = absolutepath.substring(root.getAbsolutePath().length()).replace('\\', '/');
				result.add(name);
			}
		}
	}

	public static void initLog4J() {
		if (log instanceof Log4JLogger) {
			Properties log4jProperties = new Properties();
			for (Object sprop : System.getProperties().keySet()) {
				if (((String) sprop).startsWith("log4j.")) {
					log4jProperties.put(sprop, System.getProperties().get(sprop));
				}
			}
			PropertyConfigurator.configure(log4jProperties);
		}
	}

	public static File createFileFromBuffer(String fileExtension, StringBuffer buffer) throws Exception {
		File tempFile = null;
		tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + "biocep_temp_" + System.currentTimeMillis()
				+ (fileExtension == null || fileExtension.equals("") ? "" : "." + fileExtension)).getCanonicalFile();

		if (tempFile.exists())
			tempFile.delete();

		BufferedReader breader = new BufferedReader(new StringReader(buffer.toString()));
		PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));
		String line;
		do {
			line = breader.readLine();
			if (line != null) {
				pwriter.println(line);
			}
		} while (line != null);
		pwriter.close();
		return tempFile;
	}

}
