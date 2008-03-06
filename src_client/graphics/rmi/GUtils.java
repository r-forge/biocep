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
package graphics.rmi;

import static uk.ac.ebi.microarray.pools.PoolUtils.isWindowsOs;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import net.java.dev.jspreadsheet.CellPoint;
import org.gjt.sp.jedit.gui.FloatingWindowContainer;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class GUtils {

	public static String INSTALL_DIR = new File(System.getProperty("user.home") + "/RWorkbench/").getAbsolutePath()
			+ "/";
	public static String SETTINGS_FILE = INSTALL_DIR + "settings.xml";

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
	


	
	private static Integer _localTomcatPort=null;
	public static synchronized Integer getLocalTomcatPort() {
		if (_localTomcatPort==null){			
			
			if (System.getProperty("localtomcat.port")==null || System.getProperty("localtomcat.port").equals("")) {
				_localTomcatPort=3001;
			} else {
				_localTomcatPort=Integer.decode(System.getProperty("localtomcat.port"));
			}	
			
			for (int i=0;i<1000;++i) {				
				if (!PoolUtils.isPortInUse("127.0.0.1",_localTomcatPort+i)) {
					_localTomcatPort=_localTomcatPort+i;
					break;
				}
			}
		} 
		
		return _localTomcatPort;		
	}
	private static Integer _localRmiregistryPort=null;
	public static synchronized Integer getLocalRmiRegistryPort() {
		if (_localRmiregistryPort==null){			
			
			if (System.getProperty("localrmiregistry.port")==null || System.getProperty("localrmiregistry.port").equals("")) {
				_localRmiregistryPort= 2560;
			} else {
				_localRmiregistryPort= Integer.decode(System.getProperty("localrmiregistry.port"));
			}
			for (int i=0;i<1000;++i) {				
				if (!PoolUtils.isPortInUse("127.0.0.1",_localRmiregistryPort+i)) {
					_localRmiregistryPort=_localRmiregistryPort+i;
					break;
				}
			}
		}
		return _localRmiregistryPort;

	}

	
	static final String RLIBSTART = "R$LIB$START";
	static final String RLIBEND = "R$LIB$END";
	static final String RVERSTART = "R$VER$START";
	static final String RVEREND = "R$VER$END";
	
	public static String[] getRInfo(String rhome) {
	
		File getInfoFile = new File(INSTALL_DIR + "getInfo.R");
	
		File getInfoOutputFile = new File(INSTALL_DIR + "getInfo.Rout");
	
		String rversion = null;
	
		String rlibraypath = null;
	
		try {
	
			FileWriter fw = new FileWriter(getInfoFile);
	
			PrintWriter pw = new PrintWriter(fw);
	
			pw.println("paste('" + RLIBSTART + "',.Library, '" + RLIBEND + "',sep='%')");
	
			pw.println("paste('" + RVERSTART + "', R.version.string , '" + RVEREND + "', sep='%')");
	
			fw.close();
	
			Vector<String> getInfoCommand = new Vector<String>();
	
			if (rhome != null) {
				getInfoCommand.add(rhome + "bin/R");
				getInfoCommand.add("CMD");
				getInfoCommand.add("BATCH");
				getInfoCommand.add("--no-save");
				getInfoCommand.add(getInfoFile.getAbsolutePath());
				getInfoCommand.add(getInfoOutputFile.getAbsolutePath());
	
			} else {
	
				if (isWindowsOs()) {
	
					getInfoCommand.add(System.getenv().get("ComSpec"));
					getInfoCommand.add("/C");
					getInfoCommand.add("R");
					getInfoCommand.add("CMD");
					getInfoCommand.add("BATCH");
					getInfoCommand.add("--no-save");
					getInfoCommand.add(getInfoFile.getAbsolutePath());
					getInfoCommand.add(getInfoOutputFile.getAbsolutePath());
	
				} else {
					getInfoCommand.add(/* System.getenv().get("SHELL") */"/bin/sh");
					getInfoCommand.add("-c");
					getInfoCommand.add("R CMD BATCH --no-save " + getInfoFile.getAbsolutePath() + " "
							+ getInfoOutputFile.getAbsolutePath());
				}
			}
	
			Vector<String> systemEnvVector = new Vector<String>();
	
			{
	
				Map<String, String> osenv = System.getenv();
	
				Map<String, String> env = new HashMap<String, String>(osenv);
	
				for (String k : env.keySet()) {
	
					systemEnvVector.add(k + "=" + env.get(k));
	
				}
	
			}
	
			System.out.println("exec->" + getInfoCommand);
	
			final Process getInfoProc = Runtime.getRuntime().exec(getInfoCommand.toArray(new String[0]),
	
			systemEnvVector.toArray(new String[0]));
	
			new Thread(new Runnable() {
	
				public void run() {
	
					try {
	
						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getErrorStream()));
	
						String line = null;
	
						while ((line = br.readLine()) != null) {
	
							//System.out.println(line);
	
						}
	
					} catch (Exception e) {
	
						e.printStackTrace();
	
					}
	
				}
	
			}).start();
	
			new Thread(new Runnable() {
	
				public void run() {
	
					try {
	
						BufferedReader br = new BufferedReader(new InputStreamReader(getInfoProc.getInputStream()));
	
						String line = null;
	
						while ((line = br.readLine()) != null) {
	
							//System.out.println(line);
	
						}
	
					} catch (Exception e) {
	
						e.printStackTrace();
	
					}
	
				}
	
			}).start();
	
			getInfoProc.waitFor();
	
			if (getInfoOutputFile.exists() && getInfoOutputFile.lastModified() > getInfoFile.lastModified()) {
	
				BufferedReader br = new BufferedReader(new FileReader(getInfoOutputFile));
	
				String line = null;
	
				while ((line = br.readLine()) != null) {
	
					//System.out.println(line);
	
					if (line.contains(RLIBSTART + "%")) {
	
						rlibraypath = line.substring(line.indexOf(RLIBSTART + "%") + (RLIBSTART + "%").length(), (line
								.indexOf("%" + RLIBEND) > 0 ? line.indexOf("%" + RLIBEND) : line.length()));
	
					}
	
					if (line.contains(RVERSTART + "%")) {
	
						rversion = line.substring(line.indexOf(RVERSTART + "%") + (RVERSTART + "%").length(), line
								.indexOf("%" + RVEREND));
	
					}
	
				}
	
			}
	
		} catch (Exception e) {
	
			e.printStackTrace();
	
		}
	
		if (rversion != null && rlibraypath != null) {
	
			return new String[] { rlibraypath, rversion };
	
		} else {
	
			return null;
	
		}
	
	}

}
