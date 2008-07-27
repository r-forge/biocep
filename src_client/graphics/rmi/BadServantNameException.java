package graphics.rmi;

import java.rmi.RemoteException;

/**
 * @author Karim Chine karim.chine@m4x.org
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
