package remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RCollaborationListener extends Remote{
	void chat(String sourceSession, String message) throws RemoteException;
	void consolePrint(String sourceSession, String expression, String result) throws RemoteException;
}
