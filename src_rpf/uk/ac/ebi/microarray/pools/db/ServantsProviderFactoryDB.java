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
package uk.ac.ebi.microarray.pools.db;

import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import uk.ac.ebi.microarray.pools.ManagedServant;
import uk.ac.ebi.microarray.pools.ServantProvider;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;
import uk.ac.ebi.microarray.pools.TimeoutException;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
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
