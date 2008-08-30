package uk.ac.ebi.microarray.pools.gui;

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
