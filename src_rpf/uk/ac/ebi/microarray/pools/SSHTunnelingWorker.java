package uk.ac.ebi.microarray.pools;

import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;

public class SSHTunnelingWorker {
	
	public static HashMap<String, Object> servantMap=new HashMap<String, Object>();
	public static Object invoke(String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {		
		Object servant = servantMap.get(servantName);
		if (servant == null) {
			throw new SSHTunnelingException("Bad Servant Name :" + servantName);
		}
		Method m = null;
		try {
			m=servant.getClass().getMethod(methodName, methodSignature);
		} catch (NoSuchMethodException e) {
			throw new SSHTunnelingException("Bad Method Name :" + methodName);
		}
		Object result=null;
		try {
			result = m.invoke(servant, methodParameters);
		} catch (InvocationTargetException e) {
			throw new SSHTunnelingException("",e.getCause());
		} catch (Exception e) {
			throw new SSHTunnelingException("",e);
		}		
		return result;
	}

	public static void main(String[] args) {
		try {			
			
			final String dbdriver="org.apache.derby.jdbc.ClientDriver";
			final String dburl="jdbc:derby://127.0.0.1:1527/DWEP;create=true";
			final String dbuser="DWEP";
			final String dbpassword="DWEP";		
			
			Class.forName(dbdriver);
			DBLayer registry = DBLayer.getLayer(getDBType(dburl), new ConnectionProvider() {
				public Connection newConnection() throws java.sql.SQLException {
					return DriverManager.getConnection(dburl, dbuser, dbpassword);
				};
			});			
			servantMap.put("derby", registry);		

			
			Properties invokationProps=new Properties();
			String fileIn=args[0];
			invokationProps.loadFromXML(new FileInputStream(fileIn));			
			Object result=null;
			
			try {
				result=invoke(invokationProps.getProperty("servantName"), invokationProps.getProperty("methodName"), 
						(Class<?>[])PoolUtils.hexToObject(invokationProps.getProperty("methodSignature")), (Object[])PoolUtils.hexToObject(invokationProps.getProperty("methodParameters")) );
			} catch (SSHTunnelingException e) {
				result=e;
			} catch (Exception e) {
				result=new SSHTunnelingException("",e);
			}			
			
			Properties invokationResult=new Properties();
			invokationResult.put("result", PoolUtils.objectToHex(result));
			String fileOut=fileIn.substring(0, fileIn.lastIndexOf("."))+".out";
			FileOutputStream fos=new FileOutputStream(fileOut);
			invokationResult.storeToXML(fos,"");
			fos.close();
			
			
			System.out.println("->XML");
			BufferedReader br=new BufferedReader(new FileReader(fileOut));
			String line=null;
			while ((line=br.readLine())!=null) System.out.println(line);
			br.close();
			
			new File(fileIn).delete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}