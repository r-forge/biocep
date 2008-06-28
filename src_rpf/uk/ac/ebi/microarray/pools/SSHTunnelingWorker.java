package uk.ac.ebi.microarray.pools;

import static uk.ac.ebi.microarray.pools.PoolUtils.getDBType;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		final String dbdriver = "org.apache.derby.jdbc.ClientDriver";
		final String dburl = "jdbc:derby://127.0.0.1:1527/DWEP;create=true";
		final String dbuser = "DWEP";
		final String dbpassword = "DWEP";
		Class.forName(dbdriver);
		DBLayer registry = DBLayer.getLayer(getDBType(dburl), new ConnectionProvider() {
			public Connection newConnection() throws java.sql.SQLException {
				return DriverManager.getConnection(dburl, dbuser, dbpassword);
			};
		});
		servantMap.put("derby", registry);
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
			result = new SSHTunnelingException("", e);
		}

		try {
			System.out.println("->Result");
			System.out.println(PoolUtils.objectToHex(result));
			new File(fileIn).delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
