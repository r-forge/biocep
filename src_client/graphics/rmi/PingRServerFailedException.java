package graphics.rmi;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
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
