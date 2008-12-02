package org.kchine.r.workbench.macros;

import org.kchine.r.server.RServices;

public interface Script {
	public void sourceScript(RServices r) throws Exception;
};