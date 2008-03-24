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
package uk.ac.ebi.microarray.pools.reg;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.impl.GenericObjectPool;

import uk.ac.ebi.microarray.pools.reg.ServantsProxyFactoryReg;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
