package applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

public class PDFViewer extends JApplet {
	@Override
	public void init() {
		super.init();
		JPanel root = new JPanel(new BorderLayout());
		final PagePanel panel = new PagePanel();
		root.add(panel, BorderLayout.CENTER);
		getContentPane().add(root);
		root.setBackground(Color.white);
		panel.setBackground(Color.white);
		
		System.out.println("--> init");

		new Thread(new Runnable(){
			public void run() {
				try {
					
					String pdf=getParameter("pdf");
					
					long available;
					InputStream is;
					
					
					if (pdf==null || pdf.equals("")) {
						is = this.getClass().getResourceAsStream("openplatform.pdf");
						available=is.available();
					} else {
						is = this.getClass().getResourceAsStream(pdf);
						URLConnection urlC = this.getClass().getResource(pdf).openConnection();
						available = urlC.getContentLength();				
					}
					
					System.out.println("available:"+available);
					
					byte[] buffer = new byte[(int) available];
					for (int i = 0; i < available; ++i)	buffer[i] = (byte) is.read();
					ByteBuffer buf = ByteBuffer.wrap(buffer); buf.rewind();
					PDFFile pdffile = new PDFFile(buf);
					final PDFPage page = pdffile.getPage(0);
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							panel.showPage(page);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			
				
			}
		}).start();

	}
}
