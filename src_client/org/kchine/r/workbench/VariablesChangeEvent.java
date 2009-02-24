package org.kchine.r.workbench;


import java.util.HashMap;
import java.util.HashSet;

import org.kchine.r.workbench.macros.Macro;


public class VariablesChangeEvent {	
	HashSet<String> variablesHashSet;
	String originator;
	RGui rgui;
	HashSet<Macro> forbiddenMacros=null;
	HashMap<String, Object> clientProperties;

	public VariablesChangeEvent(HashSet<String> variablesHashSet, String originator, HashMap<String, Object> clientProperties , RGui rgui) {
		super();
		this.variablesHashSet = variablesHashSet;
		this.originator = originator;
		this.clientProperties = clientProperties;
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
	public HashMap<String, Object> getClientProperties() {
		return clientProperties;
	}
	public void setClientProperties(HashMap<String, Object> clientProperties) {
		this.clientProperties = clientProperties;
	}
	
}
