package org.kchine.r.workbench.macros;

import java.util.Vector;

import org.kchine.r.workbench.CellsChangeListener;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.VariablesChangeListener;


public interface MacroInterface {
	public void sourceAll(final RGui rgui) ;
	public String[] getProbes() ;
	public void setEnabled(boolean enabled);
	boolean isShow();
	String getLabel();
	public Vector<VariablesChangeListener> getVarsListeners();
	public Vector<CellsChangeListener> getCellsListeners();

}
