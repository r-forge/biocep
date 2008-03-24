package graphics.rmi;

import java.rmi.RemoteException;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
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
