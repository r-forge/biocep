package org.kchine.r.workbench.macros;

import org.kchine.r.server.RServices;

public class GroovyScript implements Script {
	private String script;

	public GroovyScript(String script) {
		this.script = script;
	}

	public void sourceScript(RServices r) throws Exception {
		System.out.println("\n"+r.groovyExecFromBuffer(script));
	}

	public String toString() {
		return script;
	}
}