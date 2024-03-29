package org.kchine.r.workbench.spreadsheet;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.UUID;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import org.kchine.r.server.RKit;
import org.kchine.r.server.spreadsheet.AbstractSpreadsheetModel;
import org.kchine.r.server.spreadsheet.Cell;
import org.kchine.r.server.spreadsheet.CellPoint;
import org.kchine.r.server.spreadsheet.CellRange;
import org.kchine.r.server.spreadsheet.Debug;
import org.kchine.r.server.spreadsheet.Formula;
import org.kchine.r.server.spreadsheet.History;
import org.kchine.r.server.spreadsheet.ImportInfo;
import org.kchine.r.server.spreadsheet.Node;
import org.kchine.r.server.spreadsheet.SpreadsheetListener;
import org.kchine.r.server.spreadsheet.SpreadsheetSelectionEvent;
import org.kchine.r.server.spreadsheet.SpreadsheetSelectionListener;
import org.kchine.r.server.spreadsheet.SpreadsheetTableModel;
import org.kchine.r.server.spreadsheet.SpreadsheetTableModelBis;
import org.kchine.r.server.spreadsheet.SpreadsheetTableModelClipboardInterface;
import org.kchine.r.workbench.RGui;
import org.kchine.rpf.PoolUtils;


/**
 * An encapsulation of the functionality of the Sharp Tools spreadsheet
 * as a Swing component.
 * @author tonyj
 */
public class JSpreadsheet extends JComponent
{
   private CellPoint copyPoint = new CellPoint(0, 0);
   private Clipboard system = Toolkit.getDefaultToolkit().getSystemClipboard();
   
   private JTable table;
   
   private SpreadsheetTableModelClipboardInterface tableModel;
   /** Holds value of property columnWidth. */
   private int columnWidth;
   private RKit rg;
   
   private JTable rowHeader;   
   private JPanel controlsLayer;
   private String _name;
   
   

   /** Create a new spreadsheet
    * @param columns The number of columns in the spreadsheet
    * @param rows The number of rows in the spreadsheet
    */

