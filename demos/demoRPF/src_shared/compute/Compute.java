package compute;

import java.rmi.RemoteException;

import org.kchine.rpf.ManagedServant;

/**
 * @author Karim Chine kchine@m4x.org
 */
public interface Compute extends ManagedServant {
	<T> T executeTask(Task<T> t) throws RemoteException;
}