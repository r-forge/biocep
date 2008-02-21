package graphics.rmi;

import java.rmi.RemoteException;

public class BadServantName extends RemoteException {

	public BadServantName() {
		super();
	}

	public BadServantName(String s, Throwable cause) {
		super(s, cause);
	}

	public BadServantName(String s) {
		super(s);
	}
	
}
