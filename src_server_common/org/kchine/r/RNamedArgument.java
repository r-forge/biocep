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
package org.kchine.r;


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RNamedArgument extends RObject {
	private String _name;
	private Object _robject;

	public RNamedArgument() {
	}

	public RNamedArgument(String name, Object robject) {
		this._name = name;
		this._robject = robject;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public void setRobject(Object _robject) {
		this._robject = _robject;
	}

	public String getName() {
		return _name;
	}

	public Object getRobject() {
		return _robject;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return ((RNamedArgument) obj)._name.equals(_name) && ((RNamedArgument) obj)._robject.equals(_robject);
	}
}
