package python;

import remoting.RServices;
import server.RServantImpl;

public class R {
	static public RServices getInstance() {
		return RServantImpl._instance;
	}
}
