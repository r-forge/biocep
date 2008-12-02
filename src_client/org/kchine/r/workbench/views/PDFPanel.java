/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kchine.r.workbench.views;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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