package org.kchine.r.server.graphics.primitive;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

/** object storing the current graphics state */
public class GDState implements Serializable {
	
	public Color col;
	public Color fill;
	public Font f;
	
	public GDState() {
		
	}
			
	public GDState(Color col, Color fill, Font f) {
		super();
		this.col = col;
		this.fill = fill;
		this.f = f;
	}
}
