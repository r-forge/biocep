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
package remoting;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class FileDescription implements Serializable {
	private String _name;
	private long _size;
	private boolean _type;
	private Date _modifiedOn;

	public FileDescription() {
	}

	public FileDescription(String name, long size, boolean isDir, Date modifiedOn) {
		this._name = name;
		this._size = size;
		this._type = isDir;
		this._modifiedOn = modifiedOn;

	}

	public Date getModifiedOn() {
		return _modifiedOn;
	}

	public void setModifiedOn(Date modifiedOn) {
		this._modifiedOn = modifiedOn;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public long getSize() {
		return _size;
	}

	public void setSize(long size) {
		this._size = size;
	}

	public boolean isDir() {
		return _type;
	}

	public void setType(boolean isDir) {
		this._type = isDir;
	}

	public String toString() {
		return "[name=" + _name + ",size=" + _size + ",type=" + _type + ",modified on=" + _modifiedOn + "]";
	}
}
