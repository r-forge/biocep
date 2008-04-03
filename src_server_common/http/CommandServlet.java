/*
 * Copyright (C) 2007  EMBL - EBI - Microarray Informatics
 * Copyright (C) 2008  Imperial College London - Internet Center
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
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.rmi.ConnectException;
import java.rmi.registry.Registry;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.neilja.net.interruptiblermi.InterruptibleRMIThreadFactory;
import remoting.RKit;
import remoting.RServices;
import server.LocalHttpServer;
import server.LocalRmiRegistry;
import server.ServerManager;
import uk.ac.ebi.microarray.pools.PoolUtils;
import uk.ac.ebi.microarray.pools.RPFSessionInfo;
import uk.ac.ebi.microarray.pools.RmiCallInterrupted;
import uk.ac.ebi.microarray.pools.RmiCallTimeout;
import uk.ac.ebi.microarray.pools.ServantProviderFactory;


/**
 * @author Karim Chine k.chine@imperial.ac.uk
 */
public class CommandServlet extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet {
	private static final Log log = org.apache.commons.logging.LogFactory.getLog(CommandServlet.class);

	private static final int RMICALL_TIMEOUT_MILLISEC = 60 * 1000 * 10;
	private static final Integer RMICALL_DONE = new Integer(0);

	RKit _rkit = null;

	public CommandServlet(RKit rkit) {
		super();
		_rkit = rkit;
		PoolUtils.initLog4J();
	}

