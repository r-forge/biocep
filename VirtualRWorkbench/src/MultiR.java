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
import org.bioconductor.packages.biobase.ExpressionSet;
import org.bioconductor.packages.rGlobalEnv.rGlobalEnvFunction;
import org.bioconductor.packages.rservices.RNumeric;
import org.bioconductor.packages.vsn.Vsn;
import org.bioconductor.packages.vsn.vsnFunction;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.TimeoutException;

import util.Utils;

public class MultiR {

	public static void main(String args[]) throws Exception {

		int threadNumber = System.getProperty("threadnumber") != null && !System.getProperty("threadnumber").equals("") ? Integer.decode(System.getProperty("threadnumber")) : 20;
		System.out.println("Threads Number=" + threadNumber);
		for (int i = 0; i < threadNumber; ++i) {
			Thread t=new Thread(getRunnable(), "T" + i);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();
		}
	}

	static Runnable getRunnable() {

		return new Runnable() {
			public void run() {

				String TN = "Thread<" + Thread.currentThread().getName() + "> ";
				System.out.println(TN + "-> starts");
				RServices r = null;
				try {
					

					r = (RServices)ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();
					
					
					System.out.println("++ Thread<" + Thread.currentThread().getName() + "> obtained " + r.getServantName());
					System.out.println(" packages mapped =" + Utils.flatArray(r.getAllPackageNames()));
					RNumeric squareOf4 = ((rGlobalEnvFunction) r.getPackage("rGlobalEnvFunction")).square(new RNumeric(4));
					System.out.println("square Of 4 : "+squareOf4);

					
				
					r.evaluate("data(kidney)");
					ExpressionSet kidney = (ExpressionSet) r.evalAndGetObjectAsReference("kidney");
					System.out.println("kidney:" + kidney);
					
					
					vsnFunction vsnPack = (vsnFunction) r.getPackage("vsnFunction");
					
					/*
					Vsn obj = vsnPack.vsn2AsReference(kidney);
					System.out.println(TN + obj.getClass().getName());
					System.out.println(TN + obj);
					*/
					
					

				} catch (TimeoutException te) {
					System.out.println("@@@@@@ Thread<" + Thread.currentThread().getName() + "> -> time out");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (r != null)
						try {
							System.out.println("##Thread<" + Thread.currentThread().getName() + "> is releasing servant ->"	+ r.getServantName());
							
							ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);
							
							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
				}
			}
		};
	}	
}