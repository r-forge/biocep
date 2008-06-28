package uk.ac.ebi.microarray.pools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.UUID;

public class SSHTunnelingProxy {
	
	
	public static Object invoke(String sshHostIp, String sshLogin, String sshPwd, String homedir, String invokeCommand, String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {
		try {
			Properties invokationProps=new Properties();
			invokationProps.put("servantName", servantName);
			invokationProps.put("methodName", methodName);
			invokationProps.put("methodSignature", PoolUtils.objectToHex(methodSignature));
			invokationProps.put("methodParameters", PoolUtils.objectToHex(methodParameters));
			String uid=UUID.randomUUID().toString();
			
			String fileIn=System.getProperty("java.io.tmpdir")+"invoke"+uid+".in";			
			FileOutputStream fos=new FileOutputStream(fileIn);
			invokationProps.storeToXML(fos, "");
			fos.close();
			
			SSHUtils.putFileSsh(fileIn, homedir, sshHostIp, sshLogin, sshPwd);
			SSHUtils.execSsh(invokeCommand, sshHostIp, sshLogin, sshPwd);
			SSHUtils.getFileSsh(homedir+"/"+"invoke"+uid+".out", System.getProperty("java.io.tmpdir")+"invoke"+uid+".out", sshHostIp, sshLogin, sshPwd);
						
			Properties resultProps=new Properties();
			resultProps.loadFromXML(new FileInputStream(System.getProperty("java.io.tmpdir")+"invoke"+uid+".out"));
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
	
	public static Object getDynamicProxy(final String sshHostIp,final String sshLogin,final String sshPwd,final String homedir,final String invokeCommand, final String servantName, Class<?>[] c) {
		Object proxy = Proxy.newProxyInstance(SSHTunnelingProxy.class.getClassLoader(), c, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return SSHTunnelingProxy.invoke(sshHostIp,sshLogin, sshPwd,homedir,invokeCommand, servantName, method.getName(), method.getParameterTypes(), args);
			}
		});
		return proxy;
	}

}
