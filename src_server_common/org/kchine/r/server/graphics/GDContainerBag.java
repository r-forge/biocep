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
package org.kchine.r.server.graphics;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.rmi.RemoteException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.kchine.r.server.graphics.action.GDCloseDisplay;
import org.kchine.r.server.graphics.action.GDReset;
import org.kchine.r.server.graphics.action.GDSetGFont;
import org.kchine.r.server.graphics.action.GDSyncDisplay;
import org.kchine.r.server.graphics.primitive.GDObject;
import org.kchine.r.server.graphics.primitive.GDState;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GDContainerBag implements GDContainer {

	private Vector<GDObject> _actions = new Vector<GDObject>();
	private Dimension _size = null;
	private GDState _gs;
	private int _devNr = -1;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(GDContainerBag.class);

	public GDContainerBag(int w, int h) {
		// System.out.println("GDContainerBag");
		_size = new Dimension(w, h);
		_gs = new GDState();
		_gs.f = new Font(null, 0, 12);
	}

	synchronized public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) {
		// System.out.println("popAllGraphicObjects");
		if (_actions.size() == 0)
			return null;
		Vector<GDObject> result = (Vector<GDObject>) _actions.clone();
		if (maxNbrGraphicPrimitives!=-1 && result.size() > maxNbrGraphicPrimitives) {
			int delta = result.size() - maxNbrGraphicPrimitives;
			for (int i = 0; i < delta; ++i) {
				result.remove(result.size() - 1);
			}
		}
		for (int i = 0; i < result.size(); ++i)
			_actions.remove(0);
		return result;
	}

	public boolean hasGraphicObjects() {
		return _actions.size() > 0;
	}

	public Dimension getSize() throws RemoteException {
		return _size;
	}

	public void setSize(int w, int h) {
		_size = new Dimension(w, h);
	}

	public void add(GDObject o) throws RemoteException {
		_actions.add(o);
	}

	public void closeDisplay() throws RemoteException {
		_actions.add(new GDCloseDisplay());
	}

	public int getDeviceNumber() throws RemoteException {
		return _devNr;
	}

	public Font getGFont() throws RemoteException {
		return _gs.f;
	}

	public FontMetrics getGFontMetrics() throws RemoteException {
		return null;
	}

	synchronized public void reset() throws RemoteException {
		_actions = new Vector<GDObject>();
		_actions.add(new GDReset());
	}

	public void setDeviceNumber(int dn) throws RemoteException {
		if (_devNr == -1)
			_devNr = dn;
		// _actions.add(new GDSetDeviceNumber(dn));
	}

	public void setGFont(Font f) throws RemoteException {
		_gs.f = f;
		_actions.add(new GDSetGFont(f));
	}

	public void syncDisplay(boolean finish) throws RemoteException {
		_actions.add(new GDSyncDisplay(finish));
	}

}
