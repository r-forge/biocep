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

	JFrame externalFrame = null;

	class ShowPDFThread extends Thread {
		private PagePanel _panel;
		byte[] _buffer;
		URL loadingUrl = PDFViewer.class.getResource("/applet/Loading.pdf");

		public ShowPDFThread(PagePanel panel, byte[] buffer) {
			_panel = panel;
			_buffer = buffer;
		}

		@Override
		public void run() {
			MouseListener ml = _panel.getMouseListeners()[0];
			_panel.removeMouseListener(ml);
			try {
				long available;
				InputStream is;
				available = loadingUrl.openConnection().getContentLength();
				is = loadingUrl.openStream();
				byte[] buffer = new byte[(int) available];
				for (int i = 0; i < available; ++i)
					buffer[i] = (byte) is.read();
				ByteBuffer buf = ByteBuffer.wrap(buffer);
				buf.rewind();
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

				ByteBuffer buf = ByteBuffer.wrap(_buffer);
				buf.rewind();
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

	byte[] buffer = null;
	@Override
	public void init() {
		super.init();
		JPanel root = new JPanel(new BorderLayout());
		final PagePanel panel = new PagePanel();
		root.add(panel, BorderLayout.CENTER);
		getContentPane().add(root);
		root.setBackground(Color.white);
		panel.setBackground(Color.white);
		
		
		if (getParameter("embedded")!=null && getParameter("embedded").equalsIgnoreCase("true")) {
			int blockNumber=Integer.decode(getParameter("pdfhex.block.number"));
			StringBuffer sb=new StringBuffer();
			for (int i=0; i<blockNumber; ++i) {
				sb.append(getParameter("pdfhex.block."+i));
			}
			buffer=hexToBytes(sb.toString());			
		} else {
			try {
				URL _pdfUrl = this.getClass().getResource(getParameter("pdf"));
				long available;
				InputStream is;
				System.out.println("pdf url:" + _pdfUrl);
				available = _pdfUrl.openConnection().getContentLength();
				is = _pdfUrl.openStream();
				buffer = new byte[(int) available];
				for (int i = 0; i < available; ++i)
					buffer[i] = (byte) is.read();
			} catch (Exception e) {
				e.printStackTrace();
				buffer = new byte[0];
			}
		}

		
		panel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				showExternalFrame(buffer);
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

		new ShowPDFThread(panel, buffer).start();

	}

	public void showExternalFrame(byte[] buffer) {
		if (externalFrame == null) {
			externalFrame = new JFrame("");
			PagePanel panel = new PagePanel();
			externalFrame.getContentPane().setBackground(Color.white);
			panel.setBackground(Color.white);

			externalFrame.add(panel);
			externalFrame.pack();
			Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
			externalFrame.setLocation(80, 80);
			externalFrame.setSize(screenDim.width - 2 * 80, screenDim.height - 2 * 80);

			new ShowPDFThread(panel, buffer).start();
		}
		externalFrame.setVisible(true);
	}

	@Override
	public void destroy() {
		super.destroy();
		if (externalFrame != null) {
			externalFrame.setVisible(false);
			externalFrame = null;
		}
	}
	
	public static final byte[] hexToBytes(String s) throws NumberFormatException, IndexOutOfBoundsException {
		int slen = s.length();
		if ((slen % 2) != 0) {
			s = '0' + s;
		}

		byte[] out = new byte[slen / 2];

		byte b1, b2;
		for (int i = 0; i < slen; i += 2) {
			b1 = (byte) Character.digit(s.charAt(i), 16);
			b2 = (byte) Character.digit(s.charAt(i + 1), 16);
			if ((b1 < 0) || (b2 < 0)) {
				throw new NumberFormatException();
			}
			out[i / 2] = (byte) (b1 << 4 | b2);
		}
		return out;
	}

}
