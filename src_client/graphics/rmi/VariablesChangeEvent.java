package graphics.rmi;

import java.util.HashSet;


public class VariablesChangeEvent {	
	HashSet<String> variablesHashSet;
	String originator;
	RGui rgui;

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
	
}
