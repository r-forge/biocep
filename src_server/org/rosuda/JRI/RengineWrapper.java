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
package org.rosuda.JRI;

import org.apache.commons.logging.Log;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RengineWrapper extends Rengine {

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(RengineWrapper.class);

	public RengineWrapper(String[] args, boolean runMainLoop, RMainLoopCallbacks initialCallbacks) {
		super(args, runMainLoop, initialCallbacks);
	}

	public synchronized long rniParse(String s, int parts) {
		return super.rniParse(s, parts);
	}

	public synchronized long rniEval(long exp, long rho) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniEval(exp, rho);
	}

	public synchronized void rniProtect(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		super.rniProtect(exp);
	}

	public synchronized void rniUnprotect(int count) {
		super.rniUnprotect(count);
	}

	public synchronized String rniGetString(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetString(exp);
	}

	public synchronized String[] rniGetStringArray(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetStringArray(exp);
	}

	public synchronized int[] rniGetIntArray(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetIntArray(exp);
	}

	public synchronized int[] rniGetBoolArrayI(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetBoolArrayI(exp);
	}

	public synchronized double[] rniGetDoubleArray(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetDoubleArray(exp);
	}

	public synchronized long[] rniGetVector(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetVector(exp);
	}

	public synchronized long rniPutString(String s) {
		return super.rniPutString(s);
	}

	public synchronized long rniPutStringArray(String[] a) {
		return super.rniPutStringArray(a);
	}

	public synchronized long rniPutIntArray(int[] a) {
		return super.rniPutIntArray(a);
	}

	public synchronized long rniPutBoolArrayI(int[] a) {
		return super.rniPutBoolArrayI(a);
	}

	public synchronized long rniPutBoolArray(boolean[] a) {
		return super.rniPutBoolArray(a);
	}

	public synchronized long rniPutDoubleArray(double[] a) {
		return super.rniPutDoubleArray(a);
	}

	public synchronized long rniPutVector(long[] exps) {
		if (exps != null)
			for (int i = 0; i < exps.length; ++i)
				if (exps[i] <= 0)
					throw new RuntimeException("bad expression id");
		return super.rniPutVector(exps);
	}

	public synchronized long rniGetAttr(long exp, String name) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetAttr(exp, name);
	}

	public synchronized void rniSetAttr(long exp, String name, long attr) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		super.rniSetAttr(exp, name, attr);
	}

	public synchronized boolean rniInherits(long exp, String cName) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniInherits(exp, cName);
	}

	public synchronized long rniCons(long head, long tail) {
		return super.rniCons(head, tail);
	}

	public synchronized long rniCAR(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniCAR(exp);
	}

	public synchronized long rniCDR(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniCDR(exp);
	}

	public synchronized long rniTAG(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniTAG(exp);
	}

	public synchronized long rniPutList(long[] cont) {
		return super.rniPutList(cont);
	}

	public synchronized long[] rniGetList(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniGetList(exp);
	}

	public synchronized String rniGetSymbolName(long sym) {
		return super.rniGetSymbolName(sym);
	}

	public synchronized long rniInstallSymbol(String sym) {
		return super.rniInstallSymbol(sym);
	}

	synchronized long rniJavaToXref(Object o) {
		return super.rniJavaToXref(o);
	}

	synchronized Object rniXrefToJava(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniXrefToJava(exp);
	}

	public int rniStop(int flag) {
		return super.rniStop(flag);
	}

	public synchronized void rniAssign(String name, long exp, long rho) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		super.rniAssign(name, exp, rho);
	}

	public synchronized int rniExpType(long exp) {
		if (exp <= 0)
			throw new RuntimeException("bad expression id");
		return super.rniExpType(exp);
	}

}
