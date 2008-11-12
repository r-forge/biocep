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
