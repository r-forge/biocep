package model;

import java.rmi.registry.LocateRegistry;

public class ModelServer {

	/**
	 * @param args
	 */
	public static SpreadsheetTableModelRemoteImpl tmri;
	public static void main(String[] args) throws Exception{		
		tmri=new SpreadsheetTableModelRemoteImpl(3,2);
		LocateRegistry.getRegistry().bind("toto", tmri);
	}

}
