package remoting;

import java.util.concurrent.locks.ReentrantLock;

public interface RKit {
	public RServices getR();
	public ReentrantLock getRLock();	
}
