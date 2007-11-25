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

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class RNamedArgument extends RObject {
	private String _name;
	private RObject _robject;

	public RNamedArgument() {
	}

	public RNamedArgument(String name, RObject robject) {
		this._name = name;
		this._robject = robject;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public void setRobject(RObject _robject) {
		this._robject = _robject;
	}

	public String getName() {
		return _name;
	}

	public RObject getRobject() {
		return _robject;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return ((RNamedArgument) obj)._name.equals(_name) && ((RNamedArgument) obj)._robject.equals(_robject);
	}
}
