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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import org.rosuda.javaGD.GDContainer;
import org.rosuda.javaGD.GDObject;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
