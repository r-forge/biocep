package uk.ac.ebi.microarray.pools.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uk.ac.ebi.microarray.pools.PoolUtils;

public class InDialog extends JDialog {
	String[] save;
	String expr_str = null;

	private boolean _closedOnOK = false;
	final JTextField exprs;

	public String getExpr() {
		if (_closedOnOK)
			try {
				return expr_str;
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}

	public InDialog(Component father, String label, String[] expr_save) {
		super(new JFrame(), true);
		
		
		setUndecorated(true);
		
		JPanel p = ((JPanel) getContentPane());
		p.setLayout(new BorderLayout());
		p.setBorder(BorderFactory.createLineBorder(Color.black,3));
		/*
		createRProgressArea[0].setForeground(Color.white);
		createRProgressArea[0].setBackground(new Color(0x00,0x80,0x80));
		createRProgressArea[0].setBorder(BorderFactory.createLineBorder(new Color(0x00,0x80,0x80),3));
		createRProgressArea[0].setEditable(false);		
		createRProgressBar[0].setForeground(Color.white);
		createRProgressBar[0].setBackground(new Color(0x00,0x80,0x80));
		createRProgressBar[0].setIndeterminate(true);
		*/
		
		setLocationRelativeTo(father);
		
		
		save = expr_save;
		
		
		p.add(new JLabel(label), BorderLayout.WEST);

		exprs = new JTextField();
		exprs.setText(save[0]);

		KeyListener keyListener = new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 10) {
					okMethod();
				} 
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}
		};
		exprs.addKeyListener(keyListener);

		p.add(exprs, BorderLayout.CENTER );

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okMethod();
			}
		});

		p.add(ok,BorderLayout.EAST);
		setSize(new Dimension(320, 38));
		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {
		expr_str = exprs.getText();
		save[0] = expr_str;
		_closedOnOK = true;
		setVisible(false);
	}
	
	
}