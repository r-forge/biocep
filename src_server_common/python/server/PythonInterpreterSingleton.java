package python.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.python.core.PyException;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class PythonInterpreterSingleton {
	
	private static PythonInterpreter _python = null;
	private static Integer lock = new Integer(0);
	private static StringWriter sw=new StringWriter();
	
	public static PythonInterpreter getInstance() {
		if (_python != null)
			return _python;
		synchronized (lock) {
			if (_python == null) {
				_python=new PythonInterpreter(){

					public void cleanup() {
						super.cleanup();
					}

					public PyObject eval(String arg0) {
						try {
							return super.eval(arg0);
						} catch (PyException e) {
							new PrintWriter(sw).println(e.toString());
							throw new RuntimeException("python eval failed");
						}
					}

					public void exec(PyObject arg0) {
						super.exec(arg0);
					}

					public void exec(String arg0) {
						try {
							super.exec(arg0);
						} catch (PyException e) {
							new PrintWriter(sw).println(e.toString());
						}
					}

					public void execfile(InputStream arg0, String arg1) {
						super.execfile(arg0, arg1);
					}

					public void execfile(InputStream arg0) {
						super.execfile(arg0);
					}

					public void execfile(String arg0) {
						super.execfile(arg0);
					}

					public Object get(String arg0, Class arg1) {
						return super.get(arg0, arg1);
					}

					public PyObject get(String arg0) {
						try {
							return super.get(arg0);
						} catch (PyException e) {
							new PrintWriter(sw).println(e.toString());
							throw new RuntimeException("python get failed");
						}
					}

					public PyObject getLocals() {
						return super.getLocals();
					}

					public void set(String arg0, Object arg1) {
						super.set(arg0, arg1);
					}

					public void set(String arg0, PyObject arg1) {
						super.set(arg0, arg1);
					}

					public void setErr(OutputStream arg0) {
						super.setErr(arg0);
					}

					public void setErr(PyObject arg0) {
						super.setErr(arg0);
					}

					public void setErr(Writer arg0) {
						super.setErr(arg0);
					}

					public void setLocals(PyObject arg0) {
						super.setLocals(arg0);
					}

					public void setOut(OutputStream arg0) {
						super.setOut(arg0);
					}

					public void setOut(PyObject arg0) {
						super.setOut(arg0);
					}

					public void setOut(Writer arg0) {
						super.setOut(arg0);
					}

					protected void setState() {
						super.setState();
					}
					
				};
			}
			return _python;
		}
	}
	
	public static void startLogCapture() {
		sw=new StringWriter();
		PythonInterpreterSingleton.getInstance().setOut(sw);
		PythonInterpreterSingleton.getInstance().setErr(sw);
	}
	
	public static void insertLog(String log) {
		new PrintWriter(sw).println(log);
	}

	public static String getPythonStatus() {
		return sw.toString();
	}

}
