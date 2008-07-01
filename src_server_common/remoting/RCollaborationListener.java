package remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RCollaborationListener extends Remote{
	void chat(String sourceUID,String user, String message) throws RemoteException;
	void consolePrint(String sourceUID, String user, String expression, String result) throws RemoteException;
}
