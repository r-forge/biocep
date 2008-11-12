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
package uk.ac.ebi.microarray.pools;

import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Layout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.helpers.LogLog;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RemoteAppender extends WriterAppender {

	public static final Log log = org.apache.commons.logging.LogFactory.getLog(RemoteAppender.class);

	private static Vector<RemoteLogListener> _outLogListeners = new Vector<RemoteLogListener>();
	private static Vector<RemoteLogListener> _errLogListeners = new Vector<RemoteLogListener>();

	public static void addOutLogListener(RemoteLogListener listener) {
		if (listener != null)
			_outLogListeners.add(listener);
	}

	public static void removeOutLogListener(RemoteLogListener listener) {
		_outLogListeners.remove(listener);
	}

	public static void removeAllOutLogListeners() {
		_outLogListeners.removeAllElements();
	}

	public static void addErrLogListener(RemoteLogListener listener) {
		if (listener != null)
			_errLogListeners.add(listener);
	}

	public static void removeErrLogListener(RemoteLogListener listener) {
		_errLogListeners.remove(listener);
	}

	public static void removeAllErrLogListeners() {
		_errLogListeners.removeAllElements();
	}

	public static final String SYSTEM_OUT = "System.out";
	public static final String SYSTEM_ERR = "System.err";

	protected String target = SYSTEM_OUT;

	public RemoteAppender() {

	}

	public RemoteAppender(Layout layout) {
		this(layout, SYSTEM_OUT);
	}

	public RemoteAppender(Layout layout, String target) {
		setLayout(layout);
		setTarget(target);
		activateOptions();
	}

	public void setTarget(String value) {
		String v = value.trim();

		if (SYSTEM_OUT.equalsIgnoreCase(v)) {
			target = SYSTEM_OUT;
		} else if (SYSTEM_ERR.equalsIgnoreCase(v)) {
			target = SYSTEM_ERR;
		} else {
			targetWarn(value);
		}
	}

	public String getTarget() {
		return target;
	}

	void targetWarn(String val) {
		LogLog.warn("[" + val + "] should be System.out or System.err.");
		LogLog.warn("Using previously set target, System.out by default.");
	}

	public void activateOptions() {
		if (target.equals(SYSTEM_ERR)) {
			setWriter(createWriter(new RemoteErrStream()));
		} else {
			setWriter(createWriter(new RemoteOutStream()));
		}

		super.activateOptions();
	}

	protected final void closeWriter() {
		super.closeWriter();
	}

	public static void initLog() {

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

	private static class RemoteErrStream extends OutputStream {
		public RemoteErrStream() {
		}

		public void close() {
		}

		public void flush() {
			if (_errLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> errLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _errLogListeners.size(); ++i) {
				try {
					_errLogListeners.elementAt(i).flush();
				} catch (RemoteException e) {
					errLogListenersToRemove.add(_errLogListeners.elementAt(i));
				}
			}
			_errLogListeners.removeAll(errLogListenersToRemove);

		}

		public void write(final byte[] b) throws IOException {
			if (_errLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> errLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _errLogListeners.size(); ++i) {
				try {
					_errLogListeners.elementAt(i).write(b);
				} catch (RemoteException e) {
					errLogListenersToRemove.add(_errLogListeners.elementAt(i));
				}
			}
			_errLogListeners.removeAll(errLogListenersToRemove);
		}

		public void write(final byte[] b, final int off, final int len) throws IOException {
			if (_errLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> errLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _errLogListeners.size(); ++i) {
				try {
					_errLogListeners.elementAt(i).write(b, off, len);
				} catch (RemoteException e) {
					errLogListenersToRemove.add(_errLogListeners.elementAt(i));
				}
			}
			_errLogListeners.removeAll(errLogListenersToRemove);
		}

		public void write(final int b) throws IOException {
			if (_errLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> errLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _errLogListeners.size(); ++i) {
				try {
					_errLogListeners.elementAt(i).write(b);
				} catch (RemoteException e) {
					errLogListenersToRemove.add(_errLogListeners.elementAt(i));
				}
			}
			_errLogListeners.removeAll(errLogListenersToRemove);
		}
	}

	private static class RemoteOutStream extends OutputStream {
		public RemoteOutStream() {
		}

		public void close() {
		}

		public void flush() {
			if (_outLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> outLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _outLogListeners.size(); ++i) {
				try {
					_outLogListeners.elementAt(i).flush();
				} catch (RemoteException e) {
					outLogListenersToRemove.add(_outLogListeners.elementAt(i));
				}
			}
			_outLogListeners.removeAll(outLogListenersToRemove);
		}

		public void write(final byte[] b) throws IOException {

			if (_outLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> outLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _outLogListeners.size(); ++i) {
				try {
					_outLogListeners.elementAt(i).write(b);
				} catch (RemoteException e) {
					outLogListenersToRemove.add(_outLogListeners.elementAt(i));
				}
			}
			_outLogListeners.removeAll(outLogListenersToRemove);

		}

		public void write(final byte[] b, final int off, final int len) throws IOException {
			if (_outLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> outLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _outLogListeners.size(); ++i) {
				try {
					_outLogListeners.elementAt(i).write(b, off, len);
				} catch (RemoteException e) {
					outLogListenersToRemove.add(_outLogListeners.elementAt(i));
				}
			}
			_outLogListeners.removeAll(outLogListenersToRemove);
		}

		public void write(final int b) throws IOException {
			if (_outLogListeners.size() == 0)
				return;
			Vector<RemoteLogListener> outLogListenersToRemove = new Vector<RemoteLogListener>();
			for (int i = 0; i < _outLogListeners.size(); ++i) {
				try {
					_outLogListeners.elementAt(i).write(b);
				} catch (RemoteException e) {
					outLogListenersToRemove.add(_outLogListeners.elementAt(i));
				}
			}
			_outLogListeners.removeAll(outLogListenersToRemove);
		}
	}

}
