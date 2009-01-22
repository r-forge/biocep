package org.kchine.r.server.scripting;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Vector;
import org.kchine.r.server.manager.ServerManager;

public class GroovyInterpreterSingleton {

	public static GroovyInterpreter _groovy = null;
	private static Integer lock = new Integer(0);

	public static GroovyInterpreter getInstance() {
		if (_groovy != null)
			return _groovy;
		synchronized (lock) {
			if (_groovy == null) {

				try {

					System.out.println(ServerManager.EXTENSIONS_DIR);
					Vector<URL> cl_urls = new Vector<URL>();
					File[] extextensionsJarFiles = new File(ServerManager.EXTENSIONS_DIR).listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".jar");
						}
					});
					Arrays.sort(extextensionsJarFiles);
					for (int i = 0; i < extextensionsJarFiles.length; ++i) {
						cl_urls.add(extextensionsJarFiles[i].toURI().toURL());
					}

					File[] extensionsDirs = new File(ServerManager.EXTENSIONS_DIR).listFiles(new FileFilter() {
						public boolean accept(File pathname) {
							return pathname.isDirectory();
						}
					});
					Arrays.sort(extensionsDirs);

					for (int i = 0; i < extensionsDirs.length; ++i) {
						File extensionCodeBase = extensionsDirs[i];
						File classesDir = new File(extensionCodeBase.getAbsoluteFile() + "/classes");
						File libDir = new File(extensionCodeBase.getAbsoluteFile() + "/lib");
						File[] libList = new File[0];
						if (libDir.exists()) {
							libList = libDir.listFiles(new FilenameFilter() {
								public boolean accept(File dir, String name) {
									return name.endsWith(".jar");
								}
							});
						}
						if (classesDir.exists())
							cl_urls.add(classesDir.toURI().toURL());
						for (int j = 0; j < libList.length; ++j)
							cl_urls.add(libList[j].toURI().toURL());

					}

					System.out.println(cl_urls);
					ClassLoader cl = null;
					if (cl_urls.size() == 0) {
						cl = GroovyInterpreterSingleton.class.getClassLoader();
					} else {
						cl = new URLClassLoader(cl_urls.toArray(new URL[0]), GroovyInterpreterSingleton.class.getClassLoader());
					}

					_groovy = new GroovyInterpreterImpl(cl);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return _groovy;
		}
	}
	
	public static GroovyInterpreter _clientSideGroovy = null;
	private static Integer _clientSideLock = new Integer(0);

	public static GroovyInterpreter getClientSideInstance() {
		if (_clientSideGroovy != null)
			return _clientSideGroovy;
		synchronized (_clientSideLock) {
			if (_clientSideGroovy == null) {

				try {
					_clientSideGroovy = new GroovyInterpreterImpl(GroovyInterpreterSingleton.class.getClassLoader());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return _clientSideGroovy;
		}
	}

	
	static public void main(String[] args) throws Exception {
		GroovyInterpreter gr = getInstance();

		System.out.println("aa:"+gr.exec("import org.kchine.ooc.OOConverter;"));
		// System.out.println(gr.exec("print org.kchine.ooc.OOConverter.odgToWmf(\"c:/loess.odg\", \"c:/loess.wmf\");"));
		
		System.out.println("bb:"+gr.exec("print org.kchine.ooc.OOConverter.getDate();"));
		System.out.println("bb:"+gr.exec("print org.kchine.ooc.OOConverter.svgToOdg(\"c:/loess.svg\", \"c:/loess.odg\");"));
		System.exit(0);

	}

	

}
