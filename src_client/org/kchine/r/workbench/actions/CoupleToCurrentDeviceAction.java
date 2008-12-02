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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.graphics.JGDPanelPop;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class CoupleToCurrentDeviceAction extends AbstractAction implements LinkedToPanel {
	private JGDPanelPop _panel;
	private RGui _rgui;

	public CoupleToCurrentDeviceAction(RGui rgui) {
		super("Couple to Current Device");
		_rgui = rgui;
	}

	public void actionPerformed(ActionEvent e) {
		if (_rgui.getCurrentJGPanelPop().isCoupledTo(_panel)) {
			_rgui.getCurrentJGPanelPop().removeCoupledTo(_panel);
		} else {
			_rgui.getCurrentJGPanelPop().addCoupledTo(_panel);
		}
	}

	public boolean isEnabled() {
		return _rgui.getR() != null && _rgui.getCurrentDevice() != _panel.getGdDevice();
	}

	public Object getValue(String key) {
		Object result = super.getValue(key);
		if (key.equals("Name")) {
			if (_rgui.getCurrentJGPanelPop().isCoupledTo(_panel)) {
				result = "Uncouple from Current Device";
			} else {
				result = "Couple with Current Device";
			}
		}
		return result;
	}

	public void setPanel(JGDPanelPop panel) {
		_panel = panel;

	}
}