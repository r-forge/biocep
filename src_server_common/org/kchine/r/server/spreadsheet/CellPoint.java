package org.kchine.r.server.spreadsheet;

import java.io.Serializable;




/** This is an object the represents the coordinates of a cell
 * in the table. It is used as entries in the reference list
 * of each cell. In addition, it is sometimes used as parameters
 * of table manipulation methods. CellPoints are better parameters
 * because there is no worry that someone will switch the order of
 * parameters (ie. type column first and then row instead of the
 * other way around).
 * @author Ricky Chin
 * @version $Revision: 1.1 $
 */
public class CellPoint implements Comparable, Serializable
{
   /** This holds column coordinate of a cell */
   private int col;

   /** This hold row coordinate of a cell */
   private int row;

   /**
    * Creates a new CellPoint.
    *
    * @param aRow row of a cell
    * @param aCol column of a cell
    */
   public CellPoint(int aRow, int aCol)
   {
      row = aRow;
      col = aCol;
   }

   /** gets the column coordinate of cell reference stored.
    * @return column value
    */
   public int getCol()
   {
      return col;
   }

   /**
    * Gets the row of cell referenced by this CellPoint.
    *
    * @return row value
    */
   public int getRow()
   {
      return row;
   }

   /**
    * Compares CellPoints by row and then by column field. It is used by the
    * reference list to maintain itself.
    *
    * @param x CellPoint to be compares
    * @return 1 if greater, 0 if equal and -1 if > x
    */
   public int compareTo(Object x)
   {
      if (x instanceof CellPoint)
      {
         CellPoint y = (CellPoint) x;

         //by row
         if (this.row > y.row)
         {
            return 1;
         }
         else
         {
            if (this.row == y.row)
            {
               //by column
               if (this.col > y.col)
               {
                  return 1;
               }
               else
               {
                  //equal
                  if (this.col == y.col)
                  {
                     return 0;
                  }
                  else
                  {
                     return -1;
                  }
               }
            }
            else
            {
               return -1;
            }
         }
      }
      else
      {
         return 2; /* can't compare non-CellPoint objects */
      }
   }

   /**
    * Two CellPoints are equal if all the fields are equal.
    * Useful for updating the reference list.
    *
    * @param x CellPoint to compare this to
    * @return true iff all fields equal
    */
   public boolean equals(Object x)
   {
      if (x instanceof CellPoint)
      {
         CellPoint y = (CellPoint) x;

         return ((this.row == y.row) && (this.col == y.col));
      }
      else
      {
         return false;
      }
   }

   /**
    * Converts the object to a string representation
    *
    * @return a string representation of CellPoint
    */
   public String toString()
   {
      return Node.translateColumn(col) + Node.translateRow(row);
   }

   /**
    * Sets column coordinate.
    *
    * @param col value to set column coordinate to
    */
   void setCol(int col)
   {
      this.col = col;
   }

   /**
    * Sets the row coordinate of cell reference to be stored.
    *
    * @param row value to set row to
    */
   void setRow(int row)
   {
      this.row = row;
   }

   /** Increments column coordinate
    * @param x amount to increment column coordinate
    */
   void incrCol(int x)
   {
      this.col += x;
   }

   /** Increments row coordinate.
    * @param x amount to increment row coordinate by
    */
   void incrRow(int x)
   {
      this.row += x;
   }
}
