package org.kchine.r.workbench.macros;

import org.kchine.r.server.RServices;

public class  PythonScript implements Script {
	private String script;

	public PythonScript(String script) {
		this.script = script;
	}

	public void sourceScript(RServices r) throws Exception {
		System.out.println("\n"+r.pythonExecFromBuffer(script));
	}

	public String toString() {
		return script;
	}
}
