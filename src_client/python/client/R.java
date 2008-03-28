package python.client;

import graphics.rmi.GDApplet;
import remoting.RServices;

public class R {
	static public RServices getInstance() {
		return GDApplet._instance.getR();
	}
}
