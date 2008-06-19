package applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

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
										
					is = this.getClass().getResourceAsStream(pdf);
					URLConnection urlC = this.getClass().getResource(pdf).openConnection();
					available = urlC.getContentLength();				
					
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
	
	
	   public static void setup() throws IOException {
		    
	        //set up the frame and panel
	        JFrame frame = new JFrame("PDF Test");
	        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	        PagePanel panel = new PagePanel();
	        frame.add(panel);
	        frame.pack();
	        frame.setVisible(true);

	        //load a pdf from a byte buffer
	        File file = new File("J:/workspace/distrib/www/pdfviewer/pdfs/test.pdf");
	        RandomAccessFile raf = new RandomAccessFile(file, "r");
	        FileChannel channel = raf.getChannel();
	        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY,
	            0, channel.size());
	        PDFFile pdffile = new PDFFile(buf);

	        // show the first page
	        PDFPage page = pdffile.getPage(0);
	        panel.showPage(page);
	        
	    }

	    public static void main(final String[] args) {
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                try {
	                	PDFViewer.setup();
	                } catch (IOException ex) {
	                    ex.printStackTrace();
	                }
	            }
	        });
	    }
	

}
