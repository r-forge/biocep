package server;

import remoting.RServices;

public class R {
	public static RServices _instance;
	static public RServices getInstance() {
		return _instance;
	}
	
}
