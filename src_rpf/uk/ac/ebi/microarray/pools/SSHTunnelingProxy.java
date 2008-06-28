package uk.ac.ebi.microarray.pools;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.UUID;
import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHTunnelingProxy {
	
	
	public static Object invoke(String sshHostIp, String sshLogin, String sshPwd, String homedir, String invokeCommand, String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters) throws SSHTunnelingException {
		String uid=UUID.randomUUID().toString();								
		String fileIn=System.getProperty("java.io.tmpdir")+"/invoke"+uid+".in";
		String fileOut=System.getProperty("java.io.tmpdir")+"/invoke"+uid+".out";

		if (new File(fileIn).exists()) new File(fileIn).delete();
		if (new File(fileOut).exists()) new File(fileOut).delete();
		
		Connection conn = null;
		try {
			
			conn = new Connection(sshHostIp);
			conn.connect();
			boolean isAuthenticated = conn.authenticateWithPassword(sshLogin, sshPwd);
			if (isAuthenticated == false)
				throw new IOException("Authentication failed.");
			Session sess = null;
			sess = conn.openSession();		
			
			Properties invokationProps=new Properties();
			invokationProps.put("servantName", servantName);
			invokationProps.put("methodName", methodName);
			invokationProps.put("methodSignature", PoolUtils.objectToHex(methodSignature));
			invokationProps.put("methodParameters", PoolUtils.objectToHex(methodParameters));
			
			
			FileOutputStream fos=new FileOutputStream(fileIn);
			invokationProps.storeToXML(fos, "");
			fos.close();
			new SCPClient(conn).put(fileIn, homedir);			
			String cmd=PoolUtils.replaceAll(invokeCommand, "${file}", homedir+"/invoke"+uid+".in");			
			System.out.println("cmd:"+cmd);
			sess.execCommand( cmd );

			final BufferedReader brOut = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStdout())));
			final BufferedReader brErr = new BufferedReader(new InputStreamReader(new StreamGobbler(sess.getStderr())));
			final StringBuffer buffer=new StringBuffer();
			new Thread(new Runnable() {
				boolean startReadingAnswer=false;
				public void run() {
					try {
						while (true) {
							String line = brOut.readLine();
							if (line == null) break;							
							if (startReadingAnswer) buffer.append(line+"\n");
							if (line.equals("->XML")) startReadingAnswer=true;							
							System.out.println(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			new Thread(new Runnable() {
				public void run() {
					try {
						while (true) {
							String line = brErr.readLine();
							if (line == null)
								break;
							System.out.println(line);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}).start();

			sess.waitForCondition(ChannelCondition.EXIT_STATUS, 0);
			
			PrintWriter pw=new PrintWriter(fileOut);
			pw.println(buffer.toString());
			pw.close();
					
			return null;
			/*
			Properties resultProps=new Properties();
			resultProps.loadFromXML(new FileInputStream(fileOut));			
			Object result=PoolUtils.hexToObject(resultProps.getProperty("result"));			
			if (result instanceof SSHTunnelingException) throw (SSHTunnelingException)result;
			else return result;
			*/
			
		} catch (SSHTunnelingException sshe) {
			
			throw sshe;
			
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new SSHTunnelingException("",e);
			
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if (new File(fileIn).exists()) new File(fileIn).delete();
			if (new File(fileOut).exists()) new File(fileOut).delete();

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
