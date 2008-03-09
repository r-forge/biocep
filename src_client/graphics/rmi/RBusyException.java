package graphics.rmi;

import java.rmi.RemoteException;

public class RBusyException extends RemoteException {

	public RBusyException() {
		super();
	}

	public RBusyException(String message, Throwable cause) {
		super(message, cause);
	}

	public RBusyException(String message) {
		super(message);
	}


}
