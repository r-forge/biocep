/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
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
package org.bioconductor.packages.rservices;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RUnknownObjectName extends RUnknown implements ObjectNameInterface{
	private String _name; 
	private String _env;
	
	public RUnknownObjectName() {
	}

	public RUnknownObjectName(String name) {
		this._name = name;
		this._env = ".GlobalEnv";
	}

	public RUnknownObjectName(String environment, String name) {
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
		return "RRawObjectName:"+_env+"$"+_name;
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