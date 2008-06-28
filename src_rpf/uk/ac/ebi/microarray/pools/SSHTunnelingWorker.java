package uk.ac.ebi.microarray.pools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

public class SSHTunnelingWorker {
	
	private static class Logger {
		public String sayHello(String name) {
			return "Hello"+" "+name+"!";
		}
	}
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
			servantMap.put("logger", new Logger());		
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
			invokationResult.storeToXML(new FileOutputStream(fileOut),"");
			
			new File(fileIn).delete();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
