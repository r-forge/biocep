/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
 *  
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
package graphics.rmi;

import java.awt.Graphics;

import javax.swing.JSplitPane;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class JSplitPaneExt extends JSplitPane {
	protected boolean m_fIsPainted = false;
	protected double m_dProportionalLocation = -1;

	public JSplitPaneExt() {
		super();
	}

	public JSplitPaneExt(int iOrientation) {
		super(iOrientation);
	}

	protected boolean hasProportionalLocation() {
		return (m_dProportionalLocation != -1);
	}

	public void cancelDividerProportionalLocation() {
		m_dProportionalLocation = -1;
	}

	public void setDividerLocation(double dProportionalLocation) {
		if (dProportionalLocation < 0 || dProportionalLocation > 1)
			throw new IllegalArgumentException("Illegal value for divider location: " + dProportionalLocation);
		m_dProportionalLocation = dProportionalLocation;
		if (m_fIsPainted)
			super.setDividerLocation(m_dProportionalLocation);
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (hasProportionalLocation())
			super.setDividerLocation(m_dProportionalLocation);
		m_fIsPainted = true;
		cancelDividerProportionalLocation();
	}
}
