package net.java.dev.jspreadsheet;

import graphics.rmi.ColorUtil;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import org.kchine.r.workbench.CellsChangeListener;
import org.kchine.r.workbench.RGui;
import org.kchine.r.workbench.macros.DataLinkMacro;
import org.kchine.r.workbench.macros.Macro;
import org.kchine.r.workbench.macros.MacroCellsChangeListener;
import org.kchine.r.workbench.macros.MacroInterface;


/** The class for rendering (displaying) individual Cell objects in a
 * <code>JTable</code>.
 * <p>
 * Because the DefaultTableCellRender is optimized for rendering objects
 * in JTable, this renderer extends that class. SharpCellRenderer can
 * be changed to toggle between displaying the text and value of a formula
 * cell. This was done in a previous version but is not removed.
 * @author Ricky Chin
 * @version $Revision: 1.1 $
 */
class SpreadsheetCellRenderer extends DefaultTableCellRenderer
{
	RGui rgui;
	String name;
   /**
    * Creates a SharpCellRenderer.
    */
   public SpreadsheetCellRenderer(RGui rgui, String name)
   {
      super();
      this.rgui=rgui;
      this.name=name;
   }

   // implements javax.swing.table.TableCellRenderer

   /**
    * Returns the default table cell renderer.
    * @param table  the <code>JTable</code>
    * @param value  the value to assign to the cell at
    *                                   <code>[row, column]</code>
    * @param isSelected true if cell is selected
    * @param hasFocus true if cell has focus
    * @param row  the row of the cell to render
    * @param column the column of the cell to render
    * @return the default table cell renderer
    */
   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
   {
      /* the following is the similar to DefaultTableCellRenderer */
      if (isSelected)
      {
         super.setForeground(table.getSelectionForeground());
         super.setBackground(table.getSelectionBackground());
      }
      else
      {
         super.setForeground(table.getForeground());
         super.setBackground(table.getBackground());
      }

      setFont(table.getFont());

      if (hasFocus)
      {
         setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
         if (table.isCellEditable(row, column))
         {
            super.setForeground(UIManager.getColor("Table.focusCellForeground"));
            super.setBackground(UIManager.getColor("Table.focusCellBackground"));
         }
      }
      else
      {
         setBorder(noFocusBorder);
      }

      /* this method has been changed for formula feature */
      setValue(value, isSelected, hasFocus, row, column);

    	  for (int i=rgui.getMacros().size()-1;i>=0;--i) {
    		  MacroInterface m=rgui.getMacros().elementAt(i);
    		  if (m.getCellsListeners().size()>0) {    			      			  
    			  for (int k=0;k<m.getCellsListeners().size();++k) {
    				  CellsChangeListener cellsListener=m.getCellsListeners().elementAt(k);
    				  if (cellsListener instanceof MacroCellsChangeListener) { 
    					  Color c=((MacroCellsChangeListener)cellsListener).getColor(name,row,column);
		    			  if (c!=null)  {
		    				  if (isSelected) super.setBackground(ColorUtil.darker(c, 0.3)); 
		    				  else super.setBackground(c);
		    			  }
    				  }
    			  }
    		  } else {
    			  break;
    		  }
    	  
      }

      
      //DefaulTableCellRenderer code
      // begin optimization to avoid painting background
      Color back = getBackground();
      boolean colorMatch = (back != null) && (back.equals(table.getBackground())) && table.isOpaque();
      setOpaque(!colorMatch);

      
      // end optimization to aviod painting background
      return this;
   }

   /**
    * Sets the string for the cell being rendered to <code>value</code>.
    * @param value  the string value for this cell; if value is
    *                 <code>null</code> it sets the text value to an empty string
    * @param hasFocus whether cell has focus or not
    * @param isSelected whether cell is selected
    * @param row cell row
    * @param column cell column
    */
   protected void setValue(Object value, boolean hasFocus, boolean isSelected, int row, int column)
   {
      if (value instanceof Cell)
      {
         //we only care about the value, not the formula string
         Cell temp = (Cell) value;
         Object data = temp.getValue();

         if (data instanceof Number)
         {
            //numbers are right justified
            setHorizontalAlignment(JTextField.RIGHT);
         }
         else
         {
            //everything else is left justified
            setHorizontalAlignment(JTextField.LEFT);
         }

         //value to display in table
         setText((data == null) ? "" : data.toString());
      }
      else
      {
         //not cell object so render with toString of that object
         setText((value == null) ? "" : value.toString());
      }
   }
}
