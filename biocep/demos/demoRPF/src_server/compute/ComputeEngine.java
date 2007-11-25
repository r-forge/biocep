package compute;

import java.rmi.RemoteException;

import java.rmi.registry.Registry;
import java.util.Date;

import uk.ac.ebi.microarray.pools.ManagedServantAbstract;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class ComputeEngine extends ManagedServantAbstract implements Compute {

	public ComputeEngine(String name, String prefix, Registry registry) throws RemoteException {
		super(name, prefix, registry);
		System.out.println("ComputeEngine instance has been created");
	}

	public <T> T executeTask(Task<T> t) {
		System.out.println("Executing Task ->" + new Date());
		return t.execute();
	}

	public void die() throws RemoteException {
		System.exit(0);
	}

	public String getLogs() throws RemoteException {
		return null;
	}

	public String ping() throws RemoteException {
		return null;
	}

	public void reset() throws RemoteException {

	}
}
