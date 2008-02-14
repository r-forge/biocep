package org.rosuda.javaGD;

import java.awt.Component;
import java.awt.Graphics;

public class GDCircle extends GDObject {
	double x, y, r;

	public GDCircle(double x, double y, double r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		if (gs.fill != null) {
			g.setColor(gs.fill);
			g.fillOval((int) (x - r + 0.5), (int) (y - r + 0.5), (int) (r + r + 1.5), (int) (r + r + 1.5));
			if (gs.col != null)
				g.setColor(gs.col);
		}
		if (gs.col != null)
			g.drawOval((int) (x - r + 0.5), (int) (y - r + 0.5), (int) (r + r + 1.5), (int) (r + r + 1.5));
	}
}