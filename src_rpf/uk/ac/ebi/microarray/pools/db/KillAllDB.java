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
package uk.ac.ebi.microarray.pools.db;

import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JFrame;

import uk.ac.ebi.microarray.pools.ServerDefaults;
import uk.ac.ebi.microarray.pools.db.monitor.SupervisorUtils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class KillAllDB {
	public static void main(String[] args) {
		try {

			Class.forName(ServerDefaults._dbDriver);

			final DBLayer dbLayer = DBLayer.getLayer(getDBType(ServerDefaults._dbUrl), new ConnectionProvider() {
				public Connection newConnection() throws SQLException {
					return DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
				}
			});

			Vector<HashMap<String, Object>> servants = dbLayer.getTableData("SERVANTS");

			final JFrame frame = null;

			for (int i = 0; i < servants.size(); ++i) {
				final String servantName = (String) servants.elementAt(i).get("NAME");
				final String hostIp = (String) servants.elementAt(i).get("HOST_IP");
				final String processId = (String) servants.elementAt(i).get("PROCESS_ID");

				new Thread(new Runnable() {
					public void run() {
						try {
							System.out.println("killing servant<" + servantName + "> host ip:" + hostIp + " process id:" + processId);
							new SupervisorUtils().killProcess(servantName, true, frame);
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
