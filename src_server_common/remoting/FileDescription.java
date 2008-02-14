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
package remoting;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Karim Chine kchine@ebi.ac.uk
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
