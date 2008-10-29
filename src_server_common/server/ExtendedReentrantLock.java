package server;

import java.util.concurrent.locks.ReentrantLock;

abstract public class ExtendedReentrantLock extends ReentrantLock {
	abstract public void rawLock();
	abstract public void rawUnlock();
}
