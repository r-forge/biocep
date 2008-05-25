package remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RHelpListener extends Remote {
	void help(String pack, String topic) throws RemoteException;
}
