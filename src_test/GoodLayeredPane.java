import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import java.awt.event.*;
import java.util.Random;
import java.util.Vector;

public class GoodLayeredPane extends JFrame {
	static JTable table;
	static JPanel controlsLayer;
	public GoodLayeredPane() {

		JLayeredPane lp = new JLayeredPane();
		lp.setPreferredSize(new Dimension(600, 600));

		JPanel tableLayer = new JPanel(new BorderLayout());
		tableLayer.setSize(500, 500);
		// tableLayer.setOpaque(true);
		// JTable t=new JTable(5,4);
		TableModel model=new DefaultTableModel(6, 5);
		
		

		table = new JTable(model);
		
		
		tableLayer.add(table, BorderLayout.CENTER);
		lp.add(tableLayer, new Integer(1));

		controlsLayer = new JPanel(null);
		controlsLayer.setSize(500, 500);
		controlsLayer.setOpaque(false);





		lp.add(controlsLayer, new Integer(2));

		JScrollPane scrollPane = new JScrollPane(lp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(new JScrollPane(lp), BorderLayout.CENTER);
		
		
		

	}

	public static void main(String[] args) {
		JFrame frame = new GoodLayeredPane();
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.pack();
		frame.setVisible(true);
		frame.setSize(new Dimension(400, 400));
		
		new Thread(new Runnable() {
			public void run() {
				
				final Random rnd=new Random(System.currentTimeMillis());
				
				try {
					while (true) {
						
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {

								JButton b = new JButton("Hi!");

								int row = rnd.nextInt(5);
								int col = rnd.nextInt(4);
								Rectangle r1 = table.getCellRect(row, col, true);
								Rectangle r2 = table.getCellRect(row + 1, col + 1, true);

								//System.out.println(r1);
								//System.out.println(r2);

								b.setLocation(r1.x, r1.y);
								b.setSize(Math.abs(r2.x - r1.x), Math.abs(r2.y - r1.y));

								controlsLayer.removeAll();
								controlsLayer.add(b);
								controlsLayer.updateUI();
								controlsLayer.repaint();

								
							}
						});
												
						
						
						
						Thread.sleep(3000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}).start();
	}
}