   public JSpreadsheet(AbstractTableModel m, RGui rgui, String name)
   {
	   rg=rgui;
	   _name=name;
      table = createTable();

      setLayout(new BorderLayout());
      newTableModel(m, rgui);

      // clobber resizing of all columns
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      // set table editor and renderer to custom ones
      table.setDefaultRenderer(Cell.class, new SpreadsheetCellRenderer( rgui, name));
      table.setDefaultEditor(Cell.class, new SpreadsheetCellEditor(new JTextField()));

      // set selection mode for contiguous  intervals
      table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      table.setCellSelectionEnabled(true);

      MouseListener ml = new HeaderMouseAdapter();

      // we don't allow reordering
      table.getTableHeader().setReorderingAllowed(false);
      table.getTableHeader().addMouseListener(ml);

      // create selection models
      ListSelectionModel rowSelectionModel = table.getSelectionModel();
      ListSelectionModel columnSelectionModel = table.getColumnModel().getSelectionModel();

      // add selection listeners to the selection models
      //rowSelectionModel.addListSelectionListener(this);
      //columnSelectionModel.addListSelectionListener(this);
      
      Dimension tablePreferredSize=table.getPreferredSize();
      tablePreferredSize=new Dimension((int)(tablePreferredSize.getWidth()*1.5), (int)tablePreferredSize.getHeight());
      

      rowHeader = new JTable(new RowModel(table.getModel()));
      TableCellRenderer renderer = new RowHeaderRenderer();
      Component comp = renderer.getTableCellRendererComponent(rowHeader, null, false, false, rowHeader.getRowCount() - 1, 0);
      rowHeader.setIntercellSpacing(new Dimension(0, 0));
      Dimension d = rowHeader.getPreferredScrollableViewportSize();
      d.width = comp.getPreferredSize().width;
      rowHeader.setPreferredScrollableViewportSize(d);
      rowHeader.setRowHeight(table.getRowHeight());
      rowHeader.setDefaultRenderer(Object.class, renderer);
      rowHeader.addMouseListener(ml);

      /*
      JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      add(scrollPane, BorderLayout.CENTER);

      scrollPane.setRowHeaderView(rowHeader);

      JPanel blank = new JPanel();
      blank.addMouseListener(ml);
      scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, blank);
      */

      // initial selection
      //resetSelection();
      table.setRequestFocusEnabled(true);

      //menuBar.setRequestFocusEnabled(false);
      //toolBar.setRequestFocusEnabled(false);
      table.requestFocus();

      Formula.registerFunctions();

      try {
      if (PoolUtils.isMacOs()) {
    	  table.setGridColor(Color.black);
    	  
      } } catch (Exception e) {
    	  e.printStackTrace();
      }
      
      
      
      JLayeredPane lp = new JLayeredPane();
      lp.setPreferredSize(tablePreferredSize);
      
      JPanel tableLayer = new JPanel(new BorderLayout());
      tableLayer.setSize(tablePreferredSize);
      tableLayer.setOpaque(true);
      tableLayer.add(table,BorderLayout.WEST);
      lp.add(tableLayer, new Integer(1));
      
      controlsLayer = new JPanel(null);
      controlsLayer.setSize(tablePreferredSize);            	  
	  controlsLayer.setOpaque(false);
      lp.add(controlsLayer, new Integer(2));
      
      
      
      
      JScrollPane scrollPane = new JScrollPane(lp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      add(scrollPane, BorderLayout.CENTER);
      
      

      scrollPane.setRowHeaderView(rowHeader);
      JPanel blank = new JPanel();
      blank.addMouseListener(ml);
      scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, blank);
      
      
      final DefaultTableColumnModel model = new DefaultTableColumnModel();
      for (int i = 0; i < table.getColumnCount(); i++) {
         TableColumn column = new TableColumn();
         column.setHeaderValue(table.getColumnName(i));
         model.addColumn(column);
      }      
      JTableHeader extraHeader = new JTableHeader(model);
      
      model.addColumnModelListener(new TableColumnModelListener(){
     	 public void columnAdded(TableColumnModelEvent e) {}
     	 public void columnMarginChanged(ChangeEvent e) {
     		 System.out.println(e);    		 
     		  for (int i = 0; i < table.getColumnCount(); i++) {    			  
     			  table.getColumnModel().getColumn(i).setPreferredWidth(model.getColumn(i).getWidth());
     		  }
     		 new Thread(new Runnable(){
     			 public void run() {
     				SwingUtilities.invokeLater(new Runnable(){
     					public void run() {
     						refreshEmbeddedPanelsLayer();     						
     					}
     				});
     				
     			}
     		 }).start();
     	}
     	 public void columnMoved(TableColumnModelEvent e) {}
     	 public void columnRemoved(TableColumnModelEvent e) {}
     	 public void columnSelectionChanged(ListSelectionEvent e) {}
       });
      
      scrollPane.setColumnHeaderView(extraHeader);
      
      new Thread(new Runnable(){
			 public void run() {
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						for (int i = 0; i < table.getColumnCount(); i++) {    			  
							  table.getColumnModel().getColumn(i).setPreferredWidth(model.getColumn(i).getWidth());
						  }     						
					}
				});
				
			}
		 }).start();
      
      

      /*
		new Thread(new Runnable() {
			public void run() {
				
				final Random rnd=new Random(System.currentTimeMillis());
				
				try {
					while (true) {
						
						SwingUtilities.invokeLater(new Runnable(){
							public void run() {

								JButton b = new JButton("Hi!");

								int row = rnd.nextInt(5);
								int col = rnd.nextInt(4);
								Rectangle r1 = table.getCellRect(row, col, true);
								Rectangle r2 = table.getCellRect(row + 1, col + 1, true);

								b.setLocation(r1.x, r1.y);
								b.setSize(Math.abs(r2.x - r1.x), Math.abs(r2.y - r1.y));

								controlsLayer.removeAll();
								controlsLayer.add(b);
								controlsLayer.updateUI();
								controlsLayer.repaint();

								
							}
						});
												
						Thread.sleep(3000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();		
		*/
      
   }

   
   /** Get the cell at the given position
    * @param row Row number relative to <CODE>baseRow</CODE>
    * @param col Column number relative to <CODE>baseCol</CODE>
    * @return The cell at this position
    */
   public Cell getCellAt(int row, int col)
   {
      return tableModel.getCellAt(row, col);
   }

   /** Get the number of columns in the spreadsheet */
   public int getColumnCount()
   {
      return tableModel.getColumnCount();
   }

   /** Set the contents of the spreadsheet. The input string should contain a
    * spreadsheet in comma-delimited format.
    */
   public void setContents_(String text)
   {
      setContents(text, ',');
   }

   /** Sets the contents of the spreadsheet.
    * @param text The text to read
    * @param delim The delimiter used to separate columns in the text
    */
   public void setContents(String text, char delim)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }

      // create new table model
      CellPoint size = SpreadsheetTableModel.getSize(text, delim);
      newTableModel(new DefaultTableModel(size.getRow(), size.getCol()), rg);
      tableModel.fromString(text, delim, 0, 0, new CellRange(0, size.getRow(), 0, size.getCol()));
   }

   /** Get the contents of the spreadsheet in comma-delimited format. */
   public String getContents()
   {
      return getContents(',');
   }

   /** Get the contents of the spreadsheet
    * @param delim The delimiter to use to separate column values
    *
    */
   public String getContents(char delim)
   {
      return tableModel.toString(delim);
   }

   /** Checks to see if it is safe to delete the specified cells
    * @param byRow true if deleting entire rows, false if deleting columns
    * @param start Index of first row/column to delete, relative to <CODE>baseRow</CODE>/<CODE>baseCol</CODE>
    * @param end index of last row/column to delete
    * @return true if it is safe to delete the specified cells
    */
   public boolean isDeletionSafe(boolean byRow, int start, int end)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }

      CellRange range = insertRemoveRange(byRow, start, end);

      return tableModel.isDeletionSafe(range, byRow);
   }

   /** Sets the modified flag for this spreadsheet
    * @param modified The new value for the modified flag
    */
   public void setModified(boolean modified)
   {
      tableModel.setModified(modified);
   }

   /** Test if the spreadsheet is modified
    * @return true if the spreadsheet has been modified.
    */
   public boolean isModified()
   {
      return tableModel.isModified();
   }

   /** Get the number of rows in the spreadsheet */
   public int getRowCount()
   {
      return tableModel.getRowCount();
   }

   /** Set the selection range.
    * @param range The range of cells to be selected
    */
   public void setSelectedRange(CellRange range)
   {
      table.setColumnSelectionInterval(range.getStartCol(), range.getEndCol());
      table.setRowSelectionInterval(range.getStartRow(), range.getEndRow());

      // set it visible
      table.scrollRectToVisible(new Rectangle(table.getCellRect(range.getStartRow(), range.getStartCol(), true)));
   }

   /** Get the current selection range.
    * @return The selected range of cells, or null if no cells are selected.
    */
   public CellRange getSelectedRange()
   {
      if ((table.getSelectedRowCount() != 0) && (table.getSelectedColumnCount() != 0))
      {
         int[] rows = table.getSelectedRows();
         int[] cols = table.getSelectedColumns();
         int minColIndex = 0;
         if (cols[0] < 0)
         {
            if (cols.length == 1)
            {
               return null;
            }
            minColIndex++;
         }

         int minRow = rows[0];
         int maxRow = rows[rows.length - 1];

         //columns selected are in ascending order
         int minCol = cols[minColIndex];
         int maxCol = cols[cols.length - 1];

         return new CellRange(minRow, maxRow, minCol, maxCol);
      }
      else
      {
         return null;
      }
   }

   /** Clears a range of cells
    * @param range The range of cells to be deleted
    */
   public void clear(CellRange range)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }
      tableModel.historyAdd(range);
      Debug.println("Clear");
      tableModel.clearRange(range);
      tableModel.cellsChangeAdd(range);
   }

   /** Perfroms a clipboard function of COPY */
   public void copy()
   {
      doCopy(false); //sets isCut to false
   }

   /** Perfroms a clipboard function of cutting (COPY + CLEAR) */
   public void cut()
   {
      doCopy(true); //sets isCut to true
   }

   /** Fill a range of cells with a given value (or formula)
    * @param range The range to fill
    * @param value The value to fill. Should begin with '=' for a formula.
    */
   public void fill(CellRange range, String value)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }
      tableModel.historyAdd(range);
      tableModel.fillRange(range, value);
      tableModel.cellsChangeAdd(range);
   }

   /** Search for a given cell value
    * @param start The start point for the search
    * @param findValue The value to search for.
    * @param matchCase true if search should be case sensitive
    * @param matchCell true if the entire cell must match the findValue
    * @return The cell where the match was found, or null of no match
    */
   public CellPoint find(CellPoint start, String findValue, boolean matchCase, boolean matchCell)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }

      return tableModel.look(start, SpreadsheetTableModel.fieldParser(findValue), matchCase, matchCell);
   }

   /* insert a row or column */

   /** Insert new rows or columns into the spreadsheet
    * @param byRow true if inserting rows, false if inserting columns
    * @param start The row/column at which to insert (relative to <CODE>baseRow</CODE>/<CODE>baseCol</CODE>)
    * @param end The last row/column to insert
    */
   public void insert(boolean byRow, int start, int end)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }

      CellRange range = insertRemoveRange(byRow, start, end);
      if (byRow)
      {
         tableModel.historyAdd(range, History.INSERTROW);
         tableModel.insertRow(range);
         tableModel.cellsChangeAdd(range);
      }
      else
      {
    	  tableModel.historyAdd(range, History.INSERTCOLUMN);
    	  tableModel.insertColumn(range);
    	  tableModel.cellsChangeAdd(range);
      }
   }

   /** Performs a clipboard function of pasting */
   public void paste()
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }

      //checks if anything is selected	
      if (table.getSelectedRowCount() != 0)
      {
         int startRow = table.getSelectedRow();
         int startCol = table.getSelectedColumn();

         int rowOff = startRow - copyPoint.getRow();
         int colOff = startCol - copyPoint.getCol();

         try
         {
            String trstring = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));

            CellPoint size = SpreadsheetTableModel.getSize(trstring, '\t');
            
            if ( (startRow + size.getRow()) > table.getRowCount()  ||  startCol + size.getCol()>table.getColumnCount()) {
            	Toolkit.getDefaultToolkit().beep();
            }
            
            int endRow = Math.min(table.getRowCount() - 1, (startRow + size.getRow()) - 1);
            int endCol = Math.min(table.getColumnCount() - 1, (startCol + size.getCol()) - 1);
            
            
            
            CellRange affectedRange = new CellRange(startRow, endRow, startCol, endCol);

            // add to history
            tableModel.historyAdd(affectedRange);
            tableModel.fromString(trstring, '\t', rowOff, colOff, affectedRange);
            tableModel.fireSetSelection(null,affectedRange);
            tableModel.cellsChangeAdd(affectedRange);
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
   }

   /** Removes rows or columns from the spreadsheet
    * @param byRow true to delete rows, false to delete columns
    * @param start The first row/column to delete
    * @param end The last row/column to delete
    */
   public void remove(boolean byRow, int start, int end)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }

      CellRange range = insertRemoveRange(byRow, start, end);
      if (byRow)
      {
    	  tableModel.historyAdd(range, History.REMOVEROW);
         Debug.println(range);
         tableModel.removeRow(range);
         
         Debug.println("Delete row range " + range);
         tableModel.cellsChangeAdd(range);
      }
      else
      {
         Debug.println("Delete column range " + range);
         tableModel.historyAdd(range, History.REMOVECOLUMN);
         Debug.println(range);
         tableModel.removeColumn(range);
         tableModel.cellsChangeAdd(range);
                                    
      }
   }

   /** Remove a selection listener
    * @param l The listener to remove
    */
   public void removeSelectionListener(SpreadsheetSelectionListener l)
   {
      listenerList.remove(SpreadsheetSelectionListener.class, l);
   }

   /** Sort a range of cells
    * @param range Teh range of cells to be sorted
    * @param first The first row/column to use as a sort key
    * @param second The second row/column to use as a sort key, or -1 for no additional sorting
    * @param byRow true to sort based on rows, false to sort based on columns
    * @param firstAscending true to sort the first key in ascending order, false for descending
    * @param secondAscending true to sort the second key in ascending order, false for descending
    */
   public void sort(CellRange range, int first, int second, boolean byRow, boolean firstAscending, boolean secondAscending)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }
      tableModel.historyAdd(range);
      if (second < 0)
      {
         second = first;
      }
      tableModel.sort(range, first, second, byRow, firstAscending, secondAscending);
      tableModel.cellsChangeAdd(range);
   }

   /**
    * This translates the int form of column into column string (1 -> 'A')
    *
    * @param column the int value of the column to be converted
    * @return the string value of the column
    */
   public static String translateColumn(int column)
   {
      return Node.translateColumn(column);
   }

   /**
    * This translates the string form of column into column number ('A' -> 1)
    *
    * @param column the string value of the column to be converted
    * @return the int value of the column
    */
   public static int translateColumn(String column)
   {
      return Node.translateColumn(column);
   }

   /**
    * This translates the int value of row into a string (12 -> '12').
    *
    * @param row the int value of the row to be converted
    * @return the string value of the row
    */
   public static String translateRow(int row)
   {
      return Node.translateRow(row);
   }

   /**
    * This translates the string form of row into row number ('12' -> 12).
    *
    * @param row the string value of the row to be converted
    * @return the int value of the row
    */
   public static int translateRow(String row)
   {
      return Node.translateRow(row);
   }

   /** Setter for property columnWidth.
    * @param columnWidth New value of property columnWidth.
    *
    */
   public void setColumnWidth(int columnWidth)
   {
      this.columnWidth = columnWidth;
      applyColumnWidth(columnWidth);
   }

   /** Getter for property columnWidth.
    * @return Value of property columnWidth.
    *
    */
   public int getColumnWidth()
   {
      return columnWidth;
   }

   /** Can be used by subclasses to customize the underlying JTable
    * @return The JTable to be used by the spreadsheet
    */
   protected JTable createTable()
   {
      return new JTable();
   }

   /** Called when firing an event as a result of the selected cells changing. */
   protected void fireSelectionChanged()
   {
      SpreadsheetSelectionListener[] listeners = (SpreadsheetSelectionListener[]) listenerList.getListeners(SpreadsheetSelectionListener.class);
      SpreadsheetSelectionEvent event = new SpreadsheetSelectionEvent(this, getSelectedRange());
      for (int i = 0; i < listeners.length; i++)
      {
         listeners[i].selectionChanged(event);
      }
   }

   //   private void applyBaseColumnWidth()
   //   {
   //      if (SharpTools.baseCol > 0)
   //      {
   //         TableColumn firstColumn = table.getColumnModel().getColumn(SharpTools.baseCol - 1);
   //         int firstColWidth = 25;
   //         if (firstColWidth > 0)
   //         {
   //            firstColumn.setMinWidth(firstColWidth);
   //            firstColumn.setPreferredWidth(firstColWidth);
   //         }
   //      }
   //   }
   private void applyColumnWidth(int colWidth)
   {
      if (colWidth > 0)
      {
         for (int i = 0; i < table.getModel().getColumnCount(); i++)
         {
            TableColumn column = table.getColumnModel().getColumn(i);
            column.setMinWidth(colWidth);
            column.setPreferredWidth(colWidth);
         }
      }
   }

   /**
    * Performs a clipboard function of cut/copy
    *
    * @param isCut true for cut, false for copy
    */
   private void doCopy(boolean isCut)
   {
      CellEditor editor = table.getCellEditor();
      if (editor != null)
      {
         editor.cancelCellEditing();
      }
      if (table.getSelectedRowCount() != 0)
      {
         CellRange range = new CellRange(table.getSelectedRows(), table.getSelectedColumns());

         if (isCut)
         {
        	 tableModel.historyAdd(range);
         }

         // now do the copy operation
         StringBuffer sbf = new StringBuffer();

         int startRow = table.getSelectedRow();
         int startCol = table.getSelectedColumn();

         int numrows = table.getSelectedRowCount();
         int numcols = table.getSelectedColumnCount();

         copyPoint = new CellPoint(table.getSelectedRow(), table.getSelectedColumn());

         String str = tableModel.toString(range, false, '\t');
         StringSelection stsel = new StringSelection(str);
         system.setContents(stsel, stsel);

         if (isCut)
         {
            tableModel.clearRange(range);
         }
         
         tableModel.cellsChangeAdd(range);
         
      }
   }

   private CellRange insertRemoveRange(boolean byRow, int start, int end)
   {
      if (byRow)
      {
         return new CellRange(start, end, 0, tableModel.getColumnCount() - 1);
      }
      else
      {
         return new CellRange(0, tableModel.getRowCount() - 1, start, end);
      }
   }

   /**
    * Creates new blank SharpTableModel object with specified number of
    * rows and columns.  table is set to this table model to update screen.
    *
    * @param rows number of rows in new table model
    * @param cols number of columns in new table model
    */
   
   
   private String ID=null;
   public String getId() {
	   if (ID==null) {
		   ID=UUID.randomUUID().toString();
	   }
	   return ID;	   
   }
   
   private void newTableModel(final AbstractTableModel m, RKit rgui)
   {
	  if (m instanceof AbstractSpreadsheetModel) {
		  tableModel = new SpreadsheetTableModelBis(table,(AbstractSpreadsheetModel)m,rgui);
	  } else {
		  tableModel = new SpreadsheetTableModel(table,m,rgui);
	  }
      
      table.setModel((AbstractTableModel)tableModel);

      //      applyBaseColumnWidth();
      applyColumnWidth(columnWidth);

      // update history with new one
      //history.setTableModel(tableModel);
      //tableModel.setHistory(history);

      // inform tableModel that it's unmodified now
      tableModel.setPasswordModified(false);
      tableModel.setModified(false);
   }


   public Action[] getActions()
   {
      return new Action[0]; // Fixme
   }
   
   private class HeaderMouseAdapter extends MouseAdapter
   {
      public void mouseClicked(MouseEvent e)
      {
         int col = -1;
         int row = -1;
         if (e.getSource() instanceof JTableHeader)
         {
            col = table.columnAtPoint(e.getPoint());
         }
         if (e.getSource() instanceof JTable)
         {
            row = table.rowAtPoint(e.getPoint());
         }

         int rowCount = table.getRowCount();
         int colCount = table.getColumnCount();

         table.setRowSelectionInterval(0, rowCount - 1);

         if (col >= 0) // select column
         {
            table.setColumnSelectionInterval(col, col);
            table.setRowSelectionInterval(0, rowCount - 1);
         }
         else if (row >= 0) // select row
         {
            table.setColumnSelectionInterval(0, colCount - 1);
            table.setRowSelectionInterval(row, row);
         }
         else // select all
         {
            table.setColumnSelectionInterval(0, colCount - 1);
            table.setRowSelectionInterval(0, rowCount - 1);
         }
      }
   }

   private static class RowHeaderRenderer extends DefaultTableCellRenderer
   {
      public RowHeaderRenderer()
      {
         setBorder(UIManager.getBorder("TableHeader.cellBorder"));
         setHorizontalAlignment(RIGHT);
      }

      public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column)
      {
         if (table != null)
         {
            JTableHeader header = table.getTableHeader();
            if (header != null)
            {
               setForeground(header.getForeground());
               setBackground(header.getBackground());
            }
         }
         setValue(String.valueOf(row + 1));

         return this;
      }

      public void updateUI()
      {
         super.updateUI();
         setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      }
   }

   
   private static class RowModel implements TableModel
   {
      private TableModel source;

      RowModel(TableModel source)
      {
         this.source = source;
      }

      public boolean isCellEditable(int rowIndex, int columnIndex)
      {
         return false;
      }

      public Class<?> getColumnClass(int columnIndex)
      {
         return Object.class;
      }

      public int getColumnCount()
      {
         return 1;
      }

      public String getColumnName(int columnIndex)
      {
         return null;
      }

      public int getRowCount()
      {
         return source.getRowCount();
      }

      public void setValueAt(Object aValue, int rowIndex, int columnIndex)
      {
      }

      public Object getValueAt(int rowIndex, int columnIndex)
      {
         return rowIndex;
      }

      public void addTableModelListener(javax.swing.event.TableModelListener l)
      {
      }

      public void removeTableModelListener(javax.swing.event.TableModelListener l)
      {
      }
   }

   private class SelectionAdapter implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         int c = listenerList.getListenerCount(SpreadsheetSelectionListener.class);
         if (c > 0)
         {
            fireSelectionChanged();
         }
      }
   }
   
   public JTable getTable() {
	   return table;
   }
   
   public void refreshRowHeaders() {
	   rowHeader.setModel(new RowModel(table.getModel()));
	   rowHeader.repaint();
   }
   
	public void undo() {
		tableModel.undo();
	}
	
	public boolean canUndo() {
		return tableModel.canUndo();
	}
	
	public void redo() {
		tableModel.redo();
	}
	
	public boolean canRedo() {
		return tableModel.canRedo();
	}
	
	public void addSpreadsheetListener(SpreadsheetListener l) throws RemoteException {
		tableModel.addSpreadsheetListener(l);		
	}

	public void refreshEmbeddedPanelsLayer() {
		
		controlsLayer.removeAll();
		
		for (EmbeddedPanelDescription desc:((RGui)rg).getEmbeddedPanelDescriptions()) {
			if (desc.getSpreadsheetName().equals(_name)) {
				CellRange cellrange = null;
				try {
					cellrange = ImportInfo.getRange(desc.getRange());
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				if (cellrange!=null) {
					JPanel container=new JPanel(new BorderLayout());
					container.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
					container.add(desc.getPanel(), BorderLayout.CENTER);
					
					
					Rectangle r1 = table.getCellRect(cellrange.getStartRow(), cellrange.getStartCol(), true);
					Rectangle r2 = table.getCellRect(cellrange.getEndRow()+1, cellrange.getEndCol()+1, true);
					container.setLocation(r1.x-1, r1.y-1);
					container.setSize(Math.abs(r2.x - r1.x)+1, Math.abs(r2.y - r1.y)+1);
					
					controlsLayer.add(container);					
				}
				
				
			}
		}
		
		controlsLayer.updateUI();
		controlsLayer.repaint();

	}
	
	/*
	 * 

   public JSpreadsheet(AbstractTableModel m, RGui rgui, String name)
   {
	   rg=rgui;
	   
      table = createTable();

      setLayout(new BorderLayout());
      newTableModel(m, rgui);

      // clobber resizing of all columns
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      // set table editor and renderer to custom ones
      table.setDefaultRenderer(Cell.class, new SpreadsheetCellRenderer( rgui, name));
      table.setDefaultEditor(Cell.class, new SpreadsheetCellEditor(new JTextField()));

      // set selection mode for contiguous  intervals
      table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
      table.setCellSelectionEnabled(true);

      MouseListener ml = new HeaderMouseAdapter();

      // we don't allow reordering
      table.getTableHeader().setReorderingAllowed(false);
      table.getTableHeader().addMouseListener(ml);

      // create selection models
      ListSelectionModel rowSelectionModel = table.getSelectionModel();
      ListSelectionModel columnSelectionModel = table.getColumnModel().getSelectionModel();

      // add selection listeners to the selection models
      //rowSelectionModel.addListSelectionListener(this);
      //columnSelectionModel.addListSelectionListener(this);
      
      Dimension tablePreferredSize=table.getPreferredSize();
      
      
      JLayeredPane lp = new JLayeredPane();
      lp.setPreferredSize(tablePreferredSize);
      
      JPanel tableLayer = new JPanel(new BorderLayout());
      tableLayer.setSize(tablePreferredSize);
      tableLayer.setOpaque(true);
      tableLayer.add(table,BorderLayout.CENTER);
      lp.add(tableLayer, new Integer(1));
      
      controlsLayer = new JPanel(null);
      controlsLayer.setSize(tablePreferredSize);            	  
	  controlsLayer.setOpaque(false);
      lp.add(controlsLayer, new Integer(2));      
      JScrollPane scrollPane = new JScrollPane(lp, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
      add(scrollPane, BorderLayout.CENTER);

      rowHeader = new JTable(new RowModel(table.getModel()));
      TableCellRenderer renderer = new RowHeaderRenderer();
      Component comp = renderer.getTableCellRendererComponent(rowHeader, null, false, false, rowHeader.getRowCount() - 1, 0);
      rowHeader.setIntercellSpacing(new Dimension(0, 0));
      Dimension d = rowHeader.getPreferredScrollableViewportSize();
      d.width = comp.getPreferredSize().width;
      rowHeader.setPreferredScrollableViewportSize(d);
      rowHeader.setRowHeight(table.getRowHeight());
      rowHeader.setDefaultRenderer(Object.class, renderer);
      rowHeader.addMouseListener(ml);

      scrollPane.setRowHeaderView(rowHeader);

      JPanel blank = new JPanel();
      blank.addMouseListener(ml);
      scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, blank);

      // initial selection
      //resetSelection();
      table.setRequestFocusEnabled(true);

      //menuBar.setRequestFocusEnabled(false);
      //toolBar.setRequestFocusEnabled(false);
      table.requestFocus();

      Formula.registerFunctions();

      try {
      if (PoolUtils.isMacOs()) {
    	  table.setGridColor(Color.black);
    	  
      } } catch (Exception e) {
    	  e.printStackTrace();
      }
      
		new Thread(new Runnable() {
			public void run() {
				
				final Random rnd=new Random(System.currentTimeMillis());
				
				try {
					while (true) {
						
c
												
						Thread.sleep(3000);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
      
   }

	 * 
	 */	
}
