package graphics.rmi;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.svg.SVGUserAgent;

public class SVGPanel extends JSVGCanvas {

	public SVGPanel() {
	}

	public SVGPanel(SVGUserAgent ua, boolean eventsEnabled, boolean selectableText) {
		super(ua, eventsEnabled, selectableText);
	}

	public void setSVGContent(Vector<String> v) {
		try {
			final String tempFile = System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg";
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			for (int i = 0; i < v.size(); ++i)
				pw.println(v.elementAt(i));
			pw.close();
			setURI(new File(tempFile).toURL().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void setSVGContent(String[] v) {
		try {
			final String tempFile = System.getProperty("java.io.tmpdir") + "/svgview" + System.currentTimeMillis() + ".svg";
			PrintWriter pw = new PrintWriter(new FileWriter(tempFile));
			for (int i = 0; i < v.length; ++i)
				pw.println(v[i]);
			pw.close();
			setURI(new File(tempFile).toURL().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
