/*
 * Copyright (C) 2007 EMBL-EBI
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
package org.bioconductor.packages.rservices;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

import remoting.AssignInterface;
import util.Utils;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class RMatrixRef extends RMatrix implements mapping.ReferenceInterface, mapping.StandardReference,
		Externalizable {
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

	public RMatrixRef() {
		super();
		_rObjectIdHolder = new long[1];
	}

	public RMatrixRef(long rObjectId, String slotsPath) {
		super();
		_rObjectIdHolder = new long[1];
		_rObjectIdHolder[0] = rObjectId;
		_slotsPath = slotsPath;
	}

	public RMatrixRef(long[] rObjectIdHolder, String slotsPath) {
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
		if (inputObject == null || !(inputObject instanceof RMatrixRef))
			return false;
		return ((RMatrixRef) inputObject)._rObjectIdHolder[0] == _rObjectIdHolder[0]
				&& ((RMatrixRef) inputObject)._slotsPath.equals(_slotsPath);
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append("A Reference to an object of Class \"RMatrix\" on the R servant <"
					+ _assignInterface.getName() + ">  [" + _rObjectIdHolder[0] + "/" + _slotsPath + "]\n");
		} catch (java.rmi.RemoteException e) {
			e.printStackTrace();
		}
		return result.toString();
	}

	@Override
	public int[] getDim() {
		try {
			return _assignInterface.getArrayDim(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public RList getDimnames() {
		try {
			return _assignInterface.getArrayDimnames(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public RVector getValue() {
		try {
			return _assignInterface.getArrayValue(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setDim(int[] dim) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setArrayDim(_rObjectIdHolder[0], _slotsPath, dim);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setDimnames(RList dimnames) throws Exception {
		try {
			_rObjectIdHolder[0] = _assignInterface.setArrayDimnames(_rObjectIdHolder[0], _slotsPath, dimnames);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

	@Override
	public void setValue(RVector value) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setArrayValue(_rObjectIdHolder[0], _slotsPath, value);
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
