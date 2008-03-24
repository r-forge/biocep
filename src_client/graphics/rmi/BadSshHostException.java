package graphics.rmi;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
