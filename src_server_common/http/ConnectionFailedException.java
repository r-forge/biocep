package http;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ConnectionFailedException extends TunnelingException {

	public ConnectionFailedException() {
	}

	public ConnectionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConnectionFailedException(String message) {
		super(message);
	}

}
