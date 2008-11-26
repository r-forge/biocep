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
package views;


import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.kchine.r.workbench.RGui;
import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.gui.ConsolePanel;
import org.kchine.rpf.gui.SubmitInterface;


public class UnsafeEvaluatorView extends DynamicView {
	ConsolePanel _consolePanel;
	RGui _rgui;

	public UnsafeEvaluatorView(String title, Icon icon, int id, RGui rgui) {
		super(title, icon, new JPanel(), id);
		_rgui = rgui;
		((JPanel) getComponent()).setLayout(new BorderLayout());
		_consolePanel = new ConsolePanel(new SubmitInterface() {
			public String submit(final String expression) {
				try {
					return _rgui.getR().unsafeGetObjectAsString(expression)+"\n";
				} catch (Exception e) {
					return PoolUtils.getStackTraceAsString(e);
				}
			}
		}, "R Expression", Color.red, true, null);
		((JPanel) getComponent()).add(_consolePanel);
	}

	public ConsolePanel getConsolePanel() {
		return _consolePanel;
	}
}
