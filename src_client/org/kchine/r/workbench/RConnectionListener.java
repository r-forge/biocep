package org.kchine.r.workbench;

public interface RConnectionListener {
	void connecting();
	void connected();
	void disconnecting();
	void disconnected();
}
