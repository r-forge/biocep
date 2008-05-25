/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
 * Copyright (C) 2007 - 2008  Karim Chine
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import remoting.RCallBack;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
