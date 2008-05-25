package remoting;

import java.rmi.RemoteException;
import java.util.Vector;

public interface GenericCallbackDevice extends RCallBack, RHelpListener, RCollaborationListener{
	public void dispose() throws RemoteException;
	public String getId() throws RemoteException;
	public Vector<RAction> popRActions() throws RemoteException;
}
