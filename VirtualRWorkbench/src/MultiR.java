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
import java.util.Arrays;

import org.kchine.r.packages.biobase.ExpressionSet;
import org.kchine.r.packages.rGlobalEnv.rGlobalEnvFunction;
import org.kchine.r.RNumeric;
import org.kchine.r.packages.vsn.vsnFunction;
import org.kchine.r.server.RServices;
import org.kchine.rpf.ServantProviderFactory;
import org.kchine.rpf.TimeoutException;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
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
					System.out.println(" packages mapped =" + Arrays.toString(r.listPackages()));
					RNumeric squareOf4 = ((rGlobalEnvFunction) r.getPackage("rGlobalEnvFunction")).square(new RNumeric(4));
					System.out.println("square Of 4 : "+squareOf4);

					
				
					r.evaluate("data(kidney)");
					ExpressionSet kidney = (ExpressionSet) r.getReference("kidney");
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