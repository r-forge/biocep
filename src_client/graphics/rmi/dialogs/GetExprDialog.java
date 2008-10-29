package graphics.rmi.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
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

public class GetExprDialog extends JDialog {
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

	public GetExprDialog(Component father, String label, String[] expr_save) {
		super(new JFrame(), true);
		save = expr_save;
		setLocationRelativeTo(father);
		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		p1.add(new JLabel(label));

		exprs = new JTextField();
		exprs.setText(save[0]);

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
		exprs.addKeyListener(keyListener);

		p2.add(exprs);

		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okMethod();
			}
		});

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelMethod();
			}
		});

		p1.add(ok);
		p2.add(cancel);

		setSize(new Dimension(410, 120));

		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {
		expr_str = exprs.getText();
		save[0] = expr_str;
		_closedOnOK = true;
		setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		setVisible(false);
	}

}