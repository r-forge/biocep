package graphics.rmi;

import java.awt.Container;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;


import remoting.RServices;

public interface RGui {	
	public RServices getR();
	public ReentrantLock getRLock();
	public boolean isCollaborativeMode();
	public void synchronizeCollaborators() throws java.rmi.RemoteException;	
	public ConsoleLogger getConsoleLogger();
	public Container createView(JPanel panel,String title);
}
