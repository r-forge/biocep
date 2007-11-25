package org.rosuda.javaGD;

import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;

public class GDFont extends GDObject {
	double cex, ps, lineheight;
	int face;
	String family;

	Font font;

	public GDFont(double cex, double ps, double lineheight, int face, String family) {
		//System.out.println(">> FONT(cex="+cex+",ps="+ps+",lh="+lineheight+",face="+face+",\""+family+"\")");
		this.cex = cex;
		this.ps = ps;
		this.lineheight = lineheight;
		this.face = face;
		this.family = family;
		int jFT = Font.PLAIN;
		if (face == 2)
			jFT = Font.BOLD;
		if (face == 3)
			jFT = Font.ITALIC;
		if (face == 4)
			jFT = Font.BOLD | Font.ITALIC;
		if (face == 5)
			family = "Symbol";
		font = new Font(family.equals("") ? null : family, jFT, (int) (cex * ps + 0.5));
	}

	public Font getFont() {
		return font;
	}

	public void paint(Component c, GDState gs, Graphics g) {
		g.setFont(font);
		gs.f = font;
	}
}