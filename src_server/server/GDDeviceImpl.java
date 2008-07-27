/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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
package server;

import graphics.pop.GDDevice;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;

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

	public Vector<org.rosuda.javaGD.GDObject> popAllGraphicObjects() throws RemoteException {
		return _localGdDevice.popAllGraphicObjects();
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
						e.printStackTrace();
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

	public Vector<String> getSVG() throws RemoteException {
		return _localGdDevice.getSVG();
	}

	public byte[] getBmp() throws RemoteException {
		return _localGdDevice.getBmp();
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