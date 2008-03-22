package graphics.rmi.action;

import graphics.rmi.RGui;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;



/*
import graphics.rmi.GDApplet;
import graphics.rmi.JBufferedImagePanel;
import graphics.rmi.JGDPanelPop;
import java.awt.Component;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.Dimension;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.OutputStreamWriter;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Element;
*/

public class SaveDeviceAsSvgAction extends AbstractAction {

	RGui _rgui;

	public SaveDeviceAsSvgAction(RGui rgui) {
		super("Save as Svg");
		_rgui = rgui;
	}

	//CairoSVG(file = "c:/Rplots2.svg",width = 6, height = 6, onefile = TRUE, bg = "transparent",pointsize = 12)

	public void actionPerformed(final ActionEvent e) {
		new Thread(new Runnable() {
			public void run() {
				try {
					final JFileChooser chooser = new JFileChooser();
					int returnVal = chooser.showOpenDialog(_rgui.getRootComponent());
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						
						/*
						JGDPanelPop panel = (JGDPanelPop) GDApplet.getComponentParent((Component) e.getSource(), JBufferedImagePanel.class);

						// Get a DOMImplementation.
						DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

						// Create an instance of org.w3c.dom.Document.
						String svgNS = "http://www.w3.org/2000/svg";
						Document document = domImpl.createDocument(svgNS, "svg", null);

						document.getDocumentElement().setAttribute("viewBox", "0,0," + panel.getWidth() + "," + panel.getHeight());

						// Create an instance of the SVG Generator.
						SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

						// Ask the test to render into the SVG Graphics2D implementation.

						panel.paintAll(svgGenerator);
						svgGenerator.setSVGCanvasSize(new Dimension(panel.getWidth(), panel.getHeight()));

						// Finally, stream out SVG to the standard output using
						// UTF-8 encoding.
						boolean useCSS = true; // we want to use CSS style attributes
						Writer out = new OutputStreamWriter(new FileOutputStream(chooser.getSelectedFile()), "UTF-8");
						svgGenerator.stream(out, useCSS);
						out.close();

						DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
						Document doc = builder.parse(chooser.getSelectedFile());

						((Element) doc.getElementsByTagName("svg").item(0)).setAttribute("viewBox", "0,0," + panel.getWidth() + "," + panel.getHeight());
						;

						DOMSource source = new DOMSource(doc);
						StreamResult result = new StreamResult(new FileOutputStream(chooser.getSelectedFile()));

						TransformerFactory transFactory = TransformerFactory.newInstance();
						Transformer transformer = transFactory.newTransformer();

						transformer.transform(source, result);
						*/

					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}).start();

	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
