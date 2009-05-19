package org.rosuda.javaGD;

import java.awt.*;
import org.kchine.r.server.graphics.utils.Point2D;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;

import org.kchine.r.server.graphics.GDContainer;
import org.kchine.r.server.graphics.primitive.GDCircle;
import org.kchine.r.server.graphics.primitive.GDClip;
import org.kchine.r.server.graphics.primitive.GDColor;
import org.kchine.r.server.graphics.primitive.GDFill;
import org.kchine.r.server.graphics.primitive.GDFont;
import org.kchine.r.server.graphics.primitive.GDLine;
import org.kchine.r.server.graphics.primitive.GDLinePar;
import org.kchine.r.server.graphics.primitive.GDPolygon;
import org.kchine.r.server.graphics.primitive.GDRect;
import org.kchine.r.server.graphics.primitive.GDText;

/*---- external API: those methods are called via JNI from the GD C code

 public void     gdOpen(int devNr, double w, double h);
 public void     gdActivate();
 public void     gdCircle(double x, double y, double r);
 public void     gdClip(double x0, double x1, double y0, double y1);
 public void     gdClose();
 public void     gdDeactivate();
 public void     gdHold();
 public double[] gdLocator();
 public void     gdLine(double x1, double y1, double x2, double y2);
 public double[] gdMetricInfo(int ch);
 public void     gdMode(int mode);
 public void     gdNewPage(int deviceNumber);
 public void     gdPolygon(int n, double[] x, double[] y);
 public void     gdPolyline(int n, double[] x, double[] y);
 public void     gdRect(double x0, double y0, double x1, double y1);
 public double[] gdSize();
 public double   gdStrWidth(String str);
 public void     gdText(double x, double y, String str, double rot, double hadj);


 -- GDC - manipulation of the current graphics state
 public void gdcSetColor(int cc);
 public void gdcSetFill(int cc);
 public void gdcSetLine(double lwd, int lty);
 public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily);

 -- implementation --*/

public class GDInterface {
	public boolean active = false;

	public boolean open = false;

	int devNr = -1;

	public GDContainer c = null;

	//public LocatorSync ls = null;

	public void gdOpen(double w, double h) {
		open = true;
	}

	public void gdActivate() {
		active = true;
	}

	public void gdCircle(double x, double y, double r) {
		if (c == null)
			return;
		try {
			c.add(new GDCircle(x, y, r));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdClip(double x0, double x1, double y0, double y1) {
		if (c == null)
			return;
		try {
			c.add(new GDClip(x0, y0, x1, y1));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdClose() {
		try {
			if (c != null)
				c.closeDisplay();
			open = false;
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdDeactivate() {
		active = false;
	}

	public void gdHold() {
	}

	public static void putLocatorLocation(Point2D p) {
		try {
			coords.put(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean hasLocations() {
		return coords.size()>0;
	}

	private static ArrayBlockingQueue<Point2D> coords = new ArrayBlockingQueue<Point2D>(200);
	private static Vector<Point2D> savedCoords = null;

	public static void saveLocations() {
		savedCoords = new Vector<Point2D>();
		Iterator<Point2D> iter = coords.iterator();
		while (iter.hasNext()) {
			savedCoords.add(iter.next());
		}
		coords = new ArrayBlockingQueue<Point2D>(200);
	}

	public static void restoreLocations() {
		if (savedCoords != null) {
			for (int i = 0; i < savedCoords.size(); ++i) {
				putLocatorLocation(savedCoords.elementAt(i));
			}
			savedCoords = null;
		}
	}

	public double[] gdLocator() {
		if (c == null)
			return null;
		try {
			Point2D p = coords.poll();
			if (p != null) {
				double[] pos = new double[2];
				pos[0] = p.getX();
				pos[1] = p.getY();
				return pos;
			} else {
				return null;
			}
		} catch (Throwable e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdLine(double x1, double y1, double x2, double y2) {
		if (c == null)
			return;
		try {
			c.add(new GDLine(x1, y1, x2, y2));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public double[] gdMetricInfo(int ch) {
		try {
			double[] res = new double[3];
			double ascent = 0.0, descent = 0.0, width = 8.0;
			if (c != null) {
				FontMetrics fm = c.getGFontMetrics();
				if (fm != null) {
					ascent = (double) fm.getAscent();
					descent = (double) fm.getDescent();
					width = (double) fm.charWidth((ch == 0) ? 77 : ch);
				}
			}
			res[0] = ascent;
			res[1] = descent;
			res[2] = width;
			return res;
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdMode(int mode) {
		if (c != null) {
			try {
				c.syncDisplay(mode == 0);
			} catch (RemoteException e) {
				e.printStackTrace();
				c = null;
				throw new RuntimeException(getStackTraceAsString(e));
			}
		}
	}

	public void gdNewPage() {

		if (c != null) {
			try {
				c.reset();
			} catch (RemoteException e) {
				e.printStackTrace();
				c = null;
				throw new RuntimeException(getStackTraceAsString(e));
			}
		}

	}

	public void gdNewPage(int devNr) { // new API: provides the device Nr.
		try {
			this.devNr = devNr;
			if (c != null) {
				c.reset();
				c.setDeviceNumber(devNr);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdPolygon(int n, double[] x, double[] y) {
		// System.out.println("gdPolygon");
		if (c == null)
			return;
		try {
			c.add(new GDPolygon(n, x, y, false));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdPolyline(int n, double[] x, double[] y) {
		// System.out.println("gdPolyline");
		if (c == null)
			return;
		try {
			c.add(new GDPolygon(n, x, y, true));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdRect(double x0, double y0, double x1, double y1) {

		if (c == null)
			return;
		try {
			c.add(new GDRect(x0, y0, x1, y1));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public double[] gdSize() {
		try {

			double[] res = new double[4];
			double width = 0d, height = 0d;
			if (c != null) {
				width = c.getContainerSize().getWidth();
				height = c.getContainerSize().getHeight();
			}
			res[0] = 0d;
			res[1] = width;
			res[2] = height;
			res[3] = 0;
			return res;
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public double gdStrWidth(String str) {
		try {
			double width = (double) (8 * str.length()); // rough estimate
			if (c != null) { // if canvas is active, we can do better
				FontMetrics fm = c.getGFontMetrics();
				if (fm != null)
					width = (double) fm.stringWidth(str);
			}
			return width;
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdText(double x, double y, String str, double rot, double hadj) {
		if (c == null)
			return;
		try {
			c.add(new GDText(x, y, rot, hadj, str));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	/*-- GDC - manipulation of the current graphics state */
	public void gdcSetColor(int cc) {
		if (c == null)
			return;
		try {
			c.add(new GDColor(cc));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdcSetFill(int cc) {
		if (c == null)
			return;
		try {
			c.add(new GDFill(cc));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdcSetLine(double lwd, int lty) {
		if (c == null)
			return;
		try {
			c.add(new GDLinePar(lwd, lty));
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public void gdcSetFont(double cex, double ps, double lineheight, int fontface, String fontfamily) {
		if (c == null)
			return;
		try {
			GDFont f = new GDFont(cex, ps, lineheight, fontface, fontfamily);
			c.add(f);
			c.setGFont(f.getFont());
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public int getDeviceNumber() {
		try {
			return (c == null) ? devNr : c.getDeviceNumber();
		} catch (RemoteException e) {
			e.printStackTrace();
			c = null;
			throw new RuntimeException(getStackTraceAsString(e));
		}
	}

	public static String getStackTraceAsString(Throwable e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		return sw.getBuffer().toString();
	}
}
