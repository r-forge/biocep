package graphics.rmi;

import java.rmi.RemoteException;

public class NoDbRegistryAvailable extends RemoteException {

	public NoDbRegistryAvailable() {
		super();
	}

	public NoDbRegistryAvailable(String s, Throwable cause) {
		super(s, cause);
	}

	public NoDbRegistryAvailable(String s) {
		super(s);
	}
}
