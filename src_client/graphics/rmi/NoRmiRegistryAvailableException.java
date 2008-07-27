package graphics.rmi;

import java.rmi.RemoteException;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class NoRmiRegistryAvailableException extends RemoteException {

	public NoRmiRegistryAvailableException() {

	}

	public NoRmiRegistryAvailableException(String s, Throwable cause) {
		super(s, cause);

	}

	public NoRmiRegistryAvailableException(String s) {
		super(s);

	}

}
