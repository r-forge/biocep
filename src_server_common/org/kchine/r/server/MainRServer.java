/*
 * Biocep: R-based Platform for Computational e-Science.
 *
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kchine.r.server;

import org.kchine.r.server.impl.RServantImpl;
import org.kchine.r.server.manager.ServerManager;
import org.kchine.rpf.MainServer;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class MainRServer {

	public static void main(String[] args) throws Exception {
		
		boolean keepAlive = new Boolean(System.getProperty("keepalive"));			
		if (!keepAlive) {
			ServerManager.startPortInUseDogwatcher(System.getProperty("code.server.host"),Integer.decode(System.getProperty("code.server.port")), 3,3);
		}
		
		Class<?> servantClass=RServantImpl.class;
		System.setProperty("servantclass", servantClass.getName());
		
		System.setProperty("scilab_enabled", "false");
		
		if (ServerManager.sci!=null) {
			try {
				Class<?> scilabClass=MainRServer.class.getClassLoader().loadClass("javasci.Scilab");
				scilabClass.getMethod("Exec", String.class).invoke(null, "disp(1+3)");
				System.setProperty("scilab_enabled", "true");		
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		
		MainServer.main(args);		
	}

}
