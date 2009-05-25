package org.kchine.r.server.scripting;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

public class GroovyInterpreterImpl implements GroovyInterpreter {
	private String _status;
	final Class<?> _groovyShellClass;
	final Object _groovyShell;
	private ClassLoader _cl;
	
	public GroovyInterpreterImpl(ClassLoader cl) throws Exception{
		_cl=cl;
		_groovyShellClass = cl.loadClass("groovy.lang.GroovyShell");
		_groovyShell = _groovyShellClass.getConstructor(ClassLoader.class).newInstance(cl);
	}
	
	public ClassLoader getClassLoader() throws Exception {		
		return _cl;
	}
	
	public String exec(String expression) throws Exception {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream saveOut = System.out;
		PrintStream saveErr = System.err;
		System.setOut(new PrintStream(baos));
		System.setErr(new PrintStream(baos));
		try {

			_groovyShellClass.getMethod("evaluate", String.class).invoke(_groovyShell, expression);

		} catch (InvocationTargetException e) {
			return e.getCause().getMessage() + "\n";
		} finally {
			System.setOut(saveOut);
			System.setErr(saveErr);
		}
		_status = new String(baos.toByteArray(), "UTF-8");
		return _status;

	}

	public String execFromFile(File f) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream saveOut = System.out;
		PrintStream saveErr = System.err;
		System.setOut(new PrintStream(baos));
		System.setErr(new PrintStream(baos));
		try {
			System.out.println("going to evaluate from file :" + f);
			_groovyShellClass.getMethod("evaluate", File.class).invoke(_groovyShell, f);
		} catch (InvocationTargetException e) {
			return e.getCause().getMessage() + "\n";
		} finally {
			System.setOut(saveOut);
			System.setErr(saveErr);
		}
		_status = new String(baos.toByteArray(), "UTF-8");
		return _status;
	}

	public String getStatus() throws Exception {
		return _status;
	}

	public String execFromBuffer(String buffer) throws Exception {

		File tempFile = null;
		tempFile = new File(System.getProperty("java.io.tmpdir") + "/" + "biocep_temp_" + System.currentTimeMillis()).getCanonicalFile();

		if (tempFile.exists())
			tempFile.delete();

		BufferedReader breader = new BufferedReader(new StringReader(buffer));
		PrintWriter pwriter = new PrintWriter(new FileWriter(tempFile));
		String line;
		boolean rscriptingOn = false;
		int rLinesCounter = 0;
		while ((line = breader.readLine()) != null) {
			if (line.trim().equals("<R>")) {
				pwriter.print("String log_R_sourceFromBuffer=");
				if (org.kchine.r.server.R.getInstance() != null)
					pwriter.print("server.");
				else
					pwriter.print("client.");
				pwriter.print("R.getInstance().sourceFromBuffer(new StringBuffer(\"");
				rscriptingOn = true;
				rLinesCounter = 0;
			} else if (line.trim().equals("</R>")) {
				pwriter.println("\"));System.out.println(\"\\n\"+log_R_sourceFromBuffer+ \"\\n\" );");

				for (int i = 0; i < rLinesCounter - 1; ++i)
					pwriter.println();
				rscriptingOn = false;
				rLinesCounter = 0;
			} else {
				if (rscriptingOn) {
					pwriter.print(line + "\\n");
					rLinesCounter++;
				} else {
					pwriter.println(line);
				}
			}
		}
		pwriter.close();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream saveOut = System.out;
		PrintStream saveErr = System.err;
		System.setOut(new PrintStream(baos));
		System.setErr(new PrintStream(baos));
		try {
			_groovyShellClass.getMethod("evaluate", File.class).invoke(_groovyShell, tempFile);
		} catch (InvocationTargetException e) {
			return e.getCause().getMessage() + "\n";
		} finally {
			System.setOut(saveOut);
			System.setErr(saveErr);
			// if (tempFile!=null) tempFile.delete();
		}
		_status = new String(baos.toByteArray(), "UTF-8");
		return _status;
	}
}