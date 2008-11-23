package graphics.rmi;

import java.awt.Color;

import net.java.dev.jspreadsheet.CellRange;

public class DataLinkMacro extends Macro{
	
	CellRange range;
	Color color;
	String ssName;
	
	public DataLinkMacro(String ssName,CellRange range, Color color) {
		this.ssName=ssName;
		this.range=range;
		this.color=color;		
	}
	
	public Color getColor(String name,int row,int col) {
		if (color==null || !name.equals(ssName)) return null;
		if (row>=range.getStartRow() && row<=range.getEndRow() && col>=range.getStartCol() && col<=range.getEndCol()) {
			return color;
		} else  {
			return null;
		}
	}

}
