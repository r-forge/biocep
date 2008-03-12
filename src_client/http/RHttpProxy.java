/*
 * Copyright (C) 2007 EMBL-EBI
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package http;

import graphics.pop.GDDevice;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.ConnectException;
import java.util.HashMap;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine kchine@ebi.ac.uk
 */
public class RHttpProxy {
	
	public static final String FAKE_SESSION = "11111111111111111111111111111111";
	
	static HttpClient mainHttpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

	public static String logOn(String url, String sessionId, String login, String pwd, HashMap<String, Object> options)
			throws TunnelingException {

		GetMethod getSession = null;
		try {
			Object result = null;
			try {
				getSession = new GetMethod(url
						+ "?method=logon&login=" + PoolUtils.objectToHex(login) + "&pwd=" + PoolUtils.objectToHex(pwd)
						+ "&options=" + PoolUtils.objectToHex(options));	
				if (sessionId != null && !sessionId.equals("")) {
					getSession.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getSession.setRequestHeader("Cookie", "JSESSIONID="+sessionId);
				}
				mainHttpClient.executeMethod(getSession);
				result = new ObjectInputStream(getSession.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				e.printStackTrace();
				throw new ConnectionFailedException();
			} catch (Exception e) {
				e.printStackTrace();
				throw new TunnelingException("Client Side", e);
			}

			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}
			return (String) result;
		} finally {
			if (getSession != null) {
				getSession.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}

	public static void logOff(String url, String sessionId) throws TunnelingException {
		GetMethod getLogOut = null;
		try {
			Object result = null;
			getLogOut = new GetMethod(url
					+ "?method=logoff");
			if (sessionId != null && !sessionId.equals("")) {
				getLogOut.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
				getLogOut.setRequestHeader("Cookie", "JSESSIONID="+sessionId);
			}
			try {
				mainHttpClient.executeMethod(getLogOut);
				result = new ObjectInputStream(getLogOut.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("", e);
			}

			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}

		} finally {
			if (getLogOut != null) {
				getLogOut.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}

	public static Object invoke(String url, String sessionId, String servantName, String methodName,
			Class<?>[] methodSignature, Object[] methodParameters, HttpClient httpClient) throws TunnelingException {
		PostMethod postPush = null;
		try {
			Object result = null;
			try {
				postPush = new PostMethod(url						
						+ "?method=invoke");
				NameValuePair[] data = { new NameValuePair("servantname", PoolUtils.objectToHex(servantName)),
						new NameValuePair("methodname", PoolUtils.objectToHex(methodName)),
						new NameValuePair("methodsignature", PoolUtils.objectToHex(methodSignature)),
						new NameValuePair("methodparameters", PoolUtils.objectToHex(methodParameters)) };
				postPush.setRequestBody(data);

				if (sessionId != null && !sessionId.equals("")) {
					postPush.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					postPush.setRequestHeader("Cookie", "JSESSIONID="+sessionId);
				}
				httpClient.executeMethod(postPush);
				result = new ObjectInputStream(postPush.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("Client Side", e);
			}
			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}
			return result;
		} finally {
			if (postPush != null) {
				postPush.releaseConnection();
			}
		}
	}

	public static Object invoke(String url, String sessionId, String servantName, String methodName,
			Class<?>[] methodSignature, Object[] methodParameters) throws TunnelingException {
		return invoke(url, sessionId, servantName, methodName, methodSignature, methodParameters, mainHttpClient);
	}

	public static Object getDynamicProxy(final String url, final String sessionId, final String servantName,
			Class<?> c, final HttpClient httpClient) {
		Object proxy = Proxy.newProxyInstance(RHttpProxy.class.getClassLoader(), new Class[] { c },
				new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return RHttpProxy.invoke(url, sessionId, servantName, method.getName(), method
								.getParameterTypes(), args, httpClient);
					}
				});
		return proxy;
	}

	public static void interrupt(String url, String sessionId) throws TunnelingException {
		GetMethod getInterrupt = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getInterrupt = new GetMethod(url
					+ "?method=interrupt");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getInterrupt.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getInterrupt.setRequestHeader("Cookie", "JSESSIONID="+sessionId);
				}
				mainHttpClient.executeMethod(getInterrupt);
				result = new ObjectInputStream(getInterrupt.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("", e);
			}
			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}

		} finally {
			if (getInterrupt != null) {
				getInterrupt.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}
	
	public static GDDevice newDevice(String url, String sessionId, int width, int height) throws TunnelingException {
		GetMethod getNewDevice = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getNewDevice = new GetMethod(url
					+ "?method=newdevice" +
					"&width="+width+"&height="+height);
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getNewDevice.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getNewDevice.setRequestHeader("Cookie", "JSESSIONID="+sessionId);
				}
				mainHttpClient.executeMethod(getNewDevice);
				result = new ObjectInputStream(getNewDevice.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("", e);
			}
			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}
			
			String deviceName=(String)result;
			return (GDDevice) RHttpProxy.getDynamicProxy(url, sessionId, deviceName,
					GDDevice.class, new HttpClient(new MultiThreadedHttpConnectionManager()));

		} finally {
			if (getNewDevice != null) {
				getNewDevice.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}

	public static void saveimage(String url, String sessionId) throws TunnelingException {
		GetMethod getInterrupt = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getInterrupt = new GetMethod(url
					+ "?method=saveimage");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getInterrupt.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getInterrupt.setRequestHeader("Cookie", "JSESSIONID="+sessionId);
				}
				mainHttpClient.executeMethod(getInterrupt);
				result = new ObjectInputStream(getInterrupt.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("", e);
			}
			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}

		} finally {
			if (getInterrupt != null) {
				getInterrupt.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}

	public static void loadimage(String url,String sessionId) throws TunnelingException {
		GetMethod getInterrupt = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getInterrupt = new GetMethod(url
					+ "?method=loadimage");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getInterrupt.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getInterrupt.setRequestHeader("Cookie", "JSESSIONID="+sessionId);
				}
				mainHttpClient.executeMethod(getInterrupt);
				result = new ObjectInputStream(getInterrupt.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("", e);
			}
			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}

		} finally {
			if (getInterrupt != null) {
				getInterrupt.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}
	
	
	

}
