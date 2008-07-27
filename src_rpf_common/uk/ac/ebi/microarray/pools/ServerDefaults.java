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
import java.sql.SQLException;

import org.apache.commons.logging.Log;

import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;
import static uk.ac.ebi.microarray.pools.PoolUtils.*;
import static uk.ac.ebi.microarray.pools.ServerDefaults._registryHost;
import static uk.ac.ebi.microarray.pools.ServerDefaults._registryPort;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public abstract class ServerDefaults {

	public static String _namingMode;	
	public static String _servantPoolPrefix;	
	public static String _registryHost;
	public static int _registryPort;
	public static String _dbUrl ;
	public static String _dbDriver ;
	public static String _dbUser;
	public static String _dbPassword;			
	public static int _memoryMin;
	public static int _memoryMax;

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(ServerDefaults.class);

	static {

		_namingMode = System.getProperty("naming.mode") != null && !System.getProperty("naming.mode").equals("") ? System.getProperty("naming.mode") : DEFAULT_NAMING_MODE;
		
		System.out.println("----> naming mode :"+_namingMode);
		
		
		_servantPoolPrefix = System.getProperty("prefix") != null && !System.getProperty("prefix").equals("") ? System.getProperty("prefix") : DEFAULT_PREFIX;

		_registryHost = System.getProperty("registry.host") != null && !System.getProperty("registry.host").equals("") ? System.getProperty("registry.host")
				: DEFAULT_REGISTRY_HOST;		
		_registryPort = System.getProperty("registry.port") != null && !System.getProperty("registry.port").equals("") ? Integer.decode(System
				.getProperty("registry.port")) : DEFAULT_REGISTRY_PORT;
		_memoryMin = System.getProperty("memorymin") != null && !System.getProperty("memorymin").equals("") ? Integer.decode(System
				.getProperty("memorymin")) : DEFAULT_MEMORY_MIN;
		_memoryMax = System.getProperty("memorymax") != null && !System.getProperty("memorymax").equals("") ? Integer.decode(System
				.getProperty("memorymax")) : DEFAULT_MEMORY_MAX;
				
		String _DB_TYPE =  System.getProperty("db.type") != null && !System.getProperty("db.type").equals("") ? System.getProperty("db.type") : DEFAULT_DB_TYPE;
		String _DB_HOST = System.getProperty("db.host") != null && !System.getProperty("db.host").equals("") ? System.getProperty("db.host") : DEFAULT_DB_HOST;
		int    _DB_PORT = System.getProperty("db.port") != null && !System.getProperty("db.port").equals("") ? Integer.decode(System.getProperty("db.port")) : DEFAULT_DB_PORT;		
		String _DB_NAME = System.getProperty("db.name") != null && !System.getProperty("db.name").equals("") ? System.getProperty("db.name") : DEFAULT_DB_NAME;	
		
		if (_DB_TYPE.equals("derby")) {
			_dbUrl = "jdbc:derby://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME+";create=true";
			_dbDriver="org.apache.derby.jdbc.ClientDriver";
		} else if (_DB_TYPE.equals("mysql")) {			
			_dbUrl = "jdbc:mysql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;			
			_dbDriver="org.gjt.mm.mysql.Driver";
				
		} else if (_DB_TYPE.equals("oracle")) {			
			_dbUrl = "jdbc:oracle:thin:@"+_DB_HOST+":"+_DB_PORT+":"+_DB_NAME; 
			_dbDriver="oracle.jdbc.driver.OracleDriver";
		}
		
		_dbUser = System.getProperty("db.user") != null && !System.getProperty("db.user").equals("") ? System.getProperty("db.user") : DEFAULT_DB_USER;
		_dbPassword = System.getProperty("db.password") != null && !System.getProperty("db.password").equals("") ? System.getProperty("db.password") : DEFAULT_DB_PASSWORD;
		
	}
	
	public static boolean isRegistryProvided() {
		return (System.getProperty("registryhost") != null && !System.getProperty("registryhost").equals("")) || (System.getProperty("registryport") != null && !System.getProperty("registryport").equals(""));
	}
	
	public static boolean isRegistryAccessible() {
		try {
			ServerDefaults.getRmiRegistry().list();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static Registry _registry = null;
	public static Integer _lock = new Integer(0);
	public static Registry getRmiRegistry() throws Exception {	
		if (_registry != null)
			return _registry;
		synchronized (_lock) {
			if (_registry == null) {
				if (_namingMode.equals("db")) {						
					Class.forName(_dbDriver);
					_registry = DBLayer.getLayer(PoolUtils.getDBType(_dbUrl), new ConnectionProvider() {
						public Connection newConnection() throws java.sql.SQLException {
							return DriverManager.getConnection(_dbUrl, _dbUser, _dbPassword);
						};
					});					
				} else {
					_registry = LocateRegistry.getRegistry(_registryHost, _registryPort);
				}
			}
			return _registry;
		}
	}

	
}
