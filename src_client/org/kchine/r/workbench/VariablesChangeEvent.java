package org.kchine.r.workbench;


import java.util.HashSet;

import org.kchine.r.workbench.macros.Macro;


public class VariablesChangeEvent {	
	HashSet<String> variablesHashSet;
	String originator;
	RGui rgui;
	HashSet<Macro> forbiddenMacros=null;

	public VariablesChangeEvent(HashSet<String> variablesHashSet, String originator, RGui rgui) {
		super();
		this.variablesHashSet = variablesHashSet;
		this.originator = originator;
		this.rgui=rgui;
	}
	public HashSet<String> getVariablesHashSet() {
		return variablesHashSet;
	}	
	public String getOriginator() {
		return originator;
	}
	public RGui getRGui() {
		return rgui;
	}
	
	public HashSet<Macro> getFrobiddenMacros() {
		return forbiddenMacros;
	}
	
	public void setFrobiddenMacros(HashSet<Macro> forbiddenMacros) {
		this.forbiddenMacros=forbiddenMacros;
	}
	
	
}
