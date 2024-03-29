/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
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
package org.kchine.r.workbench.views;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public 	 class LogView extends DynamicView {
	JTextArea _area;

	public LogView(String title, Icon icon, int id) {
		super(title, icon, new JPanel(), id);
		((JPanel) getComponent()).setLayout(new BorderLayout());
		_area = new JTextArea();
		((JPanel) getComponent()).add(_area);
	}

	public JTextArea getArea() {
		return _area;
	}
}