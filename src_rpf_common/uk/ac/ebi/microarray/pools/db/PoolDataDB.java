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
package uk.ac.ebi.microarray.pools.db;

import java.io.Serializable;

import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class PoolDataDB implements Serializable{
	private String _poolName;
	private int _borrowTimeout;
	private String[] _prefixes;

	public PoolDataDB(String name, String[] prefixes, int timeout) {
		super();
		_poolName = name;
		_prefixes = prefixes;
		_borrowTimeout = timeout;
	}

	public int getBorrowTimeout() {
		return _borrowTimeout;
	}

	public String getPoolName() {
		return _poolName;
	}

	public String[] getPrefixes() {
		return _prefixes;
	}

	public String toString() {
		return "PoolData[name=" + _poolName + " prefixes=" + PoolUtils.flatArray(_prefixes) + " bto=" + _borrowTimeout + "]";
	}

}
