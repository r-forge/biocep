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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.commons.logging.Log;

import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;

import static uk.ac.ebi.microarray.pools.ServerDefaults.*;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public abstract class KillAll {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(KillAll.class);

	public static void main(String[] args) throws Exception {
		String stub = System.getProperty("stub");
		if (stub == null || stub.equals("")) {

			Registry rmiRegistry = null;
			final String dburl = System.getProperty("db.url");
			if (dburl != null && !dburl.equals("")) {
				final String user = System.getProperty("db.user");
				final String password = System.getProperty("db.password");
				System.out.println("DB url:" + dburl);
				System.out.println("DB user:" + user);
				System.out.println("DB password:" + password);
				Class.forName(System.getProperty("db.driver"));
				rmiRegistry = DBLayer.getLayer(getDBType(dburl), new ConnectionProvider() {
					public Connection newConnection() throws java.sql.SQLException {
						return DriverManager.getConnection(dburl, user, password);
					};

				});
			} else {
				rmiRegistry = LocateRegistry.getRegistry(_registryHost, _registryPort);
			}

			String[] servantNames = rmiRegistry.list();

			for (int i = 0; i < servantNames.length; ++i) {
				if (PoolUtils.shortRmiName(servantNames[i]).startsWith(_servantPoolPrefix)) {
					try {
						((ManagedServant) rmiRegistry.lookup(servantNames[i])).die();
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}

		} else {

			((ManagedServant) PoolUtils.hexToStub(stub, KillAll.class.getClassLoader())).die();
		}

	}
}
