package http;

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
