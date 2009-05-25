package org.kchine.openoffice.server;


public class OpenOfficeServicesSingleton {
	public static OpenOfficeServices _oo = null;
	private static Integer lock = new Integer(0);
	
	public static OpenOfficeServices getInstance() {
		if (_oo != null)
			return _oo;
		synchronized (lock) {
			if (_oo == null) {
				try {
					_oo = (OpenOfficeServices)OpenOfficeServicesSingleton.class.forName("org.kchine.openoffice.server.OpenOfficeServicesImpl").newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return _oo;
		}
	}
	

}
