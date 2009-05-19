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
package org.kchine.r.server.impl;

import org.kchine.r.server.graphics.utils.Dimension;
import org.kchine.r.server.graphics.utils.Point2D;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;
import org.kchine.r.server.DirectJNI;
import org.kchine.r.server.graphics.GDDevice;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class GDDeviceImpl extends UnicastRemoteObject implements GDDevice {
	GDDevice _localGdDevice = null;
	HashMap<Integer, GDDevice> _deviceHashMap;

	private static int _gdDeviceCounter=0;
	private String _id="device_"+(_gdDeviceCounter++);
	private static int _port=System.getProperty("rmi.port.start")!=null && !System.getProperty("rmi.port.start").equals("") ? 1+Integer.decode(System.getProperty("rmi.port.start")) : 0;
	
	public GDDeviceImpl(int w, int h, boolean broadcasted, HashMap<Integer, GDDevice> deviceHashMap) throws RemoteException {
		super(_port);
		_localGdDevice = new DirectJNI.GDDeviceLocal(w, h, broadcasted);
		_deviceHashMap = deviceHashMap;
		_deviceHashMap.put(_localGdDevice.getDeviceNumber(), this);
	}

	public Vector<org.kchine.r.server.graphics.primitive.GDObject> popAllGraphicObjects(int maxNbrGraphicPrimitives) throws RemoteException {
		return _localGdDevice.popAllGraphicObjects(maxNbrGraphicPrimitives);
	};
	
	public byte[] popAllGraphicObjectsSerialized(int maxNbrGraphicPrimitives) throws RemoteException {
		return _localGdDevice.popAllGraphicObjectsSerialized(maxNbrGraphicPrimitives);
	};

	public boolean hasGraphicObjects() throws RemoteException {
		return _localGdDevice.hasGraphicObjects();
	}

	public void fireSizeChangedEvent(int w, int h) throws RemoteException {
		_localGdDevice.fireSizeChangedEvent(w, h);
	};

	public void dispose() throws RemoteException {
		final int deviceNbr = _localGdDevice.getDeviceNumber();
		_localGdDevice.dispose();
		new Thread(new Runnable() {

			public void run() {
				boolean shutdownSucceeded = false;
				while (true) {
					try {
						shutdownSucceeded = unexportObject(GDDeviceImpl.this, false);
					} catch (Exception e) {
						//TODO: Something to fix here
						//e.printStackTrace();
						shutdownSucceeded = true;
					}
					System.out.println("-----shutdownSucceeded:" + shutdownSucceeded);
					if (shutdownSucceeded) {
						_deviceHashMap.remove(deviceNbr);
						break;
					}
					try {
						Thread.sleep(200);
					} catch (Exception e) {
					}
				}
			}
		}).start();

	};

	public int getDeviceNumber() throws RemoteException {
		return _localGdDevice.getDeviceNumber();
	}

	public boolean isCurrentDevice() throws RemoteException {
		return _localGdDevice.isCurrentDevice();
	}

	public void setAsCurrentDevice() throws RemoteException {
		_localGdDevice.setAsCurrentDevice();
	}

	public Dimension getSize() throws RemoteException {
		return _localGdDevice.getSize();
	}

	public void putLocation(Point2D p) throws RemoteException {
		_localGdDevice.putLocation(p);
	}

	public boolean hasLocations() throws RemoteException {
		return _localGdDevice.hasLocations();
	}

	public Point2D[] getRealPoints(Point2D[] points) throws RemoteException {
		return _localGdDevice.getRealPoints(points);
	}
	
	public byte[] getSvg() throws RemoteException {
		return _localGdDevice.getSvg();
	}	
	public byte[] getSvg(Integer width, Integer height, Boolean onefile, String bg, String pointsize) throws RemoteException{
		return _localGdDevice.getSvg(width, height, onefile, bg, pointsize);
	}

	public byte[] getPostscript() throws RemoteException {
		return _localGdDevice.getPostscript();
	}
	public byte[] getPostscript(Boolean onefile, String family, String title, String[] fonts, String encoding, String bg, String fg,
			Integer width, Integer height, Boolean horizontal, Integer pointsize, String paper , Boolean pagecentre, String colormodel) throws RemoteException {
		return _localGdDevice.getPostscript(onefile, family, title, fonts, encoding, bg, fg, width, height, horizontal, pointsize, paper, pagecentre, colormodel);
	}
	
	public byte[] getPdf() throws RemoteException {
		return _localGdDevice.getPdf();
	}
	public byte[] getPdf(Integer width, Integer height, Boolean onefile, String family, String title, String[] fonts, String version, String paper, 
			String encoding, String bg, String fg, Integer pointsize, Boolean pagecentre, String colormodel, Boolean useDingbats) throws RemoteException {
		return _localGdDevice.getPdf(width, height, onefile, family, title, fonts, version, paper, encoding, bg, fg, pointsize, pagecentre, colormodel, useDingbats);
	}
	
	public byte[] getPictex() throws RemoteException {
		return _localGdDevice.getPictex();
	}
	public byte[] getPictex(Integer width, Integer height, Boolean debug, String bg, String fg) throws RemoteException {
		return _localGdDevice.getPictex(width, height, debug, bg, fg);
	}
	
	public byte[] getBmp() throws RemoteException {
		return _localGdDevice.getBmp();
	}
	public byte[] getBmp(Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException {
		return _localGdDevice.getBmp(width, height, units, pointsize, bg, res);
	}
		     	     
	public byte[] getJpeg() throws RemoteException {
		return _localGdDevice.getJpeg();
	}
	
	public byte[] getJpeg(Integer width, Integer height, String units, Integer pointsize, Integer quality, String bg,Integer res) throws RemoteException  {
		return _localGdDevice.getJpeg(width, height, units, pointsize, quality, bg, res);
	}
	
	public byte[] getPng() throws RemoteException {
		return _localGdDevice.getPng();
	}
	public byte[] getPng(Integer width, Integer height, String units, Integer pointsize, String bg,Integer res) throws RemoteException {
		return _localGdDevice.getPng(width, height, units, pointsize, bg, res);
	}

	public byte[] getTiff() throws RemoteException {
		return _localGdDevice.getTiff();
	}
	
	public byte[] getTiff(Integer width, Integer height, String units, Integer pointsize, String compression, String bg,Integer res) throws RemoteException {
		return _localGdDevice.getTiff(width, height, units, pointsize, compression, bg, res);
	}
	
	public byte[] getXfig() throws RemoteException {
		return _localGdDevice.getXfig();
	}
	
	public byte[] getXfig( Boolean onefile, String encoding , String paper, Boolean horizontal, 
			Integer width, Integer height, String family , Integer pointsize, String bg, String fg, Boolean pagecentre) throws RemoteException {
		return _localGdDevice.getXfig(onefile, encoding, paper, horizontal, width, height, family, pointsize, bg, fg, pagecentre);
	}
	
	public byte[] getWmf(boolean useserver) throws RemoteException {
		return _localGdDevice.getWmf(useserver);
	}
	
	public byte[] getEmf(boolean useserver) throws RemoteException {
		return _localGdDevice.getEmf(useserver);
	}
	
	public byte[] getOdg() throws RemoteException {
		return _localGdDevice.getOdg();
	}
	
	public byte[] getFromImageIOWriter(String format) throws RemoteException {
		return _localGdDevice.getFromImageIOWriter(format);
	}	
	
/*
	public byte[] getSVG() throws RemoteException {
		return _localGdDevice.getSVG();
	}
	public Vector<String> getSVGAsText() throws RemoteException {
		return _localGdDevice.getSVGAsText();
	}

	public byte[] getBmp() throws RemoteException {
		return _localGdDevice.getBmp();
	}
	
	public byte[] getWmf(boolean useServer) throws RemoteException {
		return _localGdDevice.getWmf(useServer);
	}
	
	public byte[] getEmf(boolean useServer) throws RemoteException {
		return _localGdDevice.getEmf(useServer);
	}
	
	public byte[] getOdg() throws RemoteException {
		return _localGdDevice.getOdg();
	}
	
	public byte[] getJpg() throws RemoteException {
		return _localGdDevice.getJpg();
	}

	public byte[] getPdf() throws RemoteException {
		return _localGdDevice.getPdf();
	}

	public byte[] getPictex() throws RemoteException {
		return _localGdDevice.getPictex();
	}

	public byte[] getPng() throws RemoteException {
		return _localGdDevice.getPng();
	}

	public byte[] getPostScript() throws RemoteException {
		return _localGdDevice.getPostScript();
	}

	public byte[] getXfig() throws RemoteException {
		return _localGdDevice.getXfig();
	}
*/		
	public String getId() throws RemoteException {
		return _id;
	}
	
	public boolean isBroadcasted() throws RemoteException {
		return _localGdDevice.isBroadcasted();
	}
	
	public void broadcast() throws RemoteException {
		_localGdDevice.broadcast();		
	}
}