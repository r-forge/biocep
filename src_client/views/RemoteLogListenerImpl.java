/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
package views;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.SwingUtilities;

import uk.ac.ebi.microarray.pools.RemoteLogListener;

public class RemoteLogListenerImpl extends UnicastRemoteObject implements RemoteLogListener {
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