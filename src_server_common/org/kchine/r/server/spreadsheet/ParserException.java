package org.kchine.r.server.spreadsheet;


/**
 * This Exception is raised when Formula fails in tokenizing or parsing the
 * formula.
 *
 * @author Hua Zhong <huaz@cs.columbia.edu>
 * @version $Revision: 1.1 $
 */
public class ParserException extends java.rmi.RemoteException
{
   private String msg;
   private boolean quiet;

   /**
    * Contructor for ParserException.  By default, sets quiet to true.
    */
   public ParserException()
   {
      quiet = true;
   }

   /**
    * @param msg the error message string
    */
   public ParserException(String msg)
   {
      super(msg);
      this.msg = msg;
   }

   /**
    * @param msg the error object
    */
   public ParserException(Object msg)
   {
      super(msg.toString());
      this.msg = msg.toString();
   }

   /**
    * This returns the value of quiet.
    *
    * @return true if quiet is true, false otherwise
    */
   public boolean isQuiet()
   {
      return quiet;
   }

   /**
    * toString method for ParserException.
    *
    * @return the error message string
    */
   public String toString()
   {
      return msg;
   }
}
