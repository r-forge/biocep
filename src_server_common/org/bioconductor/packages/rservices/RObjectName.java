/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
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
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class RObjectName extends RObject {
	private String _name;
	private String _env;

	public RObjectName() {
	}

	public RObjectName(String name) {
		this._name = name;
		this._env = ".GlobalEnv";
	}

	public RObjectName(String name, String environment) {
		this._name = name;
		this._env = environment;
	}
	
	public void setName(String _name) {
		this._name = _name;
	}

	public String getName() {
		return _name;
	}

	public void setEnv(String _env) {
		this._env = _env;
	}

	public String getEnv() {
		return _env;
	}

	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return (((RObjectName) obj)._name.equals(this._name)) && (((RObjectName) obj)._env.equals(_env));
	}

}
