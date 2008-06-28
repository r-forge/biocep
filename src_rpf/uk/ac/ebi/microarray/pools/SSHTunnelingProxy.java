package uk.ac.ebi.microarray.pools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import uk.ac.ebi.microarray.pools.db.DBLayerInterface;

public class SSHTunnelingProxy {
	
	static Random rnd=new Random(System.currentTimeMillis());
	
	public static Object invoke(String dir, String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {
		try {
			Properties invokationPros=new Properties();
			invokationPros.put("servantName", servantName);
			invokationPros.put("methodName", methodName);
			invokationPros.put("methodSignature", PoolUtils.objectToHex(methodSignature));
			invokationPros.put("methodParameters", PoolUtils.objectToHex(methodParameters));
			
			String fileIn=dir+"invoke"+rnd.nextInt(100000)+".in";
			
			FileOutputStream fos=new FileOutputStream(fileIn);
			invokationPros.storeToXML(fos, "");
			fos.close();
						
			SSHTunnelingWorker.main(new String[]{fileIn});
						
			String fileOut=fileIn.substring(0, fileIn.lastIndexOf("."))+".out";
			
			Properties resultProps=new Properties();
			resultProps.loadFromXML(new FileInputStream(fileOut));
			Object result=PoolUtils.hexToObject(resultProps.getProperty("result"));
			if (result instanceof SSHTunnelingException) throw (SSHTunnelingException)result;
			else return result;
			
		} catch (SSHTunnelingException sshe) {
			
			throw sshe;
			
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new SSHTunnelingException("",e);
			
		}
	}
	
	public static Object getDynamicProxy(final String dir, final String servantName, Class<?>[] c) {
		Object proxy = Proxy.newProxyInstance(SSHTunnelingProxy.class.getClassLoader(), c, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return SSHTunnelingProxy.invoke(dir, servantName, method.getName(), method.getParameterTypes(), args);
			}
		});
		return proxy;
	}

	public static void main(String[] args) throws Exception {
		
		DBLayerInterface dbLayer=(DBLayerInterface)getDynamicProxy("", "derby", new Class<?>[] {DBLayerInterface.class});		
		String[] result=dbLayer.list();
		System.out.println("result="+Arrays.toString(result));		
	}
}
