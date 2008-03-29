package server;

import graphics.pop.GDActionMarker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Vector;

import org.rosuda.javaGD.GDObject;
import org.rosuda.javaGD.GDState;

public class Java2DUtils {
	private static boolean _forceAntiAliasing = true;
	private static GDState _gs=new GDState( Color.black,Color.white,new Font(null, 0, 12));
	static public void paintAll(Graphics2D g, Point o, Dimension dSize, Vector<GDObject> _l) {
		if (_forceAntiAliasing) {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		int i = 0, j = _l.size();
		g.setFont(_gs.f);
		g.setClip(o.x, o.y, dSize.width, dSize.height); // reset
		g.setColor(Color.white);
		g.fillRect(o.x, o.y, dSize.width, dSize.height);
		g.translate(-o.x, -o.y);
		while (i < j) {
			GDObject gdo = (GDObject) _l.elementAt(i++);
			if (gdo instanceof GDActionMarker) {

			} else {
				gdo.paint(null, _gs, g);
			}
		}
	}

	static public BufferedImage getBufferedImage(Point o, Dimension dSize, Vector<GDObject> g2dObjects) {
		BufferedImage bufferedImage = new BufferedImage(dSize.width, dSize.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = bufferedImage.createGraphics();
		Java2DUtils.paintAll(g2d, new Point(0, 0), new Dimension(dSize.width,dSize.height), g2dObjects);				
		g2d.dispose();
		return bufferedImage;
	}
}
