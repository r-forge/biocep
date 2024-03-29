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
package org.kchine.r.workbench.actions;


import java.awt.Component;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.WorkbenchApplet;
import org.kchine.r.workbench.graphics.JGDPanelPop;

/**
 * @author Karim Chine   karim.chine@m4x.org
 */
public class FitDeviceAction extends AbstractAction {
	private GDDevice _device;
	private RGui _rgui;

	public FitDeviceAction(RGui rgui, GDDevice device) {
		super("Fit Device to Panel");
		_device = device;
		_rgui = rgui;
	}

	public void actionPerformed(ActionEvent e) {
		try {

			JGDPanelPop panel = (JGDPanelPop) WorkbenchApplet.getComponentParent((Component) e.getSource(), JGDPanelPop.class);

			panel.fit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	@Override
	public boolean isEnabled() {
		return _rgui.getR() != null;
	}

}
