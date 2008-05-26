package graphics.rmi;

import java.util.concurrent.locks.ReentrantLock;

abstract public class RGuiReentrantLock extends ReentrantLock {	
	abstract public void unlockNoBroadcast();
}
