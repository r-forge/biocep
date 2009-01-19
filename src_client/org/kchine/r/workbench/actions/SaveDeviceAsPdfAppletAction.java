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
package org.kchine.r.workbench.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.kchine.r.server.http.frontend.GraphicsServlet;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.graphics.JBufferedImagePanel;
import org.kchine.r.workbench.graphics.JGDPanelPop;
import org.kchine.rpf.PoolUtils;



import java.awt.Component;
import java.io.File;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SaveDeviceAsPdfAppletAction extends AbstractAction {

	RGui _rgui;

	public SaveDeviceAsPdfAppletAction(RGui rgui) {
		super("Save as Applet");
		_rgui = rgui;
	}

	public void actionPerformed(final ActionEvent e) {
		if (_rgui.getRLock().isLocked()) {
			JOptionPane.showMessageDialog(null, "R is busy");
			return;
		}
		final JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(_rgui.getRootComponent());
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			new Thread(new Runnable() {
				public void run() {
					try {
						_rgui.getRLock().lock();
						JGDPanelPop panel = (JGDPanelPop) WorkbenchApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);
						byte[] result = panel.getGdDevice().getPdf();
						
						File selectedFile=chooser.getSelectedFile();
						String name=selectedFile.getName();
						String dir=selectedFile.getParent();
						if (name.indexOf(".")!=-1) name=name.substring(0,name.lastIndexOf("."));

						
						new java.io.File(dir+"/appletlibs/").mkdirs();
						
						PrintWriter htmlWriter= new PrintWriter(new java.io.FileWriter(dir+"/"+name+".html"));
						GraphicsServlet.pdfAppletHtml(htmlWriter, result, true, true);
						htmlWriter.close();
						
						PrintWriter htmlIEWriter= new PrintWriter(new java.io.FileWriter(dir+"/"+name+"_ie"+".html"));
						GraphicsServlet.pdfAppletHtml(htmlIEWriter, result, true,false);
						htmlIEWriter.close();
						
						PrintWriter htmlMozillaWriter= new PrintWriter(new java.io.FileWriter(dir+"/"+name+"_mozilla"+".html"));
						GraphicsServlet.pdfAppletHtml(htmlMozillaWriter, result, false,true);
						htmlMozillaWriter.close();
						
						PrintWriter htmlIndexWriter= new PrintWriter(new java.io.FileWriter(dir+"/"+name+"_index"+".html"));
						htmlIndexWriter.println("<html><head><script language=\"javascript\">if (navigator.appName.indexOf('Microsoft') != -1)"); 
						htmlIndexWriter.println("{location.replace(\""+name+"_ie"+".html"+"\")}"); 
						htmlIndexWriter.println("else {location.replace(\""+name+"_mozilla"+".html"+"\")}");
						htmlIndexWriter.println("</script></head><body></body></html>");						
						htmlIndexWriter.close();
						
						try {
							PoolUtils.cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/kaleidoscope/appletlibs/pdfviewer_unsigned.jar"), new File(dir+"/appletlibs").getAbsolutePath(), PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT, false);
							PoolUtils.cacheJar(new URL("http://biocep-distrib.r-forge.r-project.org/kaleidoscope/appletlibs/PDFRenderer_unsigned.jar"), new File(dir+"/appletlibs").getAbsolutePath(), PoolUtils.LOG_PRGRESS_TO_SYSTEM_OUT, false);
						} catch (Exception e) {
							e.printStackTrace();
						}
							
						
					} catch (Exception ex) {
						ex.printStackTrace();
					} finally {
						_rgui.getRLock().unlock();
					}
				}
			}).start();
		}
	}

	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
