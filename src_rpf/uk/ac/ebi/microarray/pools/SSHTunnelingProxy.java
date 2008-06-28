package uk.ac.ebi.microarray.pools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Random;

public class SSHTunnelingProxy {
	
	static Random rnd=new Random(System.currentTimeMillis());
	
	public static Object invoke(String dir, String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {
		try {
			Properties invokationPros=new Properties();
			invokationPros.put("servantName", servantName);
			invokationPros.put("methodName", methodName);
			invokationPros.put("methodSignature", PoolUtils.objectToHex(methodSignature));
			invokationPros.put("methodParameters", PoolUtils.objectToHex(methodParameters));
			
			String fileIn=dir+"/invoke"+rnd.nextInt(100000)+".in";
			
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

	public static void main(String[] args) throws Exception {
		Object result=invoke("C:/", "logger", "sayHello", new Class<?>[]{String.class}, new Object[]{"karim"});
		System.out.println("result="+result);		
	}
}
