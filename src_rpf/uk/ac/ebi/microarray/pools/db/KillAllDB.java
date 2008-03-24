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

import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JFrame;
import uk.ac.ebi.microarray.pools.db.monitor.SupervisorUtils;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class KillAllDB {
	public static void main(String[] args) {
		try {

			final String dburl = System.getProperty("db.url");
			final String user = System.getProperty("db.user");
			final String password = System.getProperty("db.password");
			Class.forName(System.getProperty("db.driver"));

			final DBLayer dbLayer = DBLayer.getLayer(getDBType(dburl), new ConnectionProvider() {
				public Connection newConnection() throws SQLException {
					return DriverManager.getConnection(dburl, user, password);
				}
			});

			Vector<HashMap<String, Object>> servants = dbLayer.getTableData("SERVANTS");

			final JFrame frame = null;

			for (int i = 0; i < servants.size(); ++i) {
				final String servantName = (String) servants.elementAt(i).get("NAME");
				final String hostIp = (String) servants.elementAt(i).get("REGISTER_HOST_IP");
				final String processId = (String) servants.elementAt(i).get("REGISTER_PROCESS_ID");

				new Thread(new Runnable() {
					public void run() {
						try {
							System.out.println("killing servant<" + servantName + "> host ip:" + hostIp + " process id:" + processId);
							SupervisorUtils.killProcess(servantName, true, frame);
							dbLayer.unbind(servantName);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}

		} catch (Exception e) {
		}
	}
}
