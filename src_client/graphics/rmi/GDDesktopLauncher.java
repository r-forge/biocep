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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.JOptionPane;
import uk.ac.ebi.microarray.pools.PoolUtils;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class GDDesktopLauncher {

	/**
	 * @param args
	 */
	public static int BUFFER_SIZE = 8192 * 5;

	static void showMessage(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}

	public static void main(String[] args) throws Exception {

		if (System.getProperty("debug") != null && System.getProperty("debug").equalsIgnoreCase("true"))
			redirectIO();

		Class<?> ServiceManagerClass = GDDesktopLauncher.class.getClassLoader().loadClass("javax.jnlp.ServiceManager");
		Object basicServiceInstance = ServiceManagerClass.getMethod("lookup", String.class).invoke(null, "javax.jnlp.BasicService");
		Class<?> BasicServiceClass = GDDesktopLauncher.class.getClassLoader().loadClass("javax.jnlp.BasicService");
		String urlprefix = BasicServiceClass.getMethod("getCodeBase").invoke(basicServiceInstance).toString();

		System.out.println("code base :" + urlprefix);
		URL[] urls = new URL[] { new URL(urlprefix + "appletlibs/RJB.jar"), new URL(urlprefix + "appletlibs/commons-httpclient-3.1-rc1.jar"),
				new URL(urlprefix + "appletlibs/commons-codec-1.3.jar"), new URL(urlprefix + "appletlibs/commons-logging-1.1.jar"),
				new URL(urlprefix + "appletlibs/idw-gpl.jar"), new URL(urlprefix + "appletlibs/jeditmodes.jar"), new URL(urlprefix + "appletlibs/jedit.jar"),
				new URL(urlprefix + "appletlibs/pf-joi-full.jar"), new URL(urlprefix + "appletlibs/OpenXLS.jar"),
				new URL(urlprefix + "jawslibs/htmlparser.jar"), new URL(urlprefix + "jawslibs/webserver.jar"), new URL(urlprefix + "jawslibs/servlet-api.jar"),
				new URL(urlprefix + "jawslibs/JRI.jar") };

		URL rjbURL = null;
		for (int i = 0; i < urls.length; ++i) {
			if (urls[i].toString().endsWith("RJB.jar")) {
				rjbURL = urls[i];
				break;
			}
		}

		for (String k : System.getenv().keySet()) {
			System.out.println(k + "=" + System.getenv().get(k));
		}

		String root = GUtils.INSTALL_DIR;
		new File(root).mkdir();

		String[] rinfo = GUtils.getRInfo(null);
		if (rinfo == null && System.getenv("R_HOME") != null) {
			String home = System.getenv("R_HOME");
			if (isWindowsOs() && !home.endsWith("\\")) {
				home = home + "\\";
			}
			if (!isWindowsOs() && !home.endsWith("/")) {
				home = home + "/";
			}
			rinfo = GUtils.getRInfo(home);
		}

		String rpath = rinfo != null ? rinfo[0].substring(0, rinfo[0].length() - "library".length()) : (System.getenv("R_HOME") != null ? System
				.getenv("R_HOME") : null);
		System.out.println("rpath=" + rpath);
		System.out.println("rversion=" + (rinfo != null ? rinfo[1] : ""));

		if (rpath == null) {

			if (isWindowsOs()) {

				int n = JOptionPane.showConfirmDialog(null, "R is not accessible from the command line\nWould you like to use the Embedded R?", "",
						JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.OK_OPTION) {
					String rZipFileName = null;
					if (isWindowsOs()) {
						if (!new File(root + "R/R-2.6.0/bin/R.dll").exists()) {
							rZipFileName = "R-2.6.0Win.zip";
						} else {
							rZipFileName = null;
						}
					} else if (isMacOs()) {
						if (!new File(root + "R/R-2.6.0/lib/libR.dylib").exists()) {
							rZipFileName = "R-2.6.0MacOSX.zip";
						} else {
							rZipFileName = null;
						}
					}

					if (rZipFileName != null) {
						URL rUrl = new URL(rjbURL.toString().substring(0, rjbURL.toString().indexOf("/appletlibs")) + "/jawslibs/" + rZipFileName);

						InputStream is = null;
						try {
							is = rUrl.openConnection().getInputStream();
						} catch (Exception e) {
							rUrl = new URL("http://www.ebi.ac.uk/microarray-srv/frontendapp/" + "jawslibs/" + rZipFileName);
							is = rUrl.openConnection().getInputStream();
						}

						unzip(is, root + "R/", null, BUFFER_SIZE, true, "Unzipping R..", 3816);

					}

					rpath = root + "R/R-2.6.0/";

				} else {
					JOptionPane.showMessageDialog(null, "please add R to your System path or set R_HOME to the root Directory of your local R installation\n");
					System.exit(0);
				}

			} else {
				JOptionPane
						.showMessageDialog(null,
								"R is not accessible from the command line\n please add R to your System path \nor set R_HOME to the root Directory of your local R installation\n");
				System.exit(0);
			}

		}

		if (!rpath.endsWith("/") && !rpath.endsWith("\\"))
			rpath += "/";
		// String rlibs = System.getenv("R_LIBS") != null ?
		// System.getenv("R_LIBS") : (rinfo != null ? rinfo[0] : rpath+
		// "library");
		String rlibs = (root + "library").replace('\\', '/');
		new File(rlibs).mkdir();

		for (int i = 0; i < urls.length; ++i) {
			PoolUtils.cacheJar(urls[i], root + "lib", true);
		}

		Vector<String> envVector = new Vector<String>();
		{
			Map<String, String> osenv = System.getenv();
			Map<String, String> env = new HashMap<String, String>(osenv);
			env.put("Path", rpath + (isWindowsOs() ? "bin" : "lib"));
			env.put("LD_LIBRARY_PATH", rpath + (isWindowsOs() ? "bin" : "lib"));
			env.put("R_HOME", rpath);
			env.put("R_LIBS", rlibs + (System.getenv("R_LIBS") != null ? ";" + System.getenv("R_LIBS") : ""));
			for (String k : env.keySet()) {
				envVector.add(k + "=" + env.get(k));
			}
		}

		// -------------------------------------

		String[] requiredPackages = new String[] { "rJava", "JavaGD", "TypeInfo", "gplots" };
		Vector<String> installLibBatch = new Vector<String>();
		installLibBatch.add("source('http://bioconductor.org/biocLite.R')");

		for (int i = 0; i < requiredPackages.length; ++i) {
			if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
				installLibBatch.add("biocLite('" + requiredPackages[i] + "',lib='" + rlibs + "')");
			}
		}

		if (installLibBatch.size() > 1) {

			File installPackagesFile = new File(root + "installRequiredPackages.R");
			File installPackagesOutputFile = new File(root + "installRequiredPackages.Rout");

			FileWriter fw = new FileWriter(installPackagesFile);
			PrintWriter pw = new PrintWriter(fw);
			for (int i = 0; i < installLibBatch.size(); ++i) {
				pw.println(installLibBatch.elementAt(i));
			}
			fw.close();

			Vector<String> installCommand = new Vector<String>();
			installCommand.add(rpath + "bin/R");
			installCommand.add("CMD");
			installCommand.add("BATCH");
			installCommand.add("--no-save");
			installCommand.add(installPackagesFile.getAbsolutePath());
			installCommand.add(installPackagesOutputFile.getAbsolutePath());

			System.out.println(installCommand);

			final Process installProc = Runtime.getRuntime().exec(installCommand.toArray(new String[0]), envVector.toArray(new String[0]));
			final Vector<String> installPrint = new Vector<String>();
			final Vector<String> installErrorPrint = new Vector<String>();

			new Thread(new Runnable() {
				public void run() {
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(installProc.getErrorStream()));
						String line = null;
						while ((line = br.readLine()) != null) {
							System.out.println(line);
							installErrorPrint.add(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			new Thread(new Runnable() {
				public void run() {
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(installProc.getInputStream()));
						String line = null;
						while ((line = br.readLine()) != null) {
							System.out.println(line);
							installPrint.add(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();
			installProc.waitFor();

			if (installPackagesOutputFile.exists() && installPackagesOutputFile.lastModified() > installPackagesFile.lastModified()) {
				BufferedReader br = new BufferedReader(new FileReader(installPackagesOutputFile));
				String line = null;
				while ((line = br.readLine()) != null) {
					System.out.println(line);
				}
			}

			Vector<String> missingLibs = new Vector<String>();

			for (int i = 0; i < requiredPackages.length; ++i) {
				if (getLibraryPath(requiredPackages[i], rpath, rlibs) == null) {
					missingLibs.add(requiredPackages[i]);
				}
			}

			if (missingLibs.size() > 0) {
				System.out.println("The following packages probably couldn't be automatically installed\n" + missingLibs);
			}

		}
		// ---------------------------------------
		String jripath = getLibraryPath("rJava", rpath, rlibs) + "jri/";
		System.out.println("jripath:" + jripath + "\n");
		String cp = "";
		for (int i = 0; i < urls.length; ++i) {
			String jarName = urls[i].toString().substring(urls[i].toString().lastIndexOf("/") + 1);
			cp += (cp.equals("") ? "" : System.getProperty("path.separator")) + root + "lib/" + jarName;
		}

		Vector<String> command = new Vector<String>();
		command.add(System.getProperty("java.home") + "/bin/java");
		command.add("-classpath");
		command.add((isWindowsOs() ? "\"" : "") + cp + (isWindowsOs() ? "\"" : ""));
		command.add((isWindowsOs() ? "\"" : "") + "-Djava.library.path=" + jripath + (isWindowsOs() ? "\"" : ""));
		command.add("-Dmode=local");
		command.add("-Dautologon=true");
		command.add("-Dsave=true");
		command.add("-Dlocaltomcat.port=9298");
		command.add((isWindowsOs() ? "\"" : "") + "-Dworking.dir.root=" + root + "wdir" + (isWindowsOs() ? "\"" : ""));
		command.add("-Dpreprocess.help=true");
		command.add("-Dapply.sandbox=false");
		command.add((isWindowsOs() ? "\"" : "") + "-Dpstools.home=" + root + "PsTools/" + (isWindowsOs() ? "\"" : ""));
		command.add("graphics.rmi.GDAppletLauncher");

		if (!new File(root + "VRWorkbench.txt").exists()) {
			try {

				String launcherFile = root + "VRWorkbench.txt";
				FileWriter fw = new FileWriter(launcherFile);
				PrintWriter pw = new PrintWriter(fw);

				if (isWindowsOs()) {
					pw.println("SET" + " Path=" + rpath + "bin" + ";%Path%");
					pw.println("SET" + " R_HOME=" + rpath);

					pw.println("SET R_LIBS=" + rlibs + (System.getenv("R_LIBS") != null ? ";" + System.getenv("R_LIBS") : ""));
				} else if (isMacOs()) {
					pw.println("export" + " LD_LIBRARY_PATH=" + rpath + "lib");
					pw.println("export" + " R_HOME=" + rpath);
					pw.println("export" + " R_LIBS=" + rlibs + (System.getenv("R_LIBS") != null ? ";" + System.getenv("R_LIBS") : ""));
				} else {
					pw.println("LD_LIBRARY_PATH=" + rpath + "lib" + ";export LD_LIBRARY_PATH");
					pw.println("R_HOME=" + rpath + ";export R_HOME");
					pw.println("R_LIBS=" + rlibs + (System.getenv("R_LIBS") != null ? ";" + System.getenv("R_LIBS") : "") + ";export R_LIBS");
				}

				pw.print((isWindowsOs() ? "\"" : "") + command.elementAt(0) + (isWindowsOs() ? "\"" : "") + " ");
				for (int i = 1; i < command.size(); ++i) {
					pw.print(command.elementAt(i) + " ");
				}

				pw.println();
				fw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		final Process proc = Runtime.getRuntime().exec(command.toArray(new String[0]), envVector.toArray(new String[0]));
		final Vector<String> killPrint = new Vector<String>();
		final Vector<String> errorPrint = new Vector<String>();

		System.out.println(" command : " + command);

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

	}

	private static String getLibraryPath(String libName, String rpath, String rlibs) {
		if (!rpath.endsWith("/") && !rpath.endsWith("\\")) {
			rpath += "/";
		}
		if (rlibs != null && !rlibs.equals("") && !rlibs.endsWith("/") && !rlibs.endsWith("\\")) {
			rlibs += "/";
		}
		if (rlibs != null && !rlibs.equals("") && new File(rlibs + libName).exists()) {
			return rlibs + libName + "/";
		} else if (new File(rpath + "library/" + libName).exists()) {
			return rpath + "library/" + libName + "/";
		} else {
			return null;
		}
	}

}
