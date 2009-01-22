package org.kchine.r.workbench.scripting;

import org.kchine.r.server.scripting.GroovyInterpreter;
import org.kchine.r.server.scripting.GroovyInterpreterImpl;

public class GroovyInterpreterSingleton {

	public static GroovyInterpreter _clientSideGroovy = null;
	private static Integer _clientSideLock = new Integer(0);

	public static GroovyInterpreter getInstance() {
		if (_clientSideGroovy != null)
			return _clientSideGroovy;
		synchronized (_clientSideLock) {
			if (_clientSideGroovy == null) {

				try {
					_clientSideGroovy = new GroovyInterpreterImpl(GroovyInterpreterSingleton.class.getClassLoader());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return _clientSideGroovy;
		}
	}
}
