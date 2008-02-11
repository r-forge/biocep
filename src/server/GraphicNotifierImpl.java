package server;

import graphics.rmi.GraphicNotifier;

import java.awt.Point;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.rosuda.javaGD.GDContainer;

public class GraphicNotifierImpl extends UnicastRemoteObject implements GraphicNotifier {
	public GraphicNotifierImpl() throws RemoteException {
		super();
	}

	public void fireSizeChangedEvent(final int devNr) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().fireSizeChangedEvent(devNr);
	}

	public void registerContainer(GDContainer container) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().registerContainer(container);
	}

	public void executeDevOff(int devNr) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().executeDevOff(devNr);
	}

	public void putLocation(Point p) throws RemoteException {
		DirectJNI.getInstance().getGraphicNotifier().putLocation(p);
	}

}