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
package graphics.rmi.spreadsheet;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.kchine.rpf.PoolUtils;


public  class DimensionsDialog extends JDialog {
	static String rownum_str = "300";
	static String colnum_str = "40";

	private boolean _closedOnOK = false;
	private JTextField _rowNum;
	private JTextField _colNum;
	private JButton _ok;
	private JButton _cancel;

	public Dimension getSpreadsheetDimension() {
		if (_closedOnOK)
			try {
				return new Dimension(Integer.decode(colnum_str), Integer.decode(rownum_str));
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}

	public DimensionsDialog(Component c) {
		super((Frame) null, true);

		setTitle("Spreadsheet Size");
		setLocationRelativeTo(c);

		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		_rowNum = new JTextField(rownum_str);
		_colNum = new JTextField(colnum_str);

		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					okMethod();
				} else if (e.getKeyCode() == 27) {
					cancelMethod();
				}
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		};
		_rowNum.addKeyListener(keyListener);
		_colNum.addKeyListener(keyListener);

		p2.add(_rowNum);
		p2.add(_colNum);

		p1.add(new JLabel("  Rows Number"));
		p1.add(new JLabel("  Columns Number"));

		_ok = new JButton("Ok");
		_ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okMethod();
			}
		});

		_cancel = new JButton("Cancel");
		_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelMethod();
			}
		});

		p1.add(_ok);
		p2.add(_cancel);

		new Thread(new Runnable() {
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						_rowNum.requestFocus();
					}
				});
			}
		}).start();
		setSize(new Dimension(320, 150));
		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {
		rownum_str = _rowNum.getText();
		colnum_str = new String(_colNum.getText());
		_closedOnOK = true;
		setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		setVisible(false);
	}

}
