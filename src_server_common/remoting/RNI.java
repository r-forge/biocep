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
package remoting;

import java.rmi.Remote;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public interface RNI extends Remote {

	public long rniParse(String s, int parts) throws java.rmi.RemoteException;

	public long rniEval(long exp, long rho) throws java.rmi.RemoteException;

	public void rniProtect(long exp) throws java.rmi.RemoteException;

	public void rniUnprotect(int count) throws java.rmi.RemoteException;

	public String rniGetString(long exp) throws java.rmi.RemoteException;

	public String[] rniGetStringArray(long exp) throws java.rmi.RemoteException;

	public int[] rniGetIntArray(long exp) throws java.rmi.RemoteException;

	public int[] rniGetBoolArrayI(long exp) throws java.rmi.RemoteException;

	public double[] rniGetDoubleArray(long exp) throws java.rmi.RemoteException;

	public long[] rniGetVector(long exp) throws java.rmi.RemoteException;

	public long rniPutString(String s) throws java.rmi.RemoteException;

	public long rniPutStringArray(String[] a) throws java.rmi.RemoteException;

	public long rniPutIntArray(int[] a) throws java.rmi.RemoteException;

	public long rniPutBoolArrayI(int[] a) throws java.rmi.RemoteException;

	public long rniPutBoolArray(boolean[] a) throws java.rmi.RemoteException;

	public long rniPutDoubleArray(double[] a) throws java.rmi.RemoteException;

	public long rniPutVector(long[] exps) throws java.rmi.RemoteException;

	public long rniGetAttr(long exp, String name) throws java.rmi.RemoteException;

	public void rniSetAttr(long exp, String name, long attr) throws java.rmi.RemoteException;

	public boolean rniInherits(long exp, String cName) throws java.rmi.RemoteException;

	public long rniCons(long head, long tail) throws java.rmi.RemoteException;

	public long rniCAR(long exp) throws java.rmi.RemoteException;

	public long rniCDR(long exp) throws java.rmi.RemoteException;

	public long rniTAG(long exp) throws java.rmi.RemoteException;

	public long rniPutList(long[] cont) throws java.rmi.RemoteException;

	public long[] rniGetList(long exp) throws java.rmi.RemoteException;

	public String rniGetSymbolName(long sym) throws java.rmi.RemoteException;

	public long rniInstallSymbol(String sym) throws java.rmi.RemoteException;

	public long rniGetVersion() throws java.rmi.RemoteException;

	public int rniStop(int flag) throws java.rmi.RemoteException;

	public void rniAssign(String name, long exp, long rho) throws java.rmi.RemoteException;

	public int rniExpType(long exp) throws java.rmi.RemoteException;

	public String getStatus() throws java.rmi.RemoteException;

}
