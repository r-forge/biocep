package org.rosuda.javaGD;

import java.awt.Component;
import java.awt.Graphics;

public class GDClip extends GDObject {
	double x1, y1, x2, y2;

	public GDClip(double x1, double y1, double x2, double y2) {
		double tmp;
		if (x1 > x2) {
			tmp = x1;
			x1 = x2;
			x2 = tmp;
		}
		if (y1 > y2) {
			tmp = y1;
			y1 = y2;
			y2 = tmp;
		}
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		g.setClip((int) (x1 + 0.5), (int) (y1 + 0.5), (int) (x2 - x1 + 1.7), (int) (y2 - y1 + 1.7));
	}
}