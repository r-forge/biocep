package graphics.rmi;

import graphics.pop.GDDevice;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.Vector;

import org.rosuda.javaGD.GDObject;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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

	public Vector<GDObject> popAllGraphicObjects() throws RemoteException {
		return null;
	}

	public void putLocation(Point2D p) throws RemoteException {
	}

	public void setAsCurrentDevice() throws RemoteException {
	}

	public Vector<String> getSVG() throws RemoteException {
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
}
