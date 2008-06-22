package remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RConsoleActionListener extends Remote {
	void rConsoleActionPerformed(RConsoleAction consoleAction) throws RemoteException;
}
