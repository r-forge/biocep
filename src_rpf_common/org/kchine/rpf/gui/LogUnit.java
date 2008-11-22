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
package org.kchine.rpf.gui;

import javax.swing.text.SimpleAttributeSet;

public class LogUnit {
	private String cmd;
	private String log;
	private SimpleAttributeSet logAttributeSet;
	
	
	public LogUnit(String cmd, String log, SimpleAttributeSet logAttributeSet) {
		super();
		this.cmd = cmd;
		this.log = log;
		this.logAttributeSet = logAttributeSet;
	}
	
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	public SimpleAttributeSet getLogAttributeSet() {
		return logAttributeSet;
	}
	public void setLogAttributeSet(SimpleAttributeSet logAttributeSet) {
		this.logAttributeSet = logAttributeSet;
	}
	
}
