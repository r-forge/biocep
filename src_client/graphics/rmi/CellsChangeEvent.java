package graphics.rmi;

import net.java.dev.jspreadsheet.CellRange;


public class CellsChangeEvent {
	
	String name;
	CellRange range;
	
	int row,col,height,width;
	String originator;
	RGui rgui;
	
	public CellsChangeEvent(String name, CellRange range, String originator, RGui rgui) {
		super();
		this.name = name;
		this.range = range;
		this.originator = originator;
		this.rgui = rgui;		
	}
	
	public String getName() {
		return name;
	}
	public CellRange getRange() {
		return range;
	}
	public String getOriginator() {
		return originator;
	}
	public RGui getRGui() {
		return rgui;
	}
}
