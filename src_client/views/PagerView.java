package views;

import graphics.rmi.RGui;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.File;
import java.io.RandomAccessFile;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class PagerView extends DynamicView {
	RGui _rgui;
	public PagerView(String title, Icon icon, int id, RGui rgui, String file, byte[] content,String header, boolean deleteFile ) {		
		super(title, icon, new JPanel(), id);
		_rgui=rgui;
		((JPanel) getComponent()).setLayout(new BorderLayout());
		
		JEditorPane pane = null;
		try {
			File f=new File(System.getProperty("java.io.tmpdir")+"/"+file);
			System.out.println(f.toURL());
			RandomAccessFile raf=new RandomAccessFile(f, "rw");
			raf.setLength(0);
			raf.write(content);
			raf.close();			
			pane=new JEditorPane(f.toURL());
			pane.setFont(new Font("Monospaced", Font.PLAIN, 12));

			pane.setEditable(false);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		((JPanel) getComponent()).add(new JScrollPane(pane));
	}

}
