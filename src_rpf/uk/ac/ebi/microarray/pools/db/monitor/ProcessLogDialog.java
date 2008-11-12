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
package uk.ac.ebi.microarray.pools.db.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ProcessLogDialog extends JFrame {

	private JTextArea _textArea = null;
	private JScrollPane _scrollPane = null;

	private void scrollToEnd() {
		_scrollPane.getVerticalScrollBar().setValue(_scrollPane.getVerticalScrollBar().getMaximum());
	}

	public void append(String str) {
		_textArea.append(str);
		scrollToEnd();
	}

	public ProcessLogDialog(Frame aFrame, String hostIp, String hostName, String prefix) {
		// super(aFrame, false);

		setTitle("New Servant Process On <" + hostName + "> with prefix " + prefix);
		setLocationRelativeTo(aFrame);
		setLocation(PoolUtils.deriveLocation(getLocation(), 50));

		getContentPane().setLayout(new BorderLayout());
		_textArea = new JTextArea();
		_scrollPane = new JScrollPane(_textArea);
		getContentPane().add(_scrollPane);
		setSize(new Dimension(500, 230));

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

	}

}