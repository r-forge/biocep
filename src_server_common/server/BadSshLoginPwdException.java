package server;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class BadSshLoginPwdException extends Exception {

	public BadSshLoginPwdException() {
		super();
	}

	public BadSshLoginPwdException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadSshLoginPwdException(String message) {
		super(message);
	}

	public BadSshLoginPwdException(Throwable cause) {
		super(cause);
	}

}
