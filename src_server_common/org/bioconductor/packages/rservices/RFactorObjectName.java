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


/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RFactorObjectName extends RFactor implements ObjectNameInterface{
	private String _name; 
	private String _env;
	
	public RFactorObjectName() {
	}

	public RFactorObjectName(String name) {
		this._name = name;
		this._env = ".GlobalEnv";
	}

	public RFactorObjectName(String environment, String name) {
		this._name = name;
		this._env = environment;
	}

	
	public String getRObjectName() {return _name;}
	public void setRObjectName(String _name) {this._name = _name;}
	public String getRObjectEnvironment() {return _env;}
	public void setRObjectEnvironment(String _env) {this._env = _env;}


	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ObjectNameInterface) )	return false;
		return (((ObjectNameInterface) obj).getRObjectName().equals(this._name)) && (((ObjectNameInterface) obj).getRObjectEnvironment().equals(_env));
	}
	
	public String toString() {
		return "RFactorObjectName:"+_env+"$"+_name;
	}

    public void writeExternal(java.io.ObjectOutput out)
        throws java.io.IOException {
    	out.writeUTF(_env);
    	out.writeUTF(_name);
    }

    public void readExternal(java.io.ObjectInput in)
        throws java.io.IOException, ClassNotFoundException {
    	_env=in.readUTF();
    	_name=in.readUTF();
    }
}