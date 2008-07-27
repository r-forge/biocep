package graphics.rmi;

import java.rmi.RemoteException;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class NoDbRegistryAvailableException extends RemoteException {

	public NoDbRegistryAvailableException() {
		super();
	}

	public NoDbRegistryAvailableException(String s, Throwable cause) {
		super(s, cause);
	}

	public NoDbRegistryAvailableException(String s) {
		super(s);
	}
}
