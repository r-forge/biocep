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

import org.kchine.rpf.PoolUtils;
import org.kchine.rpf.RPFSessionInfo;
import org.kchine.rpf.ServantProviderFactory;
import org.kchine.rpf.TimeoutException;
import org.kchine.rpf.YesSecurityManager;

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
		System.setSecurityManager(new YesSecurityManager());
		

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

									rp = (RServices) org.kchine.rpf.ServantProviderFactory.getFactory()
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


}
