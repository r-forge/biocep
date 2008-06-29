package uk.ac.ebi.microarray.pools;

import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Properties;
import uk.ac.ebi.microarray.pools.db.ConnectionProvider;
import uk.ac.ebi.microarray.pools.db.DBLayer;

public class SSHTunnelingWorker {

	public static HashMap<String, Object> servantMap = new HashMap<String, Object>();

	public static Object invoke(String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {
		Object servant = servantMap.get(servantName);
		if (servant == null) {
			throw new SSHTunnelingException("Bad Servant Name :" + servantName);
		}
		Method m = null;
		try {
			m = servant.getClass().getMethod(methodName, methodSignature);
		} catch (NoSuchMethodException e) {
			throw new SSHTunnelingException("Bad Method Name :" + methodName);
		}
		Object result = null;
		try {
			result = m.invoke(servant, methodParameters);
		} catch (InvocationTargetException e) {
			throw new SSHTunnelingException("", e.getCause());
		} catch (Exception e) {
			throw new SSHTunnelingException("", e);
		}
		return result;
	}

	public static void init() throws Exception {
		Class.forName(ServerDefaults._dbDriver);
		DBLayer registry = DBLayer.getLayer(getDBType(ServerDefaults._dbUrl), new ConnectionProvider() {
			public Connection newConnection() throws java.sql.SQLException {
				return DriverManager.getConnection(ServerDefaults._dbUrl, ServerDefaults._dbUser, ServerDefaults._dbPassword);
			};
		});
		servantMap.put("db", registry);
		servantMap.put("registry", LocateRegistry.getRegistry(ServerDefaults._registryHost,ServerDefaults._registryPort));
	}

	public static void main(String[] args) {
		String fileIn = args[0];
		Object result = null;
		try {
			init();
			Properties invokationProps = new Properties();
			invokationProps.loadFromXML(new FileInputStream(fileIn));
			result = invoke(invokationProps.getProperty("servantName"), invokationProps.getProperty("methodName"), (Class<?>[]) PoolUtils
					.hexToObject(invokationProps.getProperty("methodSignature")), (Object[]) PoolUtils.hexToObject(invokationProps
					.getProperty("methodParameters")));
		} catch (SSHTunnelingException e) {
			result = e;
		} catch (Exception e) {
			result = new SSHTunnelingException(e.getMessage());
		}

		try {
			String resultBuffer=PoolUtils.objectToHex(result);
			System.out.println("result Buffer length:"+resultBuffer.length());
			System.out.println("->Result Start");
			
			int b=300;
			int d=resultBuffer.length()/b;
			int m=resultBuffer.length()%b;
			
			for (int i=0; i<d;++i) {
				System.out.println(resultBuffer.substring(i*b,i*(b+1)));
			}
			if (m>0) System.out.println(resultBuffer.substring(d*b,resultBuffer.length()));

			
			/*
			for (int i=0; i<resultBuffer.length();++i) {
				System.out.print(new String(new char[]{resultBuffer.charAt(i)}));
				if (i>0 && i%300==0) System.out.println(); 
			}
			*/
			
			
			
			System.out.println();
			System.out.println("->Result End");
			System.out.flush();
			new File(fileIn).delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
