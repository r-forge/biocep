package model;

import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

public class ModelServer {

	/**
	 * @param args
	 */
	public static SpreadsheetTableModelRemoteImpl tmri;
	public static void main(String[] args) throws Exception{		
		tmri=new SpreadsheetTableModelRemoteImpl(3,2, new HashMap<String, SpreadsheetTableModelRemoteImpl>());
		LocateRegistry.getRegistry().bind("toto", tmri);
	}

}
