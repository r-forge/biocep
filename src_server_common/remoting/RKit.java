package remoting;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public interface RKit {
	public RServices getR();

	public ReentrantLock getRLock();
}
