/*
 * Copyright (C) 2007 EMBL-EBI
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
package graphics.pop;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.Vector;

import org.rosuda.javaGD.GDObject;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class BufferDevice implements GDDevice {
	private Dimension _dim;
	private Vector<GDObject> _l;

	public BufferDevice(Vector<GDObject> l, Dimension dim) {
		_l = l;
		_dim = dim;
	}

	public void dispose() throws RemoteException {
	}

	public void fireSizeChangedEvent(int w, int h) throws RemoteException {
	}

	public Dimension getSize() throws RemoteException {
		return _dim;
	}

	public Vector<GDObject> popAllGraphicObjects() throws RemoteException {
		return _l;
	}

	@Override
	public boolean hasGraphicObjects() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void putLocation(Point2D p) throws RemoteException {
	}
	
	public int getDeviceNumber() throws RemoteException {
		return -1;
	}
	
	@Override
	public boolean isCurrentDevice() throws RemoteException {
		return false;
	}
	
	@Override
	public void setAsCurrentDevice() throws RemoteException {
	}

	public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
		return null;
	}

}
