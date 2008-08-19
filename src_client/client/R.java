package client;

import java.rmi.registry.Registry;

import graphics.rmi.GDApplet;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServerDefaults;
import uk.ac.ebi.microarray.pools.gui.InDialog;

public class R {
	public static  RServices getInstance() {
		if (GDApplet._instance==null) return null;
		else return GDApplet._instance.getR();
	}
	public static  Registry getRegistry() throws Exception {
		return ServerDefaults.getRmiRegistry();
	}
	public static String getUserInput(String label) {
		InDialog dialog=new InDialog(null,label,new String[]{""});
		dialog.setVisible(true);
		return dialog.getExpr();
	}
	
}
