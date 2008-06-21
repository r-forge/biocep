package applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

public class PDFViewer extends JApplet {
	
	JFrame externalFrame=null;
	
	class ShowPDFThread extends Thread {
		private PagePanel _panel;
		private URL _pdfUrl;		
		URL loadingUrl=PDFViewer.class.getResource("/applet/Loading.pdf");
		
		public ShowPDFThread(PagePanel panel, URL pdfUrl) {
			_panel=panel;
			_pdfUrl=pdfUrl;
		}
		
		@Override
		public void run() {
			MouseListener ml=_panel.getMouseListeners()[0];
			_panel.removeMouseListener(ml);
			try {
				long available;
				InputStream is;														
				available = loadingUrl.openConnection().getContentLength();							
				is = loadingUrl.openStream();				
				byte[] buffer = new byte[(int) available];
				for (int i = 0; i < available; ++i)	buffer[i] = (byte) is.read();
				ByteBuffer buf = ByteBuffer.wrap(buffer); buf.rewind();
				PDFFile pdffile = new PDFFile(buf);
				final PDFPage page = pdffile.getPage(0);
				
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						_panel.showPage(page);
					}
				});
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			try {
				long available;
				InputStream is;
				
				System.out.println("pdf url:"+_pdfUrl);
				available = _pdfUrl.openConnection().getContentLength();							
				is = _pdfUrl.openStream();				
				byte[] buffer = new byte[(int) available];
				for (int i = 0; i < available; ++i)	buffer[i] = (byte) is.read();
				ByteBuffer buf = ByteBuffer.wrap(buffer); buf.rewind();
				PDFFile pdffile = new PDFFile(buf);
				final PDFPage page = pdffile.getPage(0);
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						_panel.showPage(page);
					}
				});					
			} catch (Exception e) {
				e.printStackTrace();
			}
			_panel.addMouseListener(ml);
		}
	}
	@Override
	public void init() {
		super.init();
		JPanel root = new JPanel(new BorderLayout());
		final PagePanel panel = new PagePanel();
		root.add(panel, BorderLayout.CENTER);
		getContentPane().add(root);
		root.setBackground(Color.white);
		panel.setBackground(Color.white);		
		panel.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
				showExternalFrame( this.getClass().getResource(getParameter("pdf")));
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
		new ShowPDFThread(panel, this.getClass().getResource(getParameter("pdf"))).start();
	}
	
	public void showExternalFrame(URL url) {
		if (externalFrame==null) {
			externalFrame = new JFrame("");
	        PagePanel panel = new PagePanel();
	        externalFrame.getContentPane().setBackground(Color.white);
			panel.setBackground(Color.white);		
	        
			externalFrame.add(panel);
			externalFrame.pack();
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			externalFrame.setLocation(80,80);
			externalFrame.setSize(screenDim.width-2*80, screenDim.height-2*80);
	    	new ShowPDFThread(panel, url).start();
		}
		externalFrame.setVisible(true);
	}
	
		@Override
		public void destroy() {
			super.destroy();
			if (externalFrame!=null) {
				externalFrame.setVisible(false);
				externalFrame=null;
			}
			
		}


}
