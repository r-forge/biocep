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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class SymbolPushDialog extends JDialog {

	Symbol symbol_str = null;
	String pushAs_str = null;
	String file_str = null;

	JComboBox symbolName = null;

	public Symbol getSymbol() {
		if (symbol_str == null && pushAs_str == null && file_str == null)
			return null;
		else
			return symbol_str;
	}

	public String getPushAs() {
		if (symbol_str == null && pushAs_str == null && file_str == null)
			return null;
		else
			return pushAs_str;
	}

	public String getFileName() {
		if (symbol_str == null && pushAs_str == null && file_str == null)
			return null;
		else
			return file_str;
	}

	public SymbolPushDialog(final Container aFrame, String servantName, Symbol[] symbolsList, boolean fromFile) {
		super((Frame) null, true);

		if (servantName != null) {
			setTitle("Push Symbol to <" + servantName + ">");
		}
		setLocationRelativeTo(aFrame);

		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		if (!fromFile) {
			p1.add(new JLabel("  Symbol Name"));
		} else {
			p1.add(new JLabel("  Push From File"));
		}

		p1.add(new JLabel("  Push As"));
		p1.add(new JLabel(""));

		final JFileChooser chooser = new JFileChooser();
		final JTextField pushFromFile = new JTextField();
		pushFromFile.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int returnVal = chooser.showOpenDialog(aFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						pushFromFile.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
		});
		final JTextField pushAs = new JTextField();
		if (!fromFile) {
			symbolName = new JComboBox(symbolsList);
			symbolName.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					pushAs.setText((String) ((Symbol) symbolName.getSelectedItem()).getName());
				}
			});
			if (symbolsList.length > 0)
				symbolName.setSelectedIndex(0);
			p2.add(symbolName);
		} else {
			JButton browse = new JButton("Browse");
			browse.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int returnVal = chooser.showOpenDialog(aFrame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						pushFromFile.setText(chooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			JPanel field_button = new JPanel();
			field_button.setLayout(new BorderLayout());
			field_button.add(pushFromFile, BorderLayout.CENTER);
			field_button.add(browse, BorderLayout.EAST);

			p2.add(field_button);
		}

		p2.add(pushAs);

		p2.add(new JLabel(""));

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (symbolName != null) {
					symbol_str = (Symbol) symbolName.getSelectedItem();
				}
				pushAs_str = pushAs.getText();
				file_str = pushFromFile.getText();
				SymbolPushDialog.this.setVisible(false);
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SymbolPushDialog.this.setVisible(false);
			}
		});

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(500, 140));

	}

}