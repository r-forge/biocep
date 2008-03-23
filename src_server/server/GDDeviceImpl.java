package server;

import graphics.pop.GDDevice;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Vector;

public class GDDeviceImpl extends UnicastRemoteObject implements GDDevice {
	GDDevice _localGdDevice = null;
	HashMap<Integer,GDDeviceImpl> _deviceHashMap;

	public GDDeviceImpl(int w, int h, HashMap<Integer,GDDeviceImpl> deviceHashMap) throws RemoteException {
		super();
		_localGdDevice = new DirectJNI.GDDeviceLocal(w, h);
		_deviceHashMap=deviceHashMap;
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
		final int deviceNbr=_localGdDevice.getDeviceNumber();
		_localGdDevice.dispose();
		new Thread(new Runnable() {
			
			public void run() {
				boolean shutdownSucceeded=false;
				while (true) {
					try {
						shutdownSucceeded=unexportObject(GDDeviceImpl.this, false);
					} catch (Exception e) {
						e.printStackTrace();
						shutdownSucceeded=true;
					}
					System.out.println("-----shutdownSucceeded:"+shutdownSucceeded);
					if (shutdownSucceeded) {
						_deviceHashMap.remove(deviceNbr);
						break;
					}
					try {Thread.sleep(200);} catch (Exception e) {}
				}
			}
		}
		).start();
		
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
}