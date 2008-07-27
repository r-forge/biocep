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
import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Properties;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;

import org.apache.log4j.PropertyConfigurator;
import org.bioconductor.packages.rservices.RMatrix;
import org.bioconductor.packages.rservices.RNumeric;
import remoting.RServices;

import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RPFSessionInfo;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.TimeoutException;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ParallelizedMatrixProduct {

	private static final int THREAD_POOL_SIZE = 120;
	private static final int MATRIX_SIZE = 3;
	private static final int NBR_REPLAY_ON_FAILURE = 0;

	private static final int MATRIX_ELEMENT_MAX_VALUE = 10;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ParallelizedMatrixProduct.class);

	static double[] getColumn(double[][] matrix, int col_index) {
		double[] result = new double[matrix.length];
		for (int i = 0; i < matrix.length; ++i)
			result[i] = matrix[i][col_index];
		return result;
	}

	static double[] getRow(double[][] matrix, int row_index) {
		return matrix[row_index];
	}

	static double vecprod(double[] v1, double[] v2) {
		double result = 0;
		for (int i = 0; i < v1.length; ++i) {
			result += v1[i] * v2[i];
		}
		return result;
	}

	static int countDone(Future<Double>[][] futures) {
		int result = 0;
		for (int i = 0; i < futures.length; ++i)
			for (int j = 0; j < futures[0].length; ++j)
				if (futures[i][j].isDone())
					++result;
		return result;
	}

	public static void main(String args[]) throws Exception {

		double[][] matrix1 = new double[MATRIX_SIZE][MATRIX_SIZE];
		double[][] matrix2 = new double[MATRIX_SIZE][MATRIX_SIZE];

		for (int i = 0; i < MATRIX_SIZE; ++i)
			for (int j = 0; j < MATRIX_SIZE; ++j) {
				matrix1[i][j] = Math.round(Math.random() * MATRIX_ELEMENT_MAX_VALUE);
				matrix2[i][j] = Math.round(Math.random() * MATRIX_ELEMENT_MAX_VALUE);
			}

		ExecutorService exec = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		Future<Double>[][] futures = new Future[MATRIX_SIZE][MATRIX_SIZE];
		for (int i = 0; i < MATRIX_SIZE; ++i) {
			for (int j = 0; j < MATRIX_SIZE; ++j) {
				final double[] v1 = getRow(matrix1, i);
				final double[] v2 = getColumn(matrix2, j);

				if (i % 2 == 0) {
					futures[i][j] = exec.submit(new Callable<Double>() {
						public Double call() {

							RPFSessionInfo.get().put("USER", "USER FOR " + Thread.currentThread().getName());
							RServices rp = null;
							int replayCounter = NBR_REPLAY_ON_FAILURE;

							while (replayCounter >= 0) {

								try {

									rp = (RServices) uk.ac.ebi.microarray.pools.ServantProviderFactory.getFactory()
											.getServantProvider().borrowServantProxy();

									rp.putAndAssign(new RNumeric(v1), "rv1");
									rp.putAndAssign(new RNumeric(v2), "rv2");
									RMatrix res = ((RMatrix) rp.getObject("rv1%*%rv2"));

									return ((RNumeric) res.getValue()).getValue()[0];

								} catch (TimeoutException e) {
									e.printStackTrace();
									return null;
								} catch (RemoteException re) {
									re.printStackTrace();
									--replayCounter;

								} finally {

									try {
										if (rp != null) {
											ServantProviderFactory.getFactory().getServantProvider()
													.returnServantProxy(rp);
											log.info("<" + Thread.currentThread().getName() + "> returned resource : "
													+ rp.getServantName());
										}
									} catch (Exception e) {
										e.printStackTrace();
									}

								}

							}

							return null;

						}
					});
				} else {
					futures[i][j] = exec.submit(new Callable<Double>() {
						public Double call() {

							try {
								return vecprod(v1, v2);
							} finally {
								log.info("<" + Thread.currentThread().getName() + "> Java task ended successfully");
							}
						}
					});
				}
			}

		}

		while (true) {
			if (countDone(futures) == (MATRIX_SIZE * MATRIX_SIZE))
				break;
			try {
				Thread.sleep(20);
			} catch (Exception e) {
			}
		}

		log.info(" done --  product matrix -->");

		Double[][] matrix1_x_matrix2 = new Double[MATRIX_SIZE][MATRIX_SIZE];
		for (int i = 0; i < MATRIX_SIZE; ++i)
			for (int j = 0; j < MATRIX_SIZE; ++j)
				matrix1_x_matrix2[i][j] = futures[i][j].get();

		System.out.println(showMatrix(matrix1, "M1"));
		System.out.println(showMatrix(matrix2, "M2"));
		System.out.println(showMatrix(matrix1_x_matrix2, "M1 x M2"));

		System.exit(0);
	}

	public static String showMatrix(Double[][] matrix, String label) {
		StringBuffer result = new StringBuffer();
		result.append("Matrix " + label + "\n");
		for (int i = 0; i < MATRIX_SIZE; ++i) {
			result.append("[");
			for (int j = 0; j < MATRIX_SIZE; ++j) {
				result.append(matrix[i][j] + (j == (MATRIX_SIZE - 1) ? "" : ","));
			}
			result.append("]" + (i == (MATRIX_SIZE - 1) ? "" : ",\n"));
		}
		result.append("\n");
		return result.toString();
	}

	public static String showMatrix(double[][] matrix, String label) {
		StringBuffer result = new StringBuffer();
		result.append("Matrix " + label + "\n");
		for (int i = 0; i < MATRIX_SIZE; ++i) {
			result.append("[");
			for (int j = 0; j < MATRIX_SIZE; ++j) {
				result.append(matrix[i][j] + (j == (MATRIX_SIZE - 1) ? "" : ","));
			}
			result.append("]" + (i == (MATRIX_SIZE - 1) ? "" : ",\n"));
		}
		result.append("\n\n");
		return result.toString();
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

	static Remote cservant = null;
	static {
		/*		
		 try {		
		
		 GenericObjectPool gop=ServantProxyPoolSingletonDB.getInstance("RSERVANT_", "com.mysql.jdbc.Driver", "jdbc:mysql://172.22.68.47/DWEP", "DWEP", "DWEP");
		 RServices rservice=null;
		 long t1,t2;
		
		
		 for (int i=0; i<100; ++i) {
		 t1=System.currentTimeMillis();	
		 rservice=(RServices)gop.borrowObject();			
		 t2=System.currentTimeMillis();
		 System.out.println(" *** borrowServantProxy took :"+(t2-t1));		
		
		 t1=System.currentTimeMillis();	
		 gop.returnObject(rservice);			
		 t2=System.currentTimeMillis();
		 System.out.println(" *** returnServantProxy took :"+(t2-t1));
		 }
		
		 System.exit(0);
		
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		
		 try {
		
		 Class.forName  ("com.mysql.jdbc.Driver");		
		 Connection conn = DriverManager.getConnection("jdbc:mysql://172.22.68.47/DWEP", "DWEP", "DWEP");
		
		 //Class.forName  ("oracle.jdbc.OracleDriver");		
		 //Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@progression.ebi.ac.uk:1521:AEDWT", "DWEP", "DWEP");
		
		 DBLayer registry=new DBLayer(conn);
		 //registry.recreateTables();
		
		 Remote remote=null;
		
		 remote=registry.lookup("RSERVANT_1");
		
		 ((ManagedServant)remote).ping();
		 System.out.println(remote);
		
		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		
		 System.exit(0);

		 */

	}

}
