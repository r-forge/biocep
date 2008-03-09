package graphics.rmi;

import java.rmi.RemoteException;

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
