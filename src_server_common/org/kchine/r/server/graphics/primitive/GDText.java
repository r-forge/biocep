package org.kchine.r.server.graphics.primitive;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;


public class GDText extends GDObject {
	double x, y, r, h;
	String txt;

	public GDText(double x, double y, double r, double h, String txt) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.h = h;
		this.txt = txt;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		if (gs.col != null) {
			double rx = x, ry = y;
			double hc = 0d;
			if (h != 0d) {
				FontMetrics fm = g.getFontMetrics();
				int w = fm.stringWidth(txt);
				hc = ((double) w) * h;
				rx = x - (((double) w) * h);
			}
			int ix = (int) (rx + 0.5), iy = (int) (ry + 0.5);

			if (r != 0d) {
				Graphics2D g2d = (Graphics2D) g;
				g2d.translate(x, y);
				double rr = -r / 180d * Math.PI;
				g2d.rotate(rr);
				if (hc != 0d)
					g2d.translate(-hc, 0d);
				g2d.drawString(txt, 0, 0);
				if (hc != 0d)
					g2d.translate(hc, 0d);
				g2d.rotate(-rr);
				g2d.translate(-x, -y);
			} else
				g.drawString(txt, ix, iy);
		}
	}
}
