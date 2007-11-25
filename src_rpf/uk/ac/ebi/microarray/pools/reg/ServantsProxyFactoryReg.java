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
package uk.ac.ebi.microarray.pools.reg;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.pool.PoolableObjectFactory;

import uk.ac.ebi.microarray.pools.ManagedServant;

/**
 * @author Karim Chine   kchine@ebi.ac.uk
 */
public class ServantsProxyFactoryReg implements PoolableObjectFactory {
	private Vector<ManagedServant> servantsInUse = new Vector<ManagedServant>();

	private String _poolNamingPrefix;

	private Registry _rmiRegistry;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServantsProxyFactoryReg.class);

	public ServantsProxyFactoryReg(String registryHost, int registryPort, String poolNamingPrefix) {
		super();
		_poolNamingPrefix = poolNamingPrefix;
		try {
			_rmiRegistry = LocateRegistry.getRegistry(registryHost, registryPort);
			_rmiRegistry.list();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void activateObject(Object obj) throws Exception {
	}

	public void destroyObject(Object obj) throws Exception {
		servantsInUse.remove(((ManagedServant) obj));
	}

	public Object makeObject() throws Exception {
		String[] servantNames = null;
		try {
			servantNames = _rmiRegistry.list();
		} catch (Exception e) {
			throw new NoSuchElementException("No R Servant available / No RMI Naming Server");
		}
		for (int i = 0; i < servantNames.length; ++i) {
			if (shortRmiName(servantNames[i]).startsWith(_poolNamingPrefix)) {
				try {
					ManagedServant servant = (ManagedServant) _rmiRegistry.lookup(servantNames[i]);
					if (servantsInUse.contains(servant))
						continue;
					servant.ping();
					servantsInUse.add(servant);
					return servant;
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		}
		throw new NoSuchElementException("No R Servant available");
	}

	public void passivateObject(Object obj) throws Exception {
		((ManagedServant) obj).reset();
	}

	public boolean validateObject(Object obj) {
		try {
			((ManagedServant) obj).ping();
			return true;
		} catch (Exception e) {
			log.info("## Validation failed, couldn't ping");
			return false;
		}
	}

	public static String shortRmiName(String fullRmiName) {
		return fullRmiName.substring(fullRmiName.lastIndexOf('/') + 1);
	}
}