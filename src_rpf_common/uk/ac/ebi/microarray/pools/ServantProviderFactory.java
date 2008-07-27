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
package uk.ac.ebi.microarray.pools;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public abstract class ServantProviderFactory {

	static ServantProviderFactory _defaultFacory;

	synchronized public static void init() {
		if (_defaultFacory != null)
			return;
		PoolUtils.injectSystemProperties(true);
		PoolUtils.initRmiSocketFactory();
		if (System.getProperty("pools.provider.factory") != null && !System.getProperty("pools.provider.factory").equals("")) {
			try {
				_defaultFacory = (ServantProviderFactory) Class.forName(System.getProperty("pools.provider.factory")).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			try {
				_defaultFacory = (ServantProviderFactory) Class.forName("uk.ac.ebi.microarray.pools.reg.ServantProviderFactoryReg").newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// static { init(); }

	public static ServantProviderFactory getFactory() {
		if (_defaultFacory == null)
			init();
		return _defaultFacory;
	}

	public abstract ServantProvider getServantProvider();

}
