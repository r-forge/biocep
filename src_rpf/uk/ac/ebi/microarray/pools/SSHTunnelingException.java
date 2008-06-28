package uk.ac.ebi.microarray.pools;

import java.rmi.RemoteException;

public class SSHTunnelingException extends RemoteException{
	public SSHTunnelingException() {
		super();
	}
	public SSHTunnelingException(String s, Throwable cause) {
		super(s, cause);
	}
	public SSHTunnelingException(String s) {
		super(s);
	}
}
