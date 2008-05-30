package model;

import java.rmi.registry.LocateRegistry;
import java.util.HashMap;

public class ModelServer {

	/**
	 * @param args
	 */
	public static SpreadsheetModelRemoteImpl tmri;
	public static void main(String[] args) throws Exception{		
		tmri=new SpreadsheetModelRemoteImpl(3,2, new HashMap<String, SpreadsheetModelRemoteImpl>());
		LocateRegistry.getRegistry().bind("toto", tmri);
	}

}
