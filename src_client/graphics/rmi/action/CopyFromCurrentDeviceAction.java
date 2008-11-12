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
package graphics.rmi.action;

import graphics.rmi.JGDPanelPop;
import graphics.rmi.RGui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class CopyFromCurrentDeviceAction extends AbstractAction implements LinkedToPanel {
	private RGui _rgui;
	private JGDPanelPop _panel;

	public CopyFromCurrentDeviceAction(RGui rgui) {
		super("Copy From Current Device");
		_rgui = rgui;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			_rgui.getCurrentJGPanelPop().removeCoupledTo(_panel);
			_rgui.getRLock().lock();
			int desinationDeviceNumber = _panel.getGdDevice().getDeviceNumber();
			int sourceDeviceNumber = _rgui.getCurrentDevice().getDeviceNumber();

			System.out.println(_rgui.getR().consoleSubmit(
					".PrivateEnv$dev.copy(which=" + desinationDeviceNumber + ");" + ".PrivateEnv$dev.set(" + sourceDeviceNumber + ");"));

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			_rgui.getRLock().unlock();
		}

	}

	public boolean isEnabled() {
		return _rgui.getR() != null && _rgui.getCurrentDevice() != _panel.getGdDevice();
	}

	public void setPanel(JGDPanelPop panel) {
		_panel = panel;

	}
}