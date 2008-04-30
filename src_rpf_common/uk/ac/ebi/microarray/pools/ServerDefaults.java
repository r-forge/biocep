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
package uk.ac.ebi.microarray.pools;

import java.rmi.registry.LocateRegistry;

import org.apache.commons.logging.Log;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public abstract class ServerDefaults {

	public static String _servantPoolPrefix;
	public static String _registryHost;
	public static int _registryPort;
	public static int _memoryMin;
	public static int _memoryMax;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServerDefaults.class);

	static {

		_servantPoolPrefix = System.getProperty("prefix") != null && !System.getProperty("prefix").equals("") ? System.getProperty("prefix") : DEFAULT_PREFIX;
		_registryHost = System.getProperty("registryhost") != null && !System.getProperty("registryhost").equals("") ? System.getProperty("registryhost")
				: DEFAULT_REGISTRY_HOST;
		_registryPort = System.getProperty("registryport") != null && !System.getProperty("registryport").equals("") ? Integer.decode(System
				.getProperty("registryport")) : DEFAULT_REGISTRY_PORT;

		
		_memoryMin = System.getProperty("memorymin") != null && !System.getProperty("memorymin").equals("") ? Integer.decode(System
				.getProperty("memorymin")) : DEFAULT_MEMORY_MIN;

		_memoryMax = System.getProperty("memorymax") != null && !System.getProperty("memorymax").equals("") ? Integer.decode(System
				.getProperty("memorymax")) : DEFAULT_MEMORY_MAX;

		
	}
	
	public static boolean isRegistryProvided() {
		return (System.getProperty("registryhost") != null && !System.getProperty("registryhost").equals("")) || (System.getProperty("registryport") != null && !System.getProperty("registryport").equals(""));
	}
	
	public static boolean isRegistryAccessible() {
		try {
			LocateRegistry.getRegistry(_registryHost, _registryPort).list();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
