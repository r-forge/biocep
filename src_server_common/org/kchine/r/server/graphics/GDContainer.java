package org.kchine.r.server.graphics;

import java.awt.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.kchine.r.server.graphics.primitive.GDObject;

public interface GDContainer extends Remote {

	public void add(GDObject o) throws RemoteException;

	public void reset() throws RemoteException;

	//public void repaint();
	//public void repaint(long tm);        

	public void syncDisplay(boolean finish) throws RemoteException;

	public void setDeviceNumber(int dn) throws RemoteException;

	public void closeDisplay() throws RemoteException;

	public void setGFont(Font f) throws RemoteException;

	public int getDeviceNumber() throws RemoteException;

	public org.kchine.r.server.graphics.utils.Dimension getContainerSize() throws RemoteException;

	public Font getGFont() throws RemoteException;

	public FontMetrics getGFontMetrics() throws RemoteException;
}
