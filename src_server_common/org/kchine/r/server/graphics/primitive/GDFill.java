package org.kchine.r.server.graphics.primitive;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;


public class GDFill extends GDObject {
	int col;
	Color gc;

	public GDFill(int col) {
		this.col = col;
		//System.out.println(">> FILL COLOR: "+Integer.toString(col,16));
		if ((col & 0xff000000) == 0)
			gc = null; // opacity=0 -> no color -> don't paint
		else
			gc = new Color(((float) (col & 255)) / 255f, ((float) ((col >> 8) & 255)) / 255f,
					((float) ((col >> 16) & 255)) / 255f, ((float) ((col >> 24) & 255)) / 255f);
		//System.out.println("          "+gc);
	}

	public void paint(Component c, GDState gs, Graphics g) {
		gs.fill = gc;
	}
}