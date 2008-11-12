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
package graphics.rmi;

import graphics.pop.GDDevice;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.Vector;

import org.rosuda.javaGD.GDObject;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class TempGDDevice implements GDDevice {

	private Dimension _dim = new Dimension(100, 100);

	public void dispose() throws RemoteException {
	}

	public void fireSizeChangedEvent(int w, int h) throws RemoteException {
		_dim = new Dimension(w, h);
	}

	public int getDeviceNumber() throws RemoteException {
		return 0;
	}

	public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
		return null;
	}

	public Dimension getSize() throws RemoteException {
		return _dim;
	}

	public boolean hasGraphicObjects() throws RemoteException {
		return false;
	}

	public boolean hasLocations() throws RemoteException {
		return false;
	}

	public boolean isCurrentDevice() throws RemoteException {
		return true;
	}

	public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException {
		return null;
	}

	public void putLocation(Point2D p) throws RemoteException {
	}

	public void setAsCurrentDevice() throws RemoteException {
	}

	public byte[] getSVG() throws RemoteException {
		return null;
	}
	
	public Vector<String> getSVGAsText() throws RemoteException {
		return null;
	}

	public byte[] getBmp() throws RemoteException {
		return null;
	}

	public byte[] getJpg() throws RemoteException {
		return null;
	}

	public byte[] getPdf() throws RemoteException {
		return null;
	}

	public byte[] getPictex() throws RemoteException {
		return null;
	}

	public byte[] getPng() throws RemoteException {
		return null;
	}

	public byte[] getPostScript() throws RemoteException {
		return null;
	}

	public byte[] getXfig() throws RemoteException {
		return null;
	}
	
	public String getId() throws RemoteException {
		return null;
	}
	
	public boolean isBroadcasted() throws RemoteException {
		return false;
	}
	
	public void broadcast() throws RemoteException {
		
	}
	
}
