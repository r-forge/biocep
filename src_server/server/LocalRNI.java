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
package server;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.rosuda.JRI.Rengine;

import remoting.RNI;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class LocalRNI implements RNI {

	DirectJNI _jni;
	String _status;
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(LocalRNI.class);

	public LocalRNI(DirectJNI jni) {
		_jni = jni;
	}

	public void rniAssign(final String name, final long exp, final long rho) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniAssign(name, exp, rho);
			}
		});
	}

	public long rniCAR(final long exp) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniCAR(exp);
			}
		});
		return holder[0];
	}

	public long rniCDR(final long exp) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniCDR(exp);
			}
		});
		return holder[0];
	}

	public long rniCons(final long head, final long tail) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniCons(head, tail);
			}
		});
		return holder[0];
	}

	public long rniEval(final long exp, final long rho) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniEval(exp, rho);
			}
		});
		return holder[0];
	}

	public int rniExpType(final long exp) throws RemoteException {
		final int[] holder = new int[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniExpType(exp);
			}
		});
		return holder[0];
	}

	public long rniGetAttr(final long exp, final String name) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetAttr(exp, name);
			}
		});
		return holder[0];
	}

	public int[] rniGetBoolArrayI(final long exp) throws RemoteException {
		final int[][] holder = new int[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetBoolArrayI(exp);
			}
		});
		return holder[0];
	}

	public double[] rniGetDoubleArray(final long exp) throws RemoteException {
		final double[][] holder = new double[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetDoubleArray(exp);
			}
		});
		return holder[0];
	}

	public int[] rniGetIntArray(final long exp) throws RemoteException {
		final int[][] holder = new int[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetIntArray(exp);
			}
		});
		return holder[0];
	}

	public long[] rniGetList(final long exp) throws RemoteException {
		final long[][] holder = new long[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetList(exp);
			}
		});
		return holder[0];
	}

	public String rniGetString(final long exp) throws RemoteException {
		final String[] holder = new String[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetString(exp);
			}
		});
		return holder[0];
	}

	public String[] rniGetStringArray(final long exp) throws RemoteException {
		final String[][] holder = new String[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetStringArray(exp);
			}
		});
		return holder[0];
	}

	public String rniGetSymbolName(final long sym) throws RemoteException {
		final String[] holder = new String[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetSymbolName(sym);
			}
		});
		return holder[0];
	}

	public long[] rniGetVector(final long exp) throws RemoteException {
		final long[][] holder = new long[1][];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniGetList(exp);
			}
		});
		return holder[0];
	}

	public long rniGetVersion() throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = Rengine.rniGetVersion();
			}
		});
		return holder[0];
	}

	public boolean rniInherits(final long exp, final String cName) throws RemoteException {
		final boolean[] holder = new boolean[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniInherits(exp, cName);
			}
		});
		return holder[0];
	}

	public long rniInstallSymbol(final String sym) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniInstallSymbol(sym);
			}
		});
		return holder[0];
	}

	public long rniParse(final String s, final int parts) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniParse(s, parts);
			}
		});
		return holder[0];
	}

	public void rniProtect(final long exp) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniProtect(exp);
			}
		});
	}

	public long rniPutBoolArray(final boolean[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutBoolArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutBoolArrayI(final int[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutBoolArrayI(a);
			}
		});
		return holder[0];
	}

	public long rniPutDoubleArray(final double[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutDoubleArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutIntArray(final int[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutIntArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutList(final long[] cont) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutList(cont);
			}
		});
		return holder[0];
	}

	public long rniPutString(final String s) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutString(s);
			}
		});
		return holder[0];
	}

	public long rniPutStringArray(final String[] a) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutStringArray(a);
			}
		});
		return holder[0];
	}

	public long rniPutVector(final long[] exps) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniPutVector(exps);
			}
		});
		return holder[0];
	}

	public void rniSetAttr(final long exp, final String name, final long attr) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniSetAttr(exp, name, attr);
			}
		});
	}

	public int rniStop(final int flag) throws RemoteException {
		final int[] holder = new int[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniStop(flag);
			}
		});
		return holder[0];
	}

	public long rniTAG(final long exp) throws RemoteException {
		final long[] holder = new long[1];
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				holder[0] = e.rniTAG(exp);
			}
		});
		return holder[0];
	}

	public void rniUnprotect(final int count) throws RemoteException {
		_status = _jni.runR(new ExecutionUnit() {
			public void run(Rengine e) {
				e.rniUnprotect(count);
			}
		});
	}

	public String getStatus() throws RemoteException {
		return _status;
	}
}
