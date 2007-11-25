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

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
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