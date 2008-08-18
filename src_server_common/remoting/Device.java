package remoting;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Vector;

public interface Device extends Remote {
	public void dispose() throws RemoteException;
	public String getId() throws RemoteException;
	public Vector<RAction> popRActions(int MAX_NBR_RACTION_ON_POP) throws RemoteException;
}
