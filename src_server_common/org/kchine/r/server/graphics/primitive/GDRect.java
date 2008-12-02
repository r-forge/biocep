package org.kchine.r.server.graphics.primitive;

import java.awt.Component;
import java.awt.Graphics;


public class GDRect extends GDObject {
	double x1, y1, x2, y2;

	public GDRect(double x1, double y1, double x2, double y2) {
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
		//System.out.println(">> RECT "+x1+":"+y1+" "+x2+":"+y2);
	}

	public void paint(Component c, GDState gs, Graphics g) {
		//System.out.println(" paint> rect: "+x1+":"+y1+" "+x2+":"+y2);
		int x = (int) (x1 + 0.5);
		int y = (int) (y1 + 0.5);
		int w = (int) (x2 + 0.5) - x;
		int h = (int) (y2 + 0.5) - y;
		if (gs.fill != null) {
			g.setColor(gs.fill);
			g.fillRect(x, y, w + 1, h + 1);
			if (gs.col != null)
				g.setColor(gs.col);
		}
		if (gs.col != null)
			g.drawRect(x, y, w, h);
	}
}