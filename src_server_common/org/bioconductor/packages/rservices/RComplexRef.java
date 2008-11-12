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
package org.bioconductor.packages.rservices;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

import remoting.AssignInterface;
import util.Utils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RComplexRef extends RComplex implements mapping.ReferenceInterface, mapping.StandardReference, Externalizable {
	private long[] _rObjectIdHolder;

	private String _slotsPath;

	private remoting.AssignInterface _assignInterface;

	public void setAssignInterface(AssignInterface assignInterface) {
		_assignInterface = assignInterface;
	}

	public AssignInterface getAssignInterface() {
		return _assignInterface;
	}

	public RObject extractRObject() {
		try {
			return _assignInterface.getObjectFromReference(this);
		} catch (RemoteException re) {
			throw new RuntimeException(util.Utils.getStackTraceAsString(re));
		}
	}

	public long getRObjectId() {
		return _rObjectIdHolder[0];
	}

	public String getSlotsPath() {
		return _slotsPath;
	}

	public RComplexRef() {
		super();
		_rObjectIdHolder = new long[1];
	}

	public RComplexRef(long rObjectId, String slotsPath) {
		super();
		_rObjectIdHolder = new long[1];
		_rObjectIdHolder[0] = rObjectId;
		_slotsPath = slotsPath;
	}

	public RComplexRef(long[] rObjectIdHolder, String slotsPath) {
		super();
		_rObjectIdHolder = rObjectIdHolder;
		_slotsPath = slotsPath;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(_rObjectIdHolder[0]);
		out.writeUTF(_slotsPath);
		out.writeObject(_assignInterface);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		_rObjectIdHolder[0] = in.readLong();
		_slotsPath = in.readUTF();
		_assignInterface = (AssignInterface) in.readObject();
	}

	@Override
	public boolean equals(Object inputObject) {
		if (inputObject == null || !(inputObject instanceof RComplexRef))
			return false;
		return ((RComplexRef) inputObject)._rObjectIdHolder[0] == _rObjectIdHolder[0] && ((RComplexRef) inputObject)._slotsPath.equals(_slotsPath);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append("A Reference to an object of Class \"RComplex\" on the R servant <" + _assignInterface.getName() + ">  [" + _rObjectIdHolder[0] + "/"
					+ _slotsPath + "]\n");
		} catch (java.rmi.RemoteException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	@Override
	public String[] getNames() {
		try {
			return _assignInterface.getNames(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setNames(String[] names) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setNames(_rObjectIdHolder[0], _slotsPath, names);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public double[] getImaginary() {
		try {
			return _assignInterface.getValueCPImaginary(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public double[] getReal() {
		try {
			return _assignInterface.getValueCPReal(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setImaginary(double... _imaginary) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setValueCP(_rObjectIdHolder[0], _slotsPath, getReal(), _imaginary);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setImaginaryI(Double imaginary) {
		super.setImaginary(new double[] { imaginary });
	}

	@Override
	public void setReal(double... _real) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setValueCP(_rObjectIdHolder[0], _slotsPath, _real, getImaginary());
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setRealI(Double real) {
		setReal(new double[] { real });
	}

	@Override
	public int length() {
		try {
			return _assignInterface.length(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public int[] getIndexNA() {
		try {
			return _assignInterface.getIndexNA(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setIndexNA(int[] indexNA) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setIndexNA(_rObjectIdHolder[0], _slotsPath, indexNA);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public String getOutputMsg() {
		try {
			return _assignInterface.getOutputMsg(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}

	}

	@Override
	public void setOutputMsg(String msg) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setOutputMsg(_rObjectIdHolder[0], _slotsPath, msg);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

}
