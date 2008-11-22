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
package org.kchine.rpf.db.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RemoteLogListener;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class LogDialog extends JFrame {

	private JTextArea _textArea = null;
	private JScrollPane _scrollPane = null;

	public void scrollToEnd() {
		_scrollPane.getVerticalScrollBar().setValue(_scrollPane.getVerticalScrollBar().getMaximum());
	}

	public class LogListener extends UnicastRemoteObject implements RemoteLogListener {

		public LogListener() throws RemoteException {
			super();
		}

		public void flush() throws RemoteException {
		}

		public void write(final byte[] b) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_textArea.append(new String(b));
					scrollToEnd();
				}
			});
		}

		public void write(final byte[] b, final int off, final int len) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_textArea.append(new String(b, off, len));
					scrollToEnd();
				}
			});
		}

		public void write(final int b) throws RemoteException {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					_textArea.append(new String(new byte[] { (byte) b, (byte) (b >> 8) }));
					scrollToEnd();
				}
			});
		}

	}

	public LogDialog(Frame aFrame, String servantName, Registry registry) {

		setTitle("Servant <" + servantName + "> Log");
		setLocationRelativeTo(aFrame);
		setLocation(PoolUtils.deriveLocation(getLocation(), 50));

		getContentPane().setLayout(new BorderLayout());
		_textArea = new JTextArea();
		_scrollPane = new JScrollPane(_textArea);
		getContentPane().add(_scrollPane);
		setSize(new Dimension(400, 130));
		_textArea.setEditable(false);
		_textArea.addMouseListener(new MouseAdapter() {
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
							_textArea.setText("");

						}

						@Override
						public boolean isEnabled() {
							return !_textArea.getText().equals("");
						}
					});

					popupMenu.show(_textArea, e.getX(), e.getY());
				}
			}
		});

		try {
			ManagedServant servant = ((ManagedServant) registry.lookup(servantName));
			RemoteLogListener rll = new LogListener();
			servant.addOutListener(rll);
			servant.addErrListener(rll);
		} catch (NotBoundException nbe) {
			nbe.printStackTrace();
		} catch (RemoteException re) {
			// re.printStackTrace();
		}

	}

}
