package graphics.rmi;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.servlet.http.HttpServletResponse;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PagePanel;

public class PDFPanel extends PagePanel {

	public static int BUFFER_SIZE=1024*16;
	
	public PDFPanel() {
		setBackground(Color.white);
	}

	File _pdfFile = null;
	boolean _isTempPdfFile = true;

	public File getPDFFile() {
		return _pdfFile;
	}

	public void setPDFContent(byte[] buffer) {

		if (_pdfFile != null && _isTempPdfFile) {
			try {
				_pdfFile.delete();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		_pdfFile = new File(System.getProperty("java.io.tmpdir") + "/pdfview" + System.currentTimeMillis() + ".pdf");
		_isTempPdfFile = true;

		try {
			RandomAccessFile wtraf = new RandomAccessFile(_pdfFile, "rw");
			wtraf.setLength(0);
			wtraf.write(buffer);
			wtraf.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			RandomAccessFile raf = new RandomAccessFile(_pdfFile, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			PDFFile pdffile = new PDFFile(buf);
			// show the first page
			PDFPage page = pdffile.getPage(0);
			showPage(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPDFContent(File pdfFile) {

		_pdfFile = pdfFile;
		_isTempPdfFile = false;

		try {
			RandomAccessFile raf = new RandomAccessFile(_pdfFile, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			PDFFile pdffile = new PDFFile(buf);
			// show the first page
			PDFPage page = pdffile.getPage(0);
			showPage(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void savePDFContent(File f) throws Exception {
		if (f.equals(_pdfFile)) return;		
		InputStream is=null;
		OutputStream os=null;		
		try {
			is = new FileInputStream(_pdfFile);
			os = new FileOutputStream(f);
			byte data[] = new byte[BUFFER_SIZE];
			int count=0;
			while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
				os.write(data, 0, count);
				os.flush();
			}
		} finally {
			try {is.close();}catch (Exception e) {}
			try {os.close();}catch (Exception e) {}
		}
	}
}