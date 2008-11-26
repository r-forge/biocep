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
package graphics.rmi.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.kchine.r.workbench.RGui;

import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.JGDPanelPop;
import java.awt.Component;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class SaveDeviceAsSvgAction extends AbstractAction {

	RGui _rgui;

	public SaveDeviceAsSvgAction(RGui rgui) {
		super("Save as SVG");
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
						JGDPanelPop panel = (JGDPanelPop) GDApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);

						Vector<String> result = panel.getGdDevice().getSVGAsText();
						PrintWriter pw = new PrintWriter(new FileWriter(chooser.getSelectedFile()));
						for (int i = 0; i < result.size(); ++i)
							pw.println(result.elementAt(i));
						pw.close();

					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						_rgui.getRLock().unlock();
					}
				}
			}).start();
		}

	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}

/*
 * JGDPanelPop panel = (JGDPanelPop) GDApplet.getComponentParent((Component)
 * e.getSource(), JBufferedImagePanel.class);
 *  // Get a DOMImplementation. DOMImplementation domImpl =
 * GenericDOMImplementation.getDOMImplementation();
 *  // Create an instance of org.w3c.dom.Document. String svgNS =
 * "http://www.w3.org/2000/svg"; Document document =
 * domImpl.createDocument(svgNS, "svg", null);
 * 
 * document.getDocumentElement().setAttribute("viewBox", "0,0," +
 * panel.getWidth() + "," + panel.getHeight());
 *  // Create an instance of the SVG Generator. SVGGraphics2D svgGenerator = new
 * SVGGraphics2D(document);
 *  // Ask the test to render into the SVG Graphics2D implementation.
 * 
 * panel.paintAll(svgGenerator); svgGenerator.setSVGCanvasSize(new
 * Dimension(panel.getWidth(), panel.getHeight()));
 *  // Finally, stream out SVG to the standard output using // UTF-8 encoding.
 * boolean useCSS = true; // we want to use CSS style attributes Writer out =
 * new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile()),
 * "UTF-8"); svgGenerator.stream(out, useCSS); out.close();
 * 
 * DocumentBuilder builder =
 * DocumentBuilderFactory.newInstance().newDocumentBuilder(); Document doc =
 * builder.parse(chooser.getSelectedFile());
 * 
 * ((Element) doc.getElementsByTagName("svg").item(0)).setAttribute("viewBox",
 * "0,0," + panel.getWidth() + "," + panel.getHeight()); ;
 * 
 * DOMSource source = new DOMSource(doc); StreamResult result = new
 * StreamResult(new FileOutputStream(chooser.getSelectedFile()));
 * 
 * TransformerFactory transFactory = TransformerFactory.newInstance();
 * Transformer transformer = transFactory.newTransformer();
 * 
 * transformer.transform(source, result);
 * 
 */