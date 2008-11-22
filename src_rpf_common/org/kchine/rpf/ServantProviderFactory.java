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
package org.kchine.rpf;

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
				_defaultFacory = (ServantProviderFactory) Class.forName("org.kchine.rpf.reg.ServantProviderFactoryReg").newInstance();
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
