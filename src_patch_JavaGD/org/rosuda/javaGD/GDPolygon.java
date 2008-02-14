package org.rosuda.javaGD;

import java.awt.Component;
import java.awt.Graphics;

public class GDPolygon extends GDObject {
	int n;
	double x[], y[];
	int xi[], yi[];
	boolean isPolyline;

	public GDPolygon(int n, double[] x, double[] y, boolean isPolyline) {
		this.x = x;
		this.y = y;
		this.n = n;
		this.isPolyline = isPolyline;
		int i = 0;
		xi = new int[n];
		yi = new int[n];
		while (i < n) {
			xi[i] = (int) (x[i] + 0.5);
			yi[i] = (int) (y[i] + 0.5);
			i++;
		}
	}

	public void paint(Component c, GDState gs, Graphics g) {
		if (gs.fill != null && !isPolyline) {
			g.setColor(gs.fill);
			g.fillPolygon(xi, yi, n);
			if (gs.col != null)
				g.setColor(gs.col);
		}
		if (gs.col != null) {
			if (isPolyline)
				g.drawPolyline(xi, yi, n);
			else
				g.drawPolygon(xi, yi, n);
		}
	}
}
