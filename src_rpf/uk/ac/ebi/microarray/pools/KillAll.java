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
