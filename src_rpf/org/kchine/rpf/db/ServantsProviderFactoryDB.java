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
package org.kchine.rpf.db;

import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.kchine.rpf.ManagedServant;
import org.kchine.rpf.ServantProvider;
import org.kchine.rpf.ServantProviderFactory;
import org.kchine.rpf.TimeoutException;

import static org.kchine.rpf.PoolUtils.*;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class ServantsProviderFactoryDB extends ServantProviderFactory {

	private final Log log = org.apache.commons.logging.LogFactory.getLog(ServantsProviderFactoryDB.class);
	private ServantProvider _servantProvider = null;

	public ServantsProviderFactoryDB() throws Exception {
		super();
		_servantProvider = new ServantProviderDB();
	}

	@Override
	public ServantProvider getServantProvider() {
		return _servantProvider;
	}

}