/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2007 - 2008  Karim Chine
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
import java.util.Random;
import java.util.Vector;
import model.SpreadsheetModelDevice;
import model.SpreadsheetModelRemoteProxy;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import remoting.RAction;
import remoting.RCallBack;
import remoting.GenericCallbackDevice;
import remoting.RCollaborationListener;
import remoting.RConsoleAction;
import remoting.RConsoleActionListener;
import remoting.RServices;
import uk.ac.ebi.microarray.pools.PoolUtils;

/**
 * @author Karim Chine karim.chine@m4x.org
 */
public class RHttpProxy {

	public static final String FAKE_SESSION = getRandomSession();
	
	private static HttpClient mainHttpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

	public static String logOn(String url, String sessionId, String login, String pwd, HashMap<String, Object> options) throws TunnelingException {

		GetMethod pingServer = null;
		try {
			Object result = null;
			try {
				pingServer = new GetMethod(url + "?method=ping");
				mainHttpClient.executeMethod(pingServer);
				result = new ObjectInputStream(pingServer.getResponseBodyAsStream()).readObject();
				if (!result.equals("pong")) throw new ConnectionFailedException(); 
			} catch (Exception e) {
				e.printStackTrace();
				throw new ConnectionFailedException();
			}
		} finally {
			if (pingServer != null) {
				pingServer.releaseConnection();
			}
		}
		
		GetMethod getSession = null;
		try {
			Object result = null;
			try {
				getSession = new GetMethod(url + "?method=logon&login=" + PoolUtils.objectToHex(login) + "&pwd=" + PoolUtils.objectToHex(pwd) + "&options="
						+ PoolUtils.objectToHex(options));
				if (sessionId != null && !sessionId.equals("")) {
					getSession.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getSession.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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
		}
	}

	public static void logOff(String url, String sessionId) throws TunnelingException {
		GetMethod getLogOut = null;
		try {
			Object result = null;
			getLogOut = new GetMethod(url + "?method=logoff");
			if (sessionId != null && !sessionId.equals("")) {
				getLogOut.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
				getLogOut.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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
		}
	}

	public static void logOffAndKill(String url, String sessionId) throws TunnelingException {
		GetMethod getLogOut = null;
		try {
			Object result = null;
			getLogOut = new GetMethod(url + "?method=logoff&kill=true");
			if (sessionId != null && !sessionId.equals("")) {
				getLogOut.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
				getLogOut.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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
		}
	}
	public static Object invoke(String url, String sessionId, String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters,
			HttpClient httpClient) throws TunnelingException {
		
		PostMethod postPush = null;
		try {
			Object result = null;
			try {
				postPush = new PostMethod(url + "?method=invoke");
				NameValuePair[] data = { new NameValuePair("servantname", PoolUtils.objectToHex(servantName)),
						new NameValuePair("methodname", PoolUtils.objectToHex(methodName)),
						new NameValuePair("methodsignature", PoolUtils.objectToHex(methodSignature)),
						new NameValuePair("methodparameters", PoolUtils.objectToHex(methodParameters)) };
				postPush.setRequestBody(data);

				if (sessionId != null && !sessionId.equals("")) {
					postPush.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					postPush.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
				}
				httpClient.executeMethod(postPush);
				result = new ObjectInputStream(postPush.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("Client Side", e);
			}
			if (result != null && result instanceof TunnelingException) {
				new Exception().printStackTrace();
				throw (TunnelingException) result;
			}
			return result;
		} finally {
			if (postPush != null) {
				postPush.releaseConnection();
			}
		}
	}

	public static Object invoke(String url, String sessionId, String servantName, String methodName, Class<?>[] methodSignature, Object[] methodParameters)
			throws TunnelingException {
		return invoke(url, sessionId, servantName, methodName, methodSignature, methodParameters, mainHttpClient);
	}

	public static Object getDynamicProxy(final String url, final String sessionId, final String servantName, Class<?>[] c, final HttpClient httpClient) {
		Object proxy = Proxy.newProxyInstance(RHttpProxy.class.getClassLoader(), c, new InvocationHandler() {
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				return RHttpProxy.invoke(url, sessionId, servantName, method.getName(), method.getParameterTypes(), args, httpClient);
			}
		});
		return proxy;
	}

	public static void interrupt(String url, String sessionId) throws TunnelingException {
		GetMethod getInterrupt = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getInterrupt = new GetMethod(url + "?method=interrupt");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getInterrupt.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getInterrupt.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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

	private static GDDevice newDevice(String url, String sessionId, int width, int height, boolean broadcasted) throws TunnelingException {
		GetMethod getNewDevice = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getNewDevice = new GetMethod(url + "?method=newdevice" + "&width=" + width + "&height=" + height+ "&broadcasted=" + broadcasted);
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getNewDevice.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getNewDevice.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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

			String deviceName = (String) result;
			return (GDDevice) RHttpProxy.getDynamicProxy(url, sessionId, deviceName, new Class[] { GDDevice.class }, new HttpClient(
					new MultiThreadedHttpConnectionManager()));

		} finally {
			if (getNewDevice != null) {
				getNewDevice.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}

	private static GenericCallbackDevice newGenericCallbackDevice(String url, String sessionId) throws TunnelingException {
		GetMethod getNewDevice = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getNewDevice = new GetMethod(url + "?method=newgenericcallbackdevice");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getNewDevice.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getNewDevice.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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

			String deviceName = (String) result;
			return (GenericCallbackDevice) RHttpProxy.getDynamicProxy(url, sessionId, deviceName, new Class[] { GenericCallbackDevice.class }, new HttpClient(
					new MultiThreadedHttpConnectionManager()));

		} finally {
			if (getNewDevice != null) {
				getNewDevice.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}

	private static GDDevice[] listDevices(String url, String sessionId) throws TunnelingException {
		GetMethod getListDevices = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getListDevices = new GetMethod(url + "?method=listdevices");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getListDevices.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getListDevices.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
				}
				mainHttpClient.executeMethod(getListDevices);
				result = new ObjectInputStream(getListDevices.getResponseBodyAsStream()).readObject();
			} catch (ConnectException e) {
				throw new ConnectionFailedException();
			} catch (Exception e) {
				throw new TunnelingException("", e);
			}
			if (result != null && result instanceof TunnelingException) {
				throw (TunnelingException) result;
			}

			Vector<String> deviceNames = (Vector<String>) result;

			GDDevice[] devices = new GDDevice[deviceNames.size()];

			for (int i = 0; i < deviceNames.size(); ++i) {
				devices[i] = (GDDevice) RHttpProxy.getDynamicProxy(url, sessionId, deviceNames.elementAt(i), new Class[] { GDDevice.class }, new HttpClient(
						new MultiThreadedHttpConnectionManager()));
			}

			return devices;

		} finally {
			if (getListDevices != null) {
				getListDevices.releaseConnection();
			}
			if (mainHttpClient != null) {
			}
		}
	}

	public static SpreadsheetModelDevice newSpreadsheetModelDevice(String url, String sessionId, String id, String rowcount, String colcount) throws TunnelingException {
		GetMethod getNewDevice = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getNewDevice = new GetMethod(url + "?method=newspreadsheetmodeldevice&id="+id+"&rowcount="+rowcount+"&colcount="+colcount);
			
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getNewDevice.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getNewDevice.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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

			String deviceName = (String) result;
			return (SpreadsheetModelDevice) RHttpProxy.getDynamicProxy(url, sessionId, deviceName, new Class[] { SpreadsheetModelDevice.class }, new HttpClient(
					new MultiThreadedHttpConnectionManager()));

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
			getInterrupt = new GetMethod(url + "?method=saveimage");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getInterrupt.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getInterrupt.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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

	public static void loadimage(String url, String sessionId) throws TunnelingException {
		GetMethod getInterrupt = null;
		try {
			Object result = null;
			mainHttpClient = new HttpClient();
			getInterrupt = new GetMethod(url + "?method=loadimage");
			try {
				if (sessionId != null && !sessionId.equals("")) {
					getInterrupt.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getInterrupt.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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


	public static RServices getR(final String url, final String sessionId, final boolean handleCallbacks,final int maxNbrRactionsOnPop) {
		final HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());

		final Object proxy = Proxy.newProxyInstance(RHttpProxy.class.getClassLoader(), new Class<?>[] { RServices.class, HttpMarker.class },
				new InvocationHandler() {

					Vector<RCallBack> rCallbacks = new Vector<RCallBack>();
					Vector<RCollaborationListener> rCollaborationListeners = new Vector<RCollaborationListener>();
					Vector<RConsoleActionListener> rConsoleActionListeners = new Vector<RConsoleActionListener>();
					
					GenericCallbackDevice genericCallBackDevice = null;
					Thread popThread=null;

					boolean _stopThreads = false;
					{
						if (handleCallbacks) {
							
							try {
								genericCallBackDevice = newGenericCallbackDevice(url, sessionId);
								popThread=new Thread(new Runnable() {
									public void run() {
										while (true && !_stopThreads) {										
											popActions();
											
											try {
												Thread.sleep(10);
											} catch (Exception e) {
											}
										}
																				
									}
								});
								popThread.start();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}

					private synchronized void popActions() {
						try {

							Vector<RAction> ractions = genericCallBackDevice.popRActions(maxNbrRactionsOnPop);
							if (ractions != null) {

								for (int i = 0; i < ractions.size(); ++i) {
									final RAction action = ractions.elementAt(i);
									if (action.getActionName().equals("notify")) {
										HashMap<String, String> parameters=(HashMap<String, String>)action.getAttributes().get("parameters");
										for (int j=0; j<rCallbacks.size();++j) {
											rCallbacks.elementAt(j).notify(parameters);
										}
									} if (action.getActionName().equals("rConsoleActionPerformed")) {
										RConsoleAction consoleAction=(RConsoleAction)action.getAttributes().get("consoleAction");
										for (int j=0; j<rConsoleActionListeners.size();++j) {
											rConsoleActionListeners.elementAt(j).rConsoleActionPerformed(consoleAction);
										}
									} else if (action.getActionName().equals("chat")) {										
										String sourceUID= (String)action.getAttributes().get("sourceUID");
										String user= (String)action.getAttributes().get("user");
										String message= (String)action.getAttributes().get("message");											
										for (int j=0; j<rCollaborationListeners.size();++j) {
											rCollaborationListeners.elementAt(j).chat(sourceUID,user, message);
										}
									} else if (action.getActionName().equals("consolePrint")) {
										String sourceUID= (String)action.getAttributes().get("sourceUID");
										String user= (String)action.getAttributes().get("user");
										String expression= (String)action.getAttributes().get("expression");
										String result= (String)action.getAttributes().get("result");
										for (int j=0; j<rCollaborationListeners.size();++j) {
											rCollaborationListeners.elementAt(j).consolePrint(sourceUID,user, expression, result);
										}
									}
								}

							}

						} catch (NotLoggedInException nle) {

							nle.printStackTrace();
						} catch (Exception e) {

							e.printStackTrace();
						}

					}


					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Object result = null;
						if (method.getName().equals("newDevice")) {
							result = newDevice(url, sessionId, (Integer) args[0], (Integer) args[1], false);
						} else if (method.getName().equals("newBroadcastedDevice")) {
							result = newDevice(url, sessionId, (Integer) args[0], (Integer) args[1], true);
						} else if (method.getName().equals("listDevices")) {
							result = listDevices(url, sessionId);
						} else if (method.getName().equals("addRCallback")) {
							rCallbacks.add((RCallBack) args[0]);
						} else if (method.getName().equals("removeRCallback")) {
							rCallbacks.remove((RCallBack) args[0]);
						} else if (method.getName().equals("removeAllRCallbacks")) {
							rCallbacks.removeAllElements();
						} 
						
						else if (method.getName().equals("addRCollaborationListener")) {
							rCollaborationListeners.add((RCollaborationListener) args[0]);
						} else if (method.getName().equals("removeRCollaborationListener")) {
							rCollaborationListeners.remove((RCollaborationListener) args[0]);
						} else if (method.getName().equals("removeAllRCollaborationListeners")) {
							rCollaborationListeners.removeAllElements();
						}   
						
						else if (method.getName().equals("addRConsoleActionListener")) {
							rConsoleActionListeners.add((RConsoleActionListener) args[0]);
						} else if (method.getName().equals("removeRConsoleActionListener")) {
							rConsoleActionListeners.remove((RConsoleActionListener) args[0]);
						} else if (method.getName().equals("removeAllRConsoleActionListeners")) {
							rConsoleActionListeners.removeAllElements();
						}
					
						
						else if (method.getName().equals("newSpreadsheetTableModelRemote")) {
							SpreadsheetModelDevice d=newSpreadsheetModelDevice(url, sessionId, "", ((Integer)args[0]).toString(), ((Integer)args[1]).toString());
							result=new SpreadsheetModelRemoteProxy(d);
						} else if (method.getName().equals("getSpreadsheetTableModelRemote")) {
							SpreadsheetModelDevice d=newSpreadsheetModelDevice(url, sessionId, (String)args[0], "","");
							result=new SpreadsheetModelRemoteProxy(d);
						}  
						
						
						else if (method.getName().equals("stopThreads")) {
							_stopThreads = true;
							popThread.join();
							try {
								// IMPORTANT !!!!!!!!!!!!!!!!!!!!!!!!!!!!
								genericCallBackDevice.dispose();
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else if (method.getName().equals("popActions")) {
							popActions();
						} 
						
						else {
							
							result = RHttpProxy.invoke(url, sessionId, "R", method.getName(), method.getParameterTypes(), args, httpClient);
						}
						
						
						 if (method.getName().equals("asynchronousConsoleSubmit")) {
							 popActions();
						 }
						
						
						return result;

					}
				});

		return (RServices) proxy;
	}
	
	private static String getRandomSession() {
		String HexDigits[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		Random rnd=new Random(System.currentTimeMillis());
		String result="";
		for (int i=0; i<32;++i) result+=HexDigits[rnd.nextInt(16)];
		return result;
	}
	
	
	public static String logOnDB(String url, String sessionId, String login, String pwd, HashMap<String, Object> options) throws TunnelingException {

		GetMethod getSession = null;
		try {
			Object result = null;
			try {
				getSession = new GetMethod(url + "?method=logondb&login=" + PoolUtils.objectToHex(login) + "&pwd=" + PoolUtils.objectToHex(pwd) + "&options="
						+ PoolUtils.objectToHex(options));
				if (sessionId != null && !sessionId.equals("")) {
					getSession.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
					getSession.setRequestHeader("Cookie", "JSESSIONID=" + sessionId);
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
		}
	}

	/*
	public void main(String[] args) {
		
		org.mortbay.jetty.clien
		HttpClient client = new HttpClient();
		client.setConnectorType(HttpClien.CONNECTOR_SELECT_CHANNEL);
		try
		{
		  client.start();
		}
		catch (Exception e)
		{
		  throw new ServletException(e);
		}

		// create the exchange object, which lets you define where you want to go
		// and what you want to do once you get a response
		ContentExchange exchange = new ContentExchange()
		{
		  // define the callback method to process the response when you get it back
		  protected void onResponseComplete() throws IOException
		  {
		    super.onResponseComplete();
		    String responseContent = this.getResponseContent();

		    // do something with the response content
		    ...
		  }
		};

		exchange.setMethod("GET");
		exchange.setURL("http://www.example.com/");

		// start the exchange
		client.send(exchange);	
	}
	*/
	
}