	public CommandServlet() {
		super();
		PoolUtils.initLog4J();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	class ThreadsHolder {
		private transient Vector<Thread> _threads = new Vector<Thread>();

		Vector<Thread> getThreads() {
			return _threads;
		}
	}

	protected void doAny(final HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = null;
		Object result = null;

		try {
			final String command = request.getParameter("method");
			do {

				if (command.equals("logon")) {

					session = request.getSession(false);
					if (session != null) {
						result = session.getId();
						break;
					}

					String login = (String) PoolUtils.hexToObject(request.getParameter("login"));
					String pwd = (String) PoolUtils.hexToObject(request.getParameter("pwd"));
					System.out.println("login :" + login);
					System.out.println("pwd :" + pwd);
					HashMap<String, Object> options = (HashMap<String, Object>) PoolUtils.hexToObject(request.getParameter("options"));
					if (options == null)
						options = new HashMap<String, Object>();
					System.out.println("options:" + options);

					RPFSessionInfo.get().put("LOGIN", login);
					RPFSessionInfo.get().put("REMOTE_ADDR", request.getRemoteAddr());
					RPFSessionInfo.get().put("REMOTE_HOST", request.getRemoteHost());

					boolean nopool = !options.keySet().contains("nopool") || ((String) options.get("nopool")).equals("") || !((String) options.get("nopool")).equalsIgnoreCase("false");
					boolean save = options.keySet().contains("save") && ((String) options.get("save")).equalsIgnoreCase("true");
					boolean namedAccessMode = login.contains("@@");
					String privateName=(String)options.get("privatename");
					
					System.out.println("privatename=<"+privateName+">");
					

					RServices r = null;

					if (_rkit == null) {

						if (namedAccessMode) {

							ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

							if (spFactory == null) {
								result = new NoRegistryAvailableException();
								break;
							}

							Registry registry = spFactory.getServantProvider().getRegistry();
							String sname = login.substring(login.indexOf("@@") + "@@".length());
							login = login.substring(0, login.indexOf("@@"));
							try {
								r = (RServices) registry.lookup(sname);
							} catch (Exception e) {
								e.printStackTrace();
							}

						} else {
							if (nopool) {

								/*								 
								ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

								if (spFactory == null) {
									result = new NoRegistryAvailableException();
									break;
								}

								String nodeName = options.keySet().contains("node") ? (String) options.get("node") : System
										.getProperty("private.servant.node.name");
								Registry registry = spFactory.getServantProvider().getRegistry();
								NodeManager nm = null;
								try {
									nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name") + "_" + nodeName);
								} catch (NotBoundException nbe) {
									nm = (NodeManager) registry.lookup(System.getProperty("node.manager.name"));
								} catch (Exception e) {
									result = new NoNodeManagerFound();
									break;
								}
								r = (RServices) nm.createPrivateServant(nodeName);
								*/
								
								System.out.println("LocalHttpServer.getLocalHttpServerPort():"+LocalHttpServer.getLocalHttpServerPort());
								System.out.println("LocalRmiRegistry.getLocalRmiRegistryPort():"+LocalHttpServer.getLocalHttpServerPort());
								if (privateName!=null && !privateName.equals("")) {
									try {
										r=(RServices) LocalRmiRegistry.getInstance().lookup(privateName);
									} catch (Exception e) {	
										//e.printStackTrace();
									}								
								} 
								
								if (r==null) {
									
									String urlHead=request.getRequestURL().substring(0, request.getRequestURL().lastIndexOf(request.getRequestURI()));
									r = ServerManager.createR(false, "127.0.0.1", LocalHttpServer.getLocalHttpServerPort(), "127.0.0.1", LocalRmiRegistry.getLocalRmiRegistryPort(), 256, 256, privateName, false,new URL[]{new URL(urlHead+"/rmapping/appletlibs/mapping.jar")});
								}

							} else {

								ServantProviderFactory spFactory = ServantProviderFactory.getFactory();

								if (spFactory == null) {
									result = new NoRegistryAvailableException();
									break;
								}

								boolean wait = options.keySet().contains("wait") && ((String) options.get("wait")).equalsIgnoreCase("true");
								if (wait) {
									r = (RServices) spFactory.getServantProvider().borrowServantProxy();
								} else {
									r = (RServices) spFactory.getServantProvider().borrowServantProxyNoWait();
								}
							}
						}
					} else {
						r = _rkit.getR();
					}

					if (r == null) {
						result = new NoServantAvailableException();
						break;
					}

					session = request.getSession(true);
					session.setAttribute("R", r);
					session.setAttribute("NOPOOL", nopool);
					session.setAttribute("SAVE", save);
					session.setAttribute("LOGIN", login);
					session.setAttribute("NAMED_ACCESS_MODE", namedAccessMode);
					session.setAttribute("PROCESS_ID", r.getProcessId());
					if (privateName!=null) session.setAttribute("PRIVATE_NAME", privateName);
					

					session.setAttribute("threads", new ThreadsHolder());

					((HashMap<String, HttpSession>) getServletContext().getAttribute("SESSIONS_MAP")).put(session.getId(), session);
					((HashMap<String, HashMap<String, Object>>) getServletContext().getAttribute("SESSIONS_ATTRIBUTES_MAP")).put(session.getId(),
							cloneAttributes(session));

					if (_rkit == null && save) {
						UserUtils.loadWorkspace((String) session.getAttribute("LOGIN"), r);
					}

					result = session.getId();
					break;

				}

				session = request.getSession(false);
				if (session == null) {
					result = new NotLoggedInException();
					break;
				}

				if (command.equals("logoff")) {

					if (_rkit != null) {
						Enumeration<String> attributeNames = session.getAttributeNames();
						while (attributeNames.hasMoreElements()) {
							String aname = attributeNames.nextElement();
							if (session.getAttribute(aname) instanceof GDDevice) {
								try {
									_rkit.getRLock().lock();
									((GDDevice) session.getAttribute(aname)).dispose();
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									_rkit.getRLock().unlock();
								}
							}
						}
					}

					try {
						session.invalidate();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					result = null;
					break;
				}

				final boolean[] stop = { false };
				final HttpSession currentSession = session;

				if (command.equals("invoke")) {

					String servantName = (String) PoolUtils.hexToObject(request.getParameter("servantname"));
					final Object servant = session.getAttribute(servantName);
					if (servant == null) {
						throw new Exception("Bad Servant Name :" + servantName);
					}

					String methodName = (String) PoolUtils.hexToObject(request.getParameter("methodname"));
					Class<?>[] methodSignature = (Class[]) PoolUtils.hexToObject(request.getParameter("methodsignature"));
					final Method m = servant.getClass().getMethod(methodName, methodSignature);
					if (m == null) {
						throw new Exception("Bad Method Name :" + methodName);
					}

					final Object[] methodParams = (Object[]) PoolUtils.hexToObject(request.getParameter("methodparameters"));

					final Object[] resultHolder = new Object[1];
					Runnable rmiRunnable = new Runnable() {
						public void run() {
							try {
								if (_rkit != null)
									_rkit.getRLock().lock();
								resultHolder[0] = m.invoke(servant, methodParams);
								if (resultHolder[0] == null)
									resultHolder[0] = RMICALL_DONE;
							} catch (InvocationTargetException ite) {
								if (ite.getCause() instanceof ConnectException) {
									currentSession.invalidate();
									resultHolder[0] = new NotLoggedInException();
								} else {
									resultHolder[0] = ite.getCause();
								}
							} catch (Exception e) {
								final boolean wasInterrupted = Thread.interrupted();
								if (wasInterrupted) {
									resultHolder[0] = new RmiCallInterrupted();
								} else {
									resultHolder[0] = e;
								}
							} finally {
								if (_rkit != null)
									_rkit.getRLock().unlock();
							}
						}
					};

					Thread rmiThread = InterruptibleRMIThreadFactory.getInstance().newThread(rmiRunnable);
					((ThreadsHolder) session.getAttribute("threads")).getThreads().add(rmiThread);
					rmiThread.start();

					long t1 = System.currentTimeMillis();

					while (resultHolder[0] == null) {

						if ((System.currentTimeMillis() - t1) > RMICALL_TIMEOUT_MILLISEC || stop[0]) {
							rmiThread.interrupt();
							resultHolder[0] = new RmiCallTimeout();
							break;
						}

						try {
							Thread.sleep(10);
						} catch (Exception e) {
						}
					}
					try {
						((ThreadsHolder) session.getAttribute("threads")).getThreads().remove(rmiThread);
					} catch (IllegalStateException e) {
					}

					if (resultHolder[0] instanceof Throwable) {
						throw (Throwable) resultHolder[0];
					}

					if (resultHolder[0] == RMICALL_DONE) {
						result = null;
					} else {
						result = resultHolder[0];
					}

					break;

				}

				if (command.equals("interrupt")) {
					final Vector<Thread> tvec = (Vector<Thread>) ((ThreadsHolder) session.getAttribute("threads")).getThreads().clone();
					for (int i = 0; i < tvec.size(); ++i) {
						try {
							tvec.elementAt(i).interrupt();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					stop[0] = true;
					((Vector<Thread>) ((ThreadsHolder) session.getAttribute("threads")).getThreads()).removeAllElements();
					result = null;
					break;
				} else if (command.equals("saveimage")) {
					UserUtils.saveWorkspace((String) session.getAttribute("LOGIN"), (RServices) session.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("loadimage")) {
					UserUtils.loadWorkspace((String) session.getAttribute("LOGIN"), (RServices) session.getAttribute("R"));
					result = null;
					break;
				} else if (command.equals("newdevice")) {
					try {
						if (_rkit != null)
							_rkit.getRLock().lock();
						GDDevice deviceProxy = ((RServices) session.getAttribute("R")).newDevice(Integer.decode(request.getParameter("width")), Integer
								.decode(request.getParameter("height")));
						String deviceName = "device" + "_" + deviceProxy.getDeviceNumber();
						System.out.println("deviceName=" + deviceName);
						session.setAttribute(deviceName, deviceProxy);
						result = deviceName;
						break;
					} finally {
						if (_rkit != null)
							_rkit.getRLock().unlock();
					}
				}

			} while (true);

		} catch (TunnelingException te) {
			result = te;
			te.printStackTrace();
		} catch (Throwable e) {
			result = new TunnelingException("Server Side", e);
			e.printStackTrace();
		}
		response.setContentType("application/x-java-serialized-object");
		new ObjectOutputStream(response.getOutputStream()).writeObject(result);
		response.flushBuffer();

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doAny(request, response);
	}

	public void init(ServletConfig sConfig) throws ServletException {
		super.init(sConfig);
		log.info("command servlet init");
		if (_rkit == null) {
			PoolUtils.injectSystemProperties(true);
		}
		PoolUtils.initRmiSocketFactory();
		getServletContext().setAttribute("SESSIONS_MAP", new HashMap<String, HttpSession>());
		getServletContext().setAttribute("SESSIONS_ATTRIBUTES_MAP", new HashMap<String, HashMap<String, Object>>());
	}

	HashMap<String, Object> cloneAttributes(HttpSession session) {
		HashMap<String, Object> result = new HashMap<String, Object>();

		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			String name = names.nextElement();
			result.put(name, session.getAttribute(name));
		}

		return result;
	}

}