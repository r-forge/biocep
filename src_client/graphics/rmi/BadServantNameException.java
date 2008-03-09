package graphics.rmi;

import java.rmi.RemoteException;

public class BadServantNameException extends RemoteException {

	public BadServantNameException() {
		super();
	}

	public BadServantNameException(String s, Throwable cause) {
		super(s, cause);
	}

	public BadServantNameException(String s) {
		super(s);
	}
	
}
