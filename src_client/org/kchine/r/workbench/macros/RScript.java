package org.kchine.r.workbench.macros;

import remoting.RServices;

class RScript implements Script {
	private String script;

	public RScript(String script) {
		this.script = script;
	}

	public void sourceScript(RServices r) throws Exception {
		r.sourceFromBuffer(script);
	}

	public String toString() {
		return script;
	}
}
