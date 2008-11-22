package org.gjt.sp.jedit;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import static org.gjt.sp.jedit.jEdit.getRGui;
import static org.gjt.sp.jedit.jEdit.getActiveView;

public class WorkbenchActions {

	
	public static void runR(final String path) {

		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}

		final View activeView = getActiveView();
		if (activeView.getBuffer().isDirty()) {
			activeView.getInputHandler().invokeAction("save");
		}
		if (getRGui().getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}

		new Thread(new Runnable() {
			FileReader freader = null;

			public void run() {

				while (activeView.getBuffer().isDirty()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}

				try {
					getRGui().getRLock().lock();
					freader = new FileReader(path);

					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(freader);
					String l = null;
					while ((l = br.readLine()) != null)
						sb.append(l + "\n");
					final String rLog = getRGui().getR().sourceFromBuffer(sb.toString());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getRGui().getConsoleLogger().printAsOutput("script sourced to R\n");
							// getRGui().getConsoleLogger().printAsOutput("R:\n" +
							// rLog + "\n");
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					getRGui().getRLock().unlock();
					if (freader != null)
						try {
							freader.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		}).start();

	}

	public static void runRSelection(final String path) {
		
		
		

		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}

		
		
		final View activeView = getActiveView();
		final Clipboard clipboard = activeView.getToolkit().getSystemClipboard();
		
        StringSelection data = new StringSelection("");
        clipboard.setContents(data, data);
        
		activeView.getInputHandler().invokeAction("copy");

		if (getRGui().getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}

		new Thread(new Runnable() {
			public void run() {

				while (activeView.getBuffer().isPerformingIO() || activeView.getBuffer().isReadOnly()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}
				
				try {
					getRGui().getRLock().lock();

					
					Transferable clipData = clipboard.getContents(clipboard);
					if (clipData != null) {
							if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
								String s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));								
								getRGui().getR().sourceFromBuffer(s);
								SwingUtilities.invokeLater(new Runnable() {
									public void run() {
										getRGui().getConsoleLogger().printAsOutput("selection sourced to R\n");
									}
								});
							}
					}


				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					getRGui().getRLock().unlock();
				}
			}
		}).start();

	}

	
	
	public static void pasteConsole(final String path) {

		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}
		
		final View activeView = getActiveView();
		final Clipboard clipboard = activeView.getToolkit().getSystemClipboard();
		
        StringSelection data = new StringSelection("");
        clipboard.setContents(data, data);
        
		activeView.getInputHandler().invokeAction("copy");
		
		getRGui().getConsoleLogger().pasteToConsoleEditor();

	}
	
	
	public static void runP(final String path) {
		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}

		final View activeView = getActiveView();
		if (activeView.getBuffer().isDirty()) {
			activeView.getInputHandler().invokeAction("save");
		}

		if (getRGui().getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}

		new Thread(new Runnable() {
			FileReader freader = null;

			public void run() {
				while (activeView.getBuffer().isDirty()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}

				try {
					getRGui().getRLock().lock();

					freader = new FileReader(path);
					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(freader);
					String l = null;
					while ((l = br.readLine()) != null)
						sb.append(l + "\n");
					final String pythonLog = getRGui().getR().pythonExecFromBuffer(sb.toString());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getRGui().getConsoleLogger().printAsInput("script executed by Python On Server\n");
							getRGui().getConsoleLogger().printAsOutput("Python:\n" + pythonLog + "\n");
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					getRGui().getRLock().unlock();
					if (freader != null)
						try {
							freader.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		}).start();

	}

	public static void runG(final String path) {

		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}

		final View activeView = getActiveView();
		if (activeView.getBuffer().isDirty()) {
			activeView.getInputHandler().invokeAction("save");
		}

		try {
			if (!getRGui().getR().isGroovyEnabled()) {
				JOptionPane.showMessageDialog(null, "Groovy Unavailable, Download Core Jars and Retry");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (getRGui().getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}

		new Thread(new Runnable() {
			public void run() {

				while (activeView.getBuffer().isDirty()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}

				FileReader freader = null;
				try {
					getRGui().getRLock().lock();
					freader = new FileReader(path);

					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(freader);
					String l = null;
					while ((l = br.readLine()) != null)
						sb.append(l + "\n");
					final String groovyLog = getRGui().getR().groovyExecFromBuffer(sb.toString());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getRGui().getConsoleLogger().printAsInput("script executed by Groovy on Server\n");
							getRGui().getConsoleLogger().printAsOutput("Groovy:\n" + groovyLog + "\n");
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					getRGui().getRLock().unlock();
					if (freader != null)
						try {
							freader.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		}).start();

	}

	public static void runPL(final String path) {
		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}

		if (true) {
			JOptionPane.showMessageDialog(null, "Not Yet Available");
			return;
		}

		final View activeView = getActiveView();
		if (activeView.getBuffer().isDirty()) {
			activeView.getInputHandler().invokeAction("save");
		}

		if (getRGui().getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}

		new Thread(new Runnable() {
			public void run() {

				while (activeView.getBuffer().isDirty()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}

				FileReader freader = null;
				try {
					getRGui().getRLock().lock();
					freader = new FileReader(path);
					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(freader);
					String l = null;
					while ((l = br.readLine()) != null)
						sb.append(l + "\n");
					final String pythonLog = getRGui().getR().pythonExecFromBuffer(sb.toString());
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getRGui().getConsoleLogger().printAsInput("script executed by Python Locally\n");
							getRGui().getConsoleLogger().printAsOutput("Python:\n" + pythonLog + "\n");
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					getRGui().getRLock().unlock();
					if (freader != null)
						try {
							freader.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		}).start();

	}

	public static File createFileFromBuffer(StringBuffer buffer) throws Exception {
		final File tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + "temp_" + System.currentTimeMillis() + ".groovy").getCanonicalFile();
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

	public static void runGL(final String path) {

		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}

		final View activeView = getActiveView();
		if (activeView.getBuffer().isDirty()) {
			activeView.getInputHandler().invokeAction("save");
		}

		if (getRGui().getGroovyInterpreter() == null) {
			JOptionPane.showMessageDialog(null, "Groovy Unavailable, Download Core Jars and retry");
			return;
		}

		if (getRGui().getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}

		new Thread(new Runnable() {
			public void run() {

				while (activeView.getBuffer().isDirty()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}

				File f = null;
				FileReader freader = null;
				try {
					getRGui().getRLock().lock();
					freader = new FileReader(path);

					StringBuffer sb = new StringBuffer();
					BufferedReader br = new BufferedReader(freader);
					String l = null;
					while ((l = br.readLine()) != null)
						sb.append(l + "\n");

					final String groovyLog = getRGui().getGroovyInterpreter().execFromBuffer(sb.toString());

					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							getRGui().getConsoleLogger().printAsInput("script executed by Groovy Locally\n");
							getRGui().getConsoleLogger().printAsOutput("Groovy:\n" + groovyLog + "\n");
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					getRGui().getRLock().unlock();
					if (f != null)
						f.delete();
					if (freader != null)
						try {
							freader.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}
		}).start();

	}

	public static void uploadR(final String path) {

		if (getRGui().getR() == null) {
			JOptionPane.showMessageDialog(null, "No R available");
			return;
		}

		final View activeView = getActiveView();
		if (activeView.getBuffer().isDirty()) {
			activeView.getInputHandler().invokeAction("save");
		}

		new Thread(new Runnable() {
			public void run() {

				while (activeView.getBuffer().isDirty()) {
					try {
						Thread.sleep(100);
					} catch (Exception e) {
					}
				}

				try {
					final String fileName = new File(path).getName();
					getRGui().getR().createWorkingDirectoryFile(fileName);
					getRGui().upload(new File(path), fileName);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	public static boolean isRunREnabled() {
		return getRGui().getR() != null;
	}

	public static boolean isRunPEnabled() {
		return getRGui().getR() != null;
	}

	public static boolean isRunGEnabled() {
		try {
			return getRGui().getR() != null && getRGui().getR().isGroovyEnabled();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isRunPLEnabled() {
		return getRGui().getR() != null;
	}

	public static boolean isRunGLEnabled() {
		return getRGui().getR() != null && getRGui().getGroovyInterpreter() != null;
	}

	
}
