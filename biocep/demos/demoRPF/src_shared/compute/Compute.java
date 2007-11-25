package compute;

import java.rmi.RemoteException;

import uk.ac.ebi.microarray.pools.ManagedServant;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public interface Compute extends ManagedServant {
	<T> T executeTask(Task<T> t) throws RemoteException;
}