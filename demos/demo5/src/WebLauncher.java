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
import java.util.Properties;
import javax.xml.ws.Endpoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.PropertyConfigurator;
import server.DirectJNI;

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
