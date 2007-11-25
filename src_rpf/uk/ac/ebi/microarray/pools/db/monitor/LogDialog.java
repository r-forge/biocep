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
package uk.ac.ebi.microarray.pools.db.monitor;

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

import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RemoteLogListener;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
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
			//re.printStackTrace();
		}

	}

}
