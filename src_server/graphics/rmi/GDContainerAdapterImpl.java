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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import org.rosuda.javaGD.GDContainer;
import org.rosuda.javaGD.GDObject;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GDContainerAdapterImpl extends UnicastRemoteObject implements GDContainer {

	private GDContainer _gdContainer;

	public GDContainerAdapterImpl(GDContainer gdContainer) throws RemoteException {
		super();
		_gdContainer = gdContainer;
		// System.out.println(_gdContainer.getClass().getName());
	}

	public void add(GDObject o) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.add(o);
		else
			throw new RemoteException("No GD Container connected");
	}

	public void closeDisplay() throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.closeDisplay();
		else
			throw new RemoteException("No GD Container connected");
	}

	public int getDeviceNumber() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getDeviceNumber();
		else
			throw new RemoteException("No GD Container connected");
	}

	public void setGFont(Font f) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.setGFont(f);
		else
			throw new RemoteException("No GD Container connected");
	}

	public Font getGFont() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getGFont();
		else
			throw new RemoteException("No GD Container connected");
	}

	public FontMetrics getGFontMetrics() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getGFontMetrics();
		else
			throw new RemoteException("No GD Container connected");
	}

	public Dimension getSize() throws RemoteException {
		if (_gdContainer != null)
			return _gdContainer.getSize();
		else
			throw new RemoteException("No GD Container connected");
	}

	public void reset() throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.reset();
		else
			throw new RemoteException("No GD Container connected");

	}

	public void setDeviceNumber(int dn) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.setDeviceNumber(dn);
		else
			throw new RemoteException("No GD Container connected");
	}

	public void syncDisplay(boolean finish) throws RemoteException {
		if (_gdContainer != null)
			_gdContainer.syncDisplay(finish);
		else
			throw new RemoteException("No GD Container connected");
	}
}
