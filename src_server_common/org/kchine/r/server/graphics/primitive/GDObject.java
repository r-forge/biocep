package org.kchine.r.server.graphics.primitive;

import java.awt.*;
import java.io.Serializable;


/** GDObject is an arbitrary object that can be painted */
public abstract class GDObject implements Serializable {
	public abstract void paint(Component c, GDState gs, Graphics g);
}
