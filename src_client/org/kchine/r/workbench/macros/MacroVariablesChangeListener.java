package org.kchine.r.workbench.macros;

import java.util.Vector;

import org.kchine.r.workbench.VariablesChangeEvent;
import org.kchine.r.workbench.VariablesChangeListener;

public class MacroVariablesChangeListener implements VariablesChangeListener {

	Vector<String> variables;
	boolean enabled = true;
	MacroInterface m;

	public MacroVariablesChangeListener(Vector<String> variables, MacroInterface m) {
		this.variables = variables;
		this.m=m;
	}
	
	public MacroVariablesChangeListener(String[] variables, MacroInterface m) {
		this.variables = new Vector<String>();
		for (int i=0; i<variables.length; ++i) this.variables.add(variables[i]);
		this.m=m;
	}

	public void variablesChanged(VariablesChangeEvent event) {
		if (!enabled)
			return;
		
		//prevent Larsen in collaborative mode
		if (!event.getRGui().getUID().equals(event.getOriginator())) return;
		
		for (int i = 0; i < variables.size(); ++i) {
			if (!event.getVariablesHashSet().contains(variables.elementAt(i)))
				return;
		}
		m.sourceAll(event.getRGui(), event.getFrobiddenMacros());
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String toString() {
		return variables.toString();
	}

}
