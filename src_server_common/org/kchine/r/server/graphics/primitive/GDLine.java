package org.kchine.r.server.graphics.primitive;

import java.awt.Component;
import java.awt.Graphics;


public class GDLine extends GDObject {
	double x1, y1, x2, y2;

	public GDLine(double x1, double y1, double x2, double y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		if (gs.col != null)
			g.drawLine((int) (x1 + 0.5), (int) (y1 + 0.5), (int) (x2 + 0.5), (int) (y2 + 0.5));
	}
}
