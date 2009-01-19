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
import java.util.Properties;
import javax.xml.ws.Endpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.PropertyConfigurator;
import org.kchine.r.server.DirectJNI;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class WebLauncher {

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(WebLauncher.class);

	public static void main(String[] args) {
		DirectJNI.init();
		for (String className : DirectJNI._rPackageInterfacesHash.keySet()) {
			String shortClassName = className.substring(className.lastIndexOf('.') + 1);
			try {
				Class packageWebClass = DirectJNI._mappingClassLoader.loadClass(className + "Web");
				if (packageWebClass.getDeclaredMethods().length > 0) {
					try {
						String url = "http://localhost:8080/" + "ws/" + shortClassName;
						Endpoint.publish(url, packageWebClass.newInstance());
						System.out.println(shortClassName + "Web" + " has been successfully published to " + url);
					} catch (Exception ie) {
						ie.printStackTrace();
					}
				}
			} catch (ClassNotFoundException e) {
			}
		}
	}

	static {
		if (log instanceof Log4JLogger) {
			Properties log4jProperties = new Properties();
			for (Object sprop : System.getProperties().keySet()) {
				if (((String) sprop).startsWith("log4j.")) {
					log4jProperties.put(sprop, System.getProperties().get(sprop));
				}
			}
			PropertyConfigurator.configure(log4jProperties);
		}
	}

}

/*

 public static double[][] buildMatrix(Double[] t, Integer[] dim) {
 double[][] result=new double[dim[0]][];
 for (int i=0; i<result.length; ++i) {
 result[i]=new double[dim[1]];
 for (int j=0; j<dim[1]; ++j) result[i][j]= t[i*dim[1]+j];
 }
 return result;
 }

 public static void main(String[] args) throws Exception {

 RGlobalEnvFunctionWeb g=new RGlobalEnvFunctionWebServiceLocator().getrGlobalEnvFunctionWebPort();


 RNumeric x=new RNumeric();x.setValue(new Double[]{8.5});
 System.out.println(g.square(x).getValue()[0]);

 RInteger nk=new RInteger(); nk.setValue(new Integer[]{0});
 RInteger nkvsn=new RInteger(); nkvsn.setValue(new Integer[]{1});

 org.bioconductor.packages.rGlobalEnv.RMatrix kidneymatrix=g.getKidney(nk);
 org.bioconductor.packages.rGlobalEnv.RMatrix kidneyvsnmatrix=g.getKidney(nkvsn);		


 double[][] m_kidney=buildMatrix(((RNumeric)kidneymatrix.getValue()).getValue(), kidneymatrix.getDim());
 double[][] m_kidneyvsn=buildMatrix(((RNumeric)kidneyvsnmatrix.getValue()).getValue(), kidneyvsnmatrix.getDim());

 System.out.println(Arrays.toString(m_kidney[m_kidney.length-1]));



 final double[][] m=new double[][]{new double[]{1.0,2.0,6.0,98.0},new double[]{1.0,2.0,6.0,98.0}, new double[]{1.0,2.0,6.0,98.0},new double[]{1.0,2.0,6.0,98.0} };
 Future<Double>[] result=new Future[m.length];

 ExecutorService exec = Executors.newFixedThreadPool(5);

 for (int i=0; i<result.length; ++i) {
 final double[] v=m[i];			
 result[i]= exec.submit(new Callable<Double>() {
 public Double call() throws Exception {					
 RServices r=null;
 try {
 r=(RServices)ServantProviderFactory.getFactory().getServantProvider().borrowServantProxy();												
 org.bioconductor.packages.rservices.RNumeric mean=(org.bioconductor.packages.rservices.RNumeric)r.call("mean", new org.bioconductor.packages.rservices.RNumeric(v));						
 return mean.getValue()[0];						
 }
 catch (Exception e) {
 return null;					
 } finally {
 ServantProviderFactory.getFactory().getServantProvider().returnServantProxy(r);
 }					
 }
 });
 }

 while(true) {
 int count=0;
 for (int i=0; i<result.length; ++i) if (result[i].isDone()) ++count;
 if (count==result.length) break;
 Thread.sleep(100);
 }

 for (int i=0; i<result.length; ++i) System.out.println(result[i].get());

 }



 */
