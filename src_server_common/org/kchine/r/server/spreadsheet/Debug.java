package org.kchine.r.server.spreadsheet;


/*
 * This is a class used for print out debug information and can be
 * easily turned on/off.
 */
public class Debug
{
   static private boolean debug = false;

   static void setDebug(boolean flag)
   {
      debug = flag;
   }

   public static boolean isDebug()
   {
      return debug;
   }

   public static void println(Object s)
   {
      if (debug)
      {
         System.out.println(s.toString());
      }
   }
}
