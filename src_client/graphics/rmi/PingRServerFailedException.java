package graphics.rmi;

public class PingRServerFailedException extends Exception {

	public PingRServerFailedException() {
	}

	public PingRServerFailedException(String message) {
		super(message);
	}

	public PingRServerFailedException(Throwable cause) {
		super(cause);
	}

	public PingRServerFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
