/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kchine.r.workbench;

import java.awt.Component;
import java.io.File;
import java.util.Vector;

import org.kchine.r.server.RKit;
import org.kchine.r.server.graphics.GDDevice;
import org.kchine.r.server.scripting.GroovyInterpreter;
import org.kchine.r.workbench.graphics.JGDPanelPop;
import org.kchine.r.workbench.macros.MacroInterface;
import net.infonode.docking.View;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public interface RGui extends RKit {
	
	public ConsoleLogger getConsoleLogger();

	public View createView(Component panel, String title);

	public void setCurrentDevice(GDDevice device);

	public Component getRootComponent();

	public GDDevice getCurrentDevice();

	public JGDPanelPop getCurrentJGPanelPop();
	
	public void upload(File localFile, String fileName) throws Exception;
	
	public GroovyInterpreter getGroovyInterpreter() ;
	
	public String getUserName(); 
	
	public String getUID();
	
	public String getSessionId();
	
	public String getHelpRootUrl();
	
	public String getInstallDir();
	
	public String getPluginsDir();
	
	public Vector<MacroInterface> getMacros();
	
	void pushTask(Runnable task);
	
	public void addVariablesChangeListener(VariablesChangeListener listener);
	public void removeVariablesChangeListener(VariablesChangeListener listener);
	
	public void addCellsChangeListener(CellsChangeListener listener);
	public void removeCellsChangeListener(CellsChangeListener listener);
	
	
}
