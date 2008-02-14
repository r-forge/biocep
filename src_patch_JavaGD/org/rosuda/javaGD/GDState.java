package org.rosuda.javaGD;

import java.awt.Color;
import java.awt.Font;
import java.io.Serializable;

/** object storing the current graphics state */
public class GDState implements Serializable {
	public Color col;
	public Color fill;
	public Font f;
}
