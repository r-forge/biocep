/*
 * Biocep: R-based Platform for Computational e-Science.
 *  
 * Copyright (C) 2009 Karim Chine - karim.chine@m4x.org
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
package org.kchine.r;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.rmi.RemoteException;

import org.kchine.r.server.AssignInterface;
import org.kchine.r.server.Utils;



/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RUnknownRef extends RUnknown implements org.kchine.r.server.ReferenceInterface, org.kchine.r.server.StandardReference, Externalizable {
	private long[] _rObjectIdHolder;

	private String _slotsPath;

	private org.kchine.r.server.AssignInterface _assignInterface;

	public void setAssignInterface(AssignInterface assignInterface) {
		_assignInterface = assignInterface;
	}

	public org.kchine.r.server.AssignInterface getAssignInterface() {
		return _assignInterface;
	}

	public RObject extractRObject() {
		try {
			return _assignInterface.getObjectFromReference(this);
		} catch (RemoteException re) {
			throw new RuntimeException(org.kchine.r.server.Utils.getStackTraceAsString(re));
		}
	}

	public long getRObjectId() {
		return _rObjectIdHolder[0];
	}

	public String getSlotsPath() {
		return _slotsPath;
	}

	public RUnknownRef() {
		super();
		_rObjectIdHolder = new long[1];
	}

	public RUnknownRef(long rObjectId, String slotsPath) {
		super();
		_rObjectIdHolder = new long[1];
		_rObjectIdHolder[0] = rObjectId;
		_slotsPath = slotsPath;
	}

	public RUnknownRef(long[] rObjectIdHolder, String slotsPath) {
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

	public boolean equals(Object inputObject) {
		if (inputObject == null || !(inputObject instanceof RUnknownRef))
			return false;
		return ((RUnknownRef) inputObject)._rObjectIdHolder[0] == _rObjectIdHolder[0] && ((RUnknownRef) inputObject)._slotsPath.equals(_slotsPath);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		try {
			result.append("A Reference to an object of Class \"RUnknown\" on the R servant <" + _assignInterface.getName() + ">  [" + _rObjectIdHolder[0] + "/"
					+ _slotsPath + "]\n");
		} catch (java.rmi.RemoteException e) {
			e.printStackTrace();
		}
		return result.toString();
	}


	@Override
	public int[] getValue() {
		throw new RuntimeException("not yet implemented feature");
	}

	
	@Override
	public void setValue(int[] value) {
		throw new RuntimeException("not yet implemented feature");
	}
	

	@Override
	public RList getAttributes() {
		try {
			return _assignInterface.getAttributes(_rObjectIdHolder[0], _slotsPath);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}

	}

	@Override
	public void setAttributes(RList attrs) {
		try {
			_rObjectIdHolder[0] = _assignInterface.setAttributes(_rObjectIdHolder[0], _slotsPath, attrs);
		} catch (Exception e) {
			throw new RuntimeException(Utils.getStackTraceAsString(e));
		}
	}

}
