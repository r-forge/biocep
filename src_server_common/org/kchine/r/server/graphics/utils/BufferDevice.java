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
package org.kchine.r.server.graphics.utils;

import org.kchine.r.server.graphics.utils.Dimension;
import org.kchine.r.server.graphics.utils.Point2D;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.Vector;

import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.graphics.primitive.GDObject;

/**
 * @author Karim Chine karim.chine@m4x.org
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

	public Vector<GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException {
		return _l;
	}
	
	public byte[] popAllGraphicObjectsSerialized(int maxNbrGraphicPrimitives) throws RemoteException {
		try {
			ByteArrayOutputStream baos=new ByteArrayOutputStream();		
			new ObjectOutputStream(baos).writeObject(_l);		
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RemoteException("",e);
		}
	}

	public boolean hasGraphicObjects() throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	public void putLocation(Point2D p) throws RemoteException {
	}

	public boolean hasLocations() throws RemoteException {

		return false;
	}

	public int getDeviceNumber() throws RemoteException {
		return -1;
	}

	public boolean isCurrentDevice() throws RemoteException {
		return false;
	}

	public void setAsCurrentDevice() throws RemoteException {
	}

	public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
		return null;
	}

	public Vector<String> getSVGAsText() throws RemoteException {
		return null;
	}

	public Vector<String> getSVGAsText(Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException {
		return null;
	}

	public byte[] getSvg() throws RemoteException {
		return null;
	}

	public byte[] getSvg(Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException {
		return null;
	}

	public byte[] getPostscript() throws RemoteException {
		return null;
	}

	public byte[] getPostscript(Boolean onefile, String family, String title, String[] fonts, String encoding, String bg, String fg, Integer width,
			Integer height, Boolean horizontal, Integer pointsize, String paper, Boolean pagecentre, String colormodel) throws RemoteException {
		return null;
	}

	public byte[] getPdf() throws RemoteException {
		return null;
	}

	public byte[] getPdf(Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version, String paper,
			String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats) throws RemoteException {
		return null;
	}

	public byte[] getPictex() throws RemoteException {
		return null;
	}

	public byte[] getPictex(Integer width, Integer height, Boolean debug, String bg, String fg) throws RemoteException {
		return null;
	}

	public byte[] getBmp() throws RemoteException {
		return null;
	}

	public byte[] getBmp(Integer width, Integer height, String units, Integer pointsize, String bg, Integer res) throws RemoteException {
		return null;
	}

	public byte[] getJpeg() throws RemoteException {
		return null;
	}

	public byte[] getJpeg(Integer width, Integer height, String units, Integer pointsize, Integer quality, String bg, Integer res) throws RemoteException {
		return null;
	}

	public byte[] getPng() throws RemoteException {
		return null;
	}

	public byte[] getPng(Integer width, Integer height, String units, Integer pointsize, String bg, Integer res) throws RemoteException {
		return null;
	}

	public byte[] getTiff() throws RemoteException {
		return null;
	}

	public byte[] getTiff(Integer width, Integer height, String units, Integer pointsize, String compression, String bg, Integer res) throws RemoteException {
		return null;
	}

	public byte[] getXfig() throws RemoteException {
		return null;
	}

	public byte[] getXfig(Boolean onefile, String encoding, String paper, Boolean horizontal, Integer width, Integer height, String family, Integer pointsize,
			String bg, String fg, Boolean pagecentre) throws RemoteException {
		return null;
	}

	public byte[] getWmf(boolean useserver) throws RemoteException {
		return null;
	}

	public byte[] getEmf(boolean useserver) throws RemoteException {
		return null;
	}

	public byte[] getOdg() throws RemoteException {
		return null;
	}

	public byte[] getFromImageIOWriter(String format) throws RemoteException {
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
