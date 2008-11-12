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
package uk.ac.ebi.microarray.pools.reg;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.impl.GenericObjectPool;

import uk.ac.ebi.microarray.pools.reg.ServantsProxyFactoryReg;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ServantProxyPoolSingletonReg {
	static java.util.Hashtable<String, GenericObjectPool> _pool = new Hashtable<String, GenericObjectPool>();

	static Integer lock = new Integer(0);

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServantProxyPoolSingletonReg.class);

	public static GenericObjectPool getInstance(String registryHost, int registryPort, String poolNamingPrefix) {

		String key = registryHost + '/' + registryPort + '/' + poolNamingPrefix;
		if (_pool.get(key) != null)
			return _pool.get(key);

		synchronized (lock) {

			if (_pool.get(key) == null) {
				GenericObjectPool p = new GenericObjectPool(new ServantsProxyFactoryReg(registryHost, registryPort, poolNamingPrefix));
				_pool.put(key, p);
				p.setTestOnBorrow(true);
				p.setTestOnReturn(true);
			}
			return _pool.get(key);
		}
	}
}
