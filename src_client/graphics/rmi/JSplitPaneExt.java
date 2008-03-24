/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
 * Copyright (C) 2007 - 2008  Karim Chine
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package graphics.rmi;

import java.awt.Graphics;

import javax.swing.JSplitPane;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
