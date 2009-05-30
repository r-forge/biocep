package org.kchine.scilab.server;

public class ScilabServicesSingleton {
	
	public static ScilabServices _scilabServices = null;
	private static Integer lock = new Integer(0);
	
	public static ScilabServices getInstance() {
		if (_scilabServices != null) return _scilabServices;
		
		synchronized (lock) {
			if (_scilabServices == null) {
				try {					
					//_scilabServices = (ScilabServices)ScilabServicesSingleton.class.forName("org.kchine.scilab.server.ScilabServicesImpl").newInstance();
					_scilabServices = new ScilabServicesImpl();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return _scilabServices;
		}
		
	}
	

}
