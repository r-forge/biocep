/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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
import org.bioconductor.packages.biobase.ExpressionSet;
import org.bioconductor.packages.rservices.RNamedArgument;
import org.bioconductor.packages.vsn.Vsn;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.TimeoutException;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class MultiR {

	public static void main(String args[]) throws Exception {

		int threadNumber = System.getProperty("threadnumber") != null && !System.getProperty("threadnumber").equals("") ? Integer
				.decode(System.getProperty("threadnumber"))
				: 20;
		System.out.println("Threads Number=" + threadNumber);
		for (int i = 0; i < threadNumber; ++i)
			new Thread(getRunnable(), "T" + i).start();
	}

	static Runnable getRunnable() {

		return new Runnable() {
			public void run() {

				String TN = "Thread<" + Thread.currentThread().getName() + "> ";
				System.out.println(TN + "-> starts");
				RServices r = null;
				try {

					r = (RServices) ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();

					System.out.println("++ Thread<" + Thread.currentThread().getName() + "> obtained "
							+ r.getServantName());

					r.evaluate("data(kidney)");
					ExpressionSet kidney = (ExpressionSet) r.getReference("kidney");
					Vsn fit = (Vsn) r.call("vsn2", kidney);
					ExpressionSet normalizedKidney = (ExpressionSet) r.call("predict", fit, new RNamedArgument(
							"newdata", kidney));
					System.out.println(" Normalized Kidney : " + normalizedKidney);

				} catch (TimeoutException te) {
					System.out.println("@@@@@@ Thread<" + Thread.currentThread().getName() + "> -> time out");
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (r != null)
						try {
							System.out.println("##Thread<" + Thread.currentThread().getName()
									+ "> is releasing servant ->" + r.getServantName());
							ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
				}
			}
		};
	}
}