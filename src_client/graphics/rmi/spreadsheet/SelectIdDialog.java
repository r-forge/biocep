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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import uk.ac.ebi.microarray.pools.PoolUtils;

public  class SelectIdDialog extends JDialog {

	private boolean _closedOnOK = false;
	private JComboBox _idCombobox;
	private JButton _ok;
	private JButton _cancel;
	
	private String id_str; 

	public String getId() {
		if (_closedOnOK)
			try {
				return id_str;
			} catch (Exception e) {
				return null;
			}
		else
			return null;
	}

	public SelectIdDialog(Component c, String title, String label, String[] ids) {
		super((Frame) null, true);

		setTitle(title);
		setLocationRelativeTo(c);

		getContentPane().setLayout(new GridLayout(1, 2));
		((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel p1 = new JPanel();
		p1.setLayout(new GridLayout(0, 1));
		getContentPane().add(p1);
		JPanel p2 = new JPanel();
		p2.setLayout(new GridLayout(0, 1));
		getContentPane().add(p2);

		
		_idCombobox=new JComboBox(ids);

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
		_idCombobox.addKeyListener(keyListener);

		p2.add(_idCombobox);


		p1.add(new JLabel(label));

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
						_idCombobox.requestFocus();
					}
				});
			}
		}).start();
		setSize(new Dimension(320, 110));
		PoolUtils.locateInScreenCenter(this);

	}

	private void okMethod() {
		id_str = (String)_idCombobox.getSelectedItem();
		_closedOnOK = true;
		setVisible(false);
	}

	private void cancelMethod() {
		_closedOnOK = false;
		setVisible(false);
	}

}
