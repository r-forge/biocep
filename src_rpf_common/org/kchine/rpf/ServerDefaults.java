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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.kchine.rpf.db.ConnectionProvider;
import org.kchine.rpf.db.DBLayer;

import static org.kchine.rpf.PoolUtils.*;

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
		init();
	}
	
	public static void init()	{

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
		String _DB_DIR = System.getProperty("db.dir") != null && !System.getProperty("db.dir").equals("") ? System.getProperty("db.dir") : DEFAULT_DB_DIR;
		_DB_DIR=_DB_DIR.replace('\\', '/');	if (!_DB_DIR.equals("") && !_DB_DIR.endsWith("/")) _DB_DIR=_DB_DIR+"/";
		System.out.println("DB Dir:"+_DB_DIR);
		
		if (_DB_TYPE.equals("derby")) {
			_dbUrl = "jdbc:derby://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_DIR+_DB_NAME+";create=true";
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
					
				} else if (_namingMode.equals("self")){
					_registry = LocalRmiRegistry.getInstance();
				} else if (_namingMode.equals("registry")) {
					_registry = LocateRegistry.getRegistry(_registryHost, _registryPort);
				} else {					
					_registry = ((RegistryProvider)ServerDefaults.class.forName("genericnaming."+_namingMode+"Class").newInstance() ).getRegistry(System.getProperties());					
				}
			}
			return _registry;
		}
	}

	
	
	public static Registry getRegistry(Properties props) throws Exception	{		
		String namingMode;		
		String registryHost;
		int registryPort;
		String dbUrl=null;
		String dbDriver=null;
		String dbUser;
		String dbPassword;			
		namingMode = (String)props.get("naming.mode") != null && !props.get("naming.mode").equals("") ? (String)props.get("naming.mode") : DEFAULT_NAMING_MODE;

		registryHost = (String)props.get("registry.host") != null && !props.get("registry.host").equals("") ? (String)props.get("registry.host")
				: DEFAULT_REGISTRY_HOST;		
		registryPort = (String)props.get("registry.port") != null && !props.get("registry.port").equals("") ? Integer.decode((String)props.get("registry.port")) : DEFAULT_REGISTRY_PORT;
				
		String _DB_TYPE =  (String)props.get("db.type") != null && !props.get("db.type").equals("") ? (String)props.get("db.type") : DEFAULT_DB_TYPE;
		String _DB_HOST = (String)props.get("db.host") != null &&  !props.get("db.host").equals("") ? (String)props.get("db.host") : DEFAULT_DB_HOST;
		int    _DB_PORT = (String)props.get("db.port") != null &&  !props.get("db.port").equals("") ? Integer.decode((String)props.get("db.port")) : DEFAULT_DB_PORT;		
		String _DB_NAME = (String)props.get("db.name") != null &&  !props.get("db.name").equals("") ? (String)props.get("db.name") : DEFAULT_DB_NAME;			
		String _DB_DIR = (String)props.get("db.dir") != null && !props.get("db.dir").equals("") ? (String)props.get("db.dir") : DEFAULT_DB_DIR;
		_DB_DIR=_DB_DIR.replace('\\', '/');if (!_DB_DIR.equals("") && !_DB_DIR.endsWith("/")) _DB_DIR=_DB_DIR+"/";
		
		
		if (_DB_TYPE.equals("derby")) {
			dbUrl = "jdbc:derby://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_DIR+_DB_NAME+";create=true";
			dbDriver="org.apache.derby.jdbc.ClientDriver";
			
		} else if (_DB_TYPE.equals("mysql")) {			
			dbUrl = "jdbc:mysql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;			
			dbDriver="org.gjt.mm.mysql.Driver";
				
		} else if (_DB_TYPE.equals("oracle")) {			
			dbUrl = "jdbc:oracle:thin:@"+_DB_HOST+":"+_DB_PORT+":"+_DB_NAME; 
			dbDriver="oracle.jdbc.driver.OracleDriver";
		}
		
		dbUser = (String)props.get("db.user") != null && !props.get("db.user").equals("") ? (String)props.get("db.user") : DEFAULT_DB_USER;
		dbPassword = (String)props.get("db.password") != null && !props.get("db.password").equals("") ? (String)props.get("db.password") : DEFAULT_DB_PASSWORD;
		
		 Registry registry = null;
		 
		if (namingMode.equals("db")) {
			
			Class.forName(dbDriver);
			final String dbUrlFinal=dbUrl ;
			final String dbUserFinal=dbUser;
			final String dbPasswordFinal=dbPassword;				
			registry = DBLayer.getLayer(PoolUtils.getDBType(dbUrl), new ConnectionProvider() {
				public Connection newConnection() throws java.sql.SQLException {
					return DriverManager.getConnection(dbUrlFinal, dbUserFinal, dbPasswordFinal);
				};
			});
			
		}  else if (namingMode.equals("self")){
			registry = LocalRmiRegistry.getInstance();
		} else if (namingMode.equals("registry")) {
			registry = LocateRegistry.getRegistry(registryHost, registryPort);
		} else {					
			registry = ((RegistryProvider)ServerDefaults.class.forName("genericnaming."+namingMode+"Class").newInstance() ).getRegistry(props);					
		}
		
		

		return registry;
	}

	
}
