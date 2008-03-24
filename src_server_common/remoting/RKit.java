package remoting;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public interface RKit {
	public RServices getR();

	public ReentrantLock getRLock();
}
