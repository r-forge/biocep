/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2007-2009 Karim Chine - karim.chine@m4x.org
 *  
 * Copyright (C) 2007 EMBL-EBI-Microarray Informatics
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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import remoting.RCallBack;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class TestCallback {

	public static void main(String args[]) throws Exception {
		RServices r = null;
		try {
			r = (RServices) ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();
			r.addRCallback(new RCallbackImpl());
			r.addRCallback(new RCallbackImpl());
			System.out.println("***" + r.evaluate("library(rJava)"));
			System.out
					.println("***"
							+ r.evaluate("democallback<-function() { .PrivateEnv$notifyJavaListeners('percentageDone=0.1');"+
							".PrivateEnv$notifyJavaListeners('percentageDone=0.2');"+"" +
							".PrivateEnv$notifyJavaListeners('percentageDone=0.5'); .PrivateEnv$notifyJavaListeners('percentageDone=1');}"));
			System.out.println("***" + r.evaluate(".jinit();democallback()", 2));
		} finally {
			ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);
		}
		System.exit(0);
	}

	static class RCallbackImpl extends UnicastRemoteObject implements RCallBack {
		public RCallbackImpl() throws RemoteException {
			super();
		}
		public void notify(HashMap<String, String> parameters) throws RemoteException {
			System.out.println("percentageDone=" + parameters.get("percentageDone"));			
		}
		public String getId() throws RemoteException {
			return null;
		}
		public String dispose() throws RemoteException {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
