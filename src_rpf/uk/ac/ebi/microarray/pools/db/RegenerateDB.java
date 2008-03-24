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

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;

/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class RegenerateDB {
	public static void main(String[] args) throws Exception {
		final String dburl = System.getProperty("db.url");
		final String user = System.getProperty("db.user");
		final String password = System.getProperty("db.password");
		System.out.println("DB url:" + dburl);
		System.out.println("DB user:" + user);
		System.out.println("DB password:" + password);
		Class.forName(System.getProperty("db.driver"));

		DBLayer dbLayer = DBLayer.getLayer(getDBType(dburl), new ConnectionProvider() {
			public Connection newConnection() throws SQLException {
				return DriverManager.getConnection(dburl, user, password);
			}
		});

		dbLayer.applyDBScript(RegenerateDB.class.getResourceAsStream("/dbscript.sql"));
		String fillDbScriptName = System.getProperty("db.initscript");
		if (fillDbScriptName != null && !fillDbScriptName.equals("")) {
			dbLayer.applyDBScript(new FileInputStream(fillDbScriptName));
		}

	}
}
