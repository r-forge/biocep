package org.rosuda.javaGD;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

public class GDColor extends GDObject {
	int col;
	Color gc;

	public GDColor(int col) {
		this.col = col;
		//System.out.println(">> COLOR: "+Integer.toString(col,16));
		if ((col & 0xff000000) == 0)
			gc = null; // opacity=0 -> no color -> don't paint
		else
			gc = new Color(((float) (col & 255)) / 255f, ((float) ((col >> 8) & 255)) / 255f,
					((float) ((col >> 16) & 255)) / 255f, ((float) ((col >> 24) & 255)) / 255f);
		//System.out.println("          "+gc);
	}

	public void paint(Component c, GDState gs, Graphics g) {
		gs.col = gc;
		//System.out.println(" paint > color> (col="+col+") "+gc);
		if (gc != null)
			g.setColor(gc);
	}
}
