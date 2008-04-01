package python.client;

import graphics.rmi.GDApplet;
import remoting.RServices;

public class R {
	static public RServices getInstance() {
		if (GDApplet._instance==null) return null;
		else return GDApplet._instance.getR();
	}
}
