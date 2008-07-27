package server;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class BadSshHostException extends Exception {

	public BadSshHostException() {
	}

	public BadSshHostException(String message) {
		super(message);
	}

	public BadSshHostException(Throwable cause) {
		super(cause);
	}

	public BadSshHostException(String message, Throwable cause) {
		super(message, cause);
	}

}
