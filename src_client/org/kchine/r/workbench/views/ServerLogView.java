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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public  class ServerLogView extends DynamicView {
	JTextArea _area;
	JScrollPane _scrollPane;
	RemoteLogListenerImpl _rll;

	public ServerLogView(String title, Icon icon, int id) {
		super(title, icon, new JPanel(), id);
		_area = new JTextArea();
		_scrollPane = new JScrollPane(_area);

		((JPanel) getComponent()).setLayout(new BorderLayout());
		((JPanel) getComponent()).add(_scrollPane);
		try {
			_rll = new RemoteLogListenerImpl(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JTextArea getArea() {
		return _area;
	}

	public JScrollPane getScrollPane() {
		return _scrollPane;
	}

	public RemoteLogListenerImpl getRemoteLogListenerImpl() {
		return _rll;
	}

	public void recreateRemoteLogListenerImpl() {
		try {
			_rll = new RemoteLogListenerImpl(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

