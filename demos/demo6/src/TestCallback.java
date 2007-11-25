/*
 * Copyright (C) 2007 EMBL-EBI
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
import remoting.RCallback;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class TestCallback {

	public static void main(String args[]) throws Exception {
		RServices r = null;
		try {
			r = (RServices) ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();
			r.setCallBack(new RCallbackImpl());
			System.out.println("***" + r.evaluate("library(rJava)"));
			System.out
					.println("***"
							+ r
									.evaluate("democallback<-function() { .PrivateEnv$callbackPercentage(0.1);.PrivateEnv$callbackPercentage(0.1); .PrivateEnv$callbackPercentage(0.5); .PrivateEnv$callbackPercentage(1.0);}"));
			System.out.println("***" + r.evaluateExpressions(".jinit();democallback()", 2));
		} finally {
			ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);
		}
		System.exit(0);
	}

	static class RCallbackImpl extends UnicastRemoteObject implements RCallback {
		public RCallbackImpl() throws RemoteException {
			super();
		}

		public void progress(float percentageDone, String phaseDescription, float phasePercentageDone)
				throws RemoteException {
			System.out.println("percentageDone=" + percentageDone);
		}
	}
}
