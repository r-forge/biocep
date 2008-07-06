package uk.ac.ebi.microarray.pools.db.monitor;

import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_HOST;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_NAME;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_PASSWORD;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_PORT;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_TYPE;
import static uk.ac.ebi.microarray.pools.PoolUtils.DEFAULT_DB_USER;
import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;

import java.sql.Connection;
import java.sql.DriverManager;

import uk.ac.ebi.microarray.pools.db.DBLayer;
import uk.ac.ebi.microarray.pools.db.DBLayerInterface;
import uk.ac.ebi.microarray.pools.db.DBLayerProvider;

public class DefaultDBLayerProvider implements DBLayerProvider{	
	public DBLayerInterface getDBLayer() throws Exception{
		String _DB_TYPE =  System.getProperty("pools.dbmode.type") != null && !System.getProperty("pools.dbmode.type").equals("") ? System.getProperty("pools.dbmode.type") : DEFAULT_DB_TYPE;
		String _DB_HOST = System.getProperty("pools.dbmode.host") != null && !System.getProperty("pools.dbmode.host").equals("") ? System.getProperty("pools.dbmode.host") : DEFAULT_DB_HOST;
		int    _DB_PORT = System.getProperty("pools.dbmode.port") != null && !System.getProperty("pools.dbmode.port").equals("") ? Integer.decode(System.getProperty("pools.dbmode.port")) : DEFAULT_DB_PORT;		
		String _DB_NAME = System.getProperty("pools.dbmode.name") != null && !System.getProperty("pools.dbmode.name").equals("") ? System.getProperty("pools.dbmode.name") : DEFAULT_DB_NAME;	
		
		 String driver=null;
		 String url=null;
		 String user=null;
		 String password=null;
		 DBLayerInterface dbLayer = null;
		
		if (_DB_TYPE.equals("derby")) {
			url = "jdbc:derby://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME+";create=true";
			driver="org.apache.derby.jdbc.ClientDriver";
		} else if (_DB_TYPE.equals("mysql")) {			
			url = "jdbc:mysql://"+_DB_HOST+":"+_DB_PORT+"/"+_DB_NAME;			
			driver="org.gjt.mm.mysql.Driver";
				
		} else if (_DB_TYPE.equals("oracle")) {			
			url = "jdbc:oracle:thin:@"+_DB_HOST+":"+_DB_PORT+":"+_DB_NAME; 
			driver="oracle.jdbc.driver.OracleDriver";
		}
		
		user = System.getProperty("pools.dbmode.user") != null && !System.getProperty("pools.dbmode.user").equals("") ? System.getProperty("pools.dbmode.user") : DEFAULT_DB_USER;
		password = System.getProperty("pools.dbmode.password") != null && !System.getProperty("pools.dbmode.password").equals("") ? System.getProperty("pools.dbmode.password") : DEFAULT_DB_PASSWORD;
		
		Class.forName(driver);
		Connection conn = DriverManager.getConnection(url, user, password);
		dbLayer = DBLayer.getLayer(getDBType(url), conn);
		conn.commit();
		
		return dbLayer;
	}
}
